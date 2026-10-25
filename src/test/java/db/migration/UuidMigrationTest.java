package db.migration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UuidMigrationTest {
    @Test
    void converteBancoPreenchidoPreservandoHerancaRelacionamentosERestricoes() throws Exception {
        var url = "jdbc:h2:mem:migration-uuid-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1";
        Flyway.configure().dataSource(url, "sa", "").target("12").load().migrate();
        try (var connection = DriverManager.getConnection(url, "sa", ""); var statement = connection.createStatement()) {
            statement.executeUpdate("INSERT INTO pessoa(id,nome) VALUES(101,'Cliente legado'),(102,'Usuario legado')");
            statement.executeUpdate("INSERT INTO cliente(id,cpf) VALUES(101,'52998224725')");
            statement.executeUpdate("INSERT INTO usuario(id,login,senha) VALUES(102,'usuario','hash-teste')");
            statement.executeUpdate("INSERT INTO veiculo(id,placa,modelo,cliente_id) VALUES(301,'ABC1D23','Modelo',101)");
            statement.executeUpdate("INSERT INTO produto(id,nome,valor,quantidade_estoque) VALUES(201,'Peca',12.50,10),(202,'Insumo',25.00,20)");
            statement.executeUpdate("INSERT INTO peca(id,fabricante) VALUES(201,'Fabricante')");
            statement.executeUpdate("INSERT INTO insumo(id,unidade_medida) VALUES(202,'L')");
            statement.executeUpdate("INSERT INTO orcamento(id,valor) VALUES(501,100.50)");
            statement.executeUpdate("INSERT INTO servico(id,nome,valor) VALUES(101,'Servico legado',50.00)");
            statement.executeUpdate("INSERT INTO ordem_servico(id,cliente_id,veiculo_id,orcamento_id,data_abertura,status,valor_total) VALUES(401,101,301,501,CURRENT_TIMESTAMP,'RECEBIDA',100.50),(402,101,301,NULL,CURRENT_TIMESTAMP,'RECEBIDA',0)");
            statement.executeUpdate("INSERT INTO ordem_servico_servicos VALUES(401,101)");
            statement.executeUpdate("INSERT INTO ordem_servico_produtos VALUES(401,201),(401,202)");
        }
        var flyway = Flyway.configure().dataSource(url, "sa", "").load();
        assertEquals(2, flyway.migrate().migrationsExecuted);
        try (var connection = DriverManager.getConnection(url, "sa", "")) {
            assertEquals(2, count(connection, "SELECT COUNT(*) FROM pessoa"));
            assertEquals(2, count(connection, "SELECT COUNT(*) FROM produto"));
            assertEquals(2, count(connection, "SELECT COUNT(*) FROM ordem_servico"));
            assertEquals(1, count(connection, "SELECT COUNT(*) FROM pessoa p JOIN cliente c ON p.id=c.id WHERE p.nome='Cliente legado' AND c.cpf='52998224725'"));
            assertEquals(1, count(connection, "SELECT COUNT(*) FROM pessoa p JOIN usuario u ON p.id=u.id WHERE u.login='usuario'"));
            assertEquals(1, count(connection, "SELECT COUNT(*) FROM produto p JOIN peca c ON p.id=c.id WHERE p.valor=12.50 AND p.quantidade_estoque=10"));
            assertEquals(1, count(connection, "SELECT COUNT(*) FROM produto p JOIN insumo i ON p.id=i.id WHERE i.unidade_medida='L'"));
            assertEquals(1, count(connection, "SELECT COUNT(*) FROM ordem_servico o JOIN cliente c ON c.id=o.cliente_id JOIN veiculo v ON v.id=o.veiculo_id AND v.cliente_id=c.id JOIN orcamento r ON r.id=o.orcamento_id WHERE r.valor=100.50"));
            assertEquals(1, count(connection, "SELECT COUNT(*) FROM ordem_servico WHERE orcamento_id IS NULL"));
            assertEquals(1, count(connection, "SELECT COUNT(*) FROM ordem_servico_servicos x JOIN ordem_servico o ON x.ordem_servico_entity_id=o.id JOIN servico s ON x.servicos_id=s.id WHERE s.nome='Servico legado'"));
            assertEquals(2, count(connection, "SELECT COUNT(*) FROM ordem_servico_produtos x JOIN ordem_servico o ON x.ordem_servico_entity_id=o.id JOIN produto p ON x.produtos_id=p.id"));
            try (var statement = connection.createStatement(); var result = statement.executeQuery("SELECT p.id, s.id FROM pessoa p CROSS JOIN servico s WHERE p.nome='Cliente legado'")) {
                assertTrue(result.next());
                assertInstanceOf(UUID.class, result.getObject(1));
                assertNotEquals(result.getObject(1), result.getObject(2));
                assertEquals(4, result.getObject(1, UUID.class).version());
            }
            try (var statement = connection.createStatement()) {
                var foreignKeyError = assertThrows(SQLException.class, () -> statement.executeUpdate(
                        "INSERT INTO veiculo(id,placa,modelo,cliente_id) VALUES('" + UUID.randomUUID() + "','XYZ1D23','Modelo','" + UUID.randomUUID() + "')"));
                assertEquals("23506", foreignKeyError.getSQLState());
                assertThrows(SQLException.class, () -> statement.executeUpdate(
                        "UPDATE ordem_servico SET orcamento_id=(SELECT id FROM orcamento)"));
            }
        }
        assertEquals(0, flyway.migrate().migrationsExecuted);
        assertEquals(0, flyway.validateWithResult().invalidMigrations.size());
    }

    private int count(Connection connection, String sql) throws SQLException {
        try (var statement = connection.createStatement(); var result = statement.executeQuery(sql)) {
            result.next(); return result.getInt(1);
        }
    }
}
