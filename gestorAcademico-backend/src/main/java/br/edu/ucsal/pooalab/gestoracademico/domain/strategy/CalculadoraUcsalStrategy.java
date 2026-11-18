package br.edu.ucsal.pooalab.gestoracademico.domain.strategy;

import org.springframework.stereotype.Component;

/**
 * Regra da UCSAL:
 * - o semestre tem 2 unidades, cada uma de 0 a 10 pontos (20 no total);
 * - média final = (1ª unidade + 2ª unidade) / 2, de 0 a 10;
 * - aprovação com média 6, ou seja, 12 pontos somando as unidades.
 * Ex.: com 4,5 na 1ª unidade, o aluno precisa de 7,5 na 2ª (teste CalculadoraUcsalStrategyTest).
 */
@Component
public class CalculadoraUcsalStrategy implements CalculadoraMediaStrategy {

    private static final int UNIDADES = 2;
    private static final double MEDIA_MINIMA = 6.0;

    @Override
    public double calcularMedia(double notaUnidade1, double notaUnidade2) {
        return (notaUnidade1 + notaUnidade2) / UNIDADES;
    }

    @Override
    public double calcularNotaNecessaria(double somaDasUnidades) {
        return Math.max(0.0, MEDIA_MINIMA * UNIDADES - somaDasUnidades);
    }

    @Override
    public double mediaMinima() {
        return MEDIA_MINIMA;
    }
}
