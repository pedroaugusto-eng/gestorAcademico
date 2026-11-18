package br.edu.ucsal.pooalab.gestoracademico.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/** Barra o login antes de conferir a senha quando o RA/CPF está temporariamente bloqueado. */
public class BloqueioDeForcaBrutaFilter extends OncePerRequestFilter {

    private final String urlDeLogin;
    private final TentativasDeLogin tentativas;
    private final AlunoUserDetailsService alunos;
    private final RespostasJson respostas;

    public BloqueioDeForcaBrutaFilter(String urlDeLogin, TentativasDeLogin tentativas,
                                      AlunoUserDetailsService alunos, RespostasJson respostas) {
        this.urlDeLogin = urlDeLogin;
        this.tentativas = tentativas;
        this.alunos = alunos;
        this.respostas = respostas;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String caminho = request.getRequestURI().substring(request.getContextPath().length());
        return !("POST".equals(request.getMethod()) && urlDeLogin.equals(caminho));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String chave = alunos.chaveDeTentativas(request.getParameter(SecurityConfig.CAMPO_IDENTIFICADOR));
        if (tentativas.estaBloqueado(chave)) {
            respostas.erro(response, HttpStatus.TOO_MANY_REQUESTS,
                    "Muitas tentativas sem sucesso. Aguarde 15 minutos e tente de novo.");
            return;
        }
        chain.doFilter(request, response);
    }
}
