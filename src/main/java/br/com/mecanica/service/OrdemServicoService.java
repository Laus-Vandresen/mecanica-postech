package br.com.mecanica.service;

import br.com.mecanica.domain.entity.OrdemServico;
import br.com.mecanica.domain.exception.DadosOrdemInvalidosException;
import br.com.mecanica.domain.exception.RegraOrdemServicoException;
import br.com.mecanica.domain.service.OrdemServicoDomainService;
import br.com.mecanica.domain.valueObject.DadosClienteAprovacao;
import br.com.mecanica.domain.valueObject.FilaOrdemServico;
import br.com.mecanica.domain.valueObject.StatusOrdemServico;
import br.com.mecanica.domain.valueObject.TempoMedioFinalizacao;
import br.com.mecanica.entity.ClienteEntity;
import br.com.mecanica.entity.OrdemServicoEntity;
import br.com.mecanica.entity.mapper.OrdemServicoEntityMapper;
import br.com.mecanica.exception.RecursoNaoEncontradoException;
import br.com.mecanica.repository.ClienteRepository;
import br.com.mecanica.repository.OrcamentoRepository;
import br.com.mecanica.repository.OrdemServicoRepository;
import br.com.mecanica.repository.ProdutoRepository;
import br.com.mecanica.repository.ServicoRepository;
import br.com.mecanica.repository.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrdemServicoService {

    private final OrdemServicoRepository repository;
    private final VeiculoRepository veiculoRepository;
    private final ClienteRepository clienteRepository;
    private final ServicoRepository servicoRepository;
    private final ProdutoRepository produtoRepository;
    private final OrcamentoRepository orcamentoRepository;
    private final OrdemServicoDomainService domainService;
    private final Clock clock;

    public TempoMedioFinalizacao calcularTempoMedioFinalizacao() {
        var ordens = repository.findAllByDataFechamentoIsNotNull().stream()
                .map(OrdemServicoEntityMapper::toDomain).toList();
        return domainService.calcularTempoMedioFinalizacao(ordens);
    }

    public List<OrdemServico> listar(UUID cliente, UUID veiculo, StatusOrdemServico status,
            FilaOrdemServico fila, Integer pagina, Integer tamanho) {
        pagina = pagina == null ? 0 : pagina;
        tamanho = tamanho == null ? 20 : tamanho;
        if (pagina < 0 || tamanho < 1 || tamanho > 100) {
            throw new DadosOrdemInvalidosException("Paginacao invalida; tamanho entre 1 e 100");
        }
        var paginacao = PageRequest.of(pagina, tamanho,
                Sort.by(Sort.Order.desc("dataAbertura"), Sort.Order.asc("id")));
        return repository.buscarComFiltros(cliente, veiculo, status == null ? null : status.name(),
                fila == null ? null : fila.name(), paginacao).stream()
                .map(OrdemServicoEntityMapper::toDomain).toList();
    }

    public OrdemServico buscar(UUID id) {
        return OrdemServicoEntityMapper.toDomain(encontrar(id));
    }

    @Transactional
    public OrdemServico criar(UUID veiculo, String descricao, List<UUID> servicos, List<UUID> produtos) {
        var ordem = OrdemServico.abrir(veiculo, descricao, servicos, produtos, agora());
        if (!veiculoRepository.existsById(veiculo)) {
            throw new RecursoNaoEncontradoException("Veiculo nao encontrado");
        }
        validarItens(ordem.getServicoIds(), ordem.getProdutoIds());
        return salvar(ordem, new OrdemServicoEntity());
    }

    @Transactional
    public OrdemServico iniciarDiagnostico(UUID id) {
        return alterar(id, OrdemServico::iniciarDiagnostico);
    }

    @Transactional
    public OrdemServico diagnosticar(UUID id, String diagnostico, List<UUID> servicos, List<UUID> produtos) {
        return alterar(id, ordem -> {
            ordem.registrarDiagnostico(diagnostico, servicos, produtos);
            validarItens(ordem.getServicoIds(), ordem.getProdutoIds());
        });
    }

    @Transactional
    public OrdemServico enviarOrcamento(UUID id, UUID orcamento) {
        return alterar(id, ordem -> ordem.enviarParaAprovacao(orcamento, obterTotalOrcamento(orcamento)));
    }

    @Transactional
    public OrdemServico aprovar(UUID id, UUID cliente, DadosClienteAprovacao dados) {
        return alterar(id, ordem -> {
            ordem.exigirDecisaoPendente();
            if (obterTotalOrcamento(ordem.getOrcamentoId()).compareTo(ordem.getValorTotal()) != 0) {
                throw new RegraOrdemServicoException("Orcamento alterado apos envio; aprovacao bloqueada");
            }
            var clienteId = resolverCliente(cliente, dados);
            ordem.aprovar(clienteId);
            vincularVeiculo(ordem.getVeiculoId(), clienteId);
            // TODO integrar a reserva de pecas e insumos na aprovacao com o modulo de estoque.
        });
    }

    @Transactional
    public OrdemServico recusar(UUID id, String motivo) {
        return alterar(id, ordem -> ordem.recusar(motivo, agora()));
    }

    @Transactional
    public OrdemServico iniciarExecucao(UUID id) {
        return alterar(id, ordem -> {
            ordem.iniciarExecucao();
            // TODO consultar a reserva efetiva no modulo de estoque antes da execucao.
        });
    }

    @Transactional
    public OrdemServico concluirExecucao(UUID id) {
        return alterar(id, ordem -> {
            ordem.concluirExecucao(agora());
            // TODO integrar o registro de consumo com o modulo de estoque.
        });
    }

    @Transactional
    public OrdemServico finalizar(UUID id) {
        return alterar(id, ordem -> ordem.finalizar(agora()));
    }

    @Transactional
    public OrdemServico entregar(UUID id) {
        return alterar(id, OrdemServico::entregar);
    }

    @Transactional
    public OrdemServico cancelar(UUID id, String motivo) {
        return alterar(id, ordem -> {
            ordem.cancelar(motivo, agora());
            // TODO liberar somente reservas nao consumidas no modulo de estoque.
        });
    }

    private OrdemServicoEntity encontrar(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de servico nao encontrada"));
    }

    private OrdemServico alterar(UUID id, Consumer<OrdemServico> operacao) {
        var entity = encontrar(id);
        var ordem = OrdemServicoEntityMapper.toDomain(entity);
        operacao.accept(ordem);
        return salvar(ordem, entity);
    }

    private OrdemServico salvar(OrdemServico ordem, OrdemServicoEntity entity) {
        OrdemServicoEntityMapper.atualizar(ordem, entity);
        entity.setCliente(ordem.getClienteId() == null ? null
                : clienteRepository.getReferenceById(ordem.getClienteId()));
        entity.setVeiculo(veiculoRepository.getReferenceById(ordem.getVeiculoId()));
        entity.setOrcamento(ordem.getOrcamentoId() == null ? null
                : orcamentoRepository.getReferenceById(ordem.getOrcamentoId()));
        entity.setServicos(new ArrayList<>(ordem.getServicoIds().stream()
                .map(servicoRepository::getReferenceById).toList()));
        entity.setProdutos(new ArrayList<>(ordem.getProdutoIds().stream()
                .map(produtoRepository::getReferenceById).toList()));
        return OrdemServicoEntityMapper.toDomain(repository.saveAndFlush(entity));
    }

    private void validarItens(List<UUID> servicos, List<UUID> produtos) {
        if (servicoRepository.findAllById(servicos).size() != servicos.size()) {
            throw new RecursoNaoEncontradoException("Servico nao encontrado ou repetido");
        }
        if (produtoRepository.findAllById(produtos).size() != produtos.size()) {
            throw new RecursoNaoEncontradoException("Produto nao encontrado ou repetido");
        }
    }

    private BigDecimal obterTotalOrcamento(UUID id) {
        return orcamentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Orcamento nao encontrado")).getValor();
    }

    private UUID resolverCliente(UUID id, DadosClienteAprovacao dados) {
        if ((id == null) == (dados == null)) {
            throw new DadosOrdemInvalidosException("Informe clienteId ou dados de um novo cliente, exclusivamente");
        }
        if (id != null) {
            return clienteRepository.findById(id)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente nao encontrado")).getId();
        }
        return clienteRepository.findByCpf(dados.cpf()).orElseGet(() -> {
            var cliente = new ClienteEntity();
            cliente.setNome(dados.nome());
            cliente.setCpf(dados.cpf());
            cliente.setCelular(dados.celular());
            cliente.setEmail(dados.email());
            return clienteRepository.saveAndFlush(cliente);
        }).getId();
    }

    private void vincularVeiculo(UUID id, UUID clienteId) {
        var veiculo = veiculoRepository.buscarParaVincular(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veiculo nao encontrado"));
        if (veiculo.getCliente() != null && !clienteId.equals(veiculo.getCliente().getId())) {
            throw new RegraOrdemServicoException("Veiculo ja vinculado a outro cliente");
        }
        veiculo.setCliente(clienteRepository.getReferenceById(clienteId));
        veiculoRepository.saveAndFlush(veiculo);
    }

    private LocalDateTime agora() {
        return LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
    }
}
