package br.com.mecanica.integration;

import br.com.mecanica.entity.ServicoEntity;
import br.com.mecanica.repository.ServicoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Sql({RepositoryIntegrationTest.LIMPAR_BANCO, RepositoryIntegrationTest.CADASTROS})
class ServicoRepositoryIntegrationTest extends RepositoryIntegrationTest {

    private static final UUID TROCA_OLEO = UUID.fromString("60000000-0000-0000-0000-000000000001");
    private static final UUID ALINHAMENTO = UUID.fromString("60000000-0000-0000-0000-000000000002");

    @Autowired ServicoRepository repository;

    @Test
    void buscaServicoPorId() {
        var servico = repository.findById(TROCA_OLEO).orElseThrow();

        assertEquals("Troca de oleo", servico.getNome());
        assertEquals(new BigDecimal("150.00"), servico.getValor());
    }

    @Test
    void buscaServicoInexistenteRetornaVazio() {
        assertTrue(repository.findById(UUID.randomUUID()).isEmpty());
    }

    @Test
    void buscaVariosServicosPorId() {
        var nomes = repository.findAllById(List.of(TROCA_OLEO, ALINHAMENTO)).stream()
                .map(ServicoEntity::getNome).sorted().toList();

        assertEquals(List.of("Alinhamento", "Troca de oleo"), nomes);
    }

    @Test
    void listaServicosOrdenadosPorId() {
        assertEquals(List.of(TROCA_OLEO, ALINHAMENTO),
                repository.findAll(Sort.by("id")).stream().map(ServicoEntity::getId).toList());
    }
}
