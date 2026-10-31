package br.com.mecanica.domain.valueObject;

import br.com.mecanica.domain.exception.DadosOrdemInvalidosException;
import java.util.UUID;

public record ItemProdutoOrdemServico(UUID produtoId, int quantidade, int quantidadeConsumida) {

    public ItemProdutoOrdemServico(UUID produtoId, int quantidade) {
        this(produtoId, quantidade, 0);
    }

    public ItemProdutoOrdemServico {
        if (produtoId == null || quantidade <= 0 || quantidadeConsumida < 0 || quantidadeConsumida > quantidade) {
            throw new DadosOrdemInvalidosException("Produto, quantidade positiva e consumo dentro da quantidade da OS sao obrigatorios");
        }
    }
}
