package br.com.mecanica.service;

import java.util.UUID;
import br.com.mecanica.domain.valueObject.Estoque;
import br.com.mecanica.domain.valueObject.OperacaoEstoque;
import br.com.mecanica.exception.EstoqueInsuficienteException;
import br.com.mecanica.exception.RecursoNaoEncontradoException;
import br.com.mecanica.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EstoqueService {
    private final ProdutoRepository repository;

    @Transactional
    public Estoque movimentar(UUID produtoId, OperacaoEstoque operacao, int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }

        var quantidadeAtual = repository.movimentarEstoque(produtoId, operacao.aplicarSinal(quantidade))
                .orElseThrow(() -> repository.existsById(produtoId)
                        ? new EstoqueInsuficienteException("Estoque insuficiente para a saida solicitada")
                        : new RecursoNaoEncontradoException("Produto nao encontrado"));
        return new Estoque(produtoId, quantidadeAtual);
    }
}
