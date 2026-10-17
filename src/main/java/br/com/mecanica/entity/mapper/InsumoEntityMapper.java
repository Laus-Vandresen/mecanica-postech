package br.com.mecanica.entity.mapper;

import br.com.mecanica.core.modelmapper.ModelMapperConfig;
import br.com.mecanica.domain.entity.Insumo;
import br.com.mecanica.entity.InsumoEntity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InsumoEntityMapper {

    private static final ModelMapper MODEL_MAPPER = ModelMapperConfig.getModelMapper();

    public static Insumo toDomain(InsumoEntity entity) {
        return MODEL_MAPPER.map(entity, Insumo.class);
    }

    public static InsumoEntity toEntity(Insumo insumo) {
        return MODEL_MAPPER.map(insumo, InsumoEntity.class);
    }

    public static void updateEntity(Insumo insumo, InsumoEntity entity) {
        var id = entity.getId();
        MODEL_MAPPER.map(insumo, entity);
        entity.setId(id);
    }
}
