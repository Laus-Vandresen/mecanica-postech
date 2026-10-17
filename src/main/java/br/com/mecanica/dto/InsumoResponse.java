package br.com.mecanica.dto;

import br.com.mecanica.entity.InsumoEntity;
import java.math.BigDecimal;

public record InsumoResponse(Long id, String nome, String descricao, BigDecimal valor, Integer quantidadeEstoque, String unidadeMedida) {
    public static InsumoResponse from(InsumoEntity entity) {
        return new InsumoResponse(entity.getId(), entity.getNome(), entity.getDescricao(), entity.getValor(), entity.getQuantidadeEstoque(), entity.getUnidadeMedida());
    }
}
