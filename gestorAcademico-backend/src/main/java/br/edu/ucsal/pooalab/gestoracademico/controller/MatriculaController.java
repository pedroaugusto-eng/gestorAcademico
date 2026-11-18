package br.edu.ucsal.pooalab.gestoracademico.controller;

import br.edu.ucsal.pooalab.gestoracademico.domain.dtos.AvaliacaoRequest;
import br.edu.ucsal.pooalab.gestoracademico.domain.dtos.MatriculaRequest;
import br.edu.ucsal.pooalab.gestoracademico.domain.dtos.MatriculaResponse;
import br.edu.ucsal.pooalab.gestoracademico.security.AlunoAutenticado;
import br.edu.ucsal.pooalab.gestoracademico.service.MatriculaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * O aluno é sempre o que está logado: o id não vem mais na URL,
 * então ninguém consegue mexer na matrícula de outra pessoa trocando um número.
 */
@RestController
@RequestMapping("/api/matriculas")
public class MatriculaController {

    private final MatriculaService matriculaService;

    public MatriculaController(MatriculaService matriculaService) {
        this.matriculaService = matriculaService;
    }

    @GetMapping
    public List<MatriculaResponse> minhasMatriculas(@AuthenticationPrincipal AlunoAutenticado aluno) {
        return matriculaService.listarDoAluno(aluno.getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MatriculaResponse matricular(@AuthenticationPrincipal AlunoAutenticado aluno,
                                        @Valid @RequestBody MatriculaRequest request) {
        return matriculaService.matricular(aluno.getId(), request.disciplinaId());
    }

    @PostMapping("/{matriculaId}/avaliacoes")
    @ResponseStatus(HttpStatus.CREATED)
    public MatriculaResponse adicionarAvaliacao(@AuthenticationPrincipal AlunoAutenticado aluno,
                                                @PathVariable Long matriculaId,
                                                @Valid @RequestBody AvaliacaoRequest request) {
        return matriculaService.adicionarAvaliacao(aluno.getId(), matriculaId, request);
    }

    /** Corrige uma nota lançada errada. */
    @PutMapping("/{matriculaId}/avaliacoes/{avaliacaoId}")
    public MatriculaResponse alterarAvaliacao(@AuthenticationPrincipal AlunoAutenticado aluno,
                                              @PathVariable Long matriculaId,
                                              @PathVariable Long avaliacaoId,
                                              @Valid @RequestBody AvaliacaoRequest request) {
        return matriculaService.alterarAvaliacao(aluno.getId(), matriculaId, avaliacaoId, request);
    }

    @DeleteMapping("/{matriculaId}/avaliacoes/{avaliacaoId}")
    public MatriculaResponse removerAvaliacao(@AuthenticationPrincipal AlunoAutenticado aluno,
                                              @PathVariable Long matriculaId,
                                              @PathVariable Long avaliacaoId) {
        return matriculaService.removerAvaliacao(aluno.getId(), matriculaId, avaliacaoId);
    }

    /** Registra uma falta (2 aulas). Pode passar do limite; a resposta indica se reprovou por falta. */
    @PostMapping("/{matriculaId}/faltas")
    public MatriculaResponse registrarFalta(@AuthenticationPrincipal AlunoAutenticado aluno,
                                            @PathVariable Long matriculaId) {
        return matriculaService.registrarFalta(aluno.getId(), matriculaId);
    }

    /** Desfaz a última falta registrada por engano. */
    @DeleteMapping("/{matriculaId}/faltas")
    public MatriculaResponse desfazerUltimaFalta(@AuthenticationPrincipal AlunoAutenticado aluno,
                                                 @PathVariable Long matriculaId) {
        return matriculaService.desfazerUltimaFalta(aluno.getId(), matriculaId);
    }
}
