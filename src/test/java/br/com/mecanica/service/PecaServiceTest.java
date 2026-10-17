package br.com.mecanica.service;

import br.com.mecanica.dto.PecaRequest;
import br.com.mecanica.dto.PecaResponse;
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
            salvo.setId(42L);
            return salvo;
        });
        assertEquals(resposta(42L, request), service.criar(request));
        var captor = ArgumentCaptor.forClass(PecaEntity.class);
        verify(repository).saveAndFlush(captor.capture());
        assertEquals(resposta(42L, request), PecaResponse.from(captor.getValue()));
    }

    @Test
    void buscarRetornaTodosOsCamposDoCadastro() {
        when(repository.findById(1L)).thenReturn(Optional.of(entidade(1L, dados(false))));
        assertEquals(resposta(1L, dados(false)), service.buscar(1L));
    }

    @Test
    void buscarInexistenteLancaErro() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNaoEncontradoException.class, () -> service.buscar(99L));
    }

    @Test
    void listarRetornaCadastrosComOrdenacaoPorId() {
        when(repository.findAll(Sort.by("id"))).thenReturn(List.of(
                entidade(1L, dados(false)), entidade(2L, dados(true))));
        assertEquals(List.of(resposta(1L, dados(false)), resposta(2L, dados(true))), service.listar());
    }

    @Test
    void listarSemCadastrosRetornaListaVazia() {
        when(repository.findAll(Sort.by("id"))).thenReturn(List.of());
        assertTrue(service.listar().isEmpty());
    }

    @Test
    void atualizarSubstituiCamposEPreservaIdentificador() {
        var anterior = entidade(1L, dados(false));
        var request = dados(true);
        when(repository.findById(1L)).thenReturn(Optional.of(anterior));
        when(repository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        assertEquals(resposta(1L, request), service.atualizar(1L, request));
        verify(repository).saveAndFlush(same(anterior));
        assertEquals(resposta(1L, request), PecaResponse.from(anterior));
    }

    @Test
    void atualizarInexistenteNaoCriaCadastro() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNaoEncontradoException.class, () -> service.atualizar(99L, dados(true)));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void excluirRemoveCadastroExistenteEConfirmaRestricoes() {
        var entity = entidade(1L, dados(false));
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        service.excluir(1L);
        var ordem = inOrder(repository);
        ordem.verify(repository).delete(entity);
        ordem.verify(repository).flush();
    }

    @Test
    void excluirInexistenteNaoRemoveOutrosCadastros() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNaoEncontradoException.class, () -> service.excluir(99L));
        verify(repository, never()).delete(any());
        verify(repository, never()).flush();
    }

    @Test
    void exclusaoComVinculoPropagaConflitoParaTratamentoHttp() {
        when(repository.findById(1L)).thenReturn(Optional.of(entidade(1L, dados(false))));
        var conflito = new DataIntegrityViolationException("Vinculo existente");
        doThrow(conflito).when(repository).flush();
        assertSame(conflito, assertThrows(DataIntegrityViolationException.class, () -> service.excluir(1L)));
    }

    private PecaRequest dados(boolean atualizado) {
        return atualizado ? new PecaRequest("Pastilha", null, BigDecimal.ZERO, 0, null) : new PecaRequest("Filtro", "Filtro de oleo", new BigDecimal("25.50"), 10, "Fabricante");
    }

    private PecaEntity entidade(Long id, PecaRequest request) {
        var entity = new PecaEntity();
        entity.setId(id);
        entity.setNome(request.nome());
        entity.setDescricao(request.descricao());
        entity.setValor(request.valor());
        entity.setQuantidadeEstoque(request.quantidadeEstoque());
        entity.setFabricante(request.fabricante());
        return entity;
    }

    private PecaResponse resposta(Long id, PecaRequest request) {
        return new PecaResponse(id, request.nome(), request.descricao(), request.valor(), request.quantidadeEstoque(), request.fabricante());
    }
}
