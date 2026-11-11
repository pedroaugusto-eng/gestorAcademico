package br.edu.ucsal.pooalab.gestoracademico.domain.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_cursos")
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private String codigo;

    @OneToMany(mappedBy = "curso")
    private List<Disciplina> disciplinas = new ArrayList<>();

    public Long getId() {
        return id;
    }
    public String getNome() {
        return nome;
    }
    public String getCodigo() {
        return codigo;
    }
    public List<Disciplina> getDisciplinas() {
        return disciplinas;
    }
}
