package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class V13__migrar_ids_para_uuid extends BaseJavaMigration {
    private static final List<String> TABLES = List.of("pessoa", "cliente", "usuario", "servico",
            "veiculo", "produto", "peca", "insumo", "orcamento", "ordem_servico");
    private static final List<String> ROOTS = List.of("pessoa", "servico", "veiculo", "produto", "orcamento", "ordem_servico");
    private record Reference(String table, String column, String parent, String constraint, boolean nullable) {}
    private static final List<Reference> REFERENCES = List.of(
            new Reference("cliente", "id", "pessoa", "fk_cliente_pessoa", false),
            new Reference("usuario", "id", "pessoa", "fk_usuario_pessoa", false),
            new Reference("peca", "id", "produto", "fk_peca_produto", false),
            new Reference("insumo", "id", "produto", "fk_insumo_produto", false),
            new Reference("veiculo", "cliente_id", "cliente", "fk_veiculo_cliente", false),
            new Reference("ordem_servico", "cliente_id", "cliente", "fk_ordem_servico_cliente", false),
            new Reference("ordem_servico", "veiculo_id", "veiculo", "fk_ordem_servico_veiculo", false),
            new Reference("ordem_servico", "orcamento_id", "orcamento", "fk_ordem_servico_orcamento", true),
            new Reference("ordem_servico_servicos", "ordem_servico_entity_id", "ordem_servico", "fk_ordem_servico_servicos_ordem_servico", false),
            new Reference("ordem_servico_servicos", "servicos_id", "servico", "fk_ordem_servico_servicos_servico", false),
            new Reference("ordem_servico_produtos", "ordem_servico_entity_id", "ordem_servico", "fk_ordem_servico_produtos_ordem_servico", false),
            new Reference("ordem_servico_produtos", "produtos_id", "produto", "fk_ordem_servico_produtos_produto", false));

    @Override
    public Integer getChecksum() {
        return 13001;
    }

    @Override
    public void migrate(Context context) throws Exception {
        var connection = context.getConnection();
        for (var table : TABLES) {
            execute(connection, "ALTER TABLE " + table + " ADD COLUMN id_uuid UUID");
        }
        for (var ref : REFERENCES) {
            if (!ref.column().equals("id")) {
                execute(connection, "ALTER TABLE " + ref.table() + " ADD COLUMN " + ref.column() + "_uuid UUID");
            }
        }
        for (var table : ROOTS) {
            gerarIds(connection, table);
        }
        for (var ref : REFERENCES) {
            execute(connection, "UPDATE " + ref.table() + " SET " + ref.column() + "_uuid = (SELECT id_uuid FROM "
                    + ref.parent() + " WHERE " + ref.parent() + ".id = " + ref.table() + "." + ref.column() + ")");
            try (var statement = connection.createStatement(); var result = statement.executeQuery(
                    "SELECT COUNT(*) FROM " + ref.table() + " WHERE " + ref.column() + " IS NOT NULL AND " + ref.column() + "_uuid IS NULL")) {
                result.next();
                if (result.getLong(1) != 0) throw new SQLException("Referencia sem UUID: " + ref.table() + "." + ref.column());
            }
        }
        for (var ref : REFERENCES) execute(connection, "ALTER TABLE " + ref.table() + " DROP CONSTRAINT " + ref.constraint());
        execute(connection, "ALTER TABLE ordem_servico DROP CONSTRAINT uk_ordem_servico_orcamento");
        for (var table : TABLES) removerChavePrimaria(connection, table);
        for (var table : TABLES) substituirColuna(connection, table, "id", false);
        for (var ref : REFERENCES) {
            if (!ref.column().equals("id")) substituirColuna(connection, ref.table(), ref.column(), ref.nullable());
        }
        for (var table : TABLES) execute(connection, "ALTER TABLE " + table + " ADD CONSTRAINT " + table + "_pkey PRIMARY KEY (id)");
        execute(connection, "ALTER TABLE ordem_servico ADD CONSTRAINT uk_ordem_servico_orcamento UNIQUE (orcamento_id)");
        for (var ref : REFERENCES) execute(connection, "ALTER TABLE " + ref.table() + " ADD CONSTRAINT " + ref.constraint()
                + " FOREIGN KEY (" + ref.column() + ") REFERENCES " + ref.parent() + " (id)");
        for (var ref : REFERENCES) {
            if (!ref.column().equals("id")) execute(connection, "CREATE INDEX IF NOT EXISTS idx_uuid_" + ref.table() + "_" + ref.column()
                    + " ON " + ref.table() + " (" + ref.column() + ")");
        }
    }

    private void gerarIds(Connection connection, String table) throws SQLException {
        try (var statement = connection.createStatement(); var ids = statement.executeQuery("SELECT id FROM " + table);
             var update = connection.prepareStatement("UPDATE " + table + " SET id_uuid = ? WHERE id = ?")) {
            while (ids.next()) {
                update.setObject(1, UUID.randomUUID());
                update.setLong(2, ids.getLong(1));
                update.executeUpdate();
            }
        }
    }

    private void removerChavePrimaria(Connection connection, String table) throws SQLException {
        var metadata = connection.getMetaData();
        var name = metadata.storesUpperCaseIdentifiers() ? table.toUpperCase(Locale.ROOT) : table;
        String constraint;
        try (var keys = metadata.getPrimaryKeys(connection.getCatalog(), connection.getSchema(), name)) {
            if (!keys.next()) throw new SQLException("Chave primaria ausente em " + table);
            constraint = keys.getString("PK_NAME");
        }
        execute(connection, "ALTER TABLE " + table + " DROP CONSTRAINT \"" + constraint.replace("\"", "\"\"") + "\"");
    }

    private void substituirColuna(Connection connection, String table, String column, boolean nullable) throws SQLException {
        execute(connection, "ALTER TABLE " + table + " DROP COLUMN " + column);
        execute(connection, "ALTER TABLE " + table + " RENAME COLUMN " + column + "_uuid TO " + column);
        if (!nullable) execute(connection, "ALTER TABLE " + table + " ALTER COLUMN " + column + " SET NOT NULL");
    }

    private void execute(Connection connection, String sql) throws SQLException {
        try (var statement = connection.createStatement()) { statement.execute(sql); }
    }
}
