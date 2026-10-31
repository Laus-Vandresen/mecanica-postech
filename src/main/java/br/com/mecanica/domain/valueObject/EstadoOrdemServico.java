package br.com.mecanica.domain.valueObject;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record EstadoOrdemServico(UUID id, UUID clienteId, UUID veiculoId, UUID orcamentoId,
        List<UUID> servicoIds, List<ItemProdutoOrdemServico> produtos, LocalDateTime dataAbertura,
        LocalDateTime dataFechamento, StatusOrdemServico status, String descricao,
        String diagnostico, BigDecimal valorTotal, String motivoEncerramento,
        boolean orcamentoAprovado, LocalDateTime dataConclusaoExecucao) {

    public EstadoOrdemServico {
        servicoIds = servicoIds == null ? List.of() : List.copyOf(servicoIds);
        produtos = produtos == null ? List.of() : List.copyOf(produtos);
    }
}
