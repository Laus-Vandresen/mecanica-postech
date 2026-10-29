package br.com.mecanica.integration;

import br.com.mecanica.entity.InsumoEntity;
import br.com.mecanica.entity.PecaEntity;
import br.com.mecanica.repository.ProdutoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.jdbc.Sql;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

// movimentarEstoque usa UPDATE ... RETURNING, especifico do Postgres (o H2 nao suporta);
// essa consulta e coberta pelo EstoqueApiTests com Testcontainers
@Sql({RepositoryIntegrationTest.LIMPAR_BANCO, RepositoryIntegrationTest.CADASTROS})
class ProdutoRepositoryIntegrationTest extends RepositoryIntegrationTest {

    private static final UUID PECA_FILTRO = UUID.fromString("40000000-0000-0000-0000-000000000001");
    private static final UUID INSUMO_OLEO = UUID.fromString("50000000-0000-0000-0000-000000000001");

    @Autowired ProdutoRepository repository;

    @Test
    void listaProdutosRetornandoSubtiposDaHeranca() {
        var produtos = repository.findAll(Sort.by("id"));

        assertEquals(2, produtos.size());
        assertInstanceOf(PecaEntity.class, produtos.getFirst());
        assertInstanceOf(InsumoEntity.class, produtos.get(1));
    }

    @Test
    void buscaProdutoPorIdComSubtipo() {
        var peca = assertInstanceOf(PecaEntity.class, repository.findById(PECA_FILTRO).orElseThrow());
        var insumo = assertInstanceOf(InsumoEntity.class, repository.findById(INSUMO_OLEO).orElseThrow());

        assertEquals("Tecfil", peca.getFabricante());
        assertEquals("LITRO", insumo.getUnidadeMedida());
    }

    @Test
    void buscaProdutoInexistenteRetornaVazio() {
        assertTrue(repository.findById(UUID.randomUUID()).isEmpty());
    }
}
