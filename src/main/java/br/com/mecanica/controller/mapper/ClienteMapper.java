package br.com.mecanica.controller.mapper;

import br.com.mecanica.api.dto.ClienteRequestDTOV1;
import br.com.mecanica.api.dto.ClienteResponseDTOV1;
import br.com.mecanica.domain.entity.Cliente;
import br.com.mecanica.core.modelmapper.ModelMapperConfig;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ClienteMapper {

    private static final ModelMapper MODEL_MAPPER = ModelMapperConfig.getModelMapper();

    public static Cliente toDomain(ClienteRequestDTOV1 request) {
        return MODEL_MAPPER.map(request, Cliente.class);
    }

    public static ClienteResponseDTOV1 toResponse(Cliente cliente) {
        return MODEL_MAPPER.map(cliente, ClienteResponseDTOV1.class);
    }

    public static List<ClienteResponseDTOV1> toResponse(List<Cliente> clientes) {
        return clientes.stream().map(ClienteMapper::toResponse).toList();
    }
}
