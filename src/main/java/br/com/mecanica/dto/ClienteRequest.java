package br.com.mecanica.dto;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.br.CPF;

public record ClienteRequest(
        @NotBlank @Size(max = 120) String nome,
        @Size(max = 20) String celular,
        @Email @Size(max = 120) String email,
        @NotBlank @CPF @Pattern(regexp = "[0-9]{11}") String cpf) {
    public ClienteRequest {
        nome = nome == null ? null : nome.strip();
        celular = celular == null ? null : celular.strip();
        email = email == null ? null : email.strip();
        cpf = cpf == null ? null : cpf.strip();
    }
}
