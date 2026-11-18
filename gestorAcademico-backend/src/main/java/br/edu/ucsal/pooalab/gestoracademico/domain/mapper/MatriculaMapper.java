package br.edu.ucsal.pooalab.gestoracademico.domain.mapper;

import br.edu.ucsal.pooalab.gestoracademico.domain.dtos.AvaliacaoResponse;
import br.edu.ucsal.pooalab.gestoracademico.domain.dtos.MatriculaResponse;
import br.edu.ucsal.pooalab.gestoracademico.domain.dtos.UnidadeResponse;
import br.edu.ucsal.pooalab.gestoracademico.domain.entity.Avaliacao;
import br.edu.ucsal.pooalab.gestoracademico.domain.entity.Matricula;
import br.edu.ucsal.pooalab.gestoracademico.domain.strategy.CalculadoraMediaStrategy;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

@Component
public class MatriculaMapper {

    /** Ordem na tela: 1ª unidade antes da 2ª; dentro da unidade, prova antes de atividade. */
    private static final Comparator<Avaliacao> ORDEM =
            Comparator.comparingInt(Avaliacao::getUnidade).thenComparing(Avaliacao::getTipo);

    private final DisciplinaMapper disciplinaMapper;
    private final CalculadoraMediaStrategy calculadora;

    public MatriculaMapper(DisciplinaMapper disciplinaMapper, CalculadoraMediaStrategy calculadora) {
        this.disciplinaMapper = disciplinaMapper;
        this.calculadora = calculadora;
    }

    public MatriculaResponse paraResponse(Matricula matricula) {
        List<AvaliacaoResponse> avaliacoes = matricula.getAvaliacoes().stream()
                .sorted(ORDEM)
                .map(a -> new AvaliacaoResponse(a.getId(), a.getNota(), a.getTipo(), a.getUnidade()))
                .toList();

        List<UnidadeResponse> unidades = IntStream.rangeClosed(1, Matricula.UNIDADES)
                .mapToObj(n -> new UnidadeResponse(n, arredondar(matricula.notaDaUnidade(n)),
                        Matricula.NOTA_MAXIMA_DA_UNIDADE, matricula.quantidadeDeAvaliacoesNaUnidade(n)))
                .toList();

        double unidade1 = matricula.notaDaUnidade(1);
        double unidade2 = matricula.notaDaUnidade(2);
        double media = arredondar(calculadora.calcularMedia(unidade1, unidade2));

        return new MatriculaResponse(
                matricula.getId(),
                disciplinaMapper.paraResponse(matricula.getDisciplina()),
                avaliacoes,
                unidades,
                media,
                calculadora.mediaMinima(),
                arredondar(calculadora.calcularNotaNecessaria(unidade1 + unidade2)),
                media >= calculadora.mediaMinima(),
                matricula.getFaltas(),
                matricula.faltasRegistradas(),
                matricula.getDisciplina().limiteDeFaltas(),
                matricula.reprovadoPorFalta());
    }

    /** Evita resultados como 7.499999999 na tela. */
    private static double arredondar(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
