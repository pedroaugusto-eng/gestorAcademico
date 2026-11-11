package br.edu.ucsal.pooalab.gestoracademico.domain.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_alunos")
public class Aluno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;

    @Column(unique = true)
    private String cpf;

    @Column(unique = true)
    private String ra;

    @Column(unique = true)
    private String email;
    private String senha;

    @OneToMany(mappedBy = "aluno", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Matricula> matriculas = new ArrayList<>();

    public Aluno(){}

    public Aluno(String nome, String cpf, String ra, String email, String senha) {
        this.nome = nome;
        this.cpf = cpf;
        this.ra = ra;
        this.email = email;
        this.senha = senha;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getCpf() { return cpf; }
    public String getRa() { return ra; }
    public String getEmail() { return email; }
    public String getSenha() { return senha; }
    public List<Matricula> getMatriculas() { return matriculas; }
}
