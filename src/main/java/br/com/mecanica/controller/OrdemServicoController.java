package br.com.mecanica.controller;

import br.com.mecanica.api.OrdemServicoV1Api;
import br.com.mecanica.api.dto.OrdemServicoPatchRequestDTOV1;
import br.com.mecanica.api.dto.OrdemServicoRequestDTOV1;
import br.com.mecanica.api.dto.OrdemServicoResponseDTOV1;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class OrdemServicoController implements OrdemServicoV1Api {

    @Override
    public ResponseEntity<OrdemServicoResponseDTOV1> cadastrarOrdemServicoV1(
            OrdemServicoRequestDTOV1 ordemServicoRequestDTOV1) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new OrdemServicoResponseDTOV1());
    }

    @Override
    public ResponseEntity<List<OrdemServicoResponseDTOV1>> listaOrdensServicoV1(
            Integer clienteId, Integer veiculoId, String status) {
        return ResponseEntity.ok(List.of());
    }

    @Override
    public ResponseEntity<OrdemServicoResponseDTOV1> buscarOrdemServicoV1(Integer id) {
        return ResponseEntity.ok(new OrdemServicoResponseDTOV1());
    }

    @Override
    public ResponseEntity<OrdemServicoResponseDTOV1> atualizarOrdemServicoV1(
            Integer id, OrdemServicoPatchRequestDTOV1 ordemServicoPatchRequestDTOV1) {
        return ResponseEntity.ok(new OrdemServicoResponseDTOV1());
    }

    @Override
    public ResponseEntity<OrdemServicoResponseDTOV1> cancelarOrdemServicoV1(Integer id) {
        return ResponseEntity.ok(new OrdemServicoResponseDTOV1());
    }
}
