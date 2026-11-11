package br.edu.ucsal.pooalab.gestoracademico.domain.dtos;

import java.util.List;

public record MatriculaResponse(String nomeAluno, String raAluno, String nomeDisciplina, int cargaHorariaDisciplina, int faltas, List<AvaliacaoResponse> avaliacoes) {
}
