package br.com.mecanica.controller;

import br.com.mecanica.api.VeiculoV1Api;
import br.com.mecanica.api.dto.VeiculoRequestDTOV1;
import br.com.mecanica.api.dto.VeiculoResponseDTOV1;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VeiculoController implements VeiculoV1Api {

    @Override
    public ResponseEntity<VeiculoResponseDTOV1> cadastrarVeiculoV1(VeiculoRequestDTOV1 veiculoRequestDTOV1) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new VeiculoResponseDTOV1());
    }

    @Override
    public ResponseEntity<VeiculoResponseDTOV1> buscarVeiculoV1(Integer id) {
        return ResponseEntity.ok(new VeiculoResponseDTOV1());
    }

    @Override
    public ResponseEntity<Void> excluirVeiculoV1(Integer id) {
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<VeiculoResponseDTOV1> listaVeiculosV1() {
        return ResponseEntity.ok(new VeiculoResponseDTOV1());
    }
}
