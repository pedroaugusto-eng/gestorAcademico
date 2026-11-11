package br.edu.ucsal.pooalab.gestoracademico.domain.mapper;

import br.edu.ucsal.pooalab.gestoracademico.domain.dtos.AlunoRequest;
import br.edu.ucsal.pooalab.gestoracademico.domain.dtos.AlunoResponse;
import br.edu.ucsal.pooalab.gestoracademico.domain.entity.Aluno;
import org.springframework.stereotype.Component;

@Component
public class AlunoMapper {

    public Aluno requestToEntity(AlunoRequest alunoRequest) {
        return new Aluno(alunoRequest.nome(),
                alunoRequest.cpf(),
                alunoRequest.ra(),
                alunoRequest.email(),
                alunoRequest.senha()) ;
    }

    public AlunoResponse alunoToResponse(Aluno aluno) {
        return new AlunoResponse(aluno.getNome(),
                aluno.getCpf(),
                aluno.getEmail(),
                aluno.getRa());
    }
}
