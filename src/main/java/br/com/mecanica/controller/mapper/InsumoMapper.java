package br.com.mecanica.controller.mapper;

import br.com.mecanica.api.dto.InsumoRequestDTOV1;
import br.com.mecanica.api.dto.InsumoResponseDTOV1;
import br.com.mecanica.domain.entity.Insumo;
import br.com.mecanica.core.modelmapper.ModelMapperConfig;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InsumoMapper {

    private static final ModelMapper MODEL_MAPPER = ModelMapperConfig.getModelMapper();

    public static Insumo toDomain(InsumoRequestDTOV1 request) {
        return MODEL_MAPPER.map(request, Insumo.class);
    }

    public static InsumoResponseDTOV1 toResponse(Insumo insumo) {
        return MODEL_MAPPER.map(insumo, InsumoResponseDTOV1.class);
    }

    public static List<InsumoResponseDTOV1> toResponse(List<Insumo> insumos) {
        return insumos.stream().map(InsumoMapper::toResponse).toList();
    }
}
