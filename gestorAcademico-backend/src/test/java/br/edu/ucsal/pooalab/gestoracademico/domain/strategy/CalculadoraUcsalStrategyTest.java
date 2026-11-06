package br.edu.ucsal.pooalab.gestoracademico.domain.strategy;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculadoraUcsalStrategyTest {

    @Test
    void deveCalcularNotaNecessariaComSucesso() {
        CalculadoraMediaStrategy calculadora = new CalculadoraUcsalStrategy();
        double notaNecessaria = calculadora.calcularNotaNecessaria(4.5);
        assertEquals(7.5, notaNecessaria);
    }

    @Test
    void deveRetornarZeroSeAlunoJaAtingiuMedia() {
        CalculadoraMediaStrategy calculadora = new CalculadoraUcsalStrategy();
        double notaNecessaria = calculadora.calcularNotaNecessaria(12.0);
        assertEquals(0.0, notaNecessaria);
    }
}
