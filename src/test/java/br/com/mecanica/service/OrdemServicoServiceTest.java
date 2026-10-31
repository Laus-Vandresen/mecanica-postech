package br.com.mecanica.service;

import br.com.mecanica.domain.entity.OrdemServico;
import br.com.mecanica.domain.exception.DadosOrdemInvalidosException;
import br.com.mecanica.domain.exception.RegraOrdemServicoException;
import br.com.mecanica.domain.service.OrdemServicoDomainService;
import br.com.mecanica.domain.valueObject.DadosClienteAprovacao;
import br.com.mecanica.domain.valueObject.EstadoOrdemServico;
import br.com.mecanica.domain.valueObject.FilaOrdemServico;
import br.com.mecanica.domain.valueObject.StatusOrdemServico;
import br.com.mecanica.entity.ClienteEntity;
import br.com.mecanica.entity.OrcamentoEntity;
import br.com.mecanica.entity.OrdemServicoEntity;
import br.com.mecanica.entity.ProdutoEntity;
import br.com.mecanica.entity.ServicoEntity;
import br.com.mecanica.entity.VeiculoEntity;
import br.com.mecanica.exception.RecursoNaoEncontradoException;
import br.com.mecanica.repository.ClienteRepository;
import br.com.mecanica.repository.OrcamentoRepository;
import br.com.mecanica.repository.OrdemServicoRepository;
import br.com.mecanica.repository.ProdutoRepository;
import br.com.mecanica.repository.ServicoRepository;
import br.com.mecanica.repository.VeiculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrdemServicoServiceTest {
    private static final UUID OS_ID = uuid(1);
    private static final UUID VEICULO_ID = uuid(2);
    private static final UUID CLIENTE_ID = uuid(3);
    private static final UUID ORCAMENTO_ID = uuid(4);
    private static final UUID SERVICO_ID = uuid(5);
    private static final UUID PRODUTO_ID = uuid(6);
    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 5, 10, 12, 0);

    @Mock private OrdemServicoRepository repository;
    @Mock private VeiculoRepository veiculoRepository;
    @Mock private ClienteRepository clienteRepository;
    @Mock private ServicoRepository servicoRepository;
    @Mock private ProdutoRepository produtoRepository;
    @Mock private OrcamentoRepository orcamentoRepository;
    @Mock private OrdemServicoDomainService domainService;

    private OrdemServicoService service;
    private VeiculoEntity veiculoRef;

    @BeforeEach
    void setUp() {
        var clock = Clock.fixed(Instant.parse("2026-05-10T12:00:00Z"), ZoneOffset.UTC);
        service = new OrdemServicoService(repository, veiculoRepository, clienteRepository,
                servicoRepository, produtoRepository, orcamentoRepository, domainService, clock);
        veiculoRef = new VeiculoEntity();
        veiculoRef.setId(VEICULO_ID);
        lenient().when(veiculoRepository.getReferenceById(VEICULO_ID)).thenReturn(veiculoRef);
    }

    @Test
    void criarPersisteOrdemComHorarioUtcEItensValidados() {
        when(veiculoRepository.existsById(VEICULO_ID)).thenReturn(true);
        when(servicoRepository.findAllById(List.of(SERVICO_ID))).thenReturn(List.of(servico(SERVICO_ID)));
        when(produtoRepository.findAllById(List.of(PRODUTO_ID))).thenReturn(List.of(produto(PRODUTO_ID)));
        when(servicoRepository.getReferenceById(SERVICO_ID)).thenReturn(servico(SERVICO_ID));
        when(produtoRepository.getReferenceById(PRODUTO_ID)).thenReturn(produto(PRODUTO_ID));
        when(repository.saveAndFlush(any())).thenAnswer(invocation -> {
            OrdemServicoEntity salvo = invocation.getArgument(0);
            salvo.setId(OS_ID);
            return salvo;
        });

        var criada = service.criar(VEICULO_ID, "Ruído na suspensão", List.of(SERVICO_ID), List.of(PRODUTO_ID));

        assertEquals(OS_ID, criada.getId());
        assertEquals(AGORA, criada.getDataAbertura());
        assertEquals(StatusOrdemServico.RECEBIDA, criada.getStatus());
        assertEquals(List.of(SERVICO_ID), criada.getServicoIds());
        assertEquals(List.of(PRODUTO_ID), criada.getProdutoIds());
        verify(veiculoRepository).existsById(VEICULO_ID);
    }

    @Test
    void criarVeiculoInexistenteNaoValidaItensNemPersiste() {
        when(veiculoRepository.existsById(VEICULO_ID)).thenReturn(false);

        assertThrows(RecursoNaoEncontradoException.class,
                () -> service.criar(VEICULO_ID, "Ruído", List.of(), List.of()));

        verifyNoInteractions(servicoRepository, produtoRepository, repository);
    }

    @Test
    void criarComServicoOuProdutoInexistenteNaoPersisteOrdem() {
        when(veiculoRepository.existsById(VEICULO_ID)).thenReturn(true);
        when(servicoRepository.findAllById(List.of(SERVICO_ID))).thenReturn(List.of());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> service.criar(VEICULO_ID, "Ruído", List.of(SERVICO_ID), List.of()));

        verifyNoInteractions(produtoRepository, repository);
    }

    @Test
    void listarUsaFiltrosOrdenacaoEPaginacaoPadraoQuandoOmitidos() {
        when(repository.buscarComFiltros(isNull(), isNull(), isNull(), isNull(), any()))
                .thenReturn(List.of());

        assertTrue(service.listar(null, null, null, null, null, null).isEmpty());

        var captor = ArgumentCaptor.forClass(org.springframework.data.domain.Pageable.class);
        verify(repository).buscarComFiltros(isNull(), isNull(), isNull(), isNull(), captor.capture());
        assertEquals(0, captor.getValue().getPageNumber());
        assertEquals(20, captor.getValue().getPageSize());
        assertEquals(Sort.by(Sort.Order.desc("dataAbertura"), Sort.Order.asc("id")), captor.getValue().getSort());
    }

    @Test
    void listarEncaminhaFiltrosERejeitaPaginacaoForaDosLimites() {
        when(repository.buscarComFiltros(eq(CLIENTE_ID), eq(VEICULO_ID), eq("FINALIZADA"), eq("FINALIZACAO"), any()))
                .thenReturn(List.of());
        assertTrue(service.listar(CLIENTE_ID, VEICULO_ID, StatusOrdemServico.FINALIZADA,
                FilaOrdemServico.FINALIZACAO, 2, 100).isEmpty());

        assertThrows(DadosOrdemInvalidosException.class,
                () -> service.listar(null, null, null, null, -1, 20));
        assertThrows(DadosOrdemInvalidosException.class,
                () -> service.listar(null, null, null, null, 0, 0));
        assertThrows(DadosOrdemInvalidosException.class,
                () -> service.listar(null, null, null, null, 0, 101));
    }

    @Test
    void buscarOrdemInexistenteLancaRecursoNaoEncontrado() {
        when(repository.findById(OS_ID)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.buscar(OS_ID));
    }

    @Test
    void aprovarComClienteExistenteVinculaVeiculoEAuditaOrcamento() {
        var entity = ordemAguardandoAprovacao();
        var cliente = cliente(CLIENTE_ID);
        var veiculo = new VeiculoEntity();
        veiculo.setId(VEICULO_ID);
        when(repository.findById(OS_ID)).thenReturn(Optional.of(entity));
        when(orcamentoRepository.findById(ORCAMENTO_ID)).thenReturn(Optional.of(orcamento("50.00")));
        when(clienteRepository.findById(CLIENTE_ID)).thenReturn(Optional.of(cliente));
        when(veiculoRepository.buscarParaVincular(VEICULO_ID)).thenReturn(Optional.of(veiculo));
        when(clienteRepository.getReferenceById(CLIENTE_ID)).thenReturn(cliente);
        when(repository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var aprovada = service.aprovar(OS_ID, CLIENTE_ID, null);

        assertEquals(CLIENTE_ID, aprovada.getClienteId());
        assertTrue(aprovada.isOrcamentoAprovado());
        assertSame(cliente, veiculo.getCliente());
        verify(veiculoRepository).saveAndFlush(veiculo);
        verify(clienteRepository, never()).saveAndFlush(any());
    }

    @Test
    void aprovarCadastraClienteNovoSomenteQuandoCpfAindaNaoExiste() {
        var entity = ordemAguardandoAprovacao();
        var veiculo = new VeiculoEntity();
        veiculo.setId(VEICULO_ID);
        var dados = new DadosClienteAprovacao(" Ana ", "52998224725", "11999999999", "ana@example.com");
        when(repository.findById(OS_ID)).thenReturn(Optional.of(entity));
        when(orcamentoRepository.findById(ORCAMENTO_ID)).thenReturn(Optional.of(orcamento("50.00")));
        when(clienteRepository.findByCpf(dados.cpf())).thenReturn(Optional.empty());
        when(clienteRepository.saveAndFlush(any())).thenAnswer(invocation -> {
            ClienteEntity novo = invocation.getArgument(0);
            novo.setId(CLIENTE_ID);
            return novo;
        });
        when(clienteRepository.getReferenceById(CLIENTE_ID)).thenReturn(cliente(CLIENTE_ID));
        when(veiculoRepository.buscarParaVincular(VEICULO_ID)).thenReturn(Optional.of(veiculo));
        when(repository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var aprovada = service.aprovar(OS_ID, null, dados);

        assertEquals(CLIENTE_ID, aprovada.getClienteId());
        var captor = ArgumentCaptor.forClass(ClienteEntity.class);
        verify(clienteRepository).saveAndFlush(captor.capture());
        assertEquals("Ana", captor.getValue().getNome());
        assertEquals(dados.cpf(), captor.getValue().getCpf());
        assertEquals(dados.email(), captor.getValue().getEmail());
    }

    @Test
    void aprovarBloqueiaOrcamentoAlteradoAntesDeCriarOuVincularCliente() {
        when(repository.findById(OS_ID)).thenReturn(Optional.of(ordemAguardandoAprovacao()));
        when(orcamentoRepository.findById(ORCAMENTO_ID)).thenReturn(Optional.of(orcamento("51.00")));

        assertThrows(RegraOrdemServicoException.class, () -> service.aprovar(OS_ID, CLIENTE_ID, null));

        verifyNoInteractions(clienteRepository);
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void aprovarBloqueiaVeiculoVinculadoAOutroCliente() {
        var entity = ordemAguardandoAprovacao();
        var outroCliente = cliente(uuid(99));
        var veiculo = new VeiculoEntity();
        veiculo.setId(VEICULO_ID);
        veiculo.setCliente(outroCliente);
        when(repository.findById(OS_ID)).thenReturn(Optional.of(entity));
        when(orcamentoRepository.findById(ORCAMENTO_ID)).thenReturn(Optional.of(orcamento("50.00")));
        when(clienteRepository.findById(CLIENTE_ID)).thenReturn(Optional.of(cliente(CLIENTE_ID)));
        when(veiculoRepository.buscarParaVincular(VEICULO_ID)).thenReturn(Optional.of(veiculo));

        assertThrows(RegraOrdemServicoException.class, () -> service.aprovar(OS_ID, CLIENTE_ID, null));

        verify(veiculoRepository, never()).saveAndFlush(any());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void percorreFluxoPrincipalESeusEncerramentosAlternativos() {
        var entity = ordemBase(StatusOrdemServico.RECEBIDA, AGORA.minusHours(1));
        var orcamento = orcamento("50.00");
        orcamento.setId(ORCAMENTO_ID);
        var cliente = cliente(CLIENTE_ID);
        when(repository.findById(OS_ID)).thenReturn(Optional.of(entity));
        when(repository.findById(uuid(7))).thenReturn(Optional.of(ordemBase(StatusOrdemServico.RECEBIDA, AGORA.minusHours(1))));
        when(repository.findById(uuid(8))).thenReturn(Optional.of(ordemAguardandoAprovacao()));
        when(repository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(servicoRepository.findAllById(List.of())).thenReturn(List.of());
        when(produtoRepository.findAllById(List.of())).thenReturn(List.of());
        when(orcamentoRepository.findById(ORCAMENTO_ID)).thenReturn(Optional.of(orcamento));
        when(orcamentoRepository.getReferenceById(ORCAMENTO_ID)).thenReturn(orcamento);
        when(clienteRepository.findById(CLIENTE_ID)).thenReturn(Optional.of(cliente));
        when(clienteRepository.getReferenceById(CLIENTE_ID)).thenReturn(cliente);
        when(veiculoRepository.buscarParaVincular(VEICULO_ID)).thenReturn(Optional.of(veiculoRef));

        service.iniciarDiagnostico(OS_ID);
        service.diagnosticar(OS_ID, "Trocar filtro", null, null);
        service.enviarOrcamento(OS_ID, ORCAMENTO_ID);
        assertTrue(service.aprovar(OS_ID, CLIENTE_ID, null).isOrcamentoAprovado());
        service.iniciarExecucao(OS_ID);
        assertEquals(StatusOrdemServico.EM_EXECUCAO, service.concluirExecucao(OS_ID).getStatus());
        assertEquals(StatusOrdemServico.FINALIZADA, service.finalizar(OS_ID).getStatus());
        assertEquals(StatusOrdemServico.ENTREGUE, service.entregar(OS_ID).getStatus());

        assertEquals(StatusOrdemServico.CANCELADA, service.cancelar(uuid(7), "Desistência").getStatus());
        assertEquals(StatusOrdemServico.RECUSADA, service.recusar(uuid(8), "Não aprovado").getStatus());
        verify(repository, times(10)).saveAndFlush(any());
    }

    @Test
    void enviarOrcamentoInexistenteNaoSalvaOrdem() {
        var entity = ordemEmDiagnostico();
        when(repository.findById(OS_ID)).thenReturn(Optional.of(entity));
        when(orcamentoRepository.findById(ORCAMENTO_ID)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.enviarOrcamento(OS_ID, ORCAMENTO_ID));

        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void diagnosticoComItemAusenteNaoPersisteAlteracao() {
        when(repository.findById(OS_ID)).thenReturn(Optional.of(ordemEmDiagnostico()));
        when(servicoRepository.findAllById(List.of(SERVICO_ID))).thenReturn(List.of());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> service.diagnosticar(OS_ID, "Trocar peça", List.of(SERVICO_ID), List.of()));

        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void calcularTempoMedioConsideraOrdensComFechamento() {
        var entity = ordemFinalizada();
        when(repository.findAllByDataFechamentoIsNotNull()).thenReturn(List.of(entity));
        var esperado = new br.com.mecanica.domain.valueObject.TempoMedioFinalizacao(1, java.time.Duration.ofHours(2));
        when(domainService.calcularTempoMedioFinalizacao(anyList())).thenReturn(esperado);

        assertSame(esperado, service.calcularTempoMedioFinalizacao());
        verify(domainService).calcularTempoMedioFinalizacao(argThat(ordens -> ordens.size() == 1));
    }

    private static OrdemServicoEntity ordemAguardandoAprovacao() {
        var entity = ordemBase(StatusOrdemServico.AGUARDANDO_APROVACAO, AGORA.minusHours(1));
        var orcamento = orcamento("50.00");
        orcamento.setId(ORCAMENTO_ID);
        entity.setOrcamento(orcamento);
        entity.setDiagnostico("Trocar filtro");
        entity.setValorTotal(new BigDecimal("50.00"));
        return entity;
    }

    private static OrdemServicoEntity ordemEmDiagnostico() {
        var entity = ordemBase(StatusOrdemServico.EM_DIAGNOSTICO, AGORA.minusHours(1));
        entity.setDiagnostico("Inspecionar veículo");
        return entity;
    }

    private static OrdemServicoEntity ordemFinalizada() {
        var entity = ordemBase(StatusOrdemServico.FINALIZADA, AGORA.minusHours(2));
        entity.setDataFechamento(AGORA);
        return entity;
    }

    private static OrdemServicoEntity ordemBase(StatusOrdemServico status, LocalDateTime abertura) {
        var entity = new OrdemServicoEntity();
        entity.setId(OS_ID);
        var veiculo = new VeiculoEntity();
        veiculo.setId(VEICULO_ID);
        entity.setVeiculo(veiculo);
        entity.setStatus(status.name());
        entity.setDataAbertura(abertura);
        entity.setDescricao("Ruído na suspensão");
        entity.setValorTotal(BigDecimal.ZERO);
        entity.setServicos(List.of());
        entity.setProdutos(List.of());
        return entity;
    }

    private static OrcamentoEntity orcamento(String valor) {
        var entity = new OrcamentoEntity();
        entity.setValor(new BigDecimal(valor));
        return entity;
    }

    private static ClienteEntity cliente(UUID id) {
        var entity = new ClienteEntity();
        entity.setId(id);
        entity.setNome("Ana");
        entity.setCpf("52998224725");
        return entity;
    }

    private static ServicoEntity servico(UUID id) {
        var entity = new ServicoEntity();
        entity.setId(id);
        return entity;
    }

    private static ProdutoEntity produto(UUID id) {
        var entity = new ProdutoEntity();
        entity.setId(id);
        return entity;
    }

    private static UUID uuid(long value) {
        return new UUID(0, value);
    }
}
