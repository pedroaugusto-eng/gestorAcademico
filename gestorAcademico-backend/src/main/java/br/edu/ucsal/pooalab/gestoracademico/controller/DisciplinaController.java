package br.edu.ucsal.pooalab.gestoracademico.controller;

import br.edu.ucsal.pooalab.gestoracademico.domain.dtos.DisciplinaResponse;
import br.edu.ucsal.pooalab.gestoracademico.service.DisciplinaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/disciplinas")
public class DisciplinaController {

    private final DisciplinaService disciplinaService;

    public DisciplinaController(DisciplinaService disciplinaService) {
        this.disciplinaService = disciplinaService;
    }

    @GetMapping
    public List<DisciplinaResponse> listar() {
        return disciplinaService.listar();
    }
}
