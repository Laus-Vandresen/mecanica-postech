package br.com.mecanica.integration;

import br.com.mecanica.repository.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.jdbc.Sql;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Sql({RepositoryIntegrationTest.LIMPAR_BANCO, RepositoryIntegrationTest.CADASTROS})
class ClienteRepositoryIntegrationTest extends RepositoryIntegrationTest {

    private static final UUID CLIENTE_ANA = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID CLIENTE_BRUNO = UUID.fromString("10000000-0000-0000-0000-000000000002");
    private static final UUID USUARIO_CARLOS = UUID.fromString("20000000-0000-0000-0000-000000000001");

    @Autowired ClienteRepository repository;

    @Test
    void buscaClientePorCpfComDadosDaPessoa() {
        var cliente = repository.findByCpf("111.111.111-11").orElseThrow();

        assertEquals(CLIENTE_ANA, cliente.getId());
        assertEquals("Ana Souza", cliente.getNome());
        assertEquals("11999990001", cliente.getCelular());
        assertEquals("ana@email.com", cliente.getEmail());
    }

    @Test
    void buscaClientePorCpfInexistenteRetornaVazio() {
        assertTrue(repository.findByCpf("999.999.999-99").isEmpty());
    }

    @Test
    void buscaClientePorId() {
        var cliente = repository.findById(CLIENTE_BRUNO).orElseThrow();

        assertEquals("Bruno Lima", cliente.getNome());
        assertEquals("222.222.222-22", cliente.getCpf());
    }

    @Test
    void naoRetornaUsuarioComoCliente() {
        assertTrue(repository.findById(USUARIO_CARLOS).isEmpty());
        assertEquals(2, repository.count());
    }

    @Test
    void listaClientesOrdenadosPorId() {
        var clientes = repository.findAll(Sort.by("id"));

        assertEquals(2, clientes.size());
        assertEquals(CLIENTE_ANA, clientes.getFirst().getId());
        assertEquals(CLIENTE_BRUNO, clientes.get(1).getId());
    }
}
