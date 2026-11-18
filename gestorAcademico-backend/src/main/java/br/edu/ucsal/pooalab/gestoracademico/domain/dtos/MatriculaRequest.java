package br.edu.ucsal.pooalab.gestoracademico.domain.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MatriculaRequest(
        @NotNull(message = "escolha a disciplina")
        @Positive(message = "disciplina inválida")
        Long disciplinaId
) {
}
