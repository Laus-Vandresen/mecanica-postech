package br.com.mecanica.controller;

import br.com.mecanica.api.InsumoV1Api;
import br.com.mecanica.api.dto.InsumoRequestDTOV1;
import br.com.mecanica.api.dto.InsumoResponseDTOV1;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class InsumoController implements InsumoV1Api {

    @Override
    public ResponseEntity<InsumoResponseDTOV1> cadastrarInsumoV1(InsumoRequestDTOV1 insumoRequestDTOV1) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new InsumoResponseDTOV1());
    }

    @Override
    public ResponseEntity<InsumoResponseDTOV1> buscarInsumoV1(Integer id) {
        return ResponseEntity.ok(new InsumoResponseDTOV1());
    }

    @Override
    public ResponseEntity<Void> excluirInsumoV1(Integer id) {
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<InsumoResponseDTOV1>> listaInsumosV1() {
        return ResponseEntity.ok(List.of());
    }
}
