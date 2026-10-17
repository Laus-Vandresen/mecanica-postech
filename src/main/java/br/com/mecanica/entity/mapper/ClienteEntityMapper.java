package br.com.mecanica.entity.mapper;

import br.com.mecanica.core.modelmapper.ModelMapperConfig;
import br.com.mecanica.domain.entity.Cliente;
import br.com.mecanica.entity.ClienteEntity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ClienteEntityMapper {

    private static final ModelMapper MODEL_MAPPER = ModelMapperConfig.getModelMapper();

    public static Cliente toDomain(ClienteEntity entity) {
        return MODEL_MAPPER.map(entity, Cliente.class);
    }

    public static ClienteEntity toEntity(Cliente cliente) {
        return MODEL_MAPPER.map(cliente, ClienteEntity.class);
    }

    public static void updateEntity(Cliente cliente, ClienteEntity entity) {
        var id = entity.getId();
        MODEL_MAPPER.map(cliente, entity);
        entity.setId(id);
    }
}
