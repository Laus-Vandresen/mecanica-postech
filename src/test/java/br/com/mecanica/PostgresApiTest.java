package br.com.mecanica;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;

abstract class PostgresApiTest {

    private static final String URL = System.getProperty("test.postgres.url");
    private static final PostgreSQLContainer POSTGRES;

    static {
        if (URL == null || URL.isBlank()) {
            POSTGRES = new PostgreSQLContainer("postgres:17-alpine");
            POSTGRES.start();
            Runtime.getRuntime().addShutdownHook(new Thread(POSTGRES::stop));
        } else {
            POSTGRES = null;
        }
    }

    @DynamicPropertySource
    static void configurarPostgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> POSTGRES == null ? URL : POSTGRES.getJdbcUrl());
        registry.add("spring.datasource.username", () -> POSTGRES == null
                ? System.getProperty("test.postgres.username", "postgres") : POSTGRES.getUsername());
        registry.add("spring.datasource.password", () -> POSTGRES == null
                ? System.getProperty("test.postgres.password", "") : POSTGRES.getPassword());
    }
}
