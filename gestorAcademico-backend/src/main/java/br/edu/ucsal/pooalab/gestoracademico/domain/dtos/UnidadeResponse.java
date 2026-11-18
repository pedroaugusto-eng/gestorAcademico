package br.edu.ucsal.pooalab.gestoracademico.domain.dtos;

/** Nota de uma unidade (0 a 10) e quantas avaliações já foram lançadas nela. */
public record UnidadeResponse(int numero, double nota, double notaMaxima, long avaliacoes) {
}
