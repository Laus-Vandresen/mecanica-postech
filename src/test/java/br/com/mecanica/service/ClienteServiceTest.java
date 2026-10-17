package br.com.mecanica.service;

import br.com.mecanica.dto.ClienteRequest;
import br.com.mecanica.dto.ClienteResponse;
import br.com.mecanica.entity.ClienteEntity;
import br.com.mecanica.exception.RecursoNaoEncontradoException;
import br.com.mecanica.repository.ClienteRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {
    @Mock private ClienteRepository repository;
    @InjectMocks private ClienteService service;

    @Test
    void criarRetornaIdentificadorGeradoEPreservaCampos() {
        var request = dados(false);
        when(repository.saveAndFlush(any())).thenAnswer(invocation -> {
            ClienteEntity salvo = invocation.getArgument(0);
            assertNull(salvo.getId());
            salvo.setId(42L);
            return salvo;
        });
        assertEquals(resposta(42L, request), service.criar(request));
        var captor = ArgumentCaptor.forClass(ClienteEntity.class);
        verify(repository).saveAndFlush(captor.capture());
        assertEquals(resposta(42L, request), ClienteResponse.from(captor.getValue()));
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
        assertEquals(resposta(1L, request), ClienteResponse.from(anterior));
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

    private ClienteRequest dados(boolean atualizado) {
        return atualizado ? new ClienteRequest("Beatriz", null, null, "11144477735") : new ClienteRequest("Ana", "11999999999", "ana@example.com", "52998224725");
    }

    private ClienteEntity entidade(Long id, ClienteRequest request) {
        var entity = new ClienteEntity();
        entity.setId(id);
        entity.setNome(request.nome());
        entity.setCelular(request.celular());
        entity.setEmail(request.email());
        entity.setCpf(request.cpf());
        return entity;
    }

    private ClienteResponse resposta(Long id, ClienteRequest request) {
        return new ClienteResponse(id, request.nome(), request.celular(), request.email(), request.cpf());
    }
}
