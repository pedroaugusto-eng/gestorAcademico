package br.edu.ucsal.pooalab.gestoracademico.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Login e logout são feitos pelo próprio Spring Security (ver SecurityConfig):
 * POST /api/auth/login e POST /api/auth/logout.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /** O front chama esta rota se ainda não tiver o cookie XSRF-TOKEN; o filtro CSRF o devolve. */
    @GetMapping("/csrf")
    public ResponseEntity<Void> csrf() {
        return ResponseEntity.noContent().build();
    }
}
