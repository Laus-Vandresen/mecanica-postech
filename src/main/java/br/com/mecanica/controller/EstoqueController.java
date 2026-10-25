package br.com.mecanica.controller;

import java.util.UUID;
import br.com.mecanica.api.EstoqueV1Api;
import br.com.mecanica.api.dto.EstoqueResponseDTOV1;
import br.com.mecanica.api.dto.MovimentacaoEstoqueRequestDTOV1;
import br.com.mecanica.controller.mapper.EstoqueMapper;
import br.com.mecanica.service.EstoqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class EstoqueController implements EstoqueV1Api {

    private final EstoqueService service;

    @Override
    public ResponseEntity<EstoqueResponseDTOV1> movimentarEstoqueV1(UUID id, MovimentacaoEstoqueRequestDTOV1 request) {
        var estoque = service.movimentar(id, EstoqueMapper.toDomain(request.getOperacao()), request.getQuantidade());
        return ResponseEntity.ok(EstoqueMapper.toResponse(estoque));
    }
}
