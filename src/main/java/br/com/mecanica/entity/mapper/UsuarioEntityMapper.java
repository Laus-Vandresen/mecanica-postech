package br.com.mecanica.entity.mapper;

import br.com.mecanica.core.modelmapper.ModelMapperConfig;
import br.com.mecanica.domain.entity.Usuario;
import br.com.mecanica.entity.UsuarioEntity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UsuarioEntityMapper {

    private static final ModelMapper MODEL_MAPPER = ModelMapperConfig.getModelMapper();

    public static Usuario toDomain(UsuarioEntity entity) {
        return MODEL_MAPPER.map(entity, Usuario.class);
    }

    public static UsuarioEntity toEntity(Usuario usuario) {
        return MODEL_MAPPER.map(usuario, UsuarioEntity.class);
    }
}
