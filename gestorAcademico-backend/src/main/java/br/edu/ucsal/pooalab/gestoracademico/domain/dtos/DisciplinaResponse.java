package br.edu.ucsal.pooalab.gestoracademico.domain.dtos;

/** Dados de uma disciplina, usados no catálogo e no detalhe da matrícula. */
public record DisciplinaResponse(
        Long id,
        String nome,
        String codigo,
        String curso,
        int cargaHoraria,
        int semestreRecomendado,
        int totalDeAulas,
        int limiteDeFaltas
) {
}
