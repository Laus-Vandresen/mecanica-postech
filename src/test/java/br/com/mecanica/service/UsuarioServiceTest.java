package br.com.mecanica.service;

import br.com.mecanica.domain.entity.Usuario;
import br.com.mecanica.entity.UsuarioEntity;
import br.com.mecanica.exception.CredenciaisInvalidasException;
import br.com.mecanica.exception.LoginJaCadastradoException;
import br.com.mecanica.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {
    private static final UUID USER_ID = new UUID(0, 42);
    @Mock private UsuarioRepository repository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtEncoder jwtEncoder;
    private UsuarioService service;

    @BeforeEach
    void setUp() {
        service = new UsuarioService(repository, passwordEncoder, jwtEncoder);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "expiracaoSegundos", 600L);
    }

    @Test
    void registrarNormalizaLoginArmazenaSomenteHashEDevolveIdentificador() {
        var request = new Usuario(null, "Ana", null, "  ana  ", "senha-secreta");
        when(repository.existsByLogin("ana")).thenReturn(false);
        when(passwordEncoder.encode("senha-secreta")).thenReturn("hash-seguro");
        when(repository.saveAndFlush(any())).thenAnswer(invocation -> {
            UsuarioEntity entity = invocation.getArgument(0);
            entity.setId(USER_ID);
            return entity;
        });

        var salvo = service.registrar(request);

        assertEquals(USER_ID, salvo.getId());
        assertEquals("ana", salvo.getLogin());
        assertEquals("hash-seguro", salvo.getSenha());
        var captor = ArgumentCaptor.forClass(UsuarioEntity.class);
        verify(repository).saveAndFlush(captor.capture());
        assertEquals("ana", captor.getValue().getLogin());
        assertEquals("hash-seguro", captor.getValue().getSenha());
        verify(passwordEncoder).encode("senha-secreta");
    }

    @Test
    void loginDuplicadoNaoCodificaSenhaNemPersiste() {
        var request = new Usuario(null, "Ana", null, "ana", "senha");
        when(repository.existsByLogin("ana")).thenReturn(true);

        assertThrows(LoginJaCadastradoException.class, () -> service.registrar(request));

        verifyNoInteractions(passwordEncoder, jwtEncoder);
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void autenticarNormalizaLoginEEmiteJwtComIdentidadeEExpiracao() {
        var entity = new UsuarioEntity();
        entity.setId(USER_ID);
        entity.setLogin("ana");
        entity.setSenha("hash-seguro");
        when(repository.findByLogin("ana")).thenReturn(Optional.of(entity));
        when(passwordEncoder.matches("senha-secreta", "hash-seguro")).thenReturn(true);
        var jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("jwt-assinado");
        when(jwtEncoder.encode(any())).thenReturn(jwt);

        assertEquals("jwt-assinado", service.autenticar(" ana ", "senha-secreta"));

        var captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder).encode(captor.capture());
        var claims = captor.getValue().getClaims();
        assertEquals("ana", claims.getSubject());
        assertEquals(USER_ID.toString(), claims.getClaim("uid"));
        assertEquals(claims.getIssuedAt().plusSeconds(600), claims.getExpiresAt());
        assertEquals("HS256", captor.getValue().getJwsHeader().getAlgorithm().getName());
    }

    @Test
    void autenticarRejeitaUsuarioAusenteESenhaIncorretaSemEmitirToken() {
        when(repository.findByLogin("ausente")).thenReturn(Optional.empty());
        assertThrows(CredenciaisInvalidasException.class, () -> service.autenticar("ausente", "senha"));

        var entity = new UsuarioEntity();
        entity.setId(USER_ID);
        entity.setLogin("ana");
        entity.setSenha("hash-seguro");
        when(repository.findByLogin("ana")).thenReturn(Optional.of(entity));
        when(passwordEncoder.matches("errada", "hash-seguro")).thenReturn(false);
        assertThrows(CredenciaisInvalidasException.class, () -> service.autenticar("ana", "errada"));

        verifyNoInteractions(jwtEncoder);
    }
}
