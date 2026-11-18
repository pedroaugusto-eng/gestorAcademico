package br.edu.ucsal.pooalab.gestoracademico.domain.dtos;

import br.edu.ucsal.pooalab.gestoracademico.domain.entity.AvaliacaoTipo;

public record AvaliacaoResponse(Long id, double nota, AvaliacaoTipo tipo, int unidade) {
}
