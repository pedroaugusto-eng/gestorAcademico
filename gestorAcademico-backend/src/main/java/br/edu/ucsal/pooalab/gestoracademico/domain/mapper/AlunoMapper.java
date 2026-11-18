package br.edu.ucsal.pooalab.gestoracademico.domain.mapper;

import br.edu.ucsal.pooalab.gestoracademico.domain.Identificadores;
import br.edu.ucsal.pooalab.gestoracademico.domain.dtos.AlunoRequest;
import br.edu.ucsal.pooalab.gestoracademico.domain.dtos.AlunoResponse;
import br.edu.ucsal.pooalab.gestoracademico.domain.entity.Aluno;
import org.springframework.stereotype.Component;

@Component
public class AlunoMapper {

    /** Recebe a senha já transformada em hash: o mapper nunca vê a senha em texto puro. */
    public Aluno paraEntidade(AlunoRequest request, String senhaHash) {
        return new Aluno(
                request.nome().trim(),
                Identificadores.cpf(request.cpf()),
                Identificadores.ra(request.ra()),
                Identificadores.email(request.email()),
                senhaHash);
    }

    public AlunoResponse paraResponse(Aluno aluno) {
        return new AlunoResponse(aluno.getId(), aluno.getNome(), aluno.getRa(),
                formatarCpf(aluno.getCpf()), aluno.getEmail());
    }

    private static String formatarCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) {
            return cpf;
        }
        return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-" + cpf.substring(9);
    }
}
