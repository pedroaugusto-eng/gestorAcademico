package br.edu.ucsal.pooalab.gestoracademico.security;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class TentativasDeLoginTest {

    /** Relógio que o teste consegue avançar. */
    private static final class RelogioDeTeste extends Clock {
        Instant agora = Instant.parse("2026-11-18T12:00:00Z");
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return agora; }
    }

    @Test
    void bloqueiaNoQuintoErroELiberaDepoisDe15Minutos() {
        RelogioDeTeste relogio = new RelogioDeTeste();
        TentativasDeLogin tentativas = new TentativasDeLogin(relogio);

        for (int i = 0; i < 4; i++) tentativas.registrarErro("aluno:1");
        assertThat(tentativas.estaBloqueado("aluno:1")).isFalse();

        tentativas.registrarErro("aluno:1");
        assertThat(tentativas.estaBloqueado("aluno:1")).isTrue();
        assertThat(tentativas.estaBloqueado("aluno:2")).isFalse();

        relogio.agora = relogio.agora.plus(TentativasDeLogin.BLOQUEIO).plusSeconds(1);
        assertThat(tentativas.estaBloqueado("aluno:1")).isFalse();
    }

    @Test
    void acertarASenhaZeraOsErros() {
        TentativasDeLogin tentativas = new TentativasDeLogin(new RelogioDeTeste());
        for (int i = 0; i < 4; i++) tentativas.registrarErro("aluno:1");
        tentativas.registrarSucesso("aluno:1");
        tentativas.registrarErro("aluno:1");
        assertThat(tentativas.estaBloqueado("aluno:1")).isFalse();
    }
}
