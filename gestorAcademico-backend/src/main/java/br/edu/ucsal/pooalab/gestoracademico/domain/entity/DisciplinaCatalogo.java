package br.edu.ucsal.pooalab.gestoracademico.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_disciplina_catalogo")
public class DisciplinaCatalogo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private Integer cargaHoraria;

    @Column(nullable = false)
    private String curso;

    @Column(nullable = false)
    private Integer semestreRecomendado;

    public DisciplinaCatalogo() {
    }

    public DisciplinaCatalogo(String codigo, String nome, Integer cargaHoraria, String curso, Integer semestreRecomendado) {
        this.codigo = codigo;
        this.nome = nome;
        this.cargaHoraria = cargaHoraria;
        this.curso = curso;
        this.semestreRecomendado = semestreRecomendado;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Integer getCargaHoraria() {
        return cargaHoraria;
    }

    public void setCargaHoraria(Integer cargaHoraria) {
        this.cargaHoraria = cargaHoraria;
    }

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    public Integer getSemestreRecomendado() {
        return semestreRecomendado;
    }

    public void setSemestreRecomendado(Integer semestreRecomendado) {
        this.semestreRecomendado = semestreRecomendado;
    }
}
