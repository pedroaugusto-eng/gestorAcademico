package br.edu.ucsal.pooalab.gestoracademico.domain.strategy;

public interface CalculadoraMediaStrategy {
    /**
     * Calcula a nota exata necessária na próxima avaliação para atingir a média.
     * @param notaAtual A soma das notas já obtidas pelo estudante.
     * @return A nota necessária para aprovação.
     */
    double calcularNotaNecessaria(double notaAtual);
}
