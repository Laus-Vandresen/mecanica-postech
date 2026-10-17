package br.com.mecanica.core.modelmapper;

import br.com.mecanica.domain.entity.OrdemServico;
import br.com.mecanica.entity.OrdemServicoEntity;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Configuration
public class ModelMapperConfig {

    private static final ModelMapper INSTANCE = criarModelMapper();

    public static ModelMapper getModelMapper() {
        return INSTANCE;
    }

    @Bean
    public ModelMapper modelMapper() {
        return INSTANCE;
    }

    private static ModelMapper criarModelMapper() {
        var mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        configurarConversoresDeData(mapper);
        configurarOrdemServico(mapper);
        return mapper;
    }

    private static void configurarConversoresDeData(ModelMapper mapper) {
        mapper.addConverter(
                ctx -> ctx.getSource() == null ? null
                        : ctx.getSource().atZone(ZoneId.systemDefault()).toOffsetDateTime(),
                LocalDateTime.class, OffsetDateTime.class);

        mapper.addConverter(
                ctx -> ctx.getSource() == null ? null
                        : ctx.getSource().atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime(),
                OffsetDateTime.class, LocalDateTime.class);
    }

    private static void configurarOrdemServico(ModelMapper mapper) {
        mapper.typeMap(OrdemServicoEntity.class, OrdemServico.class)
                .addMappings(m -> {
                    m.map(src -> src.getCliente().getId(), OrdemServico::setClienteId);
                    m.map(src -> src.getVeiculo().getId(), OrdemServico::setVeiculoId);
                    m.map(src -> src.getOrcamento().getId(), OrdemServico::setOrcamentoId);
                });

        mapper.typeMap(OrdemServico.class, OrdemServicoEntity.class)
                .addMappings(m -> {
                    m.skip(OrdemServicoEntity::setCliente);
                    m.skip(OrdemServicoEntity::setVeiculo);
                    m.skip(OrdemServicoEntity::setOrcamento);
                    m.skip(OrdemServicoEntity::setServicos);
                    m.skip(OrdemServicoEntity::setProdutos);
                });
    }
}
