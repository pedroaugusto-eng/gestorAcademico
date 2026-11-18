package br.edu.ucsal.pooalab.gestoracademico.service;

import br.edu.ucsal.pooalab.gestoracademico.domain.dtos.AvaliacaoRequest;
import br.edu.ucsal.pooalab.gestoracademico.domain.dtos.MatriculaResponse;
import br.edu.ucsal.pooalab.gestoracademico.domain.entity.Aluno;
import br.edu.ucsal.pooalab.gestoracademico.domain.entity.Disciplina;
import br.edu.ucsal.pooalab.gestoracademico.domain.entity.Matricula;
import br.edu.ucsal.pooalab.gestoracademico.domain.mapper.MatriculaMapper;
import br.edu.ucsal.pooalab.gestoracademico.exception.ConflitoException;
import br.edu.ucsal.pooalab.gestoracademico.exception.RecursoNaoEncontradoException;
import br.edu.ucsal.pooalab.gestoracademico.repository.AlunoRepository;
import br.edu.ucsal.pooalab.gestoracademico.repository.MatriculaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Toda operação recebe o id do aluno logado (vindo do Spring Security, nunca da URL).
 * Assim um aluno só consegue ver e alterar as próprias matrículas.
 */
@Service
public class MatriculaService {

    private final MatriculaRepository matriculaRepository;
    private final AlunoRepository alunoRepository;
    private final DisciplinaService disciplinaService;
    private final MatriculaMapper matriculaMapper;

    public MatriculaService(MatriculaRepository matriculaRepository, AlunoRepository alunoRepository,
                            DisciplinaService disciplinaService, MatriculaMapper matriculaMapper) {
        this.matriculaRepository = matriculaRepository;
        this.alunoRepository = alunoRepository;
        this.disciplinaService = disciplinaService;
        this.matriculaMapper = matriculaMapper;
    }

    @Transactional(readOnly = true)
    public List<MatriculaResponse> listarDoAluno(Long alunoId) {
        return matriculaRepository.findByAlunoIdOrderByIdAsc(alunoId).stream()
                .map(matriculaMapper::paraResponse)
                .toList();
    }

    @Transactional
    public MatriculaResponse matricular(Long alunoId, Long disciplinaId) {
        Disciplina disciplina = disciplinaService.buscarEntidade(disciplinaId);
        if (matriculaRepository.existsByAlunoIdAndDisciplinaId(alunoId, disciplinaId)) {
            throw new ConflitoException("Você já está matriculado em " + disciplina.getNome() + ".");
        }
        Aluno aluno = alunoRepository.getReferenceById(alunoId);
        return matriculaMapper.paraResponse(matriculaRepository.save(new Matricula(aluno, disciplina)));
    }

    @Transactional
    public MatriculaResponse adicionarAvaliacao(Long alunoId, Long matriculaId, AvaliacaoRequest request) {
        Matricula matricula = buscarDoAluno(alunoId, matriculaId);
        matricula.adicionarAvaliacao(request.nota(), request.tipo(), request.unidade());
        return matriculaMapper.paraResponse(matriculaRepository.saveAndFlush(matricula));
    }

    @Transactional
    public MatriculaResponse alterarAvaliacao(Long alunoId, Long matriculaId, Long avaliacaoId, AvaliacaoRequest request) {
        Matricula matricula = buscarDoAluno(alunoId, matriculaId);
        matricula.alterarAvaliacao(avaliacaoId, request.nota(), request.tipo(), request.unidade());
        return matriculaMapper.paraResponse(matriculaRepository.saveAndFlush(matricula));
    }

    @Transactional
    public MatriculaResponse removerAvaliacao(Long alunoId, Long matriculaId, Long avaliacaoId) {
        Matricula matricula = buscarDoAluno(alunoId, matriculaId);
        matricula.removerAvaliacao(avaliacaoId);
        return matriculaMapper.paraResponse(matriculaRepository.saveAndFlush(matricula));
    }

    @Transactional
    public MatriculaResponse registrarFalta(Long alunoId, Long matriculaId) {
        Matricula matricula = buscarDoAluno(alunoId, matriculaId);
        matricula.registrarFalta();
        return matriculaMapper.paraResponse(matricula);
    }

    @Transactional
    public MatriculaResponse desfazerUltimaFalta(Long alunoId, Long matriculaId) {
        Matricula matricula = buscarDoAluno(alunoId, matriculaId);
        matricula.desfazerUltimaFalta();
        return matriculaMapper.paraResponse(matricula);
    }

    private Matricula buscarDoAluno(Long alunoId, Long matriculaId) {
        return matriculaRepository.findByIdAndAlunoId(matriculaId, alunoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Matrícula não encontrada."));
    }
}
