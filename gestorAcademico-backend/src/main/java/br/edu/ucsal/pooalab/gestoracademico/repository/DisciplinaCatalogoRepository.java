package br.edu.ucsal.pooalab.gestoracademico.repository;

import br.edu.ucsal.pooalab.gestoracademico.domain.entity.DisciplinaCatalogo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DisciplinaCatalogoRepository extends JpaRepository<DisciplinaCatalogo, Long> {
    List<DisciplinaCatalogo> findByCurso(String curso);
}