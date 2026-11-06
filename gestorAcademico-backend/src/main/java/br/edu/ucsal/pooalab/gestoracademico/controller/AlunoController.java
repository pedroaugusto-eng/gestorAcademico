package br.edu.ucsal.pooalab.gestoracademico.controller;

import br.edu.ucsal.pooalab.gestoracademico.domain.dtos.AlunoRequest;
import br.edu.ucsal.pooalab.gestoracademico.domain.entity.Aluno;
import br.edu.ucsal.pooalab.gestoracademico.service.AlunoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/aluno")
public class AlunoController {

    private final AlunoService alunoService;

    public AlunoController(AlunoService alunoService) {
        this.alunoService = alunoService;
    }

    @PostMapping
    public ResponseEntity<Aluno> criarAluno(@RequestBody AlunoRequest request) {
        Aluno novoAluno = alunoService.criarAluno(request);
        return ResponseEntity.ok(novoAluno);
    }

    @GetMapping
    public ResponseEntity<List<Aluno>> buscarAlunos() {
        List<Aluno> listaAlunos = alunoService.buscarAlunos();
        return ResponseEntity.ok().body(listaAlunos);
    }
}
