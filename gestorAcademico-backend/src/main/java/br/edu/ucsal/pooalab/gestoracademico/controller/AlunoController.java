package br.edu.ucsal.pooalab.gestoracademico.controller;

import br.edu.ucsal.pooalab.gestoracademico.domain.dtos.AlunoRequest;
import br.edu.ucsal.pooalab.gestoracademico.domain.dtos.AlunoResponse;
import br.edu.ucsal.pooalab.gestoracademico.domain.dtos.AvaliacaoRequest;
import br.edu.ucsal.pooalab.gestoracademico.service.AlunoService;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<Object> criarAluno(@RequestBody AlunoRequest request) {
        try {
            AlunoResponse novoAluno = alunoService.criarAluno(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(novoAluno);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Houve um erro inesperado -> " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<Object> buscarAlunos() {
        try {
            List<AlunoResponse> listaAlunos = alunoService.buscarAlunos();
            return ResponseEntity.ok().body(listaAlunos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Houve um erro inesperado -> " + e.getMessage());
        }
    }
}
