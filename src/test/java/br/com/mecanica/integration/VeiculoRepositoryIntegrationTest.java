package br.com.mecanica.integration;

import br.com.mecanica.entity.VeiculoEntity;
import br.com.mecanica.repository.VeiculoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Sql({RepositoryIntegrationTest.LIMPAR_BANCO, RepositoryIntegrationTest.CADASTROS})
class VeiculoRepositoryIntegrationTest extends RepositoryIntegrationTest {

    private static final UUID CLIENTE_ANA = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID VEICULO_ANA = UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final UUID VEICULO_SEM_CLIENTE = UUID.fromString("30000000-0000-0000-0000-000000000003");

    @Autowired VeiculoRepository repository;

    @Test
    void buscaVeiculoParaVincularComCliente() {
        var veiculo = repository.buscarParaVincular(VEICULO_ANA).orElseThrow();

        assertEquals("ABC1D23", veiculo.getPlaca());
        assertEquals("Onix", veiculo.getModelo());
        assertEquals("Chevrolet", veiculo.getMarca());
        assertEquals(2020, veiculo.getAno());
        assertEquals(CLIENTE_ANA, veiculo.getCliente().getId());
    }

    @Test
    void buscaVeiculoParaVincularSemCliente() {
        var veiculo = repository.buscarParaVincular(VEICULO_SEM_CLIENTE).orElseThrow();

        assertEquals("SEM0C00", veiculo.getPlaca());
        assertNull(veiculo.getCliente());
    }

    @Test
    void buscaVeiculoParaVincularInexistenteRetornaVazio() {
        assertTrue(repository.buscarParaVincular(UUID.randomUUID()).isEmpty());
    }

    @Test
    void listaVeiculosOrdenadosPorId() {
        var placas = repository.findAll(Sort.by("id")).stream().map(VeiculoEntity::getPlaca).toList();

        assertEquals(List.of("ABC1D23", "XYZ9K87", "SEM0C00"), placas);
    }
}
