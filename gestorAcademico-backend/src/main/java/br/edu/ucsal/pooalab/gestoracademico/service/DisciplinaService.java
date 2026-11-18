package br.edu.ucsal.pooalab.gestoracademico.service;

import br.edu.ucsal.pooalab.gestoracademico.domain.dtos.DisciplinaResponse;
import br.edu.ucsal.pooalab.gestoracademico.domain.entity.Disciplina;
import br.edu.ucsal.pooalab.gestoracademico.domain.mapper.DisciplinaMapper;
import br.edu.ucsal.pooalab.gestoracademico.exception.RecursoNaoEncontradoException;
import br.edu.ucsal.pooalab.gestoracademico.repository.DisciplinaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DisciplinaService {

    private final DisciplinaRepository disciplinaRepository;
    private final DisciplinaMapper disciplinaMapper;

    public DisciplinaService(DisciplinaRepository disciplinaRepository, DisciplinaMapper disciplinaMapper) {
        this.disciplinaRepository = disciplinaRepository;
        this.disciplinaMapper = disciplinaMapper;
    }

    @Transactional(readOnly = true)
    public List<DisciplinaResponse> listar() {
        return disciplinaRepository.findAllByOrderBySemestreRecomendadoAscNomeAsc().stream()
                .map(disciplinaMapper::paraResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Disciplina buscarEntidade(Long id) {
        return disciplinaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Disciplina não encontrada."));
    }
}
