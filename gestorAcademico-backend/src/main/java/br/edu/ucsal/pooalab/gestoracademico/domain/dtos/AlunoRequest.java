package br.edu.ucsal.pooalab.gestoracademico.domain.dtos;

import jakarta.validation.constraints.NotBlank;

public record AlunoRequest(@NotBlank String nome, @NotBlank String cpf, @NotBlank String ra, @NotBlank String email, @NotBlank String senha) {
}
