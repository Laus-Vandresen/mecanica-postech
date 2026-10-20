package br.com.mecanica.controller;

import java.util.UUID;
import br.com.mecanica.api.InsumoV1Api;
import br.com.mecanica.api.dto.InsumoRequestDTOV1;
import br.com.mecanica.api.dto.InsumoResponseDTOV1;
import br.com.mecanica.controller.mapper.InsumoMapper;
import br.com.mecanica.service.InsumoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class InsumoController implements InsumoV1Api {

    private final InsumoService service;

    @Override
    public ResponseEntity<InsumoResponseDTOV1> cadastrarInsumoV1(InsumoRequestDTOV1 insumoRequestDTOV1) {
        var response = InsumoMapper.toResponse(service.criar(InsumoMapper.toDomain(insumoRequestDTOV1)));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<InsumoResponseDTOV1> buscarInsumoV1(UUID id) {
        return ResponseEntity.ok(InsumoMapper.toResponse(service.buscar(id)));
    }

    @Override
    public ResponseEntity<Void> excluirInsumoV1(UUID id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<InsumoResponseDTOV1>> listaInsumosV1() {
        return ResponseEntity.ok(InsumoMapper.toResponse(service.listar()));
    }
}
