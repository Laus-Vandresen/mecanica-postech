package br.com.mecanica.service;

import jakarta.annotation.PostConstruct;
import java.time.Instant;
import br.com.mecanica.domain.entity.Usuario;
import br.com.mecanica.entity.mapper.UsuarioEntityMapper;
import br.com.mecanica.exception.CredenciaisInvalidasException;
import br.com.mecanica.exception.LoginJaCadastradoException;
import br.com.mecanica.repository.UsuarioRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;

    @Getter
    @Value("${app.security.jwt.expiration-seconds}")
    private long expiracaoSegundos;

    @PostConstruct
    void validarConfiguracaoJwt() {
        validarExpiracaoSegundos(expiracaoSegundos);
    }

    static void validarExpiracaoSegundos(long expiracaoSegundos) {
        if (expiracaoSegundos <= 0) {
            throw new IllegalStateException("app.security.jwt.expiration-seconds deve ser maior que zero");
        }
    }

    @Transactional
    public Usuario registrar(Usuario usuario) {
        usuario.setLogin(usuario.getLogin().trim());
        if (repository.existsByLogin(usuario.getLogin())) {
            throw new LoginJaCadastradoException();
        }
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        return UsuarioEntityMapper.toDomain(repository.saveAndFlush(UsuarioEntityMapper.toEntity(usuario)));
    }

    public String autenticar(String login, String senha) {
        var usuario = repository.findByLogin(login.trim())
                .filter(u -> passwordEncoder.matches(senha, u.getSenha()))
                .orElseThrow(CredenciaisInvalidasException::new);

        var agora = Instant.now();
        var claims = JwtClaimsSet.builder()
                .subject(usuario.getLogin())
                .claim("uid", usuario.getId().toString())
                .issuedAt(agora)
                .expiresAt(agora.plusSeconds(expiracaoSegundos))
                .build();
        var header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
