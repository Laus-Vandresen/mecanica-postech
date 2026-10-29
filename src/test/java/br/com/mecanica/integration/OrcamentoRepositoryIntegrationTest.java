package br.com.mecanica.integration;

import br.com.mecanica.repository.OrcamentoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Sql({RepositoryIntegrationTest.LIMPAR_BANCO, RepositoryIntegrationTest.CADASTROS, RepositoryIntegrationTest.ORDENS_SERVICO})
class OrcamentoRepositoryIntegrationTest extends RepositoryIntegrationTest {

    private static final UUID ORCAMENTO_OS_802 = UUID.fromString("70000000-0000-0000-0000-000000000001");

    @Autowired OrcamentoRepository repository;

    @Test
    void buscaOrcamentoPorId() {
        var orcamento = repository.findById(ORCAMENTO_OS_802).orElseThrow();

        assertEquals(new BigDecimal("215.40"), orcamento.getValor());
    }

    @Test
    void buscaOrcamentoInexistenteRetornaVazio() {
        assertTrue(repository.findById(UUID.randomUUID()).isEmpty());
    }

    @Test
    void listaOrcamentos() {
        var orcamentos = repository.findAll();

        assertEquals(1, orcamentos.size());
        assertEquals(ORCAMENTO_OS_802, orcamentos.getFirst().getId());
    }
}
