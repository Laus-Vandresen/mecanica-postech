package br.com.mecanica.controller;

import br.com.mecanica.api.PecaV1Api;
import br.com.mecanica.api.dto.PecaRequestDTOV1;
import br.com.mecanica.api.dto.PecaResponseDTOV1;
import br.com.mecanica.controller.mapper.PecaMapper;
import br.com.mecanica.service.PecaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PecaController implements PecaV1Api {

    private final PecaService service;

    @Override
    public ResponseEntity<PecaResponseDTOV1> cadastrarPecaV1(PecaRequestDTOV1 pecaRequestDTOV1) {
        var response = PecaMapper.toResponse(service.criar(PecaMapper.toDomain(pecaRequestDTOV1)));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<PecaResponseDTOV1> buscarPecaV1(Integer id) {
        return ResponseEntity.ok(PecaMapper.toResponse(service.buscar(id.longValue())));
    }

    @Override
    public ResponseEntity<Void> excluirPecaV1(Integer id) {
        service.excluir(id.longValue());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<PecaResponseDTOV1>> listaPecasV1() {
        return ResponseEntity.ok(PecaMapper.toResponse(service.listar()));
    }
}
