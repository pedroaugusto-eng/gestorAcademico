package br.edu.ucsal.pooalab.gestoracademico.security;

import br.edu.ucsal.pooalab.gestoracademico.domain.entity.Aluno;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Padrão Adapter: adapta o Aluno ao contrato UserDetails do Spring Security.
 * Fica guardado na sessão, por isso carrega só o necessário (sem a entidade JPA).
 */
public class AlunoAutenticado implements UserDetails, CredentialsContainer {

    private final Long id;
    private final String nome;
    private final String ra;
    private String senhaHash;

    public AlunoAutenticado(Aluno aluno) {
        this.id = aluno.getId();
        this.nome = aluno.getNome();
        this.ra = aluno.getRa();
        this.senhaHash = aluno.getSenhaHash();
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }

    @Override
    public String getUsername() { return ra; }

    @Override
    public String getPassword() { return senhaHash; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_ALUNO"));
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }

    /** Depois do login o Spring chama este método: o hash não fica guardado na sessão. */
    @Override
    public void eraseCredentials() {
        this.senhaHash = null;
    }
}
