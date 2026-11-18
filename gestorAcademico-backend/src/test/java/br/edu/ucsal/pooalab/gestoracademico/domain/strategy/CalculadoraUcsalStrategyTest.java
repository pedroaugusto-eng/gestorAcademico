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

    @Test
    void mediaEhAMediaDasDuasUnidadesComMinimo6() {
        CalculadoraMediaStrategy calculadora = new CalculadoraUcsalStrategy();
        assertEquals(6.0, calculadora.mediaMinima());
        assertEquals(7.25, calculadora.calcularMedia(4.5, 10.0));
        assertEquals(6.0, calculadora.calcularMedia(5.0, 7.0));
        assertEquals(2.25, calculadora.calcularMedia(4.5, 0.0));
    }
}
