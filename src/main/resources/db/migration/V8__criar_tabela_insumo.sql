CREATE TABLE insumo (
    id BIGINT PRIMARY KEY,
    unidade_medida VARCHAR(20) NOT NULL,
    CONSTRAINT fk_insumo_produto FOREIGN KEY (id) REFERENCES produto (id),
    CONSTRAINT ck_insumo_unidade_medida_nao_vazia CHECK (CHAR_LENGTH(TRIM(unidade_medida)) > 0)
);
