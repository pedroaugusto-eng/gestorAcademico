package br.edu.ucsal.pooalab.gestoracademico.domain.dtos;

import br.edu.ucsal.pooalab.gestoracademico.domain.entity.AvaliacaoTipo;

public record AvaliacaoResponse(double nota, AvaliacaoTipo avaliacaoTipo) {
}
