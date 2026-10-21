package br.com.mecanica.domain.service;

import br.com.mecanica.domain.entity.OrdemServico;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrdemServicoDomainServiceTest {
    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 1, 1, 8, 0);
    private final OrdemServicoDomainService service = new OrdemServicoDomainService();

    @Test
    void calculaMediaApenasDasOrdensFinalizadas() {
        var emAndamento = new OrdemServico();
        emAndamento.setDataAbertura(INICIO);

        var resultado = service.calcularTempoMedioFinalizacao(
                List.of(ordem(Duration.ofHours(1)), ordem(Duration.ofHours(3)), emAndamento));

        assertEquals(2, resultado.getQuantidadeOrdens());
        assertEquals(Duration.ofHours(2), resultado.getTempoMedio());
    }

    @Test
    void semOrdensFinalizadasRetornaZero() {
        var resultado = service.calcularTempoMedioFinalizacao(List.of());
        assertEquals(0, resultado.getQuantidadeOrdens());
        assertEquals(Duration.ZERO, resultado.getTempoMedio());
    }

    @Test
    void tempoDeFinalizacaoEhDiferencaEntreAberturaEFechamento() {
        assertEquals(Duration.ofMinutes(90), ordem(Duration.ofMinutes(90)).tempoDeFinalizacao().orElseThrow());
    }

    @Test
    void ordemSemFechamentoNaoTemTempoDeFinalizacao() {
        var ordem = new OrdemServico();
        ordem.setDataAbertura(INICIO);
        assertTrue(ordem.tempoDeFinalizacao().isEmpty());
    }

    private static OrdemServico ordem(Duration duracao) {
        var ordem = new OrdemServico();
        ordem.setDataAbertura(INICIO);
        ordem.setDataFechamento(INICIO.plus(duracao));
        return ordem;
    }
}
