package br.edu.ucsal.pooalab.gestoracademico.repository;

import br.edu.ucsal.pooalab.gestoracademico.domain.entity.Disciplina;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DisciplinaRepository extends JpaRepository<Disciplina, Long> {

    /** Catálogo: já traz o curso junto, numa consulta só. */
    @EntityGraph(attributePaths = "curso")
    List<Disciplina> findAllByOrderBySemestreRecomendadoAscNomeAsc();
}
