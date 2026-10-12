CREATE TABLE usuario (
    id BIGINT PRIMARY KEY,
    login VARCHAR(80) NOT NULL,
    senha VARCHAR(255) NOT NULL,
    CONSTRAINT fk_usuario_pessoa FOREIGN KEY (id) REFERENCES pessoa (id),
    CONSTRAINT uk_usuario_login UNIQUE (login),
    CONSTRAINT ck_usuario_login_nao_vazio CHECK (CHAR_LENGTH(TRIM(login)) > 0),
    CONSTRAINT ck_usuario_senha_nao_vazia CHECK (CHAR_LENGTH(TRIM(senha)) > 0)
);
