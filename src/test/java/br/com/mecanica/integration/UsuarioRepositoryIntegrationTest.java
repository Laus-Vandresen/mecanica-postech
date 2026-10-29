package br.com.mecanica.integration;

import br.com.mecanica.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Sql({RepositoryIntegrationTest.LIMPAR_BANCO, RepositoryIntegrationTest.CADASTROS})
class UsuarioRepositoryIntegrationTest extends RepositoryIntegrationTest {

    private static final UUID USUARIO_CARLOS = UUID.fromString("20000000-0000-0000-0000-000000000001");
    private static final UUID CLIENTE_ANA = UUID.fromString("10000000-0000-0000-0000-000000000001");

    @Autowired UsuarioRepository repository;

    @Test
    void buscaUsuarioPorLoginComDadosDaPessoa() {
        var usuario = repository.findByLogin("carlos").orElseThrow();

        assertEquals(USUARIO_CARLOS, usuario.getId());
        assertEquals("Carlos Mecanico", usuario.getNome());
        assertNull(usuario.getCelular());
    }

    @Test
    void buscaUsuarioPorLoginInexistenteRetornaVazio() {
        assertTrue(repository.findByLogin("inexistente").isEmpty());
    }

    @Test
    void verificaExistenciaDeLogin() {
        assertTrue(repository.existsByLogin("carlos"));
        assertFalse(repository.existsByLogin("ana"));
    }

    @Test
    void naoRetornaClienteComoUsuario() {
        assertTrue(repository.findById(CLIENTE_ANA).isEmpty());
        assertEquals(1, repository.count());
    }
}
