package br.com.mecanica.dto;

import jakarta.validation.constraints.*;

public record VeiculoRequest(
        @NotBlank @Pattern(regexp = "[A-Z]{3}[0-9][A-Z0-9][0-9]{2}") String placa,
        @NotBlank @Size(max = 80) String modelo,
        @NotBlank @Size(max = 80) String marca,
        @NotNull @Min(1886) @Max(2100) Integer ano,
        @NotNull @Positive Long clienteId) {
    public VeiculoRequest {
        placa = placa == null ? null : placa.strip().toUpperCase(java.util.Locale.ROOT);
        modelo = modelo == null ? null : modelo.strip();
        marca = marca == null ? null : marca.strip();
    }
}
