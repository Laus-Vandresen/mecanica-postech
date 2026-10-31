package br.com.mecanica.domain.entity;

import br.com.mecanica.domain.exception.*;
import br.com.mecanica.domain.valueObject.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class OrdemServicoTest {
    private final LocalDateTime agora = LocalDateTime.of(2026, 1, 1, 12, 0);
    private OrdemServico nova() { return OrdemServico.abrir(UUID.randomUUID(), "Barulho", List.of(), List.of(), agora); }
    private OrdemServico aguardando() {
        var ordem = nova(); ordem.iniciarDiagnostico();
        ordem.registrarDiagnostico("Trocar filtro", List.of(), List.of());
        ordem.enviarParaAprovacao(UUID.randomUUID(), new BigDecimal("20.00")); return ordem;
    }
    @Test void abreSemClienteSemUsuarioDoSolicitanteEComIdentidadePropria() {
        var ordem = nova(); assertNull(ordem.getClienteId()); assertNull(ordem.getOrcamentoId());
        assertEquals(StatusOrdemServico.RECEBIDA, ordem.getStatus()); assertEquals(4, ordem.getId().version());
    }
    @Test void fluxoCompletoSeparaConclusaoFinalizacaoEEntrega() {
        var ordem = aguardando(); var id = ordem.getId(); var cliente = UUID.randomUUID();
        ordem.aprovar(cliente); assertEquals(StatusOrdemServico.AGUARDANDO_APROVACAO, ordem.getStatus());
        ordem.iniciarExecucao();
        assertThrows(RegraOrdemServicoException.class, () -> ordem.finalizar(agora.plusHours(2)));
        ordem.concluirExecucao(agora.plusHours(2));
        assertEquals(StatusOrdemServico.EM_EXECUCAO, ordem.getStatus()); assertNull(ordem.getDataFechamento());
        ordem.finalizar(agora.plusHours(3)); var fechamento = ordem.getDataFechamento();
        ordem.entregar(); assertEquals(fechamento, ordem.getDataFechamento());
        assertEquals(StatusOrdemServico.ENTREGUE, ordem.getStatus()); assertEquals(id, ordem.getId()); assertEquals(cliente, ordem.getClienteId());
    }
    @Test void recusaEncerraSemClienteENaoPermiteRevisaoNemExecucao() {
        var ordem = aguardando(); ordem.recusar("Nao autorizado", agora);
        assertNull(ordem.getClienteId()); assertEquals(StatusOrdemServico.RECUSADA, ordem.getStatus()); assertNotNull(ordem.getDataFechamento());
        assertThrows(RegraOrdemServicoException.class, () -> ordem.aprovar(UUID.randomUUID()));
        assertThrows(RegraOrdemServicoException.class, () -> ordem.iniciarExecucao());
        assertThrows(RegraOrdemServicoException.class, () -> ordem.registrarDiagnostico("Novo", List.of(), List.of()));
    }
    @Test void execucaoDependeDeAprovacaoComCliente() {
        var ordem = aguardando();
        assertThrows(RegraOrdemServicoException.class, () -> ordem.iniciarExecucao());
        assertThrows(DadosOrdemInvalidosException.class, () -> ordem.aprovar(null));
    }
    @Test void decisaoRepetidaPreservaClienteAprovado() {
        var ordem = aguardando(); var cliente = UUID.randomUUID(); ordem.aprovar(cliente);
        assertThrows(RegraOrdemServicoException.class, () -> ordem.aprovar(UUID.randomUUID()));
        assertThrows(RegraOrdemServicoException.class, () -> ordem.recusar("x", agora));
        assertEquals(cliente, ordem.getClienteId());
        assertTrue(ordem.isOrcamentoAprovado());
    }
    @Test void diagnosticoExigeEtapaETextoValidos() {
        var ordem = nova();
        assertThrows(RegraOrdemServicoException.class, () -> ordem.registrarDiagnostico("x", List.of(), List.of()));
        ordem.iniciarDiagnostico();
        assertThrows(DadosOrdemInvalidosException.class, () -> ordem.registrarDiagnostico("  ", List.of(), List.of()));
        assertThrows(RegraOrdemServicoException.class, () -> ordem.enviarParaAprovacao(UUID.randomUUID(), BigDecimal.ZERO));
    }
    @Test void diagnosticoNaoAceitaItensRepetidos() {
        var ordem = nova(); ordem.iniciarDiagnostico(); var item = UUID.randomUUID();
        assertThrows(DadosOrdemInvalidosException.class, () -> ordem.registrarDiagnostico("x", List.of(item, item), List.of()));
    }
    @Test void naoAceitaConclusaoAnteriorAAbertura() {
        var ordem = aguardando(); ordem.aprovar(UUID.randomUUID()); ordem.iniciarExecucao();
        assertThrows(RegraOrdemServicoException.class, () -> ordem.concluirExecucao(agora.minusSeconds(1)));
        assertNull(ordem.getDataConclusaoExecucao());
    }
    @Test void cancelaAntesDaExecucaoSemApagarIdentidade() {
        var ordem = aguardando(); var id = ordem.getId(); ordem.cancelar("Desistencia", agora);
        assertEquals(id, ordem.getId()); assertEquals(StatusOrdemServico.CANCELADA, ordem.getStatus());
        assertThrows(RegraOrdemServicoException.class, () -> ordem.cancelar("Outra", agora));
    }
    @Test void naoCancelaAposInicioDaExecucao() {
        var ordem = aguardando(); ordem.aprovar(UUID.randomUUID()); ordem.iniciarExecucao();
        assertThrows(RegraOrdemServicoException.class, () -> ordem.cancelar("x", agora));
    }
    @Test void naoEntregaAntesDeFinalizar() {
        assertThrows(RegraOrdemServicoException.class, () -> nova().entregar());
    }
    @Test void colecoesDoAgregadoNaoPodemSerAlteradasPorFora() {
        var ordem = aguardando();
        assertThrows(UnsupportedOperationException.class, () -> ordem.getServicoIds().add(UUID.randomUUID()));
    }
    @Test void totalInvalidoNaoAlteraOrcamento() {
        var ordem = nova(); ordem.iniciarDiagnostico(); ordem.registrarDiagnostico("x", null, null);
        assertThrows(DadosOrdemInvalidosException.class, () -> ordem.enviarParaAprovacao(UUID.randomUUID(), new BigDecimal("-1")));
        assertNull(ordem.getOrcamentoId()); assertEquals(StatusOrdemServico.EM_DIAGNOSTICO, ordem.getStatus());
    }
    @Test void validaDadosDoClienteNaAprovacao() {
        assertThrows(CpfInvalidoException.class, () -> new DadosClienteAprovacao("Ana", "11111111111", null, null));
        assertDoesNotThrow(() -> new DadosClienteAprovacao("Ana", "52998224725", null, "ana@example.com"));
    }
    @Test void produtosExigemQuantidadePositivaENaoPodemSeRepetir() {
        var produto = UUID.randomUUID();
        assertThrows(DadosOrdemInvalidosException.class, () -> new ItemProdutoOrdemServico(produto, 0));
        assertThrows(DadosOrdemInvalidosException.class, () -> new ItemProdutoOrdemServico(produto, -1));
        assertThrows(DadosOrdemInvalidosException.class, () -> new ItemProdutoOrdemServico(null, 1));
        assertThrows(DadosOrdemInvalidosException.class, () -> OrdemServico.abrir(UUID.randomUUID(), "Teste", List.of(),
                List.of(new ItemProdutoOrdemServico(produto, 1), new ItemProdutoOrdemServico(produto, 2)), agora));
        var ordem = OrdemServico.abrir(UUID.randomUUID(), "Teste", List.of(), List.of(new ItemProdutoOrdemServico(produto, 3)), agora);
        assertEquals(3, ordem.getProdutos().getFirst().quantidade());
        assertThrows(UnsupportedOperationException.class, () -> ordem.getProdutos().clear());
    }
    @Test void consumoAcumuladoRetornaSomenteDiferencaENaoPermiteReducao() {
        var produto = UUID.randomUUID();
        var ordem = OrdemServico.abrir(UUID.randomUUID(), "Teste", List.of(),
                List.of(new ItemProdutoOrdemServico(produto, 4)), agora);
        assertThrows(RegraOrdemServicoException.class, () -> ordem.registrarConsumo(produto, 1));
        ordem.iniciarDiagnostico();
        ordem.registrarDiagnostico("Trocar", null, null);
        ordem.enviarParaAprovacao(UUID.randomUUID(), BigDecimal.TEN);
        ordem.aprovar(UUID.randomUUID());
        ordem.iniciarExecucao();
        assertEquals(2, ordem.registrarConsumo(produto, 2));
        assertEquals(0, ordem.registrarConsumo(produto, 2));
        assertThrows(RegraOrdemServicoException.class, () -> ordem.registrarConsumo(produto, 1));
        assertThrows(RegraOrdemServicoException.class, () -> ordem.registrarConsumo(produto, 5));
        assertEquals(1, ordem.registrarConsumo(produto, 3));
        assertEquals(3, ordem.getProdutos().getFirst().quantidadeConsumida());
        ordem.concluirExecucao(agora);
        assertThrows(RegraOrdemServicoException.class, () -> ordem.registrarConsumo(produto, 4));
        assertEquals(3, ordem.getProdutos().getFirst().quantidadeConsumida());
    }

    @Test void itemNaoAceitaConsumoNegativoOuMaiorQueQuantidadePrevista() {
        assertThrows(DadosOrdemInvalidosException.class, () -> new ItemProdutoOrdemServico(UUID.randomUUID(), 2, -1));
        assertThrows(DadosOrdemInvalidosException.class, () -> new ItemProdutoOrdemServico(UUID.randomUUID(), 2, 3));
    }
}
