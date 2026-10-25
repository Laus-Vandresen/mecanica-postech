package br.com.mecanica.domain.valueObject;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

@Getter
@EqualsAndHashCode
public class Estoque {
    private final UUID produtoId;
    private final int quantidade;

    public Estoque(UUID produtoId, int quantidade) {
        if (quantidade < 0) {
            throw new IllegalArgumentException();
        }

        this.produtoId = Objects.requireNonNull(produtoId);
        this.quantidade = quantidade;
    }
}
