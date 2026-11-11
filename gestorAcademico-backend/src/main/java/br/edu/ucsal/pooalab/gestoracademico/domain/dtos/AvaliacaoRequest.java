package br.edu.ucsal.pooalab.gestoracademico.domain.dtos;

import br.edu.ucsal.pooalab.gestoracademico.domain.entity.AvaliacaoTipo;

public record AvaliacaoRequest(Double nota, AvaliacaoTipo tipo) {
}
