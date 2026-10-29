package br.com.mecanica.integration;

import br.com.mecanica.repository.InsumoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Sql({RepositoryIntegrationTest.LIMPAR_BANCO, RepositoryIntegrationTest.CADASTROS})
class InsumoRepositoryIntegrationTest extends RepositoryIntegrationTest {

    private static final UUID PECA_FILTRO = UUID.fromString("40000000-0000-0000-0000-000000000001");
    private static final UUID INSUMO_OLEO = UUID.fromString("50000000-0000-0000-0000-000000000001");

    @Autowired InsumoRepository repository;

    @Test
    void buscaInsumoPorIdComDadosDoProduto() {
        var insumo = repository.findById(INSUMO_OLEO).orElseThrow();

        assertEquals("Oleo 5W30", insumo.getNome());
        assertEquals("LITRO", insumo.getUnidadeMedida());
        assertEquals(40, insumo.getQuantidadeEstoque());
        assertEquals(new BigDecimal("39.90"), insumo.getValor());
    }

    @Test
    void naoRetornaPecaComoInsumo() {
        assertTrue(repository.findById(PECA_FILTRO).isEmpty());
    }

    @Test
    void listaSomenteInsumos() {
        var insumos = repository.findAll(Sort.by("id"));

        assertEquals(1, insumos.size());
        assertEquals(INSUMO_OLEO, insumos.getFirst().getId());
    }
}
