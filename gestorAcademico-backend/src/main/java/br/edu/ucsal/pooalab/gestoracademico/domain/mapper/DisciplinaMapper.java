package br.edu.ucsal.pooalab.gestoracademico.domain.mapper;

import br.edu.ucsal.pooalab.gestoracademico.domain.dtos.DisciplinaResponse;
import br.edu.ucsal.pooalab.gestoracademico.domain.entity.Disciplina;
import org.springframework.stereotype.Component;

@Component
public class DisciplinaMapper {

    public DisciplinaResponse paraResponse(Disciplina disciplina) {
        String curso = disciplina.getCurso() != null ? disciplina.getCurso().getNome() : null;
        return new DisciplinaResponse(
                disciplina.getId(),
                disciplina.getNome(),
                disciplina.getCodigo(),
                curso,
                disciplina.getCargaHoraria(),
                disciplina.getSemestreRecomendado(),
                disciplina.totalDeAulas(),
                disciplina.limiteDeFaltas());
    }
}
