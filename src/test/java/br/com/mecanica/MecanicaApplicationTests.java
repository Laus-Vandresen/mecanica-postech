package br.com.mecanica;

import br.com.mecanica.entity.ServicoEntity;
import jakarta.persistence.EntityManager;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class MecanicaApplicationTests {

    @Autowired
    private Flyway flyway;

    @Autowired
    private EntityManager entityManager;

    @Test
    void contextLoadsWithFlyway() {
        assertNotNull(flyway.info());
        assertEquals(0, flyway.info().pending().length);
    }

    @Test
    @Transactional
    void persisteServicoNaTabelaCriadaPelaMigration() {
        var servico = new ServicoEntity();
        servico.setNome("Troca de oleo");
        servico.setValor(new BigDecimal("150.00"));

        entityManager.persist(servico);
        entityManager.flush();
        entityManager.clear();

        assertNotNull(servico.getId());
        var salvo = entityManager.find(ServicoEntity.class, servico.getId());
        assertEquals("Troca de oleo", salvo.getNome());
        assertEquals(new BigDecimal("150.00"), salvo.getValor());
    }
}
