package br.com.mecanica.core.config;
import br.com.mecanica.domain.service.OrdemServicoDomainService;
import org.springframework.context.annotation.*;
import java.time.Clock;
@Configuration
public class OrdemServicoConfig {
    @Bean public Clock clock() { return Clock.systemUTC(); }
    @Bean public OrdemServicoDomainService ordemServicoDomainService() { return new OrdemServicoDomainService(); }
}
