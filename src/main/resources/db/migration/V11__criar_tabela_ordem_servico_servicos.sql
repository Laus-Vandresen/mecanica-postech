CREATE TABLE ordem_servico_servicos (
    ordem_servico_entity_id BIGINT NOT NULL,
    servicos_id BIGINT NOT NULL,
    CONSTRAINT fk_ordem_servico_servicos_ordem_servico FOREIGN KEY (ordem_servico_entity_id) REFERENCES ordem_servico (id),
    CONSTRAINT fk_ordem_servico_servicos_servico FOREIGN KEY (servicos_id) REFERENCES servico (id)
);

CREATE INDEX idx_ordem_servico_servicos_ordem_servico_id ON ordem_servico_servicos (ordem_servico_entity_id);
CREATE INDEX idx_ordem_servico_servicos_servico_id ON ordem_servico_servicos (servicos_id);
