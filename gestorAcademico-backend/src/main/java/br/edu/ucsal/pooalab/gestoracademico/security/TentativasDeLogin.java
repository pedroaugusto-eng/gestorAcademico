package br.edu.ucsal.pooalab.gestoracademico.security;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Proteção contra força bruta: depois de 5 senhas erradas para o mesmo aluno,
 * o login fica bloqueado por 15 minutos. Identificadores que não existem também são contados,
 * para não revelar quais RAs/CPFs estão cadastrados.
 * A chave vem de AlunoUserDetailsService.chaveDeTentativas (RA e CPF do mesmo aluno somam juntos).
 * Fica em memória: serve para uma instância só. Com várias instâncias, mover para Redis ou banco.
 */
@Component
public class TentativasDeLogin {

    static final int MAXIMO_DE_ERROS = 5;
    static final Duration BLOQUEIO = Duration.ofMinutes(15);
    private static final int LIMITE_DE_CHAVES = 10_000;

    private final Map<String, Registro> registros = new ConcurrentHashMap<>();
    private final Clock relogio;

    public TentativasDeLogin() {
        this(Clock.systemUTC());
    }

    TentativasDeLogin(Clock relogio) {
        this.relogio = relogio;
    }

    public boolean estaBloqueado(String chave) {
        if (chave == null || chave.isEmpty()) {
            return false;
        }
        Registro registro = registros.get(chave);
        return registro != null && registro.bloqueadoAte != null && relogio.instant().isBefore(registro.bloqueadoAte);
    }

    public void registrarErro(String chave) {
        if (chave == null || chave.isEmpty()) {
            return;
        }
        if (registros.size() >= LIMITE_DE_CHAVES) {
            limparExpirados();
        }
        if (registros.size() >= LIMITE_DE_CHAVES && !registros.containsKey(chave)) {
            return; // proteção da memória contra inundação de identificadores aleatórios
        }
        Instant agora = relogio.instant();
        registros.compute(chave, (k, atual) -> {
            Registro registro = (atual == null || atual.expirou(agora)) ? new Registro() : atual;
            registro.erros++;
            registro.ultimoErro = agora;
            if (registro.erros >= MAXIMO_DE_ERROS) {
                registro.bloqueadoAte = agora.plus(BLOQUEIO);
            }
            return registro;
        });
    }

    public void registrarSucesso(String chave) {
        if (chave != null) {
            registros.remove(chave);
        }
    }

    private void limparExpirados() {
        Instant agora = relogio.instant();
        registros.entrySet().removeIf(e -> e.getValue().expirou(agora));
    }

    private static final class Registro {
        int erros;
        Instant ultimoErro;
        Instant bloqueadoAte;

        boolean expirou(Instant agora) {
            if (bloqueadoAte != null) {
                return !agora.isBefore(bloqueadoAte);
            }
            return ultimoErro == null || agora.isAfter(ultimoErro.plus(BLOQUEIO));
        }
    }
}
