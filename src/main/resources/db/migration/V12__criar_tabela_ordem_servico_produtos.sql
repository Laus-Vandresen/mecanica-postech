CREATE TABLE ordem_servico_produtos (
    ordem_servico_entity_id BIGINT NOT NULL,
    produtos_id BIGINT NOT NULL,
    CONSTRAINT fk_ordem_servico_produtos_ordem_servico FOREIGN KEY (ordem_servico_entity_id) REFERENCES ordem_servico (id),
    CONSTRAINT fk_ordem_servico_produtos_produto FOREIGN KEY (produtos_id) REFERENCES produto (id)
);

CREATE INDEX idx_ordem_servico_produtos_ordem_servico_id ON ordem_servico_produtos (ordem_servico_entity_id);
CREATE INDEX idx_ordem_servico_produtos_produto_id ON ordem_servico_produtos (produtos_id);
