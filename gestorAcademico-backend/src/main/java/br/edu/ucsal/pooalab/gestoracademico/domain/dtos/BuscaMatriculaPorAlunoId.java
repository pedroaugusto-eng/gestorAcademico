package br.edu.ucsal.pooalab.gestoracademico.domain.dtos;

import java.util.List;

public record BuscaMatriculaPorAlunoId(String nomeAluno, String cpf, List<DisciplinaResponse> disciplinas) {
}
