package br.edu.ucsal.pooalab.gestoracademico;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Sobe o servidor de verdade e repete o que o JavaScript do front faz:
 * abre a página, lê o cookie XSRF-TOKEN, cadastra, entra por CPF e usa a API com a sessão.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:navegador;DB_CLOSE_DELAY=-1")
class FluxoDoNavegadorTest {

    @LocalServerPort
    int porta;

    private final CookieManager cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
    private final HttpClient navegador = HttpClient.newBuilder().cookieHandler(cookies).build();

    @Test
    void fluxoCompletoComCookiesReais() throws Exception {
        HttpResponse<String> pagina = enviar(HttpRequest.newBuilder(url("/")).GET());
        assertThat(pagina.statusCode()).isEqualTo(200);
        assertThat(pagina.body()).contains("Gestor Acadêmico");

        HttpCookie xsrf = cookie("XSRF-TOKEN");
        assertThat(xsrf).as("cookie XSRF-TOKEN entregue ao abrir a página").isNotNull();
        assertThat(xsrf.isHttpOnly()).as("o JavaScript precisa ler o XSRF-TOKEN").isFalse();

        // sem o cabeçalho CSRF, o cadastro é recusado
        String aluno = "{\"nome\":\"Maria Souza\",\"cpf\":\"529.982.247-25\",\"ra\":\"2026001\","
                + "\"email\":\"maria@ucsal.edu.br\",\"senha\":\"SenhaForte#1\"}";
        assertThat(enviar(json("/api/alunos", aluno, false)).statusCode()).isEqualTo(403);
        assertThat(enviar(json("/api/alunos", aluno, true)).statusCode()).isEqualTo(201);

        HttpResponse<String> login = enviar(HttpRequest.newBuilder(url("/api/auth/login"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("X-XSRF-TOKEN", cookie("XSRF-TOKEN").getValue())
                .POST(HttpRequest.BodyPublishers.ofString(
                        "identificador=" + URLEncoder.encode("529.982.247-25", StandardCharsets.UTF_8)
                                + "&senha=" + URLEncoder.encode("SenhaForte#1", StandardCharsets.UTF_8))));
        assertThat(login.statusCode()).isEqualTo(200);
        assertThat(login.body()).contains("\"ra\":\"2026001\"");

        HttpCookie sessao = cookie("JSESSIONID");
        assertThat(sessao).isNotNull();
        assertThat(sessao.isHttpOnly()).as("o JavaScript não pode ler a sessão").isTrue();

        HttpResponse<String> disciplinas = enviar(HttpRequest.newBuilder(url("/api/disciplinas")).GET());
        assertThat(disciplinas.statusCode()).isEqualTo(200);
        assertThat(disciplinas.body()).as("acentos do data.sql").contains("PROGRAMAÇÃO ORIENTADA A OBJETOS");

        // o token CSRF foi trocado no login; o front usa o novo cookie
        HttpResponse<String> matricula = enviar(json("/api/matriculas", "{\"disciplinaId\":2}", true));
        assertThat(matricula.statusCode()).isEqualTo(201);
        assertThat(matricula.body()).contains("\"limiteDeFaltas\":12");
    }

    private HttpRequest.Builder json(String caminho, String corpo, boolean comCsrf) {
        HttpRequest.Builder requisicao = HttpRequest.newBuilder(url(caminho))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(corpo));
        if (comCsrf) {
            requisicao.header("X-XSRF-TOKEN", cookie("XSRF-TOKEN").getValue());
        }
        return requisicao;
    }

    private HttpResponse<String> enviar(HttpRequest.Builder requisicao) throws Exception {
        return navegador.send(requisicao.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private HttpCookie cookie(String nome) {
        return cookies.getCookieStore().getCookies().stream()
                .filter(c -> c.getName().equals(nome) && !c.getValue().isEmpty())
                .reduce((primeiro, ultimo) -> ultimo)
                .orElse(null);
    }

    private URI url(String caminho) {
        return URI.create("http://localhost:" + porta + caminho);
    }
}
