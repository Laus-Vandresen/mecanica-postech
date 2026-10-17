package br.com.mecanica.dto;

import br.com.mecanica.entity.ServicoEntity;
import java.math.BigDecimal;

public record ServicoResponse(Long id, String nome, BigDecimal valor) {
    public static ServicoResponse from(ServicoEntity entity) {
        return new ServicoResponse(entity.getId(), entity.getNome(), entity.getValor());
    }
}
