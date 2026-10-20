package br.com.mecanica.service;

import java.util.UUID;
import br.com.mecanica.entity.mapper.PecaEntityMapper;
import br.com.mecanica.domain.entity.Peca;
import br.com.mecanica.entity.PecaEntity;
import br.com.mecanica.exception.RecursoNaoEncontradoException;
import br.com.mecanica.repository.PecaRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PecaServiceTest {
    @Mock private PecaRepository repository;
    @InjectMocks private PecaService service;

    @Test
    void criarRetornaIdentificadorGeradoEPreservaCampos() {
        var request = dados(false);
        when(repository.saveAndFlush(any())).thenAnswer(invocation -> {
            PecaEntity salvo = invocation.getArgument(0);
            assertNull(salvo.getId());
            salvo.setId(uuid(42));
            return salvo;
        });
        assertEquals(resposta(uuid(42), request), service.criar(request));
        var captor = ArgumentCaptor.forClass(PecaEntity.class);
        verify(repository).saveAndFlush(captor.capture());
        assertEquals(resposta(uuid(42), request), PecaEntityMapper.toDomain(captor.getValue()));
    }

    @Test
    void buscarRetornaTodosOsCamposDoCadastro() {
        when(repository.findById(uuid(1))).thenReturn(Optional.of(entidade(uuid(1), dados(false))));
        assertEquals(resposta(uuid(1), dados(false)), service.buscar(uuid(1)));
    }

    @Test
    void buscarInexistenteLancaErro() {
        when(repository.findById(uuid(99))).thenReturn(Optional.empty());
        assertThrows(RecursoNaoEncontradoException.class, () -> service.buscar(uuid(99)));
    }

    @Test
    void listarRetornaCadastrosComOrdenacaoPorId() {
        when(repository.findAll(Sort.by("id"))).thenReturn(List.of(
                entidade(uuid(1), dados(false)), entidade(uuid(2), dados(true))));
        assertEquals(List.of(resposta(uuid(1), dados(false)), resposta(uuid(2), dados(true))), service.listar());
    }

    @Test
    void listarSemCadastrosRetornaListaVazia() {
        when(repository.findAll(Sort.by("id"))).thenReturn(List.of());
        assertTrue(service.listar().isEmpty());
    }

    @Test
    void atualizarSubstituiCamposEPreservaIdentificador() {
        var anterior = entidade(uuid(1), dados(false));
        var request = dados(true);
        when(repository.findById(uuid(1))).thenReturn(Optional.of(anterior));
        when(repository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        assertEquals(resposta(uuid(1), request), service.atualizar(uuid(1), request));
        verify(repository).saveAndFlush(same(anterior));
        assertEquals(resposta(uuid(1), request), PecaEntityMapper.toDomain(anterior));
    }

    @Test
    void atualizarInexistenteNaoCriaCadastro() {
        when(repository.findById(uuid(99))).thenReturn(Optional.empty());
        assertThrows(RecursoNaoEncontradoException.class, () -> service.atualizar(uuid(99), dados(true)));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void excluirRemoveCadastroExistenteEConfirmaRestricoes() {
        var entity = entidade(uuid(1), dados(false));
        when(repository.findById(uuid(1))).thenReturn(Optional.of(entity));
        service.excluir(uuid(1));
        var ordem = inOrder(repository);
        ordem.verify(repository).delete(entity);
        ordem.verify(repository).flush();
    }

    @Test
    void excluirInexistenteNaoRemoveOutrosCadastros() {
        when(repository.findById(uuid(99))).thenReturn(Optional.empty());
        assertThrows(RecursoNaoEncontradoException.class, () -> service.excluir(uuid(99)));
        verify(repository, never()).delete(any());
        verify(repository, never()).flush();
    }

    @Test
    void exclusaoComVinculoPropagaConflitoParaTratamentoHttp() {
        when(repository.findById(uuid(1))).thenReturn(Optional.of(entidade(uuid(1), dados(false))));
        var conflito = new DataIntegrityViolationException("Vinculo existente");
        doThrow(conflito).when(repository).flush();
        assertSame(conflito, assertThrows(DataIntegrityViolationException.class, () -> service.excluir(uuid(1))));
    }

    private Peca dados(boolean atualizado) {
        return atualizado ? new Peca(null, "Pastilha", null, BigDecimal.ZERO, 0, null) : new Peca(null, "Filtro", "Filtro de oleo", new BigDecimal("25.50"), 10, "Fabricante");
    }

    private PecaEntity entidade(UUID id, Peca request) {
        var entity = new PecaEntity();
        entity.setId(id);
        entity.setNome(request.getNome());
        entity.setDescricao(request.getDescricao());
        entity.setValor(request.getValor());
        entity.setQuantidadeEstoque(request.getQuantidadeEstoque());
        entity.setFabricante(request.getFabricante());
        return entity;
    }

    private Peca resposta(UUID id, Peca request) {
        return new Peca(id, request.getNome(), request.getDescricao(), request.getValor(), request.getQuantidadeEstoque(), request.getFabricante());
    }
    private static UUID uuid(long value) {
        return new UUID(0L, value);
    }
}
