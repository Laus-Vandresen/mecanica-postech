package br.com.mecanica.service;

import java.util.UUID;
import br.com.mecanica.entity.mapper.VeiculoEntityMapper;
import br.com.mecanica.domain.entity.Veiculo;
import br.com.mecanica.entity.VeiculoEntity;
import br.com.mecanica.entity.ClienteEntity;
import br.com.mecanica.exception.RecursoNaoEncontradoException;
import br.com.mecanica.repository.VeiculoRepository;
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
class VeiculoServiceTest {
    @Mock private VeiculoRepository repository;
    @Mock private ClienteRepository clienteRepository;
    @InjectMocks private VeiculoService service;

    @Test
    void criarRetornaIdentificadorGeradoEPreservaCampos() {
        var request = dados(false);
        var cliente = new ClienteEntity();
        cliente.setId(request.getClienteId());
        when(clienteRepository.findById(request.getClienteId())).thenReturn(Optional.of(cliente));
        when(repository.saveAndFlush(any())).thenAnswer(invocation -> {
            VeiculoEntity salvo = invocation.getArgument(0);
            assertNull(salvo.getId());
            salvo.setId(uuid(42));
            return salvo;
        });
        assertEquals(resposta(uuid(42), request), service.criar(request));
        var captor = ArgumentCaptor.forClass(VeiculoEntity.class);
        verify(repository).saveAndFlush(captor.capture());
        assertEquals(resposta(uuid(42), request), VeiculoEntityMapper.toDomain(captor.getValue()));
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
        var cliente = new ClienteEntity();
        cliente.setId(request.getClienteId());
        when(clienteRepository.findById(request.getClienteId())).thenReturn(Optional.of(cliente));
        when(repository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        assertEquals(resposta(uuid(1), request), service.atualizar(uuid(1), request));
        verify(repository).saveAndFlush(same(anterior));
        assertEquals(resposta(uuid(1), request), VeiculoEntityMapper.toDomain(anterior));
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
    void criarComClienteInexistenteNaoPersisteVeiculo() {
        when(clienteRepository.findById(uuid(7))).thenReturn(Optional.empty());
        assertThrows(RecursoNaoEncontradoException.class, () -> service.criar(dados(false)));
        verifyNoInteractions(repository);
    }

    @Test
    void atualizarComClienteInexistenteNaoSalva() {
        when(repository.findById(uuid(1))).thenReturn(Optional.of(entidade(uuid(1), dados(false))));
        when(clienteRepository.findById(uuid(8))).thenReturn(Optional.empty());
        assertThrows(RecursoNaoEncontradoException.class, () -> service.atualizar(uuid(1), dados(true)));
        verify(repository, never()).saveAndFlush(any());
    }

    private Veiculo dados(boolean atualizado) {
        return atualizado ? new Veiculo(null, "XYZ1234", "Argo", "Fiat", 2024, uuid(8)) : new Veiculo(null, "ABC1D23", "Uno", "Fiat", 2020, uuid(7));
    }

    private VeiculoEntity entidade(UUID id, Veiculo request) {
        var entity = new VeiculoEntity();
        entity.setId(id);
        entity.setPlaca(request.getPlaca());
        entity.setModelo(request.getModelo());
        entity.setMarca(request.getMarca());
        entity.setAno(request.getAno());
        var cliente = new ClienteEntity();
        cliente.setId(request.getClienteId());
        entity.setCliente(cliente);
        return entity;
    }

    private Veiculo resposta(UUID id, Veiculo request) {
        return new Veiculo(id, request.getPlaca(), request.getModelo(), request.getMarca(), request.getAno(), request.getClienteId());
    }
    private static UUID uuid(long value) {
        return new UUID(0L, value);
    }
}
