package br.com.mecanica.service;

import java.util.UUID;
import br.com.mecanica.domain.valueObject.Estoque;
import br.com.mecanica.domain.valueObject.OperacaoEstoque;
import br.com.mecanica.exception.EstoqueInsuficienteException;
import br.com.mecanica.exception.RecursoNaoEncontradoException;
import br.com.mecanica.repository.ProdutoRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EstoqueServiceTest {
    @Mock private ProdutoRepository repository;
    @InjectMocks private EstoqueService service;

    @Test
    void entradaSomaQuantidadeERetornaEstoqueAtual() {
        when(repository.movimentarEstoque(uuid(1), 5)).thenReturn(Optional.of(15));
        assertEquals(new Estoque(uuid(1), 15), service.movimentar(uuid(1), OperacaoEstoque.ENTRADA, 5));
        verify(repository, never()).existsById(any());
    }

    @Test
    void saidaSubtraiQuantidadeERetornaEstoqueAtual() {
        when(repository.movimentarEstoque(uuid(1), -3)).thenReturn(Optional.of(7));
        assertEquals(new Estoque(uuid(1), 7), service.movimentar(uuid(1), OperacaoEstoque.SAIDA, 3));
    }

    @Test
    void saidaMaiorQueEstoqueLancaEstoqueInsuficiente() {
        when(repository.movimentarEstoque(uuid(1), -50)).thenReturn(Optional.empty());
        when(repository.existsById(uuid(1))).thenReturn(true);
        assertThrows(EstoqueInsuficienteException.class, () -> service.movimentar(uuid(1), OperacaoEstoque.SAIDA, 50));
    }

    @Test
    void produtoInexistenteLancaNaoEncontrado() {
        when(repository.movimentarEstoque(uuid(99), 1)).thenReturn(Optional.empty());
        when(repository.existsById(uuid(99))).thenReturn(false);
        assertThrows(RecursoNaoEncontradoException.class, () -> service.movimentar(uuid(99), OperacaoEstoque.ENTRADA, 1));
    }

    @Test
    void quantidadeNaoPositivaNaoMovimentaEstoque() {
        assertThrows(IllegalArgumentException.class, () -> service.movimentar(uuid(1), OperacaoEstoque.ENTRADA, 0));
        verify(repository, never()).movimentarEstoque(any(), anyInt());
    }

    private static UUID uuid(long value) {
        return new UUID(0L, value);
    }
}
