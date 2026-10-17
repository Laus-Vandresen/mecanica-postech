package br.com.mecanica.dto;

import br.com.mecanica.entity.VeiculoEntity;

public record VeiculoResponse(Long id, String placa, String modelo, String marca, Integer ano, Long clienteId) {
    public static VeiculoResponse from(VeiculoEntity entity) {
        return new VeiculoResponse(entity.getId(), entity.getPlaca(), entity.getModelo(), entity.getMarca(), entity.getAno(), entity.getCliente().getId());
    }
}
