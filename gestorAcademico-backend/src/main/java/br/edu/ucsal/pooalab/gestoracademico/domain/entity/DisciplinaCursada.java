package br.edu.ucsal.pooalab.gestoracademico.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_disciplina_cursada")
public class DisciplinaCursada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "catalogo_id")
    private DisciplinaCatalogo disciplinaMatriz;

    @Column(nullable = false)
    private Integer faltasAtuais = 0;

    @Column(nullable = false)
    private Double notaAtual = 0.0;

    public DisciplinaCursada() {
    }

    // Construtor para facilitar a vinculação no Service
    public DisciplinaCursada(DisciplinaCatalogo matriz) {
        this.disciplinaMatriz = matriz;
    }

    // Método transiente que busca a carga horária na matriz
    @Transient
    public Integer getCargaHorariaMatriz() {
        if (this.disciplinaMatriz != null) {
            return this.disciplinaMatriz.getCargaHoraria();
        }
        return 0;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DisciplinaCatalogo getDisciplinaMatriz() {
        return disciplinaMatriz;
    }

    public void setDisciplinaMatriz(DisciplinaCatalogo disciplinaMatriz) {
        this.disciplinaMatriz = disciplinaMatriz;
    }

    public Integer getFaltasAtuais() {
        return faltasAtuais;
    }

    public void setFaltasAtuais(Integer faltasAtuais) {
        this.faltasAtuais = faltasAtuais;
    }

    public Double getNotaAtual() {
        return notaAtual;
    }

    public void setNotaAtual(Double notaAtual) {
        this.notaAtual = notaAtual;
    }
}