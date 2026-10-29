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
    @Test void aberturaValidaVeiculoDescricaoEItens() {
        assertThrows(DadosOrdemInvalidosException.class, () -> OrdemServico.abrir(null, "Barulho", List.of(), List.of(), agora));
        assertThrows(DadosOrdemInvalidosException.class, () -> OrdemServico.abrir(UUID.randomUUID(), " ", List.of(), List.of(), agora));
        assertThrows(DadosOrdemInvalidosException.class, () -> OrdemServico.abrir(UUID.randomUUID(), "x".repeat(501), List.of(), List.of(), agora));
        assertThrows(DadosOrdemInvalidosException.class, () -> OrdemServico.abrir(UUID.randomUUID(), "Barulho", Arrays.asList(UUID.randomUUID(), null), List.of(), agora));
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
        assertThrows(DadosOrdemInvalidosException.class, () -> ordem.registrarDiagnostico(null, List.of(), List.of()));
        assertThrows(DadosOrdemInvalidosException.class, () -> ordem.registrarDiagnostico("  ", List.of(), List.of()));
        assertThrows(RegraOrdemServicoException.class, () -> ordem.enviarParaAprovacao(UUID.randomUUID(), BigDecimal.ZERO));
    }
    @Test void diagnosticoNaoAceitaItensRepetidos() {
        var ordem = nova(); ordem.iniciarDiagnostico(); var item = UUID.randomUUID();
        assertThrows(DadosOrdemInvalidosException.class, () -> ordem.registrarDiagnostico("x", List.of(item, item), List.of()));
    }
    @Test void diagnosticoPreservaItensQuandoListasNaoSaoInformadas() {
        var servico = UUID.randomUUID(); var produto = UUID.randomUUID();
        var ordem = OrdemServico.abrir(UUID.randomUUID(), "Barulho", List.of(servico), List.of(produto), agora);
        ordem.iniciarDiagnostico(); ordem.registrarDiagnostico("Verificado", null, null);
        assertEquals(List.of(servico), ordem.getServicoIds());
        assertEquals(List.of(produto), ordem.getProdutoIds());
    }
    @Test void diagnosticoRejeitaItensNulosETextoAcimaDoLimite() {
        var ordem = nova(); ordem.iniciarDiagnostico();
        assertThrows(DadosOrdemInvalidosException.class,
                () -> ordem.registrarDiagnostico("x", Arrays.asList((UUID) null), List.of()));
        assertThrows(DadosOrdemInvalidosException.class,
                () -> ordem.registrarDiagnostico("x".repeat(1001), List.of(), List.of()));
        assertNull(ordem.getDiagnostico());
    }
    @Test void envioDeOrcamentoValidaPrecondicoesEValores() {
        var ordem = nova(); ordem.iniciarDiagnostico();
        assertThrows(RegraOrdemServicoException.class,
                () -> ordem.enviarParaAprovacao(UUID.randomUUID(), BigDecimal.ONE));
        ordem.registrarDiagnostico("Teste", null, null);
        assertThrows(DadosOrdemInvalidosException.class,
                () -> ordem.enviarParaAprovacao(null, BigDecimal.ONE));
        assertThrows(DadosOrdemInvalidosException.class,
                () -> ordem.enviarParaAprovacao(UUID.randomUUID(), null));
        assertThrows(DadosOrdemInvalidosException.class,
                () -> ordem.enviarParaAprovacao(UUID.randomUUID(), new BigDecimal("1.001")));
        assertThrows(DadosOrdemInvalidosException.class,
                () -> ordem.enviarParaAprovacao(UUID.randomUUID(), new BigDecimal("10000000000.00")));
        assertEquals(StatusOrdemServico.EM_DIAGNOSTICO, ordem.getStatus());
        assertNull(ordem.getOrcamentoId());
    }
    @Test void aprovacoesRecusasECancelamentosValidamDadosEEstado() {
        var ordem = aguardando();
        assertThrows(DadosOrdemInvalidosException.class, () -> ordem.aprovar(null));
        assertThrows(DadosOrdemInvalidosException.class, () -> ordem.recusar(null, agora));
        assertThrows(DadosOrdemInvalidosException.class, () -> ordem.recusar(" ", agora));
        assertThrows(DadosOrdemInvalidosException.class, () -> ordem.recusar("x".repeat(501), agora));
        assertThrows(DadosOrdemInvalidosException.class, () -> ordem.cancelar(null, agora));
        assertThrows(DadosOrdemInvalidosException.class, () -> ordem.cancelar(" ", agora));
        assertThrows(DadosOrdemInvalidosException.class, () -> ordem.cancelar("x".repeat(501), agora));
        assertNull(ordem.getDataFechamento());
        assertEquals(StatusOrdemServico.AGUARDANDO_APROVACAO, ordem.getStatus());
    }
    @Test void rejeitaDatasNulasEFinalizacaoAnteriorAConclusao() {
        var ordem = aguardando(); ordem.aprovar(UUID.randomUUID()); ordem.iniciarExecucao();
        assertThrows(DadosOrdemInvalidosException.class, () -> ordem.concluirExecucao(null));
        ordem.concluirExecucao(agora.plusHours(2));
        assertThrows(RegraOrdemServicoException.class, () -> ordem.finalizar(agora.plusHours(1)));
        assertThrows(DadosOrdemInvalidosException.class, () -> ordem.finalizar(null));
        assertEquals(StatusOrdemServico.EM_EXECUCAO, ordem.getStatus());
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
    @Test void naoPermiteConcluirExecucaoMaisDeUmaVez() {
        var ordem = aguardando(); ordem.aprovar(UUID.randomUUID()); ordem.iniciarExecucao();
        var conclusao = agora.plusHours(1);
        ordem.concluirExecucao(conclusao);
        assertThrows(RegraOrdemServicoException.class, () -> ordem.concluirExecucao(conclusao.plusMinutes(1)));
        assertEquals(conclusao, ordem.getDataConclusaoExecucao());
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
    @Test void validaDadosDoClienteApenasNaAprovacao() {
        assertThrows(DadosOrdemInvalidosException.class, () -> new DadosClienteAprovacao("Ana", "11111111111", null, null));
        assertDoesNotThrow(() -> new DadosClienteAprovacao("Ana", "52998224725", null, "ana@example.com"));
    }
}
