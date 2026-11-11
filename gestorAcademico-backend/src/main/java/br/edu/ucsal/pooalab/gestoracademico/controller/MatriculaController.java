package br.edu.ucsal.pooalab.gestoracademico.controller;

import br.edu.ucsal.pooalab.gestoracademico.domain.dtos.AvaliacaoRequest;
import br.edu.ucsal.pooalab.gestoracademico.domain.dtos.BuscaMatriculaPorAlunoId;
import br.edu.ucsal.pooalab.gestoracademico.domain.dtos.MatriculaResponse;
import br.edu.ucsal.pooalab.gestoracademico.service.MatriculaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/matricula")
public class MatriculaController {

    private final MatriculaService matriculaService;

    public MatriculaController(MatriculaService matriculaService) {
        this.matriculaService = matriculaService;
    }

    @PostMapping("/{alunoId}/{disciplinaId}")
    public ResponseEntity<Object> matricularAluno(@PathVariable Long alunoId, @PathVariable Long disciplinaId) {
        try {
            MatriculaResponse matricula = matriculaService.matricularAluno(alunoId, disciplinaId);
            return ResponseEntity.status(HttpStatus.CREATED).body(matricula);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Houve um erro inesperado -> " + e.getMessage());
        }
    }

    @PostMapping("/{matriculaId}")
    public ResponseEntity<Object> adicionarNota(
            @PathVariable Long matriculaId,
            @RequestBody AvaliacaoRequest request) {
        try {
            MatriculaResponse matriculaResponseAtualizada = matriculaService.adicionarAvaliacao(matriculaId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(matriculaResponseAtualizada);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Houve um erro inesperado -> " + e.getMessage());
        }
    }

    @PutMapping("/faltas/{matriculaId}")
    public ResponseEntity<Object> registrarFalta(@PathVariable Long matriculaId) {
        try {
            MatriculaResponse response = matriculaService.registrarFalta(matriculaId);
            return ResponseEntity.ok(response);
        }  catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Houve um erro inesperado -> " + e.getMessage());
        }
    }

    @GetMapping("/{aluno}")
    public ResponseEntity<Object> buscarMatriculaPorAlunoId(@PathVariable Long alunoId) {
        try {
            BuscaMatriculaPorAlunoId matriculaPorAluno = matriculaService.buscarMatriculaPorAlunoId(alunoId);
            return ResponseEntity.ok(matriculaPorAluno);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Houve um erro inesperado -> " + e.getMessage());
        }
    }

}
