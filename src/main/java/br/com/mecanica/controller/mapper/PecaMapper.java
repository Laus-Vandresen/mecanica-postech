package br.com.mecanica.controller.mapper;

import br.com.mecanica.api.dto.PecaRequestDTOV1;
import br.com.mecanica.api.dto.PecaResponseDTOV1;
import br.com.mecanica.domain.entity.Peca;
import br.com.mecanica.core.modelmapper.ModelMapperConfig;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PecaMapper {

    private static final ModelMapper MODEL_MAPPER = ModelMapperConfig.getModelMapper();

    public static Peca toDomain(PecaRequestDTOV1 request) {
        return MODEL_MAPPER.map(request, Peca.class);
    }

    public static PecaResponseDTOV1 toResponse(Peca peca) {
        return MODEL_MAPPER.map(peca, PecaResponseDTOV1.class);
    }

    public static List<PecaResponseDTOV1> toResponse(List<Peca> pecas) {
        return pecas.stream().map(PecaMapper::toResponse).toList();
    }
}
