package br.com.mecanica.domain.entity;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class OrdemServico {

    private UUID id;
    private UUID clienteId;
    private UUID veiculoId;
    private UUID orcamentoId;
    private List<UUID> servicoIds;
    private List<UUID> produtoIds;
    private LocalDateTime dataAbertura;
    private LocalDateTime dataFechamento;
    private String status;
    private String descricao;
    private String diagnostico;
    private BigDecimal valorTotal;

    public Optional<Duration> tempoDeFinalizacao() {
        if (dataAbertura == null || dataFechamento == null || dataFechamento.isBefore(dataAbertura)) {
            return Optional.empty();
        }
        return Optional.of(Duration.between(dataAbertura, dataFechamento));
    }
}
