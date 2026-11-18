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

    @Column(nullable = false, length = 120)
    private String nome;

    /** Guardado só com dígitos (11), para o login aceitar o CPF com ou sem pontuação. */
    @Column(unique = true, nullable = false, length = 11)
    private String cpf;

    /** Guardado sem espaços e em maiúsculas. */
    @Column(unique = true, nullable = false, length = 20)
    private String ra;

    @Column(unique = true, nullable = false, length = 120)
    private String email;

    /** Hash BCrypt da senha. A senha em texto puro nunca é gravada. */
    @Column(name = "senha", nullable = false, length = 72)
    private String senhaHash;

    @OneToMany(mappedBy = "aluno", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Matricula> matriculas = new ArrayList<>();

    protected Aluno() {
    }

    public Aluno(String nome, String cpf, String ra, String email, String senhaHash) {
        this.nome = nome;
        this.cpf = cpf;
        this.ra = ra;
        this.email = email;
        this.senhaHash = senhaHash;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getCpf() { return cpf; }
    public String getRa() { return ra; }
    public String getEmail() { return email; }
    public String getSenhaHash() { return senhaHash; }
    public List<Matricula> getMatriculas() { return matriculas; }
}
