package br.edu.ucsal.pooalab.gestoracademico.domain.strategy;

import org.springframework.stereotype.Component;

@Component
public class CalculadoraUcsalStrategy implements CalculadoraMediaStrategy {

    private static final double MEDIA_APROVACAO = 6.0;
    private static final double SOMA_NECESSARIA = MEDIA_APROVACAO * 2;

    @Override
    public double calcularNotaNecessaria(double notaAtual) {
        if (notaAtual >= SOMA_NECESSARIA) {
            return 0.0;
        }

        double notaNecessaria = SOMA_NECESSARIA - notaAtual;
        return Math.round(notaNecessaria * 10.0) / 10.0;
    }
}