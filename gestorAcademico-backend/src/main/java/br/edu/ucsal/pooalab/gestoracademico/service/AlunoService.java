package br.edu.ucsal.pooalab.gestoracademico.service;

import br.edu.ucsal.pooalab.gestoracademico.domain.Identificadores;
import br.edu.ucsal.pooalab.gestoracademico.domain.dtos.AlunoRequest;
import br.edu.ucsal.pooalab.gestoracademico.domain.dtos.AlunoResponse;
import br.edu.ucsal.pooalab.gestoracademico.domain.entity.Aluno;
import br.edu.ucsal.pooalab.gestoracademico.domain.mapper.AlunoMapper;
import br.edu.ucsal.pooalab.gestoracademico.exception.ConflitoException;
import br.edu.ucsal.pooalab.gestoracademico.exception.RecursoNaoEncontradoException;
import br.edu.ucsal.pooalab.gestoracademico.repository.AlunoRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlunoService {

    private final AlunoRepository alunoRepository;
    private final AlunoMapper alunoMapper;
    private final PasswordEncoder passwordEncoder;

    public AlunoService(AlunoRepository alunoRepository, AlunoMapper alunoMapper, PasswordEncoder passwordEncoder) {
        this.alunoRepository = alunoRepository;
        this.alunoMapper = alunoMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AlunoResponse cadastrar(AlunoRequest request) {
        String cpf = Identificadores.cpf(request.cpf());
        String ra = Identificadores.ra(request.ra());
        String email = Identificadores.email(request.email());

        garantirQueNaoExiste(cpf, ra, email);

        Aluno aluno = alunoMapper.paraEntidade(request, passwordEncoder.encode(request.senha()));
        return alunoMapper.paraResponse(alunoRepository.save(aluno));
    }

    @Transactional(readOnly = true)
    public AlunoResponse buscarPorId(Long id) {
        return alunoRepository.findById(id)
                .map(alunoMapper::paraResponse)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aluno não encontrado."));
    }

    /**
     * O login aceita RA ou CPF no mesmo campo. Para nunca haver dúvida de quem é quem,
     * o RA de um aluno não pode ser igual ao CPF de outro (nem ao próprio CPF).
     */
    private void garantirQueNaoExiste(String cpf, String ra, String email) {
        boolean duplicado = cpf.equals(ra)
                || alunoRepository.existsByCpfOrRa(cpf, ra)   // CPF ou RA já usados no mesmo campo
                || alunoRepository.existsByCpfOrRa(ra, cpf)   // RA igual a um CPF, ou CPF igual a um RA
                || alunoRepository.existsByEmail(email);
        if (duplicado) {
            throw new ConflitoException("Já existe um aluno cadastrado com este CPF, RA ou e-mail.");
        }
    }
}
