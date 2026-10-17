package br.com.mecanica.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ServicoRequest(
        @NotBlank @Size(max = 120) String nome,
        @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal valor) {
    public ServicoRequest {
        nome = nome == null ? null : nome.strip();
    }
}
