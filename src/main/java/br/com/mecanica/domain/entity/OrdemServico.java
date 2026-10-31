package br.com.mecanica.domain.entity;

import br.com.mecanica.domain.exception.*;
import br.com.mecanica.domain.valueObject.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public final class OrdemServico {
    private final UUID id;
    private UUID clienteId;
    private final UUID veiculoId;
    private UUID orcamentoId;
    private List<UUID> servicoIds;
    private List<ItemProdutoOrdemServico> produtos;
    private final LocalDateTime dataAbertura;
    private LocalDateTime dataFechamento;
    private StatusOrdemServico status;
    private final String descricao;
    private String diagnostico;
    private BigDecimal valorTotal;
    private String motivoEncerramento;
    private boolean orcamentoAprovado;
    private LocalDateTime dataConclusaoExecucao;

    private OrdemServico(EstadoOrdemServico estado) {
        id = Objects.requireNonNull(estado.id()); clienteId = estado.clienteId();
        veiculoId = Objects.requireNonNull(estado.veiculoId()); orcamentoId = estado.orcamentoId();
        servicoIds = itens(estado.servicoIds()); produtos = produtos(estado.produtos());
        dataAbertura = Objects.requireNonNull(estado.dataAbertura()); dataFechamento = estado.dataFechamento();
        status = Objects.requireNonNull(estado.status()); descricao = estado.descricao(); diagnostico = estado.diagnostico();
        valorTotal = Objects.requireNonNull(estado.valorTotal()); motivoEncerramento = estado.motivoEncerramento();
        orcamentoAprovado = estado.orcamentoAprovado();
        dataConclusaoExecucao = estado.dataConclusaoExecucao();
    }

    public static OrdemServico abrir(UUID veiculoId, String descricao, List<UUID> servicos,
            List<ItemProdutoOrdemServico> produtos, LocalDateTime agora) {
        if (veiculoId == null) throw new DadosOrdemInvalidosException("Veiculo obrigatorio");
        var servicosValidados = itens(servicos);
        var produtosValidados = produtos(produtos);
        var ordem = new OrdemServico(new EstadoOrdemServico(UUID.randomUUID(), null, veiculoId, null,
                servicosValidados, produtosValidados, agora, null, StatusOrdemServico.RECEBIDA,
                texto(descricao, 500, "Descricao"), null, BigDecimal.ZERO, null, false, null));
        return ordem;
    }

    public static OrdemServico restaurar(EstadoOrdemServico estado) { return new OrdemServico(estado); }

    public void iniciarDiagnostico() {
        exigir(StatusOrdemServico.RECEBIDA);
        status = StatusOrdemServico.EM_DIAGNOSTICO;
    }

    public void registrarDiagnostico(String texto, List<UUID> servicos, List<ItemProdutoOrdemServico> produtos) {
        exigir(StatusOrdemServico.EM_DIAGNOSTICO);
        String novoDiagnostico = texto(texto, 1000, "Diagnostico");
        var novosServicos = servicos == null ? servicoIds : itens(servicos);
        var novosProdutos = produtos == null ? this.produtos : produtos(produtos);
        diagnostico = novoDiagnostico; servicoIds = novosServicos; this.produtos = novosProdutos;
    }

    public void enviarParaAprovacao(UUID orcamento, BigDecimal total) {
        exigir(StatusOrdemServico.EM_DIAGNOSTICO);
        if (diagnostico == null) throw new RegraOrdemServicoException("Registre o diagnostico antes do orcamento");
        if (orcamento == null || total == null || total.signum() < 0 || total.scale() > 2 || total.precision() - total.scale() > 10)
            throw new DadosOrdemInvalidosException("Orcamento ou total invalido");
        orcamentoId = orcamento; valorTotal = total;
        status = StatusOrdemServico.AGUARDANDO_APROVACAO;
    }

    public void aprovar(UUID cliente) {
        exigirDecisaoPendente();
        if (cliente == null) throw new DadosOrdemInvalidosException("Cliente obrigatorio na aprovacao");
        clienteId = cliente;
        orcamentoAprovado = true;
    }

    public void recusar(String motivo, LocalDateTime agora) {
        exigirDecisaoPendente();
        validarMomento(agora);
        motivoEncerramento = texto(motivo, 500, "Motivo");
        dataFechamento = agora;
        status = StatusOrdemServico.RECUSADA;
    }

    public void iniciarExecucao() {
        exigir(StatusOrdemServico.AGUARDANDO_APROVACAO);
        if (!orcamentoAprovado || clienteId == null)
            throw new RegraOrdemServicoException("Execucao exige aprovacao e cliente vinculado");
        status = StatusOrdemServico.EM_EXECUCAO;
    }

    public int registrarConsumo(UUID produtoId, int quantidadeConsumida) {
        exigir(StatusOrdemServico.EM_EXECUCAO);
        if (dataConclusaoExecucao != null) {
            throw new RegraOrdemServicoException("Nao e permitido registrar consumo apos concluir a execucao");
        }
        if (quantidadeConsumida <= 0) {
            throw new DadosOrdemInvalidosException("Quantidade consumida deve ser positiva");
        }
        var item = produtos.stream().filter(produto -> produto.produtoId().equals(produtoId)).findFirst()
                .orElseThrow(() -> new DadosOrdemInvalidosException("Produto nao associado a OS"));
        if (quantidadeConsumida < item.quantidadeConsumida() || quantidadeConsumida > item.quantidade()) {
            throw new RegraOrdemServicoException("O total consumido nao pode diminuir nem exceder a quantidade da OS");
        }
        int diferenca = quantidadeConsumida - item.quantidadeConsumida();
        var atualizado = new ItemProdutoOrdemServico(produtoId, item.quantidade(), quantidadeConsumida);
        produtos = produtos.stream().map(produto -> produto.produtoId().equals(produtoId) ? atualizado : produto).toList();
        return diferenca;
    }

    public void concluirExecucao(LocalDateTime agora) {
        exigir(StatusOrdemServico.EM_EXECUCAO);
        validarMomento(agora);
        if (dataConclusaoExecucao != null) throw new RegraOrdemServicoException("Execucao ja concluida");
        dataConclusaoExecucao = agora;
    }

    public void finalizar(LocalDateTime agora) {
        exigir(StatusOrdemServico.EM_EXECUCAO);
        validarMomento(agora);
        if (dataConclusaoExecucao == null) throw new RegraOrdemServicoException("Execucao ainda nao concluida");
        dataFechamento = agora;
        status = StatusOrdemServico.FINALIZADA;
    }

    public void entregar() {
        exigir(StatusOrdemServico.FINALIZADA);
        status = StatusOrdemServico.ENTREGUE;
    }

    public void cancelar(String motivo, LocalDateTime agora) {
        if (!Set.of(StatusOrdemServico.RECEBIDA, StatusOrdemServico.EM_DIAGNOSTICO, StatusOrdemServico.AGUARDANDO_APROVACAO).contains(status))
            throw new RegraOrdemServicoException("Cancelamento permitido apenas antes da execucao");
        validarMomento(agora); motivoEncerramento = texto(motivo, 500, "Motivo"); dataFechamento = agora;
        status = StatusOrdemServico.CANCELADA;
    }

    public void exigirDecisaoPendente() {
        exigir(StatusOrdemServico.AGUARDANDO_APROVACAO);
        if (orcamentoAprovado) throw new RegraOrdemServicoException("Orcamento ja aprovado");
    }

    private void exigir(StatusOrdemServico esperado) {
        if (status != esperado) throw new RegraOrdemServicoException("Operacao nao permitida no estado " + status);
    }
    private void validarMomento(LocalDateTime agora) {
        if (agora == null) throw new DadosOrdemInvalidosException("Data obrigatoria");
        if (agora.isBefore(dataAbertura) || (dataConclusaoExecucao != null && agora.isBefore(dataConclusaoExecucao)))
            throw new RegraOrdemServicoException("Data anterior ao atendimento ou a conclusao da execucao");
    }
    private static List<UUID> itens(List<UUID> valores) {
        if (valores == null) return List.of();
        if (valores.stream().anyMatch(Objects::isNull) || new HashSet<>(valores).size() != valores.size())
            throw new DadosOrdemInvalidosException("Itens nulos ou repetidos");
        return List.copyOf(valores);
    }
    private static List<ItemProdutoOrdemServico> produtos(List<ItemProdutoOrdemServico> valores) {
        if (valores == null) return List.of();
        if (valores.stream().anyMatch(Objects::isNull)
                || valores.stream().map(ItemProdutoOrdemServico::produtoId).distinct().count() != valores.size()) {
            throw new DadosOrdemInvalidosException("Produtos nulos ou repetidos");
        }
        return List.copyOf(valores);
    }
    private static String texto(String valor, int maximo, String campo) {
        if (valor == null || valor.isBlank() || valor.strip().length() > maximo)
            throw new DadosOrdemInvalidosException(campo + " obrigatorio, limite " + maximo);
        return valor.strip();
    }
    public EstadoOrdemServico estado() {
        return new EstadoOrdemServico(id, clienteId, veiculoId, orcamentoId, servicoIds, produtos,
                dataAbertura, dataFechamento, status, descricao, diagnostico, valorTotal, motivoEncerramento, orcamentoAprovado, dataConclusaoExecucao);
    }

    public UUID getId() { return id; }
    public UUID getClienteId() { return clienteId; }
    public UUID getVeiculoId() { return veiculoId; }
    public UUID getOrcamentoId() { return orcamentoId; }
    public List<UUID> getServicoIds() { return servicoIds; }
    public List<UUID> getProdutoIds() { return produtos.stream().map(ItemProdutoOrdemServico::produtoId).toList(); }
    public List<ItemProdutoOrdemServico> getProdutos() { return produtos; }
    public LocalDateTime getDataAbertura() { return dataAbertura; }
    public LocalDateTime getDataFechamento() { return dataFechamento; }
    public StatusOrdemServico getStatus() { return status; }
    public String getDescricao() { return descricao; }
    public String getDiagnostico() { return diagnostico; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public String getMotivoEncerramento() { return motivoEncerramento; }
    public boolean isOrcamentoAprovado() { return orcamentoAprovado; }
    public LocalDateTime getDataConclusaoExecucao() { return dataConclusaoExecucao; }

    public Optional<Duration> tempoDeFinalizacao() {
        if (dataFechamento == null || dataFechamento.isBefore(dataAbertura)) return Optional.empty();
        return Optional.of(Duration.between(dataAbertura, dataFechamento));
    }
}
