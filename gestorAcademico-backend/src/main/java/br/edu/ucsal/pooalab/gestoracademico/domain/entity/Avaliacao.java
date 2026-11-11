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

    @ManyToOne
    @JoinColumn(name = "matricula_id", nullable = false)
    private Matricula matricula;

    public Avaliacao() {
    }

    public Avaliacao(double nota, AvaliacaoTipo tipo, Matricula matricula) {
        this.nota = nota;
        this.tipo = tipo;
        this.matricula = matricula;
    }

    public Long getId() {
        return id;
    }

    public double getNota() {
        return nota;
    }

    public AvaliacaoTipo getTipo() {
        return tipo;
    }

    public void setMatricula(Matricula matricula) {
        this.matricula = matricula;
    }
}
