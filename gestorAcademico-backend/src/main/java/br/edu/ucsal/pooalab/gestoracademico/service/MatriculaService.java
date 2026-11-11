package br.edu.ucsal.pooalab.gestoracademico.service;

import br.edu.ucsal.pooalab.gestoracademico.domain.dtos.AvaliacaoRequest;
import br.edu.ucsal.pooalab.gestoracademico.domain.dtos.BuscaMatriculaPorAlunoId;
import br.edu.ucsal.pooalab.gestoracademico.domain.dtos.MatriculaResponse;
import br.edu.ucsal.pooalab.gestoracademico.domain.entity.Aluno;
import br.edu.ucsal.pooalab.gestoracademico.domain.entity.Avaliacao;
import br.edu.ucsal.pooalab.gestoracademico.domain.entity.Disciplina;
import br.edu.ucsal.pooalab.gestoracademico.domain.entity.Matricula;
import br.edu.ucsal.pooalab.gestoracademico.domain.mapper.MatriculaMapper;
import br.edu.ucsal.pooalab.gestoracademico.repository.MatriculaRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MatriculaService {

    private final MatriculaRepository matriculaRepository;
    private final AlunoService alunoService;
    private final DisciplinaService disciplinaService;
    private final AvaliacaoService avaliacaoService;
    private final MatriculaMapper matriculaMapper;

    public MatriculaService(MatriculaRepository matriculaRepository, AlunoService alunoService, DisciplinaService disciplinaService, AvaliacaoService avaliacaoService, MatriculaMapper matriculaMapper) {
        this.matriculaRepository = matriculaRepository;
        this.alunoService = alunoService;
        this.disciplinaService = disciplinaService;
        this.avaliacaoService = avaliacaoService;
        this.matriculaMapper = matriculaMapper;
    }

    public Matricula buscarEntidadePorId(Long id) {
        return matriculaRepository.findById(id).orElseThrow(() -> new RuntimeException("Matricula nao encotrada"));
    }

    @Transactional
    public MatriculaResponse matricularAluno(Long alunoId, Long disciplinaId) {
        Aluno aluno = alunoService.buscarEntidadePorId(alunoId);
        Disciplina disciplina = disciplinaService.buscarEntidadePorId(disciplinaId);

        Matricula matricula = new Matricula(aluno, disciplina);
        return matriculaMapper.entityToResponse(matriculaRepository.save(matricula));
    }

    @Transactional
    public MatriculaResponse adicionarAvaliacao(Long matriculaId, AvaliacaoRequest request) {
        Matricula matricula = buscarEntidadePorId(matriculaId);

        Avaliacao avaliacao = avaliacaoService.criarAvaliacao(request, matricula);
        matricula.adicionarAvaliacao(avaliacao);

        return matriculaMapper.entityToResponse(matriculaRepository.save(matricula));
    }

    @Transactional
    public MatriculaResponse registrarFalta(Long matriculaId) {
        Matricula matricula = buscarEntidadePorId(matriculaId);
        matricula.registrarFalta(2);

        var totalMinutosDisciplina = matricula.getDisciplina().getCargaHoraria() * 60;
        var limiteFalta = (0.25 * totalMinutosDisciplina) / 75;

        if(matricula.getFaltas() > limiteFalta) {
            throw new IllegalStateException("Limite de faltas excedido! Reprovado");
        }

        return matriculaMapper.entityToResponse(matriculaRepository.save(matricula));
    }

    public BuscaMatriculaPorAlunoId buscarMatriculaPorAlunoId(Long alunoId) {
        Aluno aluno = alunoService.buscarEntidadePorId(alunoId);
        return matriculaMapper.entityToBuscaPorAlunoDto(aluno);
    }
}
