# MecÃ¢nica Postech

Backend Java 21 / Spring Boot com PostgreSQL, JPA e migrations Flyway.

## Executar

1. Configure um JDK 21 e o JAVA_HOME.
2. Copie `.env.example` para `.env` na raiz e preencha `DB_PASSWORD`.
3. Inicie o RDS e aguarde `Available`.
4. Execute a partir da raiz:

```powershell
$env:SPRING_PROFILES_ACTIVE = 'aws'
.\mvnw.cmd spring-boot:run
```

No IntelliJ, adicione `SPRING_PROFILES_ACTIVE=aws` Ã s variÃ¡veis da configuraÃ§Ã£o de
MecanicaApplication e mantenha o diretÃ³rio de trabalho na raiz do projeto.
O perfil aws importa `.env` como properties. NÃ£o versione credenciais.
Defina tambem `JWT_SECRET` com no minimo 32 bytes em UTF-8 no `.env`. Use um valor aleatorio forte. Cadastro e login: `POST /v1/auth/registrar` e `POST /v1/auth/autenticar`.
A conexÃ£o usa TLS verify-full e o certificado pÃºblico em `certs/`.

Para Docker: gere o JAR com `.\mvnw.cmd package` e execute `docker compose up --build`.
O Compose executa a aplicaÃ§Ã£o; o PostgreSQL continua no RDS.

## API e identificadores UUID

O contrato atual é gerado de `src/main/resources/openapi/api.yaml`. As rotas de
cadastro existentes são `/v1/clientes`, `/v1/veiculos`, `/v1/pecas` e `/v1/insumos`:
POST para criar, GET para listar, GET /{id} para buscar e DELETE /{id} para excluir.
As atualizações dos cadastros permanecem na camada de services; não há PUT
publicado no contrato atual. Serviços possuem entidade e repository, mas ainda
não têm controller próprio no contrato atual.

Ordens de serviço usam `/v1/ordem-servico`, com GET/POST, GET/PATCH /{id} e
POST /{id}/cancelamento conforme o OpenAPI. As regras de execução de OS ainda
estão em desenvolvimento; esta mudança não implementa o fluxo de OS.

Todos os IDs, inclusive `clienteId`, `veiculoId`, `orcamentoId`, `servicoIds` e
`produtoIds`, são UUIDs. Nas APIs, eles são strings no formato UUID, por exemplo:

```json
{
  "placa": "ABC1D23",
  "modelo": "Uno",
  "marca": "Fiat",
  "ano": 2020,
  "clienteId": "3cfe445f-0755-4ba2-a94b-510516146145"
}
```

Use um UUID de cliente retornado pelo cadastro/listagem; o exemplo é ilustrativo.
IDs numéricos nas URLs ou referências não são mais aceitos. Consulte o schema
OpenAPI para os demais campos obrigatórios e validações.

## Conversão do banco para UUID

A V13 é uma migration Java em
`src/main/java/db/migration/V13__migrar_ids_para_uuid.java`, descoberta pelo Flyway
na mesma localização `classpath:db/migration` dos scripts SQL. V1 a V12 permanecem
inalteradas para preservar o histórico e seus checksums.

A V13 gera UUIDs aleatórios para as linhas existentes e atualiza todas as chaves
estrangeiras, inclusive herança JOINED e tabelas de associação da OS. Preserva
campos de negócio, nulabilidade do orçamento, unicidade, índices e vínculos.
A geração de novos IDs passa a ser feita pelo Hibernate com `GenerationType.UUID`
(UUID v4), sem sequência numérica nem dependência de extensão PostgreSQL.

Os UUIDs substituem os números anteriores: clientes externos devem atualizar seus
IDs armazenados consultando novamente a API. Não existe compatibilidade com os
IDs numéricos anteriores.

Para o banco compartilhado, faça backup/snapshot e pare as versões antigas da
aplicação antes de iniciar a versão nova: o Flyway aplica a V13 automaticamente
na inicialização com o perfil aws. Aplicações antigas que esperam BIGINT deixam
de ser compatíveis. No PostgreSQL, a conversão executa na transação do Flyway;
reserve um período sem acessos concorrentes para a alteração das tabelas.

Os testes locais cobrem banco vazio, banco com dados e uma segunda execução sem
migrations pendentes. Usam H2; a execução no PostgreSQL/RDS deve ser validada
separadamente. Esta alteração no código não aplica a V13 na AWS.

## Migrations e testes

As migrations V1 a V12 existentes sÃ£o preservadas. Flyway aplica pendÃªncias antes
do Hibernate validar o esquema (`ddl-auto=validate`). NÃ£o edite migrations jÃ¡
aplicadas; evolua o banco criando uma versÃ£o nova.

```powershell
.\mvnw.cmd test
```

Os testes usam H2 em memÃ³ria, sem acessar o RDS. Cobrem o ciclo dos cinco CRUDs,
validaÃ§Ãµes, CPF/placa duplicados, vÃ­nculos, exclusÃ£o com heranÃ§a JOINED e erros HTTP.
H2 nÃ£o substitui uma validaÃ§Ã£o posterior especÃ­fica no PostgreSQL.

### Testes unitários dos CRUDs

As classes `ClienteServiceTest`, `VeiculoServiceTest`, `ServicoServiceTest`,
`PecaServiceTest` e `InsumoServiceTest` usam JUnit 5 e Mockito, sem iniciar Spring
ou acessar banco. Verificam criação, consulta, listagem, atualização, exclusão,
registros inexistentes e propagação de conflitos. Veículos também verificam
cliente inexistente na criação e na atualização. Atualizações verificam que o
id é preservado e campos opcionais podem ser limpos; estoque e valor aceitam zero.

Para executar apenas esses testes:

```powershell
.\mvnw.cmd '-Dtest=*ServiceTest' test
```

Os testes HTTP de `CadastrosApiTests` continuam cobrindo validações de entrada,
status HTTP e restrições reais no H2. Os mocks dos testes unitários não substituem
esses testes de integração nem verificam transações do banco.
