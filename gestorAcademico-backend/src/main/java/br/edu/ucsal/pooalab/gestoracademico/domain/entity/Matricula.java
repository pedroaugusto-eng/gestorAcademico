package br.edu.ucsal.pooalab.gestoracademico.domain.entity;

import br.edu.ucsal.pooalab.gestoracademico.exception.RecursoNaoEncontradoException;
import br.edu.ucsal.pooalab.gestoracademico.exception.RegraDeNegocioException;
import jakarta.persistence.*;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Entity
@Table(name = "tb_matriculas",
        uniqueConstraints = @UniqueConstraint(columnNames = {"aluno_id", "disciplina_id"}))
public class Matricula {

    /** Cada falta registrada corresponde a um encontro de 2 aulas. */
    public static final int AULAS_POR_FALTA = 2;

    /** O semestre tem 2 unidades. */
    public static final int UNIDADES = 2;

    /** Cada unidade vale no máximo 10 pontos (20 no semestre). */
    public static final double NOTA_MAXIMA_DA_UNIDADE = 10.0;

    private static final double TOLERANCIA = 1e-9;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @ManyToOne(optional = false)
    @JoinColumn(name = "disciplina_id", nullable = false)
    private Disciplina disciplina;

    /** Total de aulas faltadas (cada falta registrada soma 2). */
    private int faltas = 0;

    @OneToMany(mappedBy = "matricula", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Avaliacao> avaliacoes = new ArrayList<>();

    protected Matricula() {
    }

    public Matricula(Aluno aluno, Disciplina disciplina) {
        this.aluno = aluno;
        this.disciplina = disciplina;
    }

    /* ---------------- Avaliações ---------------- */

    public void adicionarAvaliacao(double nota, AvaliacaoTipo tipo, int unidade) {
        validarLancamento(null, nota, tipo, unidade);
        avaliacoes.add(new Avaliacao(nota, tipo, unidade, this));
    }

    public void alterarAvaliacao(Long avaliacaoId, double nota, AvaliacaoTipo tipo, int unidade) {
        Avaliacao avaliacao = buscarAvaliacao(avaliacaoId);
        validarLancamento(avaliacao, nota, tipo, unidade);
        avaliacao.alterar(nota, tipo, unidade);
    }

    public void removerAvaliacao(Long avaliacaoId) {
        avaliacoes.remove(buscarAvaliacao(avaliacaoId));
    }

    /** Nota da unidade = soma do que foi lançado nela (ex.: prova 7 + atividade 3, ou uma prova de 10). */
    public double notaDaUnidade(int unidade) {
        return avaliacoes.stream()
                .filter(a -> a.getUnidade() == unidade)
                .mapToDouble(Avaliacao::getNota)
                .sum();
    }

    public long quantidadeDeAvaliacoesNaUnidade(int unidade) {
        return avaliacoes.stream().filter(a -> a.getUnidade() == unidade).count();
    }

    /**
     * Regras de cada unidade:
     * - só existem as unidades 1 e 2;
     * - no máximo uma prova e uma atividade por unidade (para corrigir, usa-se Alterar);
     * - a soma da unidade não passa de 10 pontos.
     * "ignorada" é a própria avaliação quando ela está sendo alterada.
     */
    private void validarLancamento(Avaliacao ignorada, double nota, AvaliacaoTipo tipo, int unidade) {
        if (unidade < 1 || unidade > UNIDADES) {
            throw new RegraDeNegocioException("A unidade deve ser 1 ou 2.");
        }

        boolean repetida = avaliacoes.stream()
                .anyMatch(a -> a != ignorada && a.getUnidade() == unidade && a.getTipo() == tipo);
        if (repetida) {
            String qual = tipo == AvaliacaoTipo.PROVA ? "uma prova" : "uma atividade";
            throw new RegraDeNegocioException(
                    "Já existe " + qual + " na " + unidade + "ª unidade. Para corrigir a nota, use Alterar.");
        }

        double somaDasOutras = avaliacoes.stream()
                .filter(a -> a != ignorada && a.getUnidade() == unidade)
                .mapToDouble(Avaliacao::getNota)
                .sum();
        if (somaDasOutras + nota > NOTA_MAXIMA_DA_UNIDADE + TOLERANCIA) {
            double disponivel = Math.max(0, NOTA_MAXIMA_DA_UNIDADE - somaDasOutras);
            throw new RegraDeNegocioException("Cada unidade vale no máximo 10 pontos. A " + unidade
                    + "ª unidade já tem " + formatar(somaDasOutras) + ", então esta nota pode ser no máximo "
                    + formatar(disponivel) + ".");
        }
    }

    /** Só encontra avaliações desta matrícula: uma nota de outra matrícula dá "não encontrada". */
    private Avaliacao buscarAvaliacao(Long avaliacaoId) {
        return avaliacoes.stream()
                .filter(a -> a.getId() != null && a.getId().equals(avaliacaoId))
                .findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Avaliação não encontrada."));
    }

    /* ---------------- Faltas ---------------- */

    /**
     * Registra uma falta (2 aulas). Pode passar do limite: a falta aconteceu e precisa ficar
     * registrada. Quem interpreta é reprovadoPorFalta().
     */
    public void registrarFalta() {
        faltas += AULAS_POR_FALTA;
    }

    /** Desfaz a última falta registrada por engano. */
    public void desfazerUltimaFalta() {
        if (faltas < AULAS_POR_FALTA) {
            throw new RegraDeNegocioException("Não há falta registrada para desfazer.");
        }
        faltas -= AULAS_POR_FALTA;
    }

    public int faltasRegistradas() {
        return faltas / AULAS_POR_FALTA;
    }

    public boolean reprovadoPorFalta() {
        return faltas > disciplina.limiteDeFaltas();
    }

    /* ---------------- Apoio ---------------- */

    private static String formatar(double valor) {
        return new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(new Locale("pt", "BR"))).format(valor);
    }

    public Long getId() { return id; }
    public Aluno getAluno() { return aluno; }
    public Disciplina getDisciplina() { return disciplina; }
    public List<Avaliacao> getAvaliacoes() { return avaliacoes; }
    public int getFaltas() { return faltas; }
}
