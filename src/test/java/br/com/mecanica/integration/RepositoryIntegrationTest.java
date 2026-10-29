package br.com.mecanica.integration;

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest(showSql = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public abstract class RepositoryIntegrationTest {

    public static final String LIMPAR_BANCO = "/sql/limpar-banco.sql";
    public static final String CADASTROS = "/sql/cenarios/cadastros.sql";
    public static final String ORDENS_SERVICO = "/sql/cenarios/ordens-servico.sql";
}
