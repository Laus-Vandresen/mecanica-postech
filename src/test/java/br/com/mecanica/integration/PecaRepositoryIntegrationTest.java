package br.com.mecanica.integration;

import br.com.mecanica.repository.PecaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Sql({RepositoryIntegrationTest.LIMPAR_BANCO, RepositoryIntegrationTest.CADASTROS})
class PecaRepositoryIntegrationTest extends RepositoryIntegrationTest {

    private static final UUID PECA_FILTRO = UUID.fromString("40000000-0000-0000-0000-000000000001");
    private static final UUID INSUMO_OLEO = UUID.fromString("50000000-0000-0000-0000-000000000001");

    @Autowired PecaRepository repository;

    @Test
    void buscaPecaPorIdComDadosDoProduto() {
        var peca = repository.findById(PECA_FILTRO).orElseThrow();

        assertEquals("Filtro de oleo", peca.getNome());
        assertEquals("Filtro para motor 1.0", peca.getDescricao());
        assertEquals("Tecfil", peca.getFabricante());
        assertEquals(10, peca.getQuantidadeEstoque());
        assertEquals(new BigDecimal("25.50"), peca.getValor());
    }

    @Test
    void naoRetornaInsumoComoPeca() {
        assertTrue(repository.findById(INSUMO_OLEO).isEmpty());
    }

    @Test
    void listaSomentePecas() {
        var pecas = repository.findAll(Sort.by("id"));

        assertEquals(1, pecas.size());
        assertEquals(PECA_FILTRO, pecas.getFirst().getId());
    }
}
