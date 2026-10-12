CREATE TABLE cliente (
    id BIGINT PRIMARY KEY,
    email VARCHAR(120),
    cpf VARCHAR(14) NOT NULL,
    CONSTRAINT fk_cliente_pessoa FOREIGN KEY (id) REFERENCES pessoa (id),
    CONSTRAINT uk_cliente_cpf UNIQUE (cpf),
    CONSTRAINT ck_cliente_cpf_nao_vazio CHECK (CHAR_LENGTH(TRIM(cpf)) > 0)
);
