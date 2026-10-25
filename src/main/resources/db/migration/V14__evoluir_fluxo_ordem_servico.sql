ALTER TABLE ordem_servico ALTER COLUMN cliente_id DROP NOT NULL;
ALTER TABLE veiculo ALTER COLUMN cliente_id DROP NOT NULL;
ALTER TABLE ordem_servico ADD COLUMN motivo_encerramento VARCHAR(500);
ALTER TABLE ordem_servico ADD COLUMN orcamento_aprovado BOOLEAN DEFAULT FALSE NOT NULL;
ALTER TABLE ordem_servico ADD COLUMN data_conclusao_execucao TIMESTAMP(6);
