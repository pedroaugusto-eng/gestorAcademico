package br.edu.ucsal.pooalab.gestoracademico.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;

import java.util.Map;

/**
 * Segurança da aplicação (Spring Security 6).
 *
 * - Senhas com BCrypt (custo 12).
 * - Login por RA ou CPF em POST /api/auth/login (formulário: identificador + senha).
 * - Sessão no servidor (cookie JSESSIONID HttpOnly) e proteção CSRF por cookie XSRF-TOKEN,
 *   o padrão recomendado para front-end servido pela própria aplicação.
 * - A API responde 401/403/429 em JSON, sem redirecionar para página de login.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    public static final String URL_LOGIN = "/api/auth/login";
    public static final String URL_LOGOUT = "/api/auth/logout";
    public static final String CAMPO_IDENTIFICADOR = "identificador";
    public static final String CAMPO_SENHA = "senha";

    private static final String[] PAGINAS_PUBLICAS = {
            "/", "/index.html", "/painel.html", "/css/**", "/js/**", "/favicon.svg"
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain filtrosDeSeguranca(HttpSecurity http,
                                                  TentativasDeLogin tentativas,
                                                  AlunoUserDetailsService alunos,
                                                  RespostasJson respostas) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, PAGINAS_PUBLICAS).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/csrf").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/alunos").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated())

                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
                .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)

                .addFilterBefore(new BloqueioDeForcaBrutaFilter(URL_LOGIN, tentativas, alunos, respostas),
                        UsernamePasswordAuthenticationFilter.class)

                .formLogin(form -> form
                        .loginPage("/")
                        .loginProcessingUrl(URL_LOGIN)
                        .usernameParameter(CAMPO_IDENTIFICADOR)
                        .passwordParameter(CAMPO_SENHA)
                        .successHandler((request, response, authentication) -> {
                            tentativas.registrarSucesso(
                                    alunos.chaveDeTentativas(request.getParameter(CAMPO_IDENTIFICADOR)));
                            renovarCookieCsrf(request);
                            respostas.ok(response, resumoDoAluno(authentication));
                        })
                        .failureHandler((request, response, erro) -> {
                            tentativas.registrarErro(
                                    alunos.chaveDeTentativas(request.getParameter(CAMPO_IDENTIFICADOR)));
                            respostas.erro(response, HttpStatus.UNAUTHORIZED, "RA/CPF ou senha incorretos.");
                        })
                        .permitAll())

                .logout(logout -> logout
                        .logoutUrl(URL_LOGOUT)
                        .logoutSuccessHandler((request, response, authentication) ->
                                response.setStatus(HttpStatus.NO_CONTENT.value()))
                        .deleteCookies("JSESSIONID"))

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, erro) ->
                                respostas.erro(response, HttpStatus.UNAUTHORIZED, "Faça login para continuar."))
                        .accessDeniedHandler((request, response, erro) ->
                                respostas.erro(response, HttpStatus.FORBIDDEN,
                                        "Acesso negado. Recarregue a página e tente de novo.")))

                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; img-src 'self' data:; object-src 'none'; "
                                        + "base-uri 'self'; form-action 'self'; frame-ancestors 'none'"))
                        .referrerPolicy(referrer -> referrer.policy(ReferrerPolicy.SAME_ORIGIN)));

        return http.build();
    }

    /** Depois do login o Spring troca o token CSRF; isto já devolve o novo cookie na mesma resposta. */
    private static void renovarCookieCsrf(HttpServletRequest request) {
        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (token != null) {
            token.getToken();
        }
    }

    private static Map<String, Object> resumoDoAluno(Authentication authentication) {
        AlunoAutenticado aluno = (AlunoAutenticado) authentication.getPrincipal();
        return Map.of("id", aluno.getId(), "nome", aluno.getNome(), "ra", aluno.getUsername());
    }
}
