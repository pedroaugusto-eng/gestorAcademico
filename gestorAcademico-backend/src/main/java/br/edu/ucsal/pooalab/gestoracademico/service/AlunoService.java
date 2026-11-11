package br.edu.ucsal.pooalab.gestoracademico.service;

import br.edu.ucsal.pooalab.gestoracademico.domain.dtos.AlunoRequest;
import br.edu.ucsal.pooalab.gestoracademico.domain.dtos.AlunoResponse;
import br.edu.ucsal.pooalab.gestoracademico.domain.entity.Aluno;
import br.edu.ucsal.pooalab.gestoracademico.domain.mapper.AlunoMapper;
import br.edu.ucsal.pooalab.gestoracademico.repository.AlunoRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AlunoService {

    private final AlunoRepository alunoRepository;
    private final AlunoMapper alunoMapper;

    public AlunoService(AlunoRepository alunoRepository, AlunoMapper alunoMapper, AvaliacaoService avaliacaoService) {
        this.alunoRepository = alunoRepository;
        this.alunoMapper = alunoMapper;
    }

    public AlunoResponse criarAluno(AlunoRequest request) {
        Aluno alunoEntity = alunoMapper.requestToEntity(request);
        alunoRepository.save(alunoEntity);

        return alunoMapper.alunoToResponse(alunoEntity);
    }

    public Aluno buscarEntidadePorId(Long id) {
        return alunoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado"));
    }

    public AlunoResponse buscarPorId(Long id) {
        Aluno aluno = buscarEntidadePorId(id);
        return alunoMapper.alunoToResponse(aluno);
    }

    public List<AlunoResponse> buscarAlunos() {
        List<Aluno> listaAlunos = alunoRepository.findAll();
        List<AlunoResponse> listaResponse = new ArrayList<>();

        for(Aluno aluno :  listaAlunos) {
            listaResponse.add(alunoMapper.alunoToResponse(aluno));
        }

        return listaResponse;
    }

}
