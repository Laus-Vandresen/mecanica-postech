package br.com.mecanica.controller;

import java.util.UUID;
import br.com.mecanica.api.VeiculoV1Api;
import br.com.mecanica.api.dto.VeiculoRequestDTOV1;
import br.com.mecanica.api.dto.VeiculoResponseDTOV1;
import br.com.mecanica.controller.mapper.VeiculoMapper;
import br.com.mecanica.service.VeiculoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class VeiculoController implements VeiculoV1Api {

    private final VeiculoService service;

    @Override
    public ResponseEntity<VeiculoResponseDTOV1> cadastrarVeiculoV1(VeiculoRequestDTOV1 veiculoRequestDTOV1) {
        var response = VeiculoMapper.toResponse(service.criar(VeiculoMapper.toDomain(veiculoRequestDTOV1)));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<VeiculoResponseDTOV1> buscarVeiculoV1(UUID id) {
        return ResponseEntity.ok(VeiculoMapper.toResponse(service.buscar(id)));
    }

    @Override
    public ResponseEntity<Void> excluirVeiculoV1(UUID id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<VeiculoResponseDTOV1>> listaVeiculosV1() {
        return ResponseEntity.ok(VeiculoMapper.toResponse(service.listar()));
    }
}
