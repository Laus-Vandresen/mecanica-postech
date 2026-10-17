package br.com.mecanica.controller.mapper;

import br.com.mecanica.api.dto.VeiculoRequestDTOV1;
import br.com.mecanica.api.dto.VeiculoResponseDTOV1;
import br.com.mecanica.domain.entity.Veiculo;
import br.com.mecanica.core.modelmapper.ModelMapperConfig;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class VeiculoMapper {

    private static final ModelMapper MODEL_MAPPER = ModelMapperConfig.getModelMapper();

    public static Veiculo toDomain(VeiculoRequestDTOV1 request) {
        return MODEL_MAPPER.map(request, Veiculo.class);
    }

    public static VeiculoResponseDTOV1 toResponse(Veiculo veiculo) {
        return MODEL_MAPPER.map(veiculo, VeiculoResponseDTOV1.class);
    }

    public static List<VeiculoResponseDTOV1> toResponse(List<Veiculo> veiculos) {
        return veiculos.stream().map(VeiculoMapper::toResponse).toList();
    }
}
