package br.edu.ucsal.pooalab.gestoracademico.domain.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

import java.nio.charset.StandardCharsets;

public record AlunoRequest(
        @NotBlank(message = "informe o nome")
        @Size(max = 120, message = "o nome pode ter até 120 caracteres")
        String nome,

        @NotBlank(message = "informe o CPF")
        @CPF(message = "CPF inválido")
        String cpf,

        @NotBlank(message = "informe o RA")
        @Pattern(regexp = "\\s*[A-Za-z0-9]{3,20}\\s*", message = "o RA deve ter de 3 a 20 letras ou números")
        String ra,

        @NotBlank(message = "informe o e-mail")
        @Email(message = "e-mail inválido")
        @Size(max = 120, message = "o e-mail pode ter até 120 caracteres")
        String email,

        @NotBlank(message = "informe a senha")
        @Size(min = 8, max = 72, message = "a senha deve ter de 8 a 72 caracteres")
        String senha
) {

    /** O BCrypt só considera os primeiros 72 bytes; acentos ocupam mais de 1 byte. */
    @JsonIgnore
    @AssertTrue(message = "a senha é longa demais")
    public boolean isSenhaDentroDoLimiteDoBcrypt() {
        return senha == null || senha.getBytes(StandardCharsets.UTF_8).length <= 72;
    }

    /** Evita que a senha apareça em logs se alguém imprimir o objeto. */
    @Override
    public String toString() {
        return "AlunoRequest[nome=" + nome + ", ra=" + ra + ", email=" + email + "]";
    }
}
