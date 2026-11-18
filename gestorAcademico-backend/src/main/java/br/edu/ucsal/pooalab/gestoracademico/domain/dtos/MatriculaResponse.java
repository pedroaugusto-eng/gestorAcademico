package br.edu.ucsal.pooalab.gestoracademico.domain.dtos;

import java.util.List;

/**
 * Situação do aluno numa disciplina.
 *
 * Notas: cada unidade vale até 10; media = (unidade 1 + unidade 2) / 2, de 0 a 10;
 * notaNecessaria = pontos que ainda faltam, somando as unidades, para a mediaMinima (vem da Strategy).
 *
 * Faltas: "faltas" é o total de aulas faltadas (cada falta registrada soma 2);
 * "faltasRegistradas" é quantas vezes o aluno faltou. Pode passar do limite: aí reprovadoPorFalta = true.
 */
public record MatriculaResponse(
        Long id,
        DisciplinaResponse disciplina,
        List<AvaliacaoResponse> avaliacoes,
        List<UnidadeResponse> unidades,
        double media,
        double mediaMinima,
        double notaNecessaria,
        boolean mediaAtingida,
        int faltas,
        int faltasRegistradas,
        int limiteDeFaltas,
        boolean reprovadoPorFalta
) {
}
