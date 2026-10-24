package br.com.mecanica.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class UsuarioServiceConfigurationTest {

    @Test
    void expiracaoJwtDeveSerPositiva() {
        assertDoesNotThrow(() -> UsuarioService.validarExpiracaoSegundos(1));
        assertThrows(IllegalStateException.class, () -> UsuarioService.validarExpiracaoSegundos(0));
        assertThrows(IllegalStateException.class, () -> UsuarioService.validarExpiracaoSegundos(-1));
    }
}
