package br.com.mecanica.dto;

import br.com.mecanica.entity.PecaEntity;
import java.math.BigDecimal;

public record PecaResponse(Long id, String nome, String descricao, BigDecimal valor, Integer quantidadeEstoque, String fabricante) {
    public static PecaResponse from(PecaEntity entity) {
        return new PecaResponse(entity.getId(), entity.getNome(), entity.getDescricao(), entity.getValor(), entity.getQuantidadeEstoque(), entity.getFabricante());
    }
}
