package db.migration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ConsumoDiretoMigrationTest {

    @Test
    void adicionaQuantidadesPreservandoItensESaldosSemCriarReservas() throws Exception {
        var url = "jdbc:h2:mem:consumo-direto-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1";
        Flyway.configure().dataSource(url, "sa", "").target("14").load().migrate();
        var veiculo = UUID.randomUUID();
        var os = UUID.randomUUID();
        var produtos = new UUID[]{UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()};
        try (var connection = DriverManager.getConnection(url, "sa", "")) {
            try (var statement = connection.prepareStatement("INSERT INTO veiculo(id,placa,modelo) VALUES(?,'ABC1D23','Modelo')")) {
                statement.setObject(1, veiculo);
                statement.executeUpdate();
            }
            try (var statement = connection.prepareStatement("INSERT INTO ordem_servico(id,veiculo_id,data_abertura,status,valor_total) VALUES(?,?,CURRENT_TIMESTAMP,'RECEBIDA',0)")) {
                statement.setObject(1, os);
                statement.setObject(2, veiculo);
                statement.executeUpdate();
            }
            for (int i = 0; i < produtos.length; i++) {
                try (var statement = connection.prepareStatement("INSERT INTO produto(id,nome,valor,quantidade_estoque) VALUES(?,'Produto',10,?)")) {
                    statement.setObject(1, produtos[i]);
                    statement.setInt(2, 7 + i);
                    statement.executeUpdate();
                }
                try (var statement = connection.prepareStatement("INSERT INTO ordem_servico_produtos(ordem_servico_entity_id,produtos_id) VALUES(?,?)")) {
                    statement.setObject(1, os);
                    statement.setObject(2, produtos[i]);
                    statement.executeUpdate();
                }
            }
        }
        var flyway = Flyway.configure().dataSource(url, "sa", "").load();
        assertEquals(1, flyway.migrate().migrationsExecuted);
        try (var connection = DriverManager.getConnection(url, "sa", "")) {
            for (int i = 0; i < produtos.length; i++) {
                try (var statement = connection.prepareStatement("SELECT quantidade,quantidade_consumida FROM ordem_servico_produtos WHERE ordem_servico_entity_id=? AND produtos_id=?")) {
                    statement.setObject(1, os);
                    statement.setObject(2, produtos[i]);
                    try (var result = statement.executeQuery()) {
                        assertTrue(result.next());
                        assertEquals(1, result.getInt(1));
                        assertEquals(0, result.getInt(2));
                        assertFalse(result.next());
                    }
                }
                try (var statement = connection.prepareStatement("SELECT quantidade_estoque FROM produto WHERE id=?")) {
                    statement.setObject(1, produtos[i]);
                    try (var result = statement.executeQuery()) {
                        assertTrue(result.next());
                        assertEquals(7 + i, result.getInt(1));
                    }
                }
            }
            try (var columns = connection.getMetaData().getColumns(null, null, "PRODUTO", "QUANTIDADE_RESERVADA")) {
                assertFalse(columns.next());
            }
            try (var tables = connection.getMetaData().getTables(null, null, "RESERVA_ESTOQUE", null)) {
                assertFalse(tables.next());
            }
            try (var statement = connection.createStatement()) {
                assertThrows(SQLException.class, () -> statement.executeUpdate("UPDATE ordem_servico_produtos SET quantidade=0"));
                assertThrows(SQLException.class, () -> statement.executeUpdate("UPDATE ordem_servico_produtos SET quantidade_consumida=2"));
                assertThrows(SQLException.class, () -> statement.executeUpdate("UPDATE ordem_servico_produtos SET quantidade_consumida=-1"));
            }
        }
        assertEquals(0, flyway.migrate().migrationsExecuted);
        assertTrue(flyway.validateWithResult().validationSuccessful);
    }
}
