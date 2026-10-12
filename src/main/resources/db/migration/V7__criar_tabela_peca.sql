CREATE TABLE peca (
    id BIGINT PRIMARY KEY,
    fabricante VARCHAR(80),
    CONSTRAINT fk_peca_produto FOREIGN KEY (id) REFERENCES produto (id)
);
