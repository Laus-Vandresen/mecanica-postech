package br.com.mecanica.service;

import java.util.UUID;
import br.com.mecanica.entity.mapper.ClienteEntityMapper;
import br.com.mecanica.domain.entity.Cliente;
import br.com.mecanica.domain.exception.CpfInvalidoException;
import br.com.mecanica.entity.ClienteEntity;
import br.com.mecanica.exception.RecursoNaoEncontradoException;
import br.com.mecanica.repository.ClienteRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
    private ClienteService service;


    @BeforeEach
    void setUp() {
        service = new ClienteService(repository);
    }

    @Test
    void criarRetornaIdentificadorGeradoEPreservaCampos() {
        var request = dados(false);
        when(repository.saveAndFlush(any())).thenAnswer(invocation -> {
            ClienteEntity salvo = invocation.getArgument(0);
            assertNull(salvo.getId());
            salvo.setId(uuid(42));
            return salvo;
        });
        assertEquals(resposta(uuid(42), request), service.criar(request));
        var captor = ArgumentCaptor.forClass(ClienteEntity.class);
        verify(repository).saveAndFlush(captor.capture());
        assertEquals(resposta(uuid(42), request), ClienteEntityMapper.toDomain(captor.getValue()));
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
        assertEquals(resposta(uuid(1), request), ClienteEntityMapper.toDomain(anterior));
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

    @Test
    void criacaoComCpfInvalidoNaoPersisteCliente() {
        var cliente = dados(false);
        cliente.setCpf("52998224724");
        assertThrows(CpfInvalidoException.class, () -> service.criar(cliente));
        verifyNoInteractions(repository);
    }

    @Test
    void atualizacaoComCpfInvalidoPreservaCadastroAnterior() {
        var anterior = entidade(uuid(1), dados(false));
        var cliente = dados(true);
        cliente.setCpf("11111111111");
        when(repository.findById(uuid(1))).thenReturn(Optional.of(anterior));
        assertThrows(CpfInvalidoException.class, () -> service.atualizar(uuid(1), cliente));
        assertEquals("52998224725", anterior.getCpf());
        assertEquals("Ana", anterior.getNome());
        verify(repository, never()).saveAndFlush(any());
    }

    private Cliente dados(boolean atualizado) {
        return atualizado ? new Cliente(null, "Beatriz", null, null, "11144477735") : new Cliente(null, "Ana", "11999999999", "ana@example.com", "52998224725");
    }

    private ClienteEntity entidade(UUID id, Cliente request) {
        var entity = new ClienteEntity();
        entity.setId(id);
        entity.setNome(request.getNome());
        entity.setCelular(request.getCelular());
        entity.setEmail(request.getEmail());
        entity.setCpf(request.getCpf());
        return entity;
    }

    private Cliente resposta(UUID id, Cliente request) {
        return new Cliente(id, request.getNome(), request.getCelular(), request.getEmail(), request.getCpf());
    }
    private static UUID uuid(long value) {
        return new UUID(0L, value);
    }
}
