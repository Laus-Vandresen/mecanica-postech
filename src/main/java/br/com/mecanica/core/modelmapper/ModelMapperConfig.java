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

}
