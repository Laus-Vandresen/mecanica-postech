package br.com.mecanica.integration;

import br.com.mecanica.entity.OrdemServicoEntity;
import br.com.mecanica.repository.OrdemServicoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

// Cenario descrito em src/test/resources/sql/cenarios/ordens-servico.sql
@Sql({RepositoryIntegrationTest.LIMPAR_BANCO, RepositoryIntegrationTest.CADASTROS, RepositoryIntegrationTest.ORDENS_SERVICO})
class OrdemServicoRepositoryIntegrationTest extends RepositoryIntegrationTest {

    private static final UUID CLIENTE_ANA = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID CLIENTE_BRUNO = UUID.fromString("10000000-0000-0000-0000-000000000002");
    private static final UUID VEICULO_SEM_CLIENTE = UUID.fromString("30000000-0000-0000-0000-000000000003");
    private static final UUID VEICULO_BRUNO = UUID.fromString("30000000-0000-0000-0000-000000000002");

    @Autowired OrdemServicoRepository repository;

    // Mesma ordenacao usada pelo OrdemServicoService
    private static Pageable pagina(int pagina, int tamanho) {
        return PageRequest.of(pagina, tamanho, Sort.by(Sort.Order.desc("dataAbertura"), Sort.Order.asc("id")));
    }

    private List<OrdemServicoEntity> buscar(UUID cliente, UUID veiculo, String status, String fila) {
        return repository.buscarComFiltros(cliente, veiculo, status, fila, pagina(0, 100));
    }

    private static UUID os(int numero) {
        return UUID.fromString("80000000-0000-0000-0000-00000000000" + numero);
    }

    private static List<UUID> ids(List<OrdemServicoEntity> ordens) {
        return ordens.stream().map(OrdemServicoEntity::getId).toList();
    }

    @Test
    void semFiltrosRetornaTodasOrdenadasPorAberturaDecrescente() {
        assertEquals(List.of(os(8), os(5), os(4), os(3), os(2), os(1), os(6), os(7)), ids(buscar(null, null, null, null)));
    }

    @Test
    void filtraPorCliente() {
        assertEquals(List.of(os(5), os(2), os(1)), ids(buscar(CLIENTE_ANA, null, null, null)));
        assertEquals(List.of(os(4), os(3), os(6)), ids(buscar(CLIENTE_BRUNO, null, null, null)));
    }

    @Test
    void filtraPorVeiculoIncluindoOrdensSemCliente() {
        assertEquals(List.of(os(8), os(7)), ids(buscar(null, VEICULO_SEM_CLIENTE, null, null)));
    }

    @Test
    void filtraPorStatus() {
        assertEquals(List.of(os(8), os(1)), ids(buscar(null, null, "RECEBIDA", null)));
        assertEquals(List.of(os(5), os(4)), ids(buscar(null, null, "EM_EXECUCAO", null)));
        assertTrue(buscar(null, null, "CANCELADA", null).isEmpty());
    }

    @Test
    void filaDiagnosticoRetornaOrdensRecebidas() {
        assertEquals(List.of(os(8), os(1)), ids(buscar(null, null, null, "DIAGNOSTICO")));
    }

    @Test
    void filaExecucaoRetornaSomenteOrcamentosAprovados() {
        assertEquals(List.of(os(2)), ids(buscar(null, null, null, "EXECUCAO")));
    }

    @Test
    void filaFinalizacaoRetornaSomenteExecucoesConcluidas() {
        assertEquals(List.of(os(4)), ids(buscar(null, null, null, "FINALIZACAO")));
    }

    @Test
    void combinaFiltros() {
        assertEquals(List.of(os(3)), ids(buscar(CLIENTE_BRUNO, VEICULO_BRUNO, "AGUARDANDO_APROVACAO", null)));
        assertEquals(List.of(os(2)), ids(buscar(CLIENTE_ANA, null, null, "EXECUCAO")));
        assertTrue(buscar(CLIENTE_BRUNO, null, null, "EXECUCAO").isEmpty());
        assertTrue(buscar(CLIENTE_ANA, VEICULO_BRUNO, null, null).isEmpty());
    }

    @Test
    void paginaResultados() {
        assertEquals(List.of(os(8), os(5), os(4)), ids(repository.buscarComFiltros(null, null, null, null, pagina(0, 3))));
        assertEquals(List.of(os(3), os(2), os(1)), ids(repository.buscarComFiltros(null, null, null, null, pagina(1, 3))));
        assertEquals(List.of(os(6), os(7)), ids(repository.buscarComFiltros(null, null, null, null, pagina(2, 3))));
        assertTrue(repository.buscarComFiltros(null, null, null, null, pagina(3, 3)).isEmpty());
    }

    @Test
    void buscaOrdensFechadas() {
        var fechadas = repository.findAllByDataFechamentoIsNotNull();

        assertEquals(2, fechadas.size());
        assertTrue(ids(fechadas).containsAll(List.of(os(6), os(7))));
    }

    @Test
    void buscaOrdemPorIdComRelacionamentos() {
        var ordem = repository.findById(os(2)).orElseThrow();

        assertEquals(CLIENTE_ANA, ordem.getCliente().getId());
        assertEquals("ABC1D23", ordem.getVeiculo().getPlaca());
        assertEquals(new BigDecimal("215.40"), ordem.getOrcamento().getValor());
        assertTrue(ordem.isOrcamentoAprovado());
        assertEquals(List.of("Troca de oleo"), ordem.getServicos().stream().map(s -> s.getNome()).toList());
        assertEquals(2, ordem.getProdutos().size());
    }
}
