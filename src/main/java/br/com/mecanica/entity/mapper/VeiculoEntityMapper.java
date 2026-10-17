package br.com.mecanica.entity.mapper;

import br.com.mecanica.core.modelmapper.ModelMapperConfig;
import br.com.mecanica.domain.entity.Veiculo;
import br.com.mecanica.entity.VeiculoEntity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class VeiculoEntityMapper {

    private static final ModelMapper MODEL_MAPPER = ModelMapperConfig.getModelMapper();

    public static Veiculo toDomain(VeiculoEntity entity) {
        return MODEL_MAPPER.map(entity, Veiculo.class);
    }

    public static VeiculoEntity toEntity(Veiculo veiculo) {
        return MODEL_MAPPER.map(veiculo, VeiculoEntity.class);
    }

    public static void updateEntity(Veiculo veiculo, VeiculoEntity entity) {
        var id = entity.getId();
        MODEL_MAPPER.map(veiculo, entity);
        entity.setId(id);
    }
}
