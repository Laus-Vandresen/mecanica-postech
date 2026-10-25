package br.com.mecanica.controller.mapper;

import br.com.mecanica.api.dto.ClienteAprovacaoDTOV1;
import br.com.mecanica.api.dto.OrdemServicoResponseDTOV1;
import br.com.mecanica.api.dto.TempoMedioFinalizacaoResponseDTOV1;
import br.com.mecanica.domain.entity.OrdemServico;
import br.com.mecanica.domain.exception.DadosOrdemInvalidosException;
import br.com.mecanica.domain.valueObject.DadosClienteAprovacao;
import br.com.mecanica.domain.valueObject.FilaOrdemServico;
import br.com.mecanica.domain.valueObject.StatusOrdemServico;
import br.com.mecanica.domain.valueObject.TempoMedioFinalizacao;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrdemServicoMapper {

    public static DadosClienteAprovacao toDomain(ClienteAprovacaoDTOV1 cliente) {
        if (cliente == null) {
            return null;
        }
        return new DadosClienteAprovacao(
                cliente.getNome(), cliente.getCpf(), cliente.getCelular(), cliente.getEmail());
    }

    public static StatusOrdemServico toStatus(String status) {
        if (status == null) {
            return null;
        }
        try {
            return StatusOrdemServico.valueOf(status);
        } catch (IllegalArgumentException ex) {
            throw new DadosOrdemInvalidosException("Status invalido");
        }
    }

    public static FilaOrdemServico toFila(String fila) {
        if (fila == null) {
            return null;
        }
        try {
            return FilaOrdemServico.valueOf(fila);
        } catch (IllegalArgumentException ex) {
            throw new DadosOrdemInvalidosException("Fila invalida");
        }
    }

    public static OrdemServicoResponseDTOV1 toResponse(OrdemServico ordem) {
        var response = new OrdemServicoResponseDTOV1();
        response.setId(ordem.getId());
        response.setClienteId(ordem.getClienteId());
        response.setVeiculoId(ordem.getVeiculoId());
        response.setOrcamentoId(ordem.getOrcamentoId());
        response.setServicoIds(ordem.getServicoIds());
        response.setProdutoIds(ordem.getProdutoIds());
        response.setDataAbertura(data(ordem.getDataAbertura()));
        response.setDataFechamento(data(ordem.getDataFechamento()));
        response.setStatus(ordem.getStatus().name());
        response.setDescricao(ordem.getDescricao());
        response.setDiagnostico(ordem.getDiagnostico());
        response.setValorTotal(ordem.getValorTotal());
        response.setMotivoEncerramento(ordem.getMotivoEncerramento());

        response.setOrcamentoAprovado(ordem.isOrcamentoAprovado());
        response.setDataConclusaoExecucao(data(ordem.getDataConclusaoExecucao()));
        return response;
    }

    public static List<OrdemServicoResponseDTOV1> toResponse(List<OrdemServico> ordens) {
        return ordens.stream().map(OrdemServicoMapper::toResponse).toList();
    }

    public static TempoMedioFinalizacaoResponseDTOV1 toResponse(TempoMedioFinalizacao tempo) {
        return new TempoMedioFinalizacaoResponseDTOV1(
                tempo.getQuantidadeOrdens(), tempo.getTempoMedio().toSeconds(), tempo.getTempoMedio().toString());
    }

    private static OffsetDateTime data(LocalDateTime data) {
        return data == null ? null : data.atOffset(ZoneOffset.UTC);
    }
}
