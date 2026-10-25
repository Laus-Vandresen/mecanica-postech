package br.com.mecanica.controller.mapper;

import br.com.mecanica.api.dto.EstoqueResponseDTOV1;
import br.com.mecanica.api.dto.MovimentacaoEstoqueRequestDTOV1;
import br.com.mecanica.domain.valueObject.Estoque;
import br.com.mecanica.domain.valueObject.OperacaoEstoque;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EstoqueMapper {

    public static OperacaoEstoque toDomain(MovimentacaoEstoqueRequestDTOV1.OperacaoEnum operacao) {
        return OperacaoEstoque.valueOf(operacao.getValue());
    }

    public static EstoqueResponseDTOV1 toResponse(Estoque estoque) {
        return new EstoqueResponseDTOV1(estoque.getProdutoId(), estoque.getQuantidade());
    }
}
