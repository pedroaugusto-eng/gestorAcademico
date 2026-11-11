    package br.edu.ucsal.pooalab.gestoracademico.service;

    import br.edu.ucsal.pooalab.gestoracademico.domain.dtos.AvaliacaoRequest;
    import br.edu.ucsal.pooalab.gestoracademico.domain.entity.Aluno;
    import br.edu.ucsal.pooalab.gestoracademico.domain.entity.Avaliacao;
    import br.edu.ucsal.pooalab.gestoracademico.domain.entity.Matricula;
    import br.edu.ucsal.pooalab.gestoracademico.repository.AvaliacaoRepository;
    import org.springframework.stereotype.Service;

    @Service
    public class AvaliacaoService {

        private final AvaliacaoRepository avaliacaoRepository;

        public AvaliacaoService(AvaliacaoRepository avaliacaoRepository) {
            this.avaliacaoRepository = avaliacaoRepository;
        }

        public Avaliacao criarAvaliacao(AvaliacaoRequest request, Matricula matricula) {
            return new Avaliacao(request.nota(), request.tipo(), matricula);
        }
    }
