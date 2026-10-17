package br.com.mecanica.controller;

import br.com.mecanica.api.ClienteV1Api;
import br.com.mecanica.api.dto.ClienteRequestDTOV1;
import br.com.mecanica.api.dto.ClienteResponseDTOV1;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ClienteController implements ClienteV1Api {

    @Override
    public ResponseEntity<ClienteResponseDTOV1> cadastrarClienteV1(ClienteRequestDTOV1 clienteRequestDTOV1) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ClienteResponseDTOV1());
    }

    @Override
    public ResponseEntity<ClienteResponseDTOV1> buscarClienteV1(Integer id) {
        return ResponseEntity.ok(new ClienteResponseDTOV1());
    }

    @Override
    public ResponseEntity<Void> excluirClienteV1(Integer id) {
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<ClienteResponseDTOV1> listaClientesV1() {
        return ResponseEntity.ok(new ClienteResponseDTOV1());
    }
}
