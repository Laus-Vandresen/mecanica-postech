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
        var emAndamento = OrdemServico.abrir(java.util.UUID.randomUUID(), "Teste", List.of(), List.of(), INICIO);

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
        var ordem = OrdemServico.abrir(java.util.UUID.randomUUID(), "Teste", List.of(), List.of(), INICIO);
        assertTrue(ordem.tempoDeFinalizacao().isEmpty());
    }

    private static OrdemServico ordem(Duration duracao) {
        return OrdemServico.restaurar(new br.com.mecanica.domain.valueObject.EstadoOrdemServico(
                java.util.UUID.randomUUID(), null, java.util.UUID.randomUUID(), null, List.of(), List.of(), INICIO,
                INICIO.plus(duracao), br.com.mecanica.domain.valueObject.StatusOrdemServico.FINALIZADA,
                "Teste", null, java.math.BigDecimal.ZERO, null, false, null));
    }
}
