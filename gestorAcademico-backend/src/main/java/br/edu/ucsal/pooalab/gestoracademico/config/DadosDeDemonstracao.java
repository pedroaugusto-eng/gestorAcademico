package br.edu.ucsal.pooalab.gestoracademico.config;

import br.edu.ucsal.pooalab.gestoracademico.domain.Identificadores;
import br.edu.ucsal.pooalab.gestoracademico.domain.dtos.AlunoRequest;
import br.edu.ucsal.pooalab.gestoracademico.repository.AlunoRepository;
import br.edu.ucsal.pooalab.gestoracademico.service.AlunoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Só roda com o perfil "demo" (--spring.profiles.active=demo).
 * Cria um aluno de teste para demonstrar o sistema sem precisar cadastrar ninguém.
 * Os dados ficam em application-demo.properties. Nunca ativar este perfil em produção.
 */
@Component
@Profile("demo")
public class DadosDeDemonstracao implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DadosDeDemonstracao.class);

    private final AlunoService alunoService;
    private final AlunoRepository alunoRepository;
    private final AlunoRequest alunoDeTeste;

    public DadosDeDemonstracao(AlunoService alunoService,
                               AlunoRepository alunoRepository,
                               @Value("${demo.aluno.nome}") String nome,
                               @Value("${demo.aluno.cpf}") String cpf,
                               @Value("${demo.aluno.ra}") String ra,
                               @Value("${demo.aluno.email}") String email,
                               @Value("${demo.aluno.senha}") String senha) {
        this.alunoService = alunoService;
        this.alunoRepository = alunoRepository;
        this.alunoDeTeste = new AlunoRequest(nome, cpf, ra, email, senha);
    }

    @Override
    public void run(ApplicationArguments args) {
        if (alunoRepository.findByRa(Identificadores.ra(alunoDeTeste.ra())).isPresent()) {
            return;
        }
        alunoService.cadastrar(alunoDeTeste);
        log.warn("Perfil demo ativo: aluno de teste criado (RA {}). Não use este perfil em produção.",
                Identificadores.ra(alunoDeTeste.ra()));
    }
}
