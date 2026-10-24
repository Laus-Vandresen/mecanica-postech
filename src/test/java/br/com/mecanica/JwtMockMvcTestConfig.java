package br.com.mecanica;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.MockMvcBuilderCustomizer;
import org.springframework.context.annotation.Bean;

@TestConfiguration
class JwtMockMvcTestConfig {

    @Bean
    MockMvcBuilderCustomizer jwtCustomizer() {
        return builder -> builder.defaultRequest(get("/").with(jwt()));
    }
}
