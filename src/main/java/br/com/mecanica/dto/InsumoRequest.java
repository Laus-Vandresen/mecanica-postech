package br.com.mecanica.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record InsumoRequest(
        @NotBlank @Size(max = 120) String nome,
        @Size(max = 255) String descricao,
        @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal valor,
        @NotNull @PositiveOrZero Integer quantidadeEstoque,
        @NotBlank @Size(max = 20) String unidadeMedida) {
    public InsumoRequest {
        nome = nome == null ? null : nome.strip();
        descricao = descricao == null ? null : descricao.strip();
        unidadeMedida = unidadeMedida == null ? null : unidadeMedida.strip();
    }
}
