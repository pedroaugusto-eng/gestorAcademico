package br.edu.ucsal.pooalab.gestoracademico.domain.entity;

import br.edu.ucsal.pooalab.gestoracademico.domain.dtos.AlunoRequest;
import jakarta.persistence.*;

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

    public Aluno(){}

    public Aluno(AlunoRequest request) {
        this.nome = request.nome();
        this.cpf = request.cpf();
        this.ra = request.ra();
        this.email = request.email();
        this.senha = request.senha();
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getCpf() { return cpf; }
    public String getRa() { return ra; }
    public String getEmail() { return email; }
    public String getSenha() { return senha; }
}
