package br.com.mecanica.entity.mapper;

import br.com.mecanica.domain.entity.OrdemServico;
import br.com.mecanica.domain.valueObject.EstadoOrdemServico;
import br.com.mecanica.domain.valueObject.ItemProdutoOrdemServico;
import br.com.mecanica.domain.valueObject.StatusOrdemServico;
import br.com.mecanica.entity.OrdemServicoEntity;
import br.com.mecanica.entity.ServicoEntity;
import java.util.List;

public final class OrdemServicoEntityMapper {

    private OrdemServicoEntityMapper() {}

    public static OrdemServico toDomain(OrdemServicoEntity entity) {
        return OrdemServico.restaurar(new EstadoOrdemServico(entity.getId(),
                entity.getCliente() == null ? null : entity.getCliente().getId(), entity.getVeiculo().getId(),
                entity.getOrcamento() == null ? null : entity.getOrcamento().getId(),
                entity.getServicos() == null ? List.of() : entity.getServicos().stream().map(ServicoEntity::getId).toList(),
                entity.getProdutos() == null ? List.of() : entity.getProdutos().stream()
                        .map(item -> new ItemProdutoOrdemServico(item.getProduto().getId(), item.getQuantidade(), item.getQuantidadeConsumida())).toList(),
                entity.getDataAbertura(), entity.getDataFechamento(), StatusOrdemServico.valueOf(entity.getStatus()),
                entity.getDescricao(), entity.getDiagnostico(), entity.getValorTotal(), entity.getMotivoEncerramento(),
                entity.isOrcamentoAprovado(), entity.getDataConclusaoExecucao()));
    }

    public static void atualizar(OrdemServico ordem, OrdemServicoEntity entity) {
        entity.setId(ordem.getId());
        entity.setStatus(ordem.getStatus().name());
        entity.setDescricao(ordem.getDescricao());
        entity.setDiagnostico(ordem.getDiagnostico());
        entity.setDataAbertura(ordem.getDataAbertura());
        entity.setDataFechamento(ordem.getDataFechamento());
        entity.setValorTotal(ordem.getValorTotal());
        entity.setMotivoEncerramento(ordem.getMotivoEncerramento());
        entity.setOrcamentoAprovado(ordem.isOrcamentoAprovado());
        entity.setDataConclusaoExecucao(ordem.getDataConclusaoExecucao());
    }
}
