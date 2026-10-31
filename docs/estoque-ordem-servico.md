# Consumo direto de produtos na ordem de serviço

O OrdemServicoController chama o OrdemServicoService e converte os DTOs pelo mapper. Ao registrar o uso de um produto durante a execução, o OrdemServicoService chama EstoqueService.movimentar com SAIDA, na mesma transação. Não há reserva de material nem saldo reservado no modelo atual.

## Produtos da OS

A abertura e o diagnóstico recebem a quantidade prevista de cada produto:

```json
{
  "veiculoId": "UUID_DO_VEICULO",
  "descricao": "Revisão",
  "produtos": [{"produtoId":"UUID_DA_PECA","quantidade":5}]
}
```

Quantidades são inteiras positivas. Um produto não pode se repetir na lista. No PATCH de diagnóstico, omitir a lista preserva os itens; `[]` remove todos. Depois de enviar o orçamento, os produtos e quantidades previstos não podem ser alterados pelo diagnóstico.

A resposta inclui `quantidadeConsumida`, inicialmente zero, mantida apenas pela operação de consumo. Esse campo não é aceito como consumo na abertura ou no diagnóstico.

## Baixa no momento do uso

```http
PUT /mecanica/v1/ordem-servico/{id}/produtos/{produtoId}/consumo
Authorization: Bearer <token>
Content-Type: application/json
```

```json
{"quantidadeConsumida":2}
```

`quantidadeConsumida` representa o total acumulado utilizado deste produto na OS. Ao informar 2 pela primeira vez, o estoque diminui em 2. Repetir 2 não desconta novamente. Ao informar 3 depois disso, a baixa adicional é 1.

O consumo exige OS aprovada em EM_EXECUCAO, cuja execução ainda não foi concluída. O produto deve estar associado à OS, e o total informado precisa ser positivo, não pode diminuir nem exceder a quantidade prevista. Se faltar estoque, retorna 422 e toda a operação é revertida: saldo e quantidade consumida permanecem iguais aos anteriores.

## Efeito de cada etapa

| Etapa | Estoque |
| --- | --- |
| Abertura, diagnóstico e orçamento | Não movimentam |
| Aprovação e início da execução | Não movimentam nem bloqueiam material |
| Registro de consumo durante a execução | Baixa imediatamente apenas a diferença consumida |
| Conclusão, finalização e entrega | Não movimentam nem repetem a baixa |
| Cancelamento antes da execução e recusa | Não movimentam |

Produtos previstos que não forem usados permanecem no estoque. É possível concluir a execução com consumo parcial ou sem consumo de produtos. Não há estorno automático de material utilizado; o cancelamento da OS continua permitido apenas antes de iniciar a execução.

O UPDATE condicional no ProdutoRepository impede saldo negativo, inclusive em operações concorrentes. Alterações da mesma OS são serializadas por lock transacional, protegendo o total consumido contra baixas duplicadas. Entradas e saídas gerais continuam pelo endpoint de estoque existente; produtos usados na OS são registrados pelo endpoint de consumo para manter seu total associado.

Todos os endpoints continuam protegidos pelo JWT atual. Não foram adicionados papéis, histórico de ações nem coluna de versão.

## Migrations

V1 a V14 foram preservadas. Como a V15 ainda não foi aplicada, ela foi consolidada diretamente para adicionar quantidade e quantidade_consumida aos itens da OS, com restrições de integridade. A V16 foi removida.

Itens existentes recebem quantidade prevista 1 e quantidade consumida 0. Os vínculos com OS e produtos e o saldo físico permanecem intactos. Não são criados tabela ou saldo de reservas, nem são feitas baixas retroativas.

Nenhuma migration foi aplicada no RDS durante esta alteração; o Flyway executará a V15 pendente na próxima inicialização com o perfil aws.

## Testes

A suíte cobre consumo imediato e parcial, repetição do mesmo total, falta de saldo com rollback, limites de quantidade, etapas permitidas, autenticação e concorrência entre OSs. A migração V14 para V15 é testada com itens existentes, conferindo os valores iniciais das quantidades, a preservação dos saldos e vínculos e a ausência de estruturas de reserva.

Os testes de estoque usam PostgreSQL com Testcontainers. Também podem usar um PostgreSQL local exclusivo e descartável:

```powershell
.\mvnw.cmd '-Dtest.postgres.url=jdbc:postgresql://127.0.0.1:5432/mecanica_test' '-Dtest.postgres.username=postgres' test
```

Quando necessário, informe `-Dtest.postgres.password`. Os cenários limpam os dados desse banco. Nunca informe o RDS compartilhado nessa opção.
