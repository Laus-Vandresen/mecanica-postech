package br.com.mecanica.domain.valueObject;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.Duration;
import java.util.Objects;

@Getter
@EqualsAndHashCode
public class TempoMedioFinalizacao {
    private final int quantidadeOrdens;
    private final Duration tempoMedio;

    public TempoMedioFinalizacao(int quantidadeOrdens, Duration tempoMedio) {
        if (quantidadeOrdens < 0 || Objects.requireNonNull(tempoMedio).isNegative()) {
            throw new IllegalArgumentException();
        }

        this.quantidadeOrdens = quantidadeOrdens;
        this.tempoMedio = tempoMedio;
    }
}
