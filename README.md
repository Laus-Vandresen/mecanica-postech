# Mecânica Postech

API REST para gestão de uma oficina mecânica: cadastro de clientes, veículos, peças, insumos, controle de estoque e fluxo completo de ordens de serviço (diagnóstico, orçamento, aprovação, execução, finalização e entrega).

## Sumário

- [Tecnologias](#tecnologias)
- [Arquitetura](#arquitetura)
- [Pré-requisitos](#pré-requisitos)
- [Configuração](#configuração)
- [Como executar](#como-executar)
- [Documentação da API (Swagger)](#documentação-da-api-swagger)
- [Autenticação](#autenticação)
- [Testes](#testes)
- [Cobertura (JaCoCo)](#cobertura-jacoco)
- [Análise estática (SonarQube)](#análise-estática-sonarqube)
- [Banco de dados e migrations](#banco-de-dados-e-migrations)
- [Docker](#docker)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Documentação adicional](#documentação-adicional)

## Tecnologias

| Tecnologia | Versão | Uso |
|---|---|---|
| Java | 21 | Linguagem |
| Spring Boot | 4.1.1 | Framework base (Web MVC, Data JPA, Validation, Security) |
| Spring Security + OAuth2 Resource Server | — | Autenticação via JWT (Bearer) |
| PostgreSQL | 17 | Banco de dados (AWS RDS em produção) |
| Flyway | — | Versionamento de schema |
| Hibernate / JPA | — | Persistência |
| springdoc-openapi | 3.1.1 | Swagger UI e geração do `/v3/api-docs` |
| openapi-generator | 7.26.0 | Geração das interfaces e DTOs a partir de `api.yaml` (API first) |
| ModelMapper | 3.2.4 | Mapeamento entre DTOs e entidades |
| Lombok | — | Redução de boilerplate |
| JUnit 5, Mockito, Spring Test, H2, Testcontainers | — | Testes unitários e de integração |
| JaCoCo | 0.8.16 | Cobertura de testes |
| SonarQube (Community) + sonar-maven-plugin | 5.8.0 | Análise estática |
| Maven Wrapper | — | Build (`mvnw` / `mvnw.cmd`) |
| Docker / Docker Compose | — | Empacotamento da aplicação e SonarQube local |

## Arquitetura

O projeto segue a abordagem **API first**: o contrato em `src/main/resources/openapi/api.yaml` é a fonte da verdade. No build, o `openapi-generator-maven-plugin` gera as interfaces dos controllers (`br.com.mecanica.api`) e os DTOs (`br.com.mecanica.api.dto`) em `target/generated-sources/openapi`. Os controllers em `br.com.mecanica.controller` implementam essas interfaces.

Camadas principais:

- `controller` — implementação das interfaces geradas e mappers de DTO.
- `service` — orquestração de casos de uso e transações.
- `domain` — entidades, value objects, serviços de domínio e exceções de negócio.
- `repository` — repositórios Spring Data JPA.
- `core` — configurações transversais (segurança/JWT, OpenAPI, ModelMapper).

Todos os identificadores expostos pela API são UUIDs.

## Pré-requisitos

- JDK 21 (com `JAVA_HOME` configurado)
- Docker e Docker Compose (para SonarQube, testes com Testcontainers e empacotamento)
- Acesso a um PostgreSQL (RDS da AWS ou instância local)

Não é necessário instalar o Maven; use o wrapper incluído no projeto.

## Configuração

A aplicação carrega variáveis de ambiente a partir de um arquivo `.env` na raiz (`spring.config.import=optional:file:./.env[.properties]`). Esse arquivo está no `.gitignore` e no `.dockerignore`; nunca versione credenciais.

1. Copie o exemplo:

   ```powershell
   Copy-Item .env.example .env
   ```

2. Preencha as variáveis:

| Variável | Obrigatória | Padrão | Descrição |
|---|---|---|---|
| `SPRING_PROFILES_ACTIVE` | sim | — | Perfil ativo. Use `aws` para conectar ao RDS |
| `DB_HOST` | não | endpoint do RDS | Host do PostgreSQL |
| `DB_PORT` | não | `5432` | Porta do PostgreSQL |
| `DB_NAME` | não | `mecanica` | Nome do banco |
| `DB_USERNAME` | não | `postgres` | Usuário do banco |
| `DB_PASSWORD` | sim | — | Senha do banco |
| `DB_SSL_ROOT_CERT` | não | `certs/us-east-1-bundle.pem` | Certificado CA para TLS `verify-full` |
| `JWT_SECRET` | sim | — | Segredo HMAC do JWT. Mínimo 32 bytes em UTF-8; use um valor aleatório forte |
| `JWT_EXPIRATION_SECONDS` | não | `3600` | Validade do token em segundos |

O perfil `aws` (`application-aws.properties`) conecta ao RDS com `sslmode=verify-full`, usando o certificado público em `certs/`.

## Como executar

### Via Maven (recomendado para desenvolvimento)

```powershell
$env:SPRING_PROFILES_ACTIVE = 'aws'
.\mvnw.cmd spring-boot:run
```

No Linux/macOS:

```bash
SPRING_PROFILES_ACTIVE=aws ./mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080/mecanica` (context path `/mecanica`). O Flyway aplica as migrations pendentes na inicialização e o Hibernate valida o schema (`ddl-auto=validate`).

### Via IDE (IntelliJ)

Execute a classe `MecanicaApplication` com a variável de ambiente `SPRING_PROFILES_ACTIVE=aws` e o diretório de trabalho apontando para a raiz do projeto (para que o `.env` seja encontrado).

### Gerar o JAR

```powershell
.\mvnw.cmd clean package
```

Para pular os testes: `.\mvnw.cmd clean package -DskipTests`. O artefato fica em `target/mecanica-0.0.1-SNAPSHOT.jar`.

## Documentação da API (Swagger)

Com a aplicação em execução:

| Recurso | URL |
|---|---|
| Swagger UI | <http://localhost:8080/mecanica/swagger-ui.html> |
| OpenAPI JSON | <http://localhost:8080/mecanica/v3/api-docs> |
| OpenAPI YAML | <http://localhost:8080/mecanica/v3/api-docs.yaml> |

Essas rotas são públicas (não exigem token). Para chamar endpoints protegidos pelo Swagger UI, clique em **Authorize**, informe o token no esquema `bearerAuth` (apenas o token, sem o prefixo `Bearer`) e confirme.

Para alterar a API, edite `src/main/resources/openapi/api.yaml` e rode um build (`.\mvnw.cmd compile`) para regenerar interfaces e DTOs. Em seguida, ajuste os controllers para implementar as mudanças.

### Principais endpoints

| Grupo | Rotas |
|---|---|
| Autenticação | `POST /v1/auth/registrar`, `POST /v1/auth/autenticar` |
| Clientes | `GET/POST /v1/clientes`, `GET/DELETE /v1/clientes/{id}` |
| Veículos | `GET/POST /v1/veiculos`, `GET/DELETE /v1/veiculos/{id}` |
| Peças | `GET/POST /v1/pecas`, `GET/DELETE /v1/pecas/{id}` |
| Insumos | `GET/POST /v1/insumos`, `GET/DELETE /v1/insumos/{id}` |
| Estoque | `POST /v1/produtos/{id}/estoque` (ENTRADA / SAIDA) |
| Ordem de serviço | `GET/POST /v1/ordem-servico`, `GET /v1/ordem-servico/{id}`, `GET /v1/ordem-servico/tempo-medio-finalizacao` e as transições de estado: `diagnostico/inicio`, `diagnostico`, `orcamento`, `aprovacao`, `recusa`, `execucao/inicio`, `produtos/{produtoId}/consumo`, `execucao/conclusao`, `finalizacao`, `entrega`, `cancelamento` |

Consulte o Swagger UI para payloads, validações e códigos de resposta.

## Autenticação

Todas as rotas, exceto registro, login e Swagger, exigem um JWT no header `Authorization: Bearer <token>`.

1. Registrar um usuário:

   ```http
   POST /mecanica/v1/auth/registrar
   Content-Type: application/json

   {"nome": "string", "login": "string", "senha": "string"}
   ```

2. Autenticar e obter o token:

   ```http
   POST /mecanica/v1/auth/autenticar
   Content-Type: application/json

   {"login": "string", "senha": "string"}
   ```

3. Usar o token nas demais chamadas:

   ```http
   GET /mecanica/v1/clientes
   Authorization: Bearer <token>
   ```

O token é assinado com `JWT_SECRET` e expira após `JWT_EXPIRATION_SECONDS`.

## Testes

```powershell
.\mvnw.cmd test
```

Tipos de teste no projeto:

- **Unitários** (JUnit 5 + Mockito): services, serviços de domínio, entidades e value objects. Não sobem o contexto Spring nem acessam banco. Para rodar apenas eles: `.\mvnw.cmd '-Dtest=*ServiceTest' test`.
- **Integração com H2**: testes de API (`MockMvc`) usando o perfil `test` (`src/test/resources/application-test.properties`), com H2 em memória e JWT de teste.
- **Integração com PostgreSQL**: classes que estendem `PostgresApiTest` sobem um container `postgres:17-alpine` via Testcontainers. Exigem Docker em execução. Alternativamente, aponte para um banco local exclusivo com `-Dtest.postgres.url`, `-Dtest.postgres.username` e `-Dtest.postgres.password`.

Nenhum teste acessa o RDS.

## Cobertura (JaCoCo)

```powershell
.\mvnw.cmd clean verify
```

Gera o relatório em `target/site/jacoco/index.html` (HTML) e `target/site/jacoco/jacoco.xml` (consumido pelo SonarQube). O código gerado pelo OpenAPI (`br.com.mecanica.api.**`) fica fora da medição.

A fase `verify` também aplica uma regra de cobertura mínima de **80% de linhas** nas classes críticas do domínio (`OrdemServico`, `OrdemServicoDomainService`, `OrdemServicoService`, `Dinheiro`, `Estoque`, `OperacaoEstoque`, `TempoMedioFinalizacao`). Se a regra falhar, o build falha.

## Análise estática (SonarQube)

O SonarQube Community está definido no `docker-compose.yml`.

1. Suba apenas o SonarQube:

   ```powershell
   docker compose up -d sonarqube
   ```

2. Acesse <http://localhost:9000>, entre com `admin` / `admin`, troque a senha e gere um token em **My Account > Security > Generate Tokens**.

3. Exporte o token no terminal:

   ```powershell
   $env:SONAR_TOKEN = 'token-gerado-no-sonarqube'
   ```

4. Rode testes, cobertura e análise em uma única chamada:

   ```powershell
   .\mvnw.cmd --batch-mode clean verify sonar:sonar -Dsonar.host.url=http://localhost:9000
   ```

O resultado aparece no projeto `Mecanica Postech` (chave `mecanica-postech`) em <http://localhost:9000>. A regra de 80% de cobertura precisa passar para a análise ser executada. Os dados persistem em volumes Docker; use `docker compose stop sonarqube` para parar sem apagar e `docker compose down -v` para remover tudo.

Detalhes em [docs/sonarqube-local.md](docs/sonarqube-local.md).

## Banco de dados e migrations

As migrations ficam em `src/main/resources/db/migration` (SQL) e `src/main/java/db/migration` (Java, ex.: `V13__migrar_ids_para_uuid`). O Flyway roda automaticamente na inicialização com `validate-on-migrate=true` e `clean-disabled=true`.

Regras:

- Nunca edite uma migration já aplicada; crie uma nova versão.
- O Hibernate roda com `ddl-auto=validate`, então toda alteração de entidade precisa de migration correspondente.
- Antes de subir uma versão com migration estrutural em banco compartilhado, faça backup/snapshot e pare as instâncias antigas da aplicação.

## Docker

O `Dockerfile` empacota um JAR já gerado em uma imagem `eclipse-temurin:21-jre`, com o perfil `aws` ativo e o certificado do RDS incluído.

```powershell
.\mvnw.cmd clean package -DskipTests
docker compose up --build app
```

A aplicação fica disponível em `http://localhost:8080/mecanica` e lê as variáveis do `.env` via `env_file`. O PostgreSQL continua no RDS; o Compose não sobe um banco para a aplicação.

Para subir aplicação e SonarQube juntos: `docker compose up --build`.

## Estrutura do projeto

```
.
├── certs/                          # Certificado CA do RDS (TLS verify-full)
├── docs/                           # Documentação complementar
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── br/com/mecanica/
│   │   │   │   ├── controller/     # Implementação das interfaces OpenAPI + mappers
│   │   │   │   ├── core/           # config, security (JWT), openapi, modelmapper
│   │   │   │   ├── domain/         # entity, valueObject, service, exception
│   │   │   │   ├── entity/         # Entidades JPA e mappers
│   │   │   │   ├── exception/      # Handlers HTTP
│   │   │   │   ├── repository/     # Spring Data JPA
│   │   │   │   └── service/        # Serviços de aplicação
│   │   │   └── db/migration/       # Migrations Flyway em Java
│   │   └── resources/
│   │       ├── db/migration/       # Migrations Flyway em SQL
│   │       ├── openapi/api.yaml    # Contrato OpenAPI (fonte da verdade)
│   │       ├── application.properties
│   │       └── application-aws.properties
│   └── test/                       # Unitários, H2 e Testcontainers
├── .env.example                    # Modelo de variáveis de ambiente
├── docker-compose.yml              # app + sonarqube
├── Dockerfile
└── pom.xml
```

## Documentação adicional

- [Consumo de estoque na ordem de serviço](docs/estoque-ordem-servico.md)
- [SonarQube local](docs/sonarqube-local.md)
