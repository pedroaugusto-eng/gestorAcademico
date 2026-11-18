package br.edu.ucsal.pooalab.gestoracademico.security;

import br.edu.ucsal.pooalab.gestoracademico.domain.Identificadores;
import br.edu.ucsal.pooalab.gestoracademico.domain.entity.Aluno;
import br.edu.ucsal.pooalab.gestoracademico.repository.AlunoRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Encontra o aluno pelo que ele digitou no campo de login: o RA ou o CPF.
 * Primeiro tenta como RA; se não achar e o texto tiver 11 dígitos, tenta como CPF.
 * O cadastro impede que um RA coincida com um CPF, então não há ambiguidade.
 */
@Service
public class AlunoUserDetailsService implements UserDetailsService {

    private final AlunoRepository alunoRepository;

    public AlunoUserDetailsService(AlunoRepository alunoRepository) {
        this.alunoRepository = alunoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String identificador) {
        return buscarPorRaOuCpf(identificador)
                .map(AlunoAutenticado::new)
                .orElseThrow(() -> new UsernameNotFoundException("Aluno não encontrado"));
    }

    /**
     * Chave usada na contagem de tentativas de login. Se o aluno existe, a contagem é dele
     * (RA e CPF somam juntos); se não existe, conta pelo texto digitado, já normalizado.
     */
    @Transactional(readOnly = true)
    public String chaveDeTentativas(String identificador) {
        if (identificador == null || identificador.isBlank()) {
            return "";
        }
        return buscarPorRaOuCpf(identificador)
                .map(aluno -> "aluno:" + aluno.getId())
                .orElseGet(() -> "texto:" + (Identificadores.pareceCpf(identificador)
                        ? Identificadores.cpf(identificador)
                        : Identificadores.ra(identificador)));
    }

    private Optional<Aluno> buscarPorRaOuCpf(String identificador) {
        if (identificador == null || identificador.isBlank()) {
            return Optional.empty();
        }
        Optional<Aluno> porRa = alunoRepository.findByRa(Identificadores.ra(identificador));
        if (porRa.isPresent() || !Identificadores.pareceCpf(identificador)) {
            return porRa;
        }
        return alunoRepository.findByCpf(Identificadores.cpf(identificador));
    }
}
