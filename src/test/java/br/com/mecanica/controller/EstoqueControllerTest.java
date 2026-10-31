package br.com.mecanica.controller;

import br.com.mecanica.api.dto.MovimentacaoEstoqueRequestDTOV1;
import br.com.mecanica.domain.valueObject.Estoque;
import br.com.mecanica.domain.valueObject.OperacaoEstoque;
import br.com.mecanica.service.EstoqueService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EstoqueControllerTest {
    @Mock private EstoqueService service;

    @Test
    void converteOperacoesDeEntradaESaidaParaDominio() {
        assertEquals(OperacaoEstoque.ENTRADA,
                br.com.mecanica.controller.mapper.EstoqueMapper.toDomain(MovimentacaoEstoqueRequestDTOV1.OperacaoEnum.ENTRADA));
        assertEquals(OperacaoEstoque.SAIDA,
                br.com.mecanica.controller.mapper.EstoqueMapper.toDomain(MovimentacaoEstoqueRequestDTOV1.OperacaoEnum.SAIDA));
    }

    @Test
    void movimentaEstoqueEMapeiaRespostaComIdEQuantidadeAtualizados() {
        var produtoId = UUID.randomUUID();
        var request = new MovimentacaoEstoqueRequestDTOV1(
                MovimentacaoEstoqueRequestDTOV1.OperacaoEnum.ENTRADA, 4);
        when(service.movimentar(produtoId, OperacaoEstoque.ENTRADA, 4))
                .thenReturn(new Estoque(produtoId, 14));

        var response = new EstoqueController(service).movimentarEstoqueV1(produtoId, request);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(produtoId, response.getBody().getProdutoId());
        assertEquals(14, response.getBody().getQuantidadeEstoque());
        verify(service).movimentar(produtoId, OperacaoEstoque.ENTRADA, 4);
    }
}
