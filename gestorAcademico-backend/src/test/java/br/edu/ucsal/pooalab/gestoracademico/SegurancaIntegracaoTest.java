package br.edu.ucsal.pooalab.gestoracademico;

import br.edu.ucsal.pooalab.gestoracademico.domain.entity.Aluno;
import br.edu.ucsal.pooalab.gestoracademico.repository.AlunoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SegurancaIntegracaoTest {

    private static final AtomicInteger SEQUENCIA = new AtomicInteger(314_159_000);
    private static final String SENHA = "SenhaForte#2026";
    private static final long DISCIPLINA_60H = 2L; // data.sql: POO, 60h -> limite de 12 aulas

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired AlunoRepository alunoRepository;
    @Autowired PasswordEncoder passwordEncoder;

    // ---------------- Cadastro e BCrypt ----------------

    @Test
    void cadastroGuardaSenhaComBcryptENuncaDevolveASenha() throws Exception {
        Dados aluno = novoAluno();
        mvc.perform(post("/api/alunos").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(aluno.json()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ra").value(aluno.ra))
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$.senhaHash").doesNotExist());

        Aluno salvo = alunoRepository.findByRa(aluno.ra).orElseThrow();
        assertThat(salvo.getSenhaHash()).startsWith("$2a$12$").isNotEqualTo(SENHA);
        assertThat(passwordEncoder.matches(SENHA, salvo.getSenhaHash())).isTrue();
        assertThat(salvo.getCpf()).isEqualTo(aluno.cpf).hasSize(11);
    }

    @Test
    void cadastroRepetidoOuComRaIgualAoCpfDeOutroAlunoEhRecusado() throws Exception {
        Dados aluno = cadastrar(novoAluno());

        Dados mesmoCpf = novoAluno().comCpf(aluno.cpf);
        mvc.perform(post("/api/alunos").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(mesmoCpf.json()))
                .andExpect(status().isConflict());

        // RA igual ao CPF de outro aluno deixaria o login ambíguo
        Dados raIgualCpf = novoAluno().comRa(aluno.cpf);
        mvc.perform(post("/api/alunos").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(raIgualCpf.json()))
                .andExpect(status().isConflict());
    }

    @Test
    void cadastroValidaCamposEDevolveOsErrosPorCampo() throws Exception {
        String invalido = json.writeValueAsString(Map.of(
                "nome", "", "cpf", "111.222.333-44", "ra", "RA 1", "email", "sem-arroba", "senha", "curta"));
        mvc.perform(post("/api/alunos").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(invalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").exists())
                .andExpect(jsonPath("$.campos.cpf").value("CPF inválido"))
                .andExpect(jsonPath("$.campos.ra").exists())
                .andExpect(jsonPath("$.campos.email").exists())
                .andExpect(jsonPath("$.campos.senha").exists());
    }

    // ---------------- Login por RA ou CPF ----------------

    @Test
    void loginFuncionaComRa() throws Exception {
        Dados aluno = cadastrar(novoAluno());
        mvc.perform(post("/api/auth/login").with(csrf())
                        .param("identificador", aluno.ra.toLowerCase()).param("senha", SENHA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value(aluno.nome))
                .andExpect(jsonPath("$.ra").value(aluno.ra));
    }

    @Test
    void loginFuncionaComCpfComOuSemPontuacao() throws Exception {
        Dados aluno = cadastrar(novoAluno());
        for (String cpf : new String[]{aluno.cpf, aluno.cpfFormatado()}) {
            mvc.perform(post("/api/auth/login").with(csrf()).param("identificador", cpf).param("senha", SENHA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ra").value(aluno.ra));
        }
    }

    @Test
    void senhaErradaOuAlunoInexistenteDevolve401ComAMesmaMensagem() throws Exception {
        Dados aluno = cadastrar(novoAluno());
        String mensagemSenhaErrada = mvc.perform(post("/api/auth/login").with(csrf())
                        .param("identificador", aluno.ra).param("senha", "outraSenha123"))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

        String mensagemInexistente = mvc.perform(post("/api/auth/login").with(csrf())
                        .param("identificador", "NAOEXISTE999").param("senha", "outraSenha123"))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

        assertThat(mensagemSenhaErrada).isEqualTo(mensagemInexistente); // não revela quem está cadastrado
    }

    @Test
    void cincoErrosSeguidosBloqueiamOLoginMesmoComASenhaCerta() throws Exception {
        Dados aluno = cadastrar(novoAluno());
        for (int i = 0; i < 5; i++) {
            mvc.perform(post("/api/auth/login").with(csrf()).param("identificador", aluno.ra).param("senha", "errada" + i))
                    .andExpect(status().isUnauthorized());
        }
        mvc.perform(post("/api/auth/login").with(csrf()).param("identificador", aluno.ra).param("senha", SENHA))
                .andExpect(status().isTooManyRequests());
        // trocar o RA pelo CPF não zera a contagem: o bloqueio é do aluno
        mvc.perform(post("/api/auth/login").with(csrf()).param("identificador", aluno.cpfFormatado()).param("senha", SENHA))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void errosAlternandoRaECpfSomamNaMesmaContagem() throws Exception {
        Dados aluno = cadastrar(novoAluno());
        for (int i = 0; i < 5; i++) {
            String identificador = (i % 2 == 0) ? aluno.ra : aluno.cpf;
            mvc.perform(post("/api/auth/login").with(csrf()).param("identificador", identificador).param("senha", "errada" + i))
                    .andExpect(status().isUnauthorized());
        }
        mvc.perform(post("/api/auth/login").with(csrf()).param("identificador", aluno.ra).param("senha", SENHA))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void logoutEncerraASessao() throws Exception {
        MockHttpSession sessao = entrar(cadastrar(novoAluno()));
        mvc.perform(get("/api/alunos/me").session(sessao)).andExpect(status().isOk());
        mvc.perform(post("/api/auth/logout").with(csrf()).session(sessao)).andExpect(status().isNoContent());
        mvc.perform(get("/api/alunos/me").session(sessao)).andExpect(status().isUnauthorized());
    }

    // ---------------- Proteção das rotas ----------------

    @Test
    void rotasDaApiExigemLogin() throws Exception {
        mvc.perform(get("/api/matriculas")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Faça login para continuar."));
        mvc.perform(get("/api/alunos/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/disciplinas")).andExpect(status().isUnauthorized());
    }

    @Test
    void escritaSemTokenCsrfEhRecusada() throws Exception {
        mvc.perform(post("/api/alunos").contentType(MediaType.APPLICATION_JSON).content(novoAluno().json()))
                .andExpect(status().isForbidden());
        MockHttpSession sessao = entrar(cadastrar(novoAluno()));
        mvc.perform(post("/api/matriculas").session(sessao)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"disciplinaId\":1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void paginasDoFrontSaoPublicasEVemComCabecalhosDeSeguranca() throws Exception {
        mvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Security-Policy", org.hamcrest.Matchers.containsString("default-src 'self'")))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
        mvc.perform(get("/painel.html")).andExpect(status().isOk());
        mvc.perform(get("/js/api.js")).andExpect(status().isOk());
    }

    @Test
    void umAlunoNaoEnxergaNemAlteraAMatriculaDeOutro() throws Exception {
        MockHttpSession sessaoA = entrar(cadastrar(novoAluno()));
        MockHttpSession sessaoB = entrar(cadastrar(novoAluno()));

        long matriculaDeA = idDe(mvc.perform(post("/api/matriculas").with(csrf()).session(sessaoA)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"disciplinaId\":1}"))
                .andExpect(status().isCreated()).andReturn());

        mvc.perform(get("/api/matriculas").session(sessaoB))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(post("/api/matriculas/" + matriculaDeA + "/faltas").with(csrf()).session(sessaoB))
                .andExpect(status().isNotFound());
        mvc.perform(post("/api/matriculas/" + matriculaDeA + "/avaliacoes").with(csrf()).session(sessaoB)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nota\":10,\"tipo\":\"PROVA\",\"unidade\":1}"))
                .andExpect(status().isNotFound());

        mvc.perform(get("/api/matriculas").session(sessaoA))
                .andExpect(jsonPath("$[0].faltas").value(0))
                .andExpect(jsonPath("$[0].avaliacoes.length()").value(0));
    }

    // ---------------- Regras acadêmicas ----------------

    @Test
    void fluxoCompletoDeMatriculaNotasEFaltas() throws Exception {
        MockHttpSession sessao = entrar(cadastrar(novoAluno()));

        long matricula = idDe(mvc.perform(post("/api/matriculas").with(csrf()).session(sessao)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"disciplinaId\":" + DISCIPLINA_60H + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.limiteDeFaltas").value(12))
                .andExpect(jsonPath("$.media").value(0.0))
                .andExpect(jsonPath("$.mediaMinima").value(6.0))
                .andExpect(jsonPath("$.notaNecessaria").value(12.0))
                .andReturn());

        mvc.perform(post("/api/matriculas").with(csrf()).session(sessao)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"disciplinaId\":" + DISCIPLINA_60H + "}"))
                .andExpect(status().isConflict());

        // 1ª unidade com 4,5: média parcial 2,25 e precisa de 7,5 na 2ª (regra do teste da Strategy)
        mvc.perform(post("/api/matriculas/" + matricula + "/avaliacoes").with(csrf()).session(sessao)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nota\":4.5,\"tipo\":\"PROVA\",\"unidade\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.unidades[0].nota").value(4.5))
                .andExpect(jsonPath("$.unidades[1].nota").value(0.0))
                .andExpect(jsonPath("$.media").value(2.25))
                .andExpect(jsonPath("$.notaNecessaria").value(7.5))
                .andExpect(jsonPath("$.mediaAtingida").value(false))
                .andExpect(jsonPath("$.avaliacoes[0].unidade").value(1));

        // 2ª unidade com uma prova que vale 10 sozinha: média (4,5 + 10) / 2 = 7,25
        mvc.perform(post("/api/matriculas/" + matricula + "/avaliacoes").with(csrf()).session(sessao)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nota\":10,\"tipo\":\"PROVA\",\"unidade\":2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.media").value(7.25))
                .andExpect(jsonPath("$.notaNecessaria").value(0.0))
                .andExpect(jsonPath("$.mediaAtingida").value(true));

        mvc.perform(post("/api/matriculas/" + matricula + "/avaliacoes").with(csrf()).session(sessao)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nota\":11,\"tipo\":\"ATIVIDADE\",\"unidade\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nota").exists());

        // Faltas: pode passar do limite de 12 aulas e fica reprovado por falta, mesmo com média
        for (int i = 1; i <= 6; i++) {
            mvc.perform(post("/api/matriculas/" + matricula + "/faltas").with(csrf()).session(sessao))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.faltas").value(i * 2))
                    .andExpect(jsonPath("$.faltasRegistradas").value(i))
                    .andExpect(jsonPath("$.reprovadoPorFalta").value(false));
        }
        mvc.perform(post("/api/matriculas/" + matricula + "/faltas").with(csrf()).session(sessao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.faltas").value(14))
                .andExpect(jsonPath("$.faltasRegistradas").value(7))
                .andExpect(jsonPath("$.reprovadoPorFalta").value(true))
                .andExpect(jsonPath("$.mediaAtingida").value(true));

        // Desfazer a falta lançada por engano volta para dentro do limite
        mvc.perform(delete("/api/matriculas/" + matricula + "/faltas").with(csrf()).session(sessao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.faltas").value(12))
                .andExpect(jsonPath("$.reprovadoPorFalta").value(false));
    }

    @Test
    void cadaUnidadeTemUmaProvaUmaAtividadeEValeNoMaximo10() throws Exception {
        MockHttpSession sessao = entrar(cadastrar(novoAluno()));
        long matricula = idDe(mvc.perform(post("/api/matriculas").with(csrf()).session(sessao)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"disciplinaId\":" + DISCIPLINA_60H + "}"))
                .andExpect(status().isCreated()).andReturn());
        String rota = "/api/matriculas/" + matricula + "/avaliacoes";

        mvc.perform(post(rota).with(csrf()).session(sessao).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nota\":7,\"tipo\":\"PROVA\",\"unidade\":1}"))
                .andExpect(status().isCreated());

        // segunda prova na mesma unidade
        mvc.perform(post(rota).with(csrf()).session(sessao).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nota\":2,\"tipo\":\"PROVA\",\"unidade\":1}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("Já existe uma prova na 1ª unidade. Para corrigir a nota, use Alterar."));

        // atividade que faria a unidade passar de 10
        mvc.perform(post(rota).with(csrf()).session(sessao).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nota\":3.5,\"tipo\":\"ATIVIDADE\",\"unidade\":1}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value(
                        "Cada unidade vale no máximo 10 pontos. A 1ª unidade já tem 7, então esta nota pode ser no máximo 3."));

        // prova 7 + atividade 3 = 10 na 1ª unidade
        mvc.perform(post(rota).with(csrf()).session(sessao).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nota\":3,\"tipo\":\"ATIVIDADE\",\"unidade\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.unidades[0].nota").value(10.0))
                .andExpect(jsonPath("$.media").value(5.0));

        // unidade inexistente
        mvc.perform(post(rota).with(csrf()).session(sessao).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nota\":5,\"tipo\":\"PROVA\",\"unidade\":3}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.unidade").exists());

        // desfazer falta sem nenhuma falta registrada
        mvc.perform(delete("/api/matriculas/" + matricula + "/faltas").with(csrf()).session(sessao))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("Não há falta registrada para desfazer."));
    }

    @Test
    void notaLancadaErradaPodeSerAlteradaOuExcluida() throws Exception {
        MockHttpSession sessao = entrar(cadastrar(novoAluno()));
        long matricula = idDe(mvc.perform(post("/api/matriculas").with(csrf()).session(sessao)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"disciplinaId\":" + DISCIPLINA_60H + "}"))
                .andExpect(status().isCreated()).andReturn());

        MvcResult lancada = mvc.perform(post("/api/matriculas/" + matricula + "/avaliacoes").with(csrf()).session(sessao)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nota\":4.5,\"tipo\":\"PROVA\",\"unidade\":1}"))
                .andExpect(status().isCreated()).andReturn();
        long avaliacao = json.readTree(lancada.getResponse().getContentAsString())
                .get("avaliacoes").get(0).get("id").asLong();
        String rotaDaNota = "/api/matriculas/" + matricula + "/avaliacoes/" + avaliacao;

        mvc.perform(put(rotaDaNota).with(csrf()).session(sessao)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nota\":8,\"tipo\":\"ATIVIDADE\",\"unidade\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avaliacoes.length()").value(1))
                .andExpect(jsonPath("$.avaliacoes[0].nota").value(8.0))
                .andExpect(jsonPath("$.avaliacoes[0].tipo").value("ATIVIDADE"))
                .andExpect(jsonPath("$.unidades[0].nota").value(8.0))
                .andExpect(jsonPath("$.media").value(4.0))
                .andExpect(jsonPath("$.notaNecessaria").value(4.0));

        mvc.perform(put(rotaDaNota).with(csrf()).session(sessao)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nota\":11,\"tipo\":\"PROVA\",\"unidade\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nota").exists());

        mvc.perform(delete(rotaDaNota).with(csrf()).session(sessao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avaliacoes.length()").value(0))
                .andExpect(jsonPath("$.media").value(0.0));

        mvc.perform(delete(rotaDaNota).with(csrf()).session(sessao))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Avaliação não encontrada."));
    }

    @Test
    void umAlunoNaoAlteraNemExcluiANotaDeOutro() throws Exception {
        MockHttpSession sessaoA = entrar(cadastrar(novoAluno()));
        MockHttpSession sessaoB = entrar(cadastrar(novoAluno()));

        long matriculaDeA = idDe(mvc.perform(post("/api/matriculas").with(csrf()).session(sessaoA)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"disciplinaId\":1}"))
                .andExpect(status().isCreated()).andReturn());
        MvcResult lancada = mvc.perform(post("/api/matriculas/" + matriculaDeA + "/avaliacoes").with(csrf()).session(sessaoA)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nota\":6,\"tipo\":\"PROVA\",\"unidade\":1}"))
                .andReturn();
        long notaDeA = json.readTree(lancada.getResponse().getContentAsString())
                .get("avaliacoes").get(0).get("id").asLong();

        // B tenta pela matrícula de A
        mvc.perform(put("/api/matriculas/" + matriculaDeA + "/avaliacoes/" + notaDeA).with(csrf()).session(sessaoB)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nota\":0,\"tipo\":\"PROVA\",\"unidade\":1}"))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/matriculas/" + matriculaDeA + "/avaliacoes/" + notaDeA).with(csrf()).session(sessaoB))
                .andExpect(status().isNotFound());

        // B tenta pela própria matrícula, apontando a nota de A
        long matriculaDeB = idDe(mvc.perform(post("/api/matriculas").with(csrf()).session(sessaoB)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"disciplinaId\":1}"))
                .andExpect(status().isCreated()).andReturn());
        mvc.perform(put("/api/matriculas/" + matriculaDeB + "/avaliacoes/" + notaDeA).with(csrf()).session(sessaoB)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nota\":0,\"tipo\":\"PROVA\",\"unidade\":1}"))
                .andExpect(status().isNotFound());

        mvc.perform(get("/api/matriculas").session(sessaoA))
                .andExpect(jsonPath("$[0].avaliacoes[0].nota").value(6.0));
    }

    @Test
    void catalogoTrazCursoAulasELimiteDeFaltas() throws Exception {
        MockHttpSession sessao = entrar(cadastrar(novoAluno()));
        mvc.perform(get("/api/disciplinas").session(sessao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[?(@.id == 2)].curso").value("Análise e Desenvolvimento de Sistemas"))
                .andExpect(jsonPath("$[?(@.id == 2)].totalDeAulas").value(48))
                .andExpect(jsonPath("$[?(@.id == 2)].limiteDeFaltas").value(12))
                .andExpect(jsonPath("$[?(@.id == 1)].totalDeAulas").value(72));
    }

    @Test
    void disciplinaInexistenteDevolve404() throws Exception {
        MockHttpSession sessao = entrar(cadastrar(novoAluno()));
        mvc.perform(post("/api/matriculas").with(csrf()).session(sessao)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"disciplinaId\":9999}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Disciplina não encontrada."));
    }

    // ---------------- Apoio ----------------

    private Dados novoAluno() {
        int n = SEQUENCIA.incrementAndGet();
        return new Dados("Aluno Teste " + n, cpfValido(n), "RA" + n, "aluno" + n + "@ucsal.edu.br");
    }

    private Dados cadastrar(Dados aluno) throws Exception {
        mvc.perform(post("/api/alunos").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(aluno.json()))
                .andExpect(status().isCreated());
        return aluno;
    }

    private MockHttpSession entrar(Dados aluno) throws Exception {
        MvcResult resultado = mvc.perform(post("/api/auth/login").with(csrf())
                        .param("identificador", aluno.ra).param("senha", SENHA))
                .andExpect(status().isOk()).andReturn();
        return (MockHttpSession) resultado.getRequest().getSession(false);
    }

    private long idDe(MvcResult resultado) throws Exception {
        JsonNode corpo = json.readTree(resultado.getResponse().getContentAsString());
        return corpo.get("id").asLong();
    }

    /** Gera um CPF com dígitos verificadores corretos a partir de 9 dígitos. */
    private static String cpfValido(int base) {
        String raiz = String.format("%09d", base);
        int[] d = raiz.chars().map(c -> c - '0').toArray();
        int soma = 0;
        for (int i = 0; i < 9; i++) soma += d[i] * (10 - i);
        int dv1 = 11 - soma % 11;
        if (dv1 >= 10) dv1 = 0;
        soma = 0;
        for (int i = 0; i < 9; i++) soma += d[i] * (11 - i);
        soma += dv1 * 2;
        int dv2 = 11 - soma % 11;
        if (dv2 >= 10) dv2 = 0;
        return raiz + dv1 + dv2;
    }

    private final class Dados {
        final String nome;
        final String cpf;
        final String ra;
        final String email;

        Dados(String nome, String cpf, String ra, String email) {
            this.nome = nome;
            this.cpf = cpf;
            this.ra = ra;
            this.email = email;
        }

        Dados comCpf(String outroCpf) { return new Dados(nome, outroCpf, ra, email); }
        Dados comRa(String outroRa) { return new Dados(nome, cpf, outroRa, email); }

        String cpfFormatado() {
            return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-" + cpf.substring(9);
        }

        String json() throws Exception {
            return SegurancaIntegracaoTest.this.json.writeValueAsString(
                    Map.of("nome", nome, "cpf", cpfFormatado(), "ra", ra, "email", email, "senha", SENHA));
        }
    }
}
