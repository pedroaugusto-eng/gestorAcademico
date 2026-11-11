package br.edu.ucsal.pooalab.gestoracademico.domain.mapper;

import br.edu.ucsal.pooalab.gestoracademico.domain.dtos.BuscaMatriculaPorAlunoId;
import br.edu.ucsal.pooalab.gestoracademico.domain.dtos.DisciplinaResponse;
import br.edu.ucsal.pooalab.gestoracademico.domain.dtos.MatriculaResponse;
import br.edu.ucsal.pooalab.gestoracademico.domain.entity.Aluno;
import br.edu.ucsal.pooalab.gestoracademico.domain.entity.Matricula;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MatriculaMapper {

    private AvaliacaoMapper avaliacaoMapper;

    public MatriculaMapper(AvaliacaoMapper avaliacaoMapper) {
        this.avaliacaoMapper = avaliacaoMapper;
    }

    public MatriculaResponse entityToResponse(Matricula matricula) {

        var avaliacoesResponse = matricula.getAvaliacoes().stream()
                .map(avaliacaoMapper::entityToResponse)
                .collect(Collectors.toList());

        return new MatriculaResponse(matricula.getAluno().getNome(),
                                    matricula.getAluno().getRa(),
                                    matricula.getDisciplina().getNome(),
                                    matricula.getDisciplina().getCargaHoraria(),
                                    matricula.getFaltas(),
                                    avaliacoesResponse);
    }

    public BuscaMatriculaPorAlunoId entityToBuscaPorAlunoDto(Aluno aluno) {

        List<DisciplinaResponse> disciplinasResponse = aluno.getMatriculas().stream()
                .map(matricula -> new DisciplinaResponse(
                        matricula.getDisciplina().getNome(),
                        matricula.getDisciplina().getCargaHoraria()
                ))
                .toList();

        return new BuscaMatriculaPorAlunoId(
                aluno.getNome(),
                aluno.getCpf(),
                disciplinasResponse
        );
    }
}
