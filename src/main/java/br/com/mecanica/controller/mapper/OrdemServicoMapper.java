package br.com.mecanica.controller.mapper;

import br.com.mecanica.api.dto.OrdemServicoPatchRequestDTOV1;
import br.com.mecanica.api.dto.OrdemServicoRequestDTOV1;
import br.com.mecanica.api.dto.OrdemServicoResponseDTOV1;
import br.com.mecanica.domain.entity.OrdemServico;
import br.com.mecanica.core.modelmapper.ModelMapperConfig;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrdemServicoMapper {

    private static final ModelMapper MODEL_MAPPER = ModelMapperConfig.getModelMapper();

    public static OrdemServico toDomain(OrdemServicoRequestDTOV1 request) {
        return MODEL_MAPPER.map(request, OrdemServico.class);
    }

    public static OrdemServico toDomain(OrdemServicoPatchRequestDTOV1 request) {
        return MODEL_MAPPER.map(request, OrdemServico.class);
    }

    public static OrdemServicoResponseDTOV1 toResponse(OrdemServico ordem) {
        return MODEL_MAPPER.map(ordem, OrdemServicoResponseDTOV1.class);
    }

    public static List<OrdemServicoResponseDTOV1> toResponse(List<OrdemServico> ordens) {
        return ordens.stream().map(OrdemServicoMapper::toResponse).toList();
    }
}
