package br.edu.ucsal.pooalab.gestoracademico.controller;

import br.edu.ucsal.pooalab.gestoracademico.domain.dtos.AlunoRequest;
import br.edu.ucsal.pooalab.gestoracademico.domain.dtos.AlunoResponse;
import br.edu.ucsal.pooalab.gestoracademico.security.AlunoAutenticado;
import br.edu.ucsal.pooalab.gestoracademico.service.AlunoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alunos")
public class AlunoController {

    private final AlunoService alunoService;

    public AlunoController(AlunoService alunoService) {
        this.alunoService = alunoService;
    }

    /** Cadastro público: é assim que o aluno cria o acesso dele. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AlunoResponse cadastrar(@Valid @RequestBody AlunoRequest request) {
        return alunoService.cadastrar(request);
    }

    /** Dados do aluno logado. Substitui o antigo GET /aluno, que listava todos os alunos com CPF. */
    @GetMapping("/me")
    public AlunoResponse eu(@AuthenticationPrincipal AlunoAutenticado aluno) {
        return alunoService.buscarPorId(aluno.getId());
    }
}
