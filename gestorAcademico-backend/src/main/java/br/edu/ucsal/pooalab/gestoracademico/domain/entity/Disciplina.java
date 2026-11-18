package br.edu.ucsal.pooalab.gestoracademico.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_disciplinas")
public class Disciplina {

    /** Cada aula tem 75 minutos. */
    private static final int MINUTOS_POR_AULA = 75;

    /** O aluno pode faltar a até 25% da carga horária. */
    private static final double PERCENTUAL_MAXIMO_DE_FALTAS = 0.25;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String codigo;
    private int cargaHoraria;
    private int semestreRecomendado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_id")
    private Curso curso;

    protected Disciplina() {
    }

    public Disciplina(String nome, String codigo, int cargaHoraria, Curso curso, int semestreRecomendado) {
        this.nome = nome;
        this.codigo = codigo;
        this.cargaHoraria = cargaHoraria;
        this.curso = curso;
        this.semestreRecomendado = semestreRecomendado;
    }

    /**
     * Quantidade máxima de aulas que o aluno pode faltar sem ser reprovado.
     * Ex.: 60h = 3600 min; 25% = 900 min; 900 / 75 = 12 aulas.
     */
    public int limiteDeFaltas() {
        double minutosTotais = cargaHoraria * 60.0;
        return (int) Math.floor((PERCENTUAL_MAXIMO_DE_FALTAS * minutosTotais) / MINUTOS_POR_AULA);
    }

    /** Quantidade de aulas de 75 minutos no semestre. Ex.: 60h = 3600 min / 75 = 48 aulas. */
    public int totalDeAulas() {
        return (int) Math.floor(cargaHoraria * 60.0 / MINUTOS_POR_AULA);
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getCodigo() { return codigo; }
    public int getCargaHoraria() { return cargaHoraria; }
    public int getSemestreRecomendado() { return semestreRecomendado; }
    public Curso getCurso() { return curso; }
}
