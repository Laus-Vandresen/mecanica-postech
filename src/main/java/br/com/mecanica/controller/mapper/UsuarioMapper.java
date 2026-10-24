package br.com.mecanica.controller.mapper;

import br.com.mecanica.api.dto.UsuarioRequestDTOV1;
import br.com.mecanica.api.dto.UsuarioResponseDTOV1;
import br.com.mecanica.core.modelmapper.ModelMapperConfig;
import br.com.mecanica.domain.entity.Usuario;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UsuarioMapper {

    private static final ModelMapper MODEL_MAPPER = ModelMapperConfig.getModelMapper();

    public static Usuario toDomain(UsuarioRequestDTOV1 request) {
        return MODEL_MAPPER.map(request, Usuario.class);
    }

    public static UsuarioResponseDTOV1 toResponse(Usuario usuario) {
        return MODEL_MAPPER.map(usuario, UsuarioResponseDTOV1.class);
    }
}
