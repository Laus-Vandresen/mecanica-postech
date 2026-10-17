package br.com.mecanica.controller;

import br.com.mecanica.api.ClienteV1Api;
import br.com.mecanica.api.dto.ClienteRequestDTOV1;
import br.com.mecanica.api.dto.ClienteResponseDTOV1;
import br.com.mecanica.controller.mapper.ClienteMapper;
import br.com.mecanica.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ClienteController implements ClienteV1Api {

    private final ClienteService service;

    @Override
    public ResponseEntity<ClienteResponseDTOV1> cadastrarClienteV1(ClienteRequestDTOV1 clienteRequestDTOV1) {
        var response = ClienteMapper.toResponse(service.criar(ClienteMapper.toDomain(clienteRequestDTOV1)));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<ClienteResponseDTOV1> buscarClienteV1(Integer id) {
        return ResponseEntity.ok(ClienteMapper.toResponse(service.buscar(id.longValue())));
    }

    @Override
    public ResponseEntity<Void> excluirClienteV1(Integer id) {
        service.excluir(id.longValue());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<ClienteResponseDTOV1>> listaClientesV1() {
        return ResponseEntity.ok(ClienteMapper.toResponse(service.listar()));
    }
}
