package br.edu.ucsal.pooalab.gestoracademico.domain.mapper;

import br.edu.ucsal.pooalab.gestoracademico.domain.dtos.AvaliacaoResponse;
import br.edu.ucsal.pooalab.gestoracademico.domain.entity.Avaliacao;
import org.springframework.stereotype.Component;

@Component
public class AvaliacaoMapper {
    public AvaliacaoResponse entityToResponse(Avaliacao avaliacao) {
        return new AvaliacaoResponse(avaliacao.getNota(), avaliacao.getTipo());
    }
}
