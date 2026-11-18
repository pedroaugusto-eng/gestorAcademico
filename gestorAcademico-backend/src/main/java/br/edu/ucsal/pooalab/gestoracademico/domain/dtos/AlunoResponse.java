package br.edu.ucsal.pooalab.gestoracademico.domain.dtos;

/** Nunca devolve a senha nem o hash. */
public record AlunoResponse(Long id, String nome, String ra, String cpf, String email) {
}
