package br.edu.ucsal.pooalab.gestoracademico.service;

import br.edu.ucsal.pooalab.gestoracademico.domain.dtos.AlunoRequest;
import br.edu.ucsal.pooalab.gestoracademico.domain.entity.Aluno;
import br.edu.ucsal.pooalab.gestoracademico.repository.AlunoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlunoService {

    private final AlunoRepository alunoRepository;

    public AlunoService(AlunoRepository alunoRepository) {
        this.alunoRepository = alunoRepository;
    }

    public Aluno criarAluno(AlunoRequest request) {
        Aluno novoAluno = new Aluno(request);
        return alunoRepository.save(novoAluno);
    }

    public List<Aluno> buscarAlunos() {
        List<Aluno> lista = alunoRepository.findAll();
        return lista;
    }

}
