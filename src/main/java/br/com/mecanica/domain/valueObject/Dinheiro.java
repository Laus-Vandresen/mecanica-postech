package br.com.mecanica.domain.valueObject;

import java.math.BigDecimal;

public class Dinheiro {
    private final BigDecimal valor;

    public Dinheiro(BigDecimal valor) {
        if (valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException();
        }

        this.valor = valor;
    }
}
