package br.com.mecanica.domain.valueObject;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ObjetosDeValorCriticosTest {

    @Test
    void dinheiroAceitaZeroEValoresPositivosERejeitaNegativo() {
        assertDoesNotThrow(() -> new Dinheiro(BigDecimal.ZERO));
        assertDoesNotThrow(() -> new Dinheiro(new BigDecimal("99.90")));
        assertThrows(IllegalArgumentException.class, () -> new Dinheiro(new BigDecimal("-0.01")));
    }

    @Test
    void estoqueAceitaQuantidadeZeroERejeitaQuantidadeNegativaOuIdNulo() {
        var produtoId = UUID.randomUUID();
        var estoque = new Estoque(produtoId, 0);
        assertEquals(produtoId, estoque.getProdutoId());
        assertEquals(0, estoque.getQuantidade());
        assertThrows(IllegalArgumentException.class, () -> new Estoque(produtoId, -1));
        assertThrows(NullPointerException.class, () -> new Estoque(null, 1));
    }

    @Test
    void operacaoEstoqueAplicaSinalCorreto() {
        assertEquals(7, OperacaoEstoque.ENTRADA.aplicarSinal(7));
        assertEquals(-7, OperacaoEstoque.SAIDA.aplicarSinal(7));
    }

    @Test
    void tempoMedioFinalizacaoValidaQuantidadeEDuracao() {
        var tempo = new TempoMedioFinalizacao(0, Duration.ZERO);
        assertEquals(0, tempo.getQuantidadeOrdens());
        assertEquals(Duration.ZERO, tempo.getTempoMedio());
        assertThrows(IllegalArgumentException.class,
                () -> new TempoMedioFinalizacao(-1, Duration.ZERO));
        assertThrows(IllegalArgumentException.class,
                () -> new TempoMedioFinalizacao(1, Duration.ofSeconds(-1)));
        assertThrows(NullPointerException.class,
                () -> new TempoMedioFinalizacao(1, null));
    }
}
