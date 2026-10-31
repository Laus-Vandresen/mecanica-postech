package br.com.mecanica.domain.valueObject;

import br.com.mecanica.domain.exception.CpfInvalidoException;

public record Cpf(String valor) {

    public Cpf {
        if (valor == null || !valor.matches("[0-9]{11}") || valor.chars().distinct().count() == 1) {
            throw new CpfInvalidoException();
        }
        for (int n = 9; n <= 10; n++) {
            int soma = 0;
            for (int i = 0; i < n; i++) {
                soma += (valor.charAt(i) - '0') * (n + 1 - i);
            }
            int digito = (soma * 10) % 11;
            if ((digito == 10 ? 0 : digito) != valor.charAt(n) - '0') {
                throw new CpfInvalidoException();
            }
        }
    }
}
