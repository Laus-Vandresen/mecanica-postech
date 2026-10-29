-- Cenario de ordens de servico. Depende de cadastros.sql (clientes, veiculos, produtos e servicos)
-- Prefixos: 7 = orcamento, 8 = ordem de servico
--
-- | OS  | cliente | veiculo | status               | abertura   | fila esperada |
-- | 801 | Ana     | ABC1D23 | RECEBIDA             | 2026-01-10 | DIAGNOSTICO   |
-- | 802 | Ana     | ABC1D23 | AGUARDANDO_APROVACAO | 2026-01-11 | EXECUCAO      | (orcamento aprovado)
-- | 803 | Bruno   | XYZ9K87 | AGUARDANDO_APROVACAO | 2026-01-12 | -             | (orcamento nao aprovado)
-- | 804 | Bruno   | XYZ9K87 | EM_EXECUCAO          | 2026-01-13 | FINALIZACAO   | (execucao concluida)
-- | 805 | Ana     | ABC1D23 | EM_EXECUCAO          | 2026-01-14 | -             | (execucao em andamento)
-- | 806 | Bruno   | XYZ9K87 | FINALIZADA           | 2026-01-05 | -             | (fechada)
-- | 807 | -       | SEM0C00 | ENTREGUE             | 2026-01-04 | -             | (fechada, sem cliente)
-- | 808 | -       | SEM0C00 | RECEBIDA             | 2026-01-15 | DIAGNOSTICO   | (sem cliente)

INSERT INTO orcamento (id, valor) VALUES ('70000000-0000-0000-0000-000000000001', 215.40);

INSERT INTO ordem_servico (id, cliente_id, veiculo_id, orcamento_id, data_abertura, data_fechamento, status,
                           descricao, diagnostico, valor_total, motivo_encerramento, orcamento_aprovado, data_conclusao_execucao)
VALUES ('80000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001',
        NULL, TIMESTAMP '2026-01-10 08:00:00', NULL, 'RECEBIDA', 'Barulho no motor', NULL, 0.00, NULL, FALSE, NULL);

INSERT INTO ordem_servico (id, cliente_id, veiculo_id, orcamento_id, data_abertura, data_fechamento, status,
                           descricao, diagnostico, valor_total, motivo_encerramento, orcamento_aprovado, data_conclusao_execucao)
VALUES ('80000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001',
        '70000000-0000-0000-0000-000000000001', TIMESTAMP '2026-01-11 08:00:00', NULL, 'AGUARDANDO_APROVACAO',
        'Revisao', 'Troca de oleo e filtro', 215.40, NULL, TRUE, NULL);

INSERT INTO ordem_servico (id, cliente_id, veiculo_id, orcamento_id, data_abertura, data_fechamento, status,
                           descricao, diagnostico, valor_total, motivo_encerramento, orcamento_aprovado, data_conclusao_execucao)
VALUES ('80000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000002',
        NULL, TIMESTAMP '2026-01-12 08:00:00', NULL, 'AGUARDANDO_APROVACAO', 'Direcao puxando', 'Desalinhado',
        90.00, NULL, FALSE, NULL);

INSERT INTO ordem_servico (id, cliente_id, veiculo_id, orcamento_id, data_abertura, data_fechamento, status,
                           descricao, diagnostico, valor_total, motivo_encerramento, orcamento_aprovado, data_conclusao_execucao)
VALUES ('80000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000002',
        NULL, TIMESTAMP '2026-01-13 08:00:00', NULL, 'EM_EXECUCAO', 'Revisao', 'Troca de oleo', 150.00, NULL, TRUE,
        TIMESTAMP '2026-01-13 15:00:00');

INSERT INTO ordem_servico (id, cliente_id, veiculo_id, orcamento_id, data_abertura, data_fechamento, status,
                           descricao, diagnostico, valor_total, motivo_encerramento, orcamento_aprovado, data_conclusao_execucao)
VALUES ('80000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001',
        NULL, TIMESTAMP '2026-01-14 08:00:00', NULL, 'EM_EXECUCAO', 'Alinhamento', 'Desalinhado', 90.00, NULL, TRUE, NULL);

INSERT INTO ordem_servico (id, cliente_id, veiculo_id, orcamento_id, data_abertura, data_fechamento, status,
                           descricao, diagnostico, valor_total, motivo_encerramento, orcamento_aprovado, data_conclusao_execucao)
VALUES ('80000000-0000-0000-0000-000000000006', '10000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000002',
        NULL, TIMESTAMP '2026-01-05 08:00:00', TIMESTAMP '2026-01-07 18:00:00', 'FINALIZADA', 'Revisao', 'Troca de oleo',
        150.00, NULL, TRUE, TIMESTAMP '2026-01-07 12:00:00');

INSERT INTO ordem_servico (id, cliente_id, veiculo_id, orcamento_id, data_abertura, data_fechamento, status,
                           descricao, diagnostico, valor_total, motivo_encerramento, orcamento_aprovado, data_conclusao_execucao)
VALUES ('80000000-0000-0000-0000-000000000007', NULL, '30000000-0000-0000-0000-000000000003',
        NULL, TIMESTAMP '2026-01-04 08:00:00', TIMESTAMP '2026-01-06 10:00:00', 'ENTREGUE', 'Alinhamento', 'Desalinhado',
        90.00, NULL, TRUE, TIMESTAMP '2026-01-05 17:00:00');

INSERT INTO ordem_servico (id, cliente_id, veiculo_id, orcamento_id, data_abertura, data_fechamento, status,
                           descricao, diagnostico, valor_total, motivo_encerramento, orcamento_aprovado, data_conclusao_execucao)
VALUES ('80000000-0000-0000-0000-000000000008', NULL, '30000000-0000-0000-0000-000000000003',
        NULL, TIMESTAMP '2026-01-15 08:00:00', NULL, 'RECEBIDA', 'Nao liga', NULL, 0.00, NULL, FALSE, NULL);

-- Itens da OS 802
INSERT INTO ordem_servico_servicos (ordem_servico_entity_id, servicos_id)
VALUES ('80000000-0000-0000-0000-000000000002', '60000000-0000-0000-0000-000000000001');
INSERT INTO ordem_servico_produtos (ordem_servico_entity_id, produtos_id)
VALUES ('80000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000001');
INSERT INTO ordem_servico_produtos (ordem_servico_entity_id, produtos_id)
VALUES ('80000000-0000-0000-0000-000000000002', '50000000-0000-0000-0000-000000000001');
