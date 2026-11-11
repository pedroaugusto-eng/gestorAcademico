package br.edu.ucsal.pooalab.gestoracademico.domain.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_disciplinas")
public class Disciplina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String codigo;
    private int cargaHoraria;
    private int semestreRecomendado;

    @ManyToOne
    @JoinColumn(name = "curso_id")
    private Curso curso;

    @OneToMany(mappedBy = "disciplina", cascade = CascadeType.ALL)
    private List<Matricula> matriculas = new ArrayList<>();

    public Disciplina(){}

    public Disciplina(String nome, String codigo, int cargaHoraria, Curso curso, List<Matricula> matriculas, int semestreRecomendado) {
        this.nome = nome;
        this.codigo = codigo;
        this.cargaHoraria = cargaHoraria;
        this.curso = curso;
        this.matriculas = matriculas;
        this.semestreRecomendado = semestreRecomendado;
    }

    public Long getId() {
        return id;
    }
    public String getNome() {
        return nome;
    }
    public String getCodigo() {
        return codigo;
    }
    public int getCargaHoraria() {
        return cargaHoraria;
    }
    public Curso getCurso() {
        return curso;
    }
    public List<Matricula> getMatriculas() {
        return matriculas;
    }
    public int getSemestreRecomendado() {
        return semestreRecomendado;
    }
}
