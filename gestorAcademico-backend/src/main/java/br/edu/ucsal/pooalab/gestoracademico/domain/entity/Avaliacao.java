package br.edu.ucsal.pooalab.gestoracademico.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_avaliacoes")
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double nota;

    @Enumerated(EnumType.STRING)
    private AvaliacaoTipo tipo;

    /** Unidade do semestre a que a avaliação pertence: 1 ou 2. */
    @Column(nullable = false)
    private int unidade;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "matricula_id", nullable = false)
    private Matricula matricula;

    protected Avaliacao() {
    }

    /** Só a Matricula cria avaliações, depois de validar as regras da unidade. */
    Avaliacao(double nota, AvaliacaoTipo tipo, int unidade, Matricula matricula) {
        this.nota = nota;
        this.tipo = tipo;
        this.unidade = unidade;
        this.matricula = matricula;
    }

    /** Corrige uma nota lançada errada. Só a Matricula chama, depois de validar. */
    void alterar(double novaNota, AvaliacaoTipo novoTipo, int novaUnidade) {
        this.nota = novaNota;
        this.tipo = novoTipo;
        this.unidade = novaUnidade;
    }

    public Long getId() { return id; }
    public double getNota() { return nota; }
    public AvaliacaoTipo getTipo() { return tipo; }
    public int getUnidade() { return unidade; }
    public Matricula getMatricula() { return matricula; }
}
