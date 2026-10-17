package br.com.mecanica.controller;

import br.com.mecanica.api.PecaV1Api;
import br.com.mecanica.api.dto.PecaRequestDTOV1;
import br.com.mecanica.api.dto.PecaResponseDTOV1;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PecaController implements PecaV1Api {

    @Override
    public ResponseEntity<PecaResponseDTOV1> cadastrarPecaV1(PecaRequestDTOV1 pecaRequestDTOV1) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new PecaResponseDTOV1());
    }

    @Override
    public ResponseEntity<PecaResponseDTOV1> buscarPecaV1(Integer id) {
        return ResponseEntity.ok(new PecaResponseDTOV1());
    }

    @Override
    public ResponseEntity<Void> excluirPecaV1(Integer id) {
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<PecaResponseDTOV1>> listaPecasV1() {
        return ResponseEntity.ok(List.of());
    }
}
