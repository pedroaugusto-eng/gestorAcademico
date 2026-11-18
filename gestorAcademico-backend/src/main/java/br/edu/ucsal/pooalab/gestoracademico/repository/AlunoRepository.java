package br.edu.ucsal.pooalab.gestoracademico.repository;

import br.edu.ucsal.pooalab.gestoracademico.domain.entity.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {

    Optional<Aluno> findByRa(String ra);

    Optional<Aluno> findByCpf(String cpf);

    boolean existsByEmail(String email);

    /** Usado para impedir que um RA seja igual a um CPF já cadastrado e vice-versa. */
    boolean existsByCpfOrRa(String cpf, String ra);
}
