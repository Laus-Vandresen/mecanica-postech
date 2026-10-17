package br.com.mecanica.dto;

import br.com.mecanica.entity.ClienteEntity;

public record ClienteResponse(Long id, String nome, String celular, String email, String cpf) {
    public static ClienteResponse from(ClienteEntity entity) {
        return new ClienteResponse(entity.getId(), entity.getNome(), entity.getCelular(), entity.getEmail(), entity.getCpf());
    }
}
