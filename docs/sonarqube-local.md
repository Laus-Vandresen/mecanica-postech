# SonarQube local

O SonarQube Community roda junto da aplicação no `docker-compose.yml`. Inicie-o
sem subir a aplicação:

```powershell
docker compose up -d sonarqube
```

Na primeira execução, abra <http://localhost:9000>, entre com `admin` / `admin`,
troque a senha e crie um token em **My Account > Security > Generate Tokens**.
Defina o token no terminal PowerShell:

```powershell
$env:SONAR_TOKEN = 'token-gerado-no-sonarqube'
```

Rode testes e análise na mesma chamada Maven:

```powershell
.\mvnw.cmd --batch-mode clean verify sonar:sonar -Dsonar.host.url=http://localhost:9000
```

A fase `verify` gera o relatório JaCoCo em `target/site/jacoco/jacoco.xml`, importado pelo SonarQube. O código gerado pelo OpenAPI é excluído da cobertura. A regra existente de 80% por classe crítica precisa passar para o comando seguir até a análise SonarQube. Consulte [a documentação de cobertura](cobertura-testes.md).

Os resultados, incluindo vulnerabilidades encontradas, ficam no painel do projeto
em <http://localhost:9000>. Os dados do SonarQube persistem nos volumes Docker.
Para pará-lo sem apagar os dados, execute `docker compose stop sonarqube`; para
remover os dados locais também, execute `docker compose down -v`.

O SonarQube usa seu banco local embutido para manter a configuração simples; ele
não usa o banco da aplicação. Também é possível apontá-lo para a mesma instância
RDS da AWS, mas é necessário criar um banco e credenciais exclusivos para o
SonarQube. Não use o banco `mecanica` da aplicação, pois o SonarQube cria e altera
suas próprias tabelas. Usar o RDS também exige liberar a conexão de rede da máquina
local e configurar essas credenciais no Compose.

A análise é estática do código e não substitui uma análise de CVEs nas
dependências. Valide os achados antes de tratá-los como vulnerabilidades
exploráveis.
