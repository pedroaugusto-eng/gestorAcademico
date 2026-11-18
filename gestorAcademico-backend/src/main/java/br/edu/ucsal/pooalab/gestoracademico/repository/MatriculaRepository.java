package br.edu.ucsal.pooalab.gestoracademico.repository;

import br.edu.ucsal.pooalab.gestoracademico.domain.entity.Matricula;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MatriculaRepository extends JpaRepository<Matricula, Long> {

    /** Carrega disciplina e avaliações numa consulta só (evita o problema N+1). */
    @EntityGraph(attributePaths = {"disciplina", "disciplina.curso", "avaliacoes"})
    List<Matricula> findByAlunoIdOrderByIdAsc(Long alunoId);

    /** Busca a matrícula só se ela for do aluno logado: um aluno não enxerga a do outro. */
    Optional<Matricula> findByIdAndAlunoId(Long id, Long alunoId);

    boolean existsByAlunoIdAndDisciplinaId(Long alunoId, Long disciplinaId);
}
