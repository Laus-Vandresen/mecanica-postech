package db.migration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import java.sql.DriverManager;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class SimplificacaoOsMigrationTest {
    @Test
    void criaEstruturaDaOsSemTabelasAuxiliares() throws Exception {
        var url = "jdbc:h2:mem:simplificacao-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1";
        Flyway.configure().dataSource(url, "sa", "").target("13").load().migrate();
        var veiculo = UUID.randomUUID();
        var os = UUID.randomUUID();
        var flyway = Flyway.configure().dataSource(url, "sa", "").load();
        assertEquals(1, flyway.migrate().migrationsExecuted);
        try (var connection = DriverManager.getConnection(url, "sa", "")) {
            try (var statement = connection.prepareStatement("INSERT INTO veiculo(id,placa,modelo) VALUES(?,'ABC1D23','Modelo')")) {
                statement.setObject(1, veiculo); statement.executeUpdate();
            }
            try (var statement = connection.prepareStatement("INSERT INTO ordem_servico(id,veiculo_id,data_abertura,status,valor_total) VALUES(?,?,TIMESTAMP '2026-01-01 12:00:00','EM_EXECUCAO',10)")) {
                statement.setObject(1, os); statement.setObject(2, veiculo); statement.executeUpdate();
            }

        }
        try (var connection = DriverManager.getConnection(url, "sa", ""); var statement = connection.createStatement()) {
            try (var result = statement.executeQuery("SELECT orcamento_aprovado,data_conclusao_execucao FROM ordem_servico")) {
                assertTrue(result.next()); assertFalse(result.getBoolean(1)); assertNull(result.getObject(2));
            }
            try (var result = statement.executeQuery("SELECT COUNT(*) FROM information_schema.tables WHERE table_name IN ('ORDEM_SERVICO_HISTORICO','ORDEM_SERVICO_MARCO')")) {
                result.next(); assertEquals(0, result.getInt(1));
            }
        }
        assertEquals(0, flyway.migrate().migrationsExecuted);
    }
}
