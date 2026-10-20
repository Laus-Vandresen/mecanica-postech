package br.com.mecanica.controller;

import java.util.UUID;
import br.com.mecanica.api.OrdemServicoV1Api;
import br.com.mecanica.api.dto.OrdemServicoPatchRequestDTOV1;
import br.com.mecanica.api.dto.OrdemServicoRequestDTOV1;
import br.com.mecanica.api.dto.OrdemServicoResponseDTOV1;
import br.com.mecanica.controller.mapper.OrdemServicoMapper;
import br.com.mecanica.service.OrdemServicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OrdemServicoController implements OrdemServicoV1Api {

    private final OrdemServicoService service;

    @Override
    public ResponseEntity<OrdemServicoResponseDTOV1> cadastrarOrdemServicoV1(OrdemServicoRequestDTOV1 request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(OrdemServicoMapper.toResponse(service.criar(OrdemServicoMapper.toDomain(request))));
    }

    @Override
    public ResponseEntity<List<OrdemServicoResponseDTOV1>> listaOrdensServicoV1(UUID clienteId, UUID veiculoId, String status) {
        return ResponseEntity.ok(OrdemServicoMapper.toResponse(service.listar()));
    }

    @Override
    public ResponseEntity<OrdemServicoResponseDTOV1> buscarOrdemServicoV1(UUID id) {
        return ResponseEntity.ok(OrdemServicoMapper.toResponse(service.buscar(id)));
    }

    @Override
    public ResponseEntity<OrdemServicoResponseDTOV1> atualizarOrdemServicoV1(UUID id, OrdemServicoPatchRequestDTOV1 request) {
        return ResponseEntity.ok(OrdemServicoMapper.toResponse(service.atualizar(id, OrdemServicoMapper.toDomain(request))));
    }

    @Override
    public ResponseEntity<OrdemServicoResponseDTOV1> cancelarOrdemServicoV1(UUID id) {
        var existente = service.buscar(id);
        service.excluir(id);
        return ResponseEntity.ok(OrdemServicoMapper.toResponse(existente));
    }
}
