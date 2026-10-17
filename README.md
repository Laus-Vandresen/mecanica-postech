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
O arquivo antigo `application-local.properties` nÃ£o Ã© importado pelo perfil atual.
A conexÃ£o usa TLS verify-full e o certificado pÃºblico em `certs/`.

Para Docker: gere o JAR com `.\mvnw.cmd package` e execute `docker compose up --build`.
O Compose executa a aplicaÃ§Ã£o; o PostgreSQL continua no RDS.

## CRUDs

Base local: `http://localhost:8080`. Corpos e respostas usam JSON.

| Recurso | Rota |
| --- | --- |
| Clientes | `/clientes` |
| VeÃ­culos | `/veiculos` |
| ServiÃ§os | `/servicos` |
| PeÃ§as | `/pecas` |
| Insumos | `/insumos` |

Todas as rotas oferecem:

| MÃ©todo | Caminho | Resposta |
| --- | --- | --- |
| POST | `/recurso` | 201, corpo criado e cabeÃ§alho Location |
| GET | `/recurso` | 200, lista ordenada por id (sem paginaÃ§Ã£o) |
| GET | `/recurso/{id}` | 200, registro |
| PUT | `/recurso/{id}` | 200, substituiÃ§Ã£o dos campos editÃ¡veis |
| DELETE | `/recurso/{id}` | 204, sem corpo |

PUT exige os mesmos campos obrigatÃ³rios de POST; campos opcionais omitidos sÃ£o
limpos. O id Ã© gerado pelo servidor e nÃ£o faz parte do corpo de entrada.
As respostas incluem os campos do cadastro e `id`. VeÃ­culos retornam `clienteId`,
sem serializar o grafo de entidades JPA.

### Exemplos de POST/PUT

Cliente (nome e CPF obrigatÃ³rios; CPF vÃ¡lido com 11 dÃ­gitos, sem pontuaÃ§Ã£o):

```json
{"nome":"Cliente exemplo","cpf":"52998224725","email":"dev@example.com","celular":"11999999999"}
```

VeÃ­culo (clienteId precisa existir; placa antiga ou Mercosul, sem hÃ­fen):

```json
{"placa":"ABC1D23","modelo":"Uno","marca":"Fiat","ano":2020,"clienteId":1}
```

ServiÃ§o:

```json
{"nome":"Troca de Ã³leo","valor":150.00}
```

PeÃ§a:

```json
{"nome":"Filtro de Ã³leo","descricao":"Filtro novo","valor":25.50,"quantidadeEstoque":10,"fabricante":"Fabricante"}
```

Insumo:

```json
{"nome":"Ã“leo de motor","descricao":"Ã“leo lubrificante","valor":40.00,"quantidadeEstoque":20,"unidadeMedida":"L"}
```

Strings sÃ£o aparadas e placas sÃ£o convertidas para maiÃºsculas. Valores monetÃ¡rios
aceitam atÃ© 10 dÃ­gitos inteiros e duas casas decimais; valor e estoque nÃ£o podem ser
negativos. O estoque usa quantidade inteira conforme o modelo atual.
Nomes sÃ£o limitados a 120 caracteres, descriÃ§Ãµes de produto a 255.
O cadastro atual de cliente Ã© por CPF; suporte a CNPJ permanece pendente.

### Erros

Os erros seguem ProblemDetail: `status`, `title`, `detail` e, nos erros de validaÃ§Ã£o,
`campos` com as mensagens por atributo.

- 400: campos invÃ¡lidos, JSON malformado ou id com formato incorreto.
- 404: cadastro inexistente ou cliente informado no veÃ­culo nÃ£o encontrado.
- 409: CPF/placa duplicados ou exclusÃ£o bloqueada por relacionamentos existentes.

ExclusÃµes sÃ£o fÃ­sicas e respeitam as chaves estrangeiras. Exclua o veÃ­culo antes do
cliente; cadastros utilizados em ordens de serviÃ§o nÃ£o sÃ£o removidos em cascata.
A atualizaÃ§Ã£o de estoque deste CRUD substitui o saldo; reserva/consumo automÃ¡tico
por ordem de serviÃ§o ainda nÃ£o foi implementado.

Os endpoints ainda nÃ£o possuem autenticaÃ§Ã£o JWT, que continua sendo uma etapa
pendente do desafio. `/health-check` retorna `UP`.

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
