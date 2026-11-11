package br.edu.ucsal.pooalab.gestoracademico.service;

import br.edu.ucsal.pooalab.gestoracademico.domain.entity.Disciplina;
import br.edu.ucsal.pooalab.gestoracademico.repository.DisciplinaRepository;
import org.springframework.stereotype.Service;

@Service
public class DisciplinaService {

    private final DisciplinaRepository disciplinaRepository;

    public DisciplinaService(DisciplinaRepository disciplinaRepository) {
        this.disciplinaRepository = disciplinaRepository;
    }

    public Disciplina buscarEntidadePorId(Long id) {
        return disciplinaRepository.findById(id).orElseThrow(() -> new RuntimeException("Disciplina não encontrada"));
    }
}
