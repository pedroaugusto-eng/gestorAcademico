package br.edu.ucsal.pooalab.gestoracademico.service;

import br.edu.ucsal.pooalab.gestoracademico.domain.entity.DisciplinaCatalogo;
import br.edu.ucsal.pooalab.gestoracademico.domain.entity.DisciplinaCursada;
import br.edu.ucsal.pooalab.gestoracademico.domain.strategy.CalculadoraMediaStrategy;
import br.edu.ucsal.pooalab.gestoracademico.repository.DisciplinaCatalogoRepository;
import br.edu.ucsal.pooalab.gestoracademico.repository.DisciplinaCursadaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DisciplinaService {

    private final DisciplinaCatalogoRepository catalogoRepository;
    private final DisciplinaCursadaRepository cursadaRepository;
    private final CalculadoraMediaStrategy calculadoraStrategy;

    public DisciplinaService(DisciplinaCatalogoRepository catalogoRepository,
                             DisciplinaCursadaRepository cursadaRepository,
                             CalculadoraMediaStrategy calculadoraStrategy) {
        this.catalogoRepository = catalogoRepository;
        this.cursadaRepository = cursadaRepository;
        this.calculadoraStrategy = calculadoraStrategy;
    }

    // 1. Para o frontend (React) listar as disciplinas na hora de montar a grade
    @Transactional(readOnly = true)
    public List<DisciplinaCatalogo> listarCatalogoPorCurso(String curso) {
        return catalogoRepository.findByCurso(curso);
    }

    // 2. O aluno seleciona uma disciplina da matriz e adiciona ao seu semestre
    @Transactional
    public DisciplinaCursada adicionarDisciplinaAoSemestre(Long catalogoId) {
        DisciplinaCatalogo matriz = catalogoRepository.findById(catalogoId)
                .orElseThrow(() -> new RuntimeException("Disciplina não encontrada no catálogo"));

        DisciplinaCursada novaDisciplina = new DisciplinaCursada(matriz);
        return cursadaRepository.save(novaDisciplina);
    }

    // 3. Atualiza as faltas e as notas parciais (AV1, trabalhos, etc.)
    @Transactional
    public DisciplinaCursada atualizarProgresso(Long cursadaId, Double notaAtual, Integer faltasAtuais) {
        DisciplinaCursada cursada = cursadaRepository.findById(cursadaId)
                .orElseThrow(() -> new RuntimeException("Disciplina cursada não encontrada"));

        cursada.setNotaAtual(notaAtual);
        cursada.setFaltasAtuais(faltasAtuais);

        return cursadaRepository.save(cursada);
    }

    // 4. Calcula a nota que falta aplicando a nossa regra de negócio isolada (Strategy)
    public double calcularNotaNecessariaParaAprovacao(Long cursadaId) {
        DisciplinaCursada cursada = cursadaRepository.findById(cursadaId)
                .orElseThrow(() -> new RuntimeException("Disciplina cursada não encontrada"));

        return calculadoraStrategy.calcularNotaNecessaria(cursada.getNotaAtual());
    }

    // 5. Calcula o limite de faltas com base na carga horária da matriz
    public int calcularFaltasRestantes(Long cursadaId) {
        DisciplinaCursada cursada = cursadaRepository.findById(cursadaId)
                .orElseThrow(() -> new RuntimeException("Disciplina cursada não encontrada"));

        // Considerando a regra padrão de 25% de faltas permitidas
        int maxFaltas = (int) (cursada.getCargaHorariaMatriz() * 0.25);
        int faltasRestantes = maxFaltas - cursada.getFaltasAtuais();

        // Evita devolver números negativos caso o aluno já esteja reprovado por falta
        return Math.max(faltasRestantes, 0);
    }
}