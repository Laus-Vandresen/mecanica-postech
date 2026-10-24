package br.com.mecanica.controller;

import br.com.mecanica.api.AutenticacaoV1Api;
import br.com.mecanica.api.dto.TokenRequestDTOV1;
import br.com.mecanica.api.dto.TokenResponseDTOV1;
import br.com.mecanica.api.dto.UsuarioRequestDTOV1;
import br.com.mecanica.api.dto.UsuarioResponseDTOV1;
import br.com.mecanica.controller.mapper.UsuarioMapper;
import br.com.mecanica.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AutenticacaoController implements AutenticacaoV1Api {

    private final UsuarioService service;

    @Override
    public ResponseEntity<UsuarioResponseDTOV1> registrarV1(UsuarioRequestDTOV1 request) {
        var usuario = service.registrar(UsuarioMapper.toDomain(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioMapper.toResponse(usuario));
    }

    @Override
    public ResponseEntity<TokenResponseDTOV1> autenticarV1(TokenRequestDTOV1 request) {
        var response = new TokenResponseDTOV1();
        response.setAccessToken(service.autenticar(request.getLogin(), request.getSenha()));
        response.setTokenType("Bearer");
        response.setExpiresIn(service.getExpiracaoSegundos());
        return ResponseEntity.ok(response);
    }
}
