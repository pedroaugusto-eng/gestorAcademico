package br.edu.ucsal.pooalab.gestoracademico.repository;

import br.edu.ucsal.pooalab.gestoracademico.domain.entity.Disciplina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DisciplinaRepository extends JpaRepository<Disciplina, Long> {
}
