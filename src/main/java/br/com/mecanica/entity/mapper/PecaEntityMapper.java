package br.com.mecanica.entity.mapper;

import br.com.mecanica.core.modelmapper.ModelMapperConfig;
import br.com.mecanica.domain.entity.Peca;
import br.com.mecanica.entity.PecaEntity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PecaEntityMapper {

    private static final ModelMapper MODEL_MAPPER = ModelMapperConfig.getModelMapper();

    public static Peca toDomain(PecaEntity entity) {
        return MODEL_MAPPER.map(entity, Peca.class);
    }

    public static PecaEntity toEntity(Peca peca) {
        return MODEL_MAPPER.map(peca, PecaEntity.class);
    }

    public static void updateEntity(Peca peca, PecaEntity entity) {
        var id = entity.getId();
        MODEL_MAPPER.map(peca, entity);
        entity.setId(id);
    }
}
