ALTER TABLE ordem_servico_produtos ADD COLUMN quantidade INTEGER DEFAULT 1 NOT NULL;
ALTER TABLE ordem_servico_produtos ADD COLUMN quantidade_consumida INTEGER DEFAULT 0 NOT NULL;
ALTER TABLE ordem_servico_produtos ADD CONSTRAINT ck_os_produto_quantidade_positiva CHECK (quantidade > 0);
ALTER TABLE ordem_servico_produtos ADD CONSTRAINT ck_os_produto_consumo_valido
    CHECK (quantidade_consumida >= 0 AND quantidade_consumida <= quantidade);
ALTER TABLE ordem_servico_produtos ADD CONSTRAINT uk_os_produto UNIQUE (ordem_servico_entity_id, produtos_id);
