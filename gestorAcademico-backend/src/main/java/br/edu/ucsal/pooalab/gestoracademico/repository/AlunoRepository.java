package br.edu.ucsal.pooalab.gestoracademico.repository;

import br.edu.ucsal.pooalab.gestoracademico.domain.entity.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlunoRepository extends JpaRepository<Aluno, Long> {
}
