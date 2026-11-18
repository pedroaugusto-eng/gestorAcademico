package br.edu.ucsal.pooalab.gestoracademico.domain.strategy;

/**
 * Padrão Strategy: cada instituição pode ter a sua regra de média e aprovação.
 * Para trocar a regra, basta criar outra implementação e registrá-la como bean.
 */
public interface CalculadoraMediaStrategy {

    /** Média final de 0 a 10, a partir das notas das duas unidades (cada uma de 0 a 10). */
    double calcularMedia(double notaUnidade1, double notaUnidade2);

    /**
     * Quantos pontos ainda faltam, somando as unidades, para chegar à média mínima (nunca negativo).
     * Com a 1ª unidade lançada, é exatamente a nota de que o aluno precisa na 2ª.
     */
    double calcularNotaNecessaria(double somaDasUnidades);

    /** Média mínima para aprovação. */
    double mediaMinima();
}
