package br.edu.ucsal.pooalab.gestoracademico.domain.dtos;

import br.edu.ucsal.pooalab.gestoracademico.domain.entity.AvaliacaoTipo;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AvaliacaoRequest(
        @NotNull(message = "informe a nota")
        @DecimalMin(value = "0.0", message = "a nota mínima é 0")
        @DecimalMax(value = "10.0", message = "a nota máxima é 10")
        Double nota,

        @NotNull(message = "informe o tipo da avaliação")
        AvaliacaoTipo tipo,

        @NotNull(message = "informe a unidade")
        @Min(value = 1, message = "a unidade deve ser 1 ou 2")
        @Max(value = 2, message = "a unidade deve ser 1 ou 2")
        Integer unidade
) {
}
