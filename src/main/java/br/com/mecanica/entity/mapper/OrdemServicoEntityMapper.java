package br.com.mecanica.entity.mapper;

import br.com.mecanica.core.modelmapper.ModelMapperConfig;
import br.com.mecanica.domain.entity.OrdemServico;
import br.com.mecanica.entity.OrdemServicoEntity;
import br.com.mecanica.entity.ProdutoEntity;
import br.com.mecanica.entity.ServicoEntity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrdemServicoEntityMapper {

    private static final ModelMapper MODEL_MAPPER = ModelMapperConfig.getModelMapper();

    public static OrdemServico toDomain(OrdemServicoEntity entity) {
        var ordem = MODEL_MAPPER.map(entity, OrdemServico.class);
        ordem.setServicoIds(entity.getServicos() == null ? List.of()
                : entity.getServicos().stream().map(ServicoEntity::getId).toList());
        ordem.setProdutoIds(entity.getProdutos() == null ? List.of()
                : entity.getProdutos().stream().map(ProdutoEntity::getId).toList());
        return ordem;
    }

    public static OrdemServicoEntity toEntity(OrdemServico ordem) {
        return MODEL_MAPPER.map(ordem, OrdemServicoEntity.class);
    }
}
