-- Cenario base de cadastros. IDs fixos para que os testes possam referencia-los
-- Prefixos: 1 = cliente, 2 = usuario, 3 = veiculo, 4 = peca, 5 = insumo, 6 = servico

-- Clientes
INSERT INTO pessoa (id, nome, celular) VALUES ('10000000-0000-0000-0000-000000000001', 'Ana Souza', '11999990001');
INSERT INTO cliente (id, email, cpf) VALUES ('10000000-0000-0000-0000-000000000001', 'ana@email.com', '111.111.111-11');

INSERT INTO pessoa (id, nome, celular) VALUES ('10000000-0000-0000-0000-000000000002', 'Bruno Lima', '11999990002');
INSERT INTO cliente (id, email, cpf) VALUES ('10000000-0000-0000-0000-000000000002', 'bruno@email.com', '222.222.222-22');

-- Usuarios
INSERT INTO pessoa (id, nome, celular) VALUES ('20000000-0000-0000-0000-000000000001', 'Carlos Mecanico', NULL);
INSERT INTO usuario (id, login, senha) VALUES ('20000000-0000-0000-0000-000000000001', 'carlos', '$2a$10$hashDeTesteNaoUtilizavel');

-- Veiculos
INSERT INTO veiculo (id, placa, modelo, marca, ano, cliente_id)
VALUES ('30000000-0000-0000-0000-000000000001', 'ABC1D23', 'Onix', 'Chevrolet', 2020, '10000000-0000-0000-0000-000000000001');
INSERT INTO veiculo (id, placa, modelo, marca, ano, cliente_id)
VALUES ('30000000-0000-0000-0000-000000000002', 'XYZ9K87', 'Gol', 'Volkswagen', 2015, '10000000-0000-0000-0000-000000000002');
INSERT INTO veiculo (id, placa, modelo, marca, ano, cliente_id)
VALUES ('30000000-0000-0000-0000-000000000003', 'SEM0C00', 'Uno', 'Fiat', 2010, NULL);

-- Produtos: pecas e insumos (heranca JOINED sobre produto)
INSERT INTO produto (id, quantidade_estoque, valor, nome, descricao)
VALUES ('40000000-0000-0000-0000-000000000001', 10, 25.50, 'Filtro de oleo', 'Filtro para motor 1.0');
INSERT INTO peca (id, fabricante) VALUES ('40000000-0000-0000-0000-000000000001', 'Tecfil');

INSERT INTO produto (id, quantidade_estoque, valor, nome, descricao)
VALUES ('50000000-0000-0000-0000-000000000001', 40, 39.90, 'Oleo 5W30', 'Oleo sintetico');
INSERT INTO insumo (id, unidade_medida) VALUES ('50000000-0000-0000-0000-000000000001', 'LITRO');

-- Servicos
INSERT INTO servico (id, nome, valor) VALUES ('60000000-0000-0000-0000-000000000001', 'Troca de oleo', 150.00);
INSERT INTO servico (id, nome, valor) VALUES ('60000000-0000-0000-0000-000000000002', 'Alinhamento', 90.00);
