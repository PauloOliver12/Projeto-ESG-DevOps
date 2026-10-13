# Passo a passo para execução - ESG Compliance API

Este guia explica, do zero, como executar o projeto em outra máquina.

> **Entrega de 13/10/2026:** o projeto usa o modo **runner**, sem VM Azure. Consulte o README e o PDF para os resultados e a execução comprovada. As Partes 6 e 7 descrevem a alternativa opcional por SSH. O repositório e o pacote GHCR são privados.

- **Parte 1 a 4:** rodar a aplicação e os testes **na sua máquina** (é o que a maioria precisa).
- **Parte 5 a 8:** colocar o pipeline CI/CD para rodar no GitHub e fazer o deploy em **staging** e **produção**.
- **Parte 9:** solução de problemas comuns.

---

## Visão geral do projeto

| Item | Descrição |
|---|---|
| O que é | API REST (Java 21 + Spring Boot) de governança e compliance ambiental (ESG) |
| Banco de dados | Oracle Database Free 23 (roda em container, não precisa instalar) |
| Containers | `oracle-db` (banco) e `app` (API), orquestrados com Docker Compose |
| Porta da API | `8080` |
| Porta do banco | `1521` |
| Documentação da API | Swagger em `http://localhost:8080/swagger-ui.html` |

Usuários de acesso da API (ambiente local):

| Usuário | Senha | Pode fazer |
|---|---|---|
| `admin` | `Admin@123` | Tudo (criar, editar, excluir) |
| `auditor` | `Auditor@123` | Ler tudo, criar auditorias e alertas |
| `analyst` | `Analyst@123` | Somente leitura |

---

## Parte 1 - Pré-requisitos (instalar uma única vez)

Para **rodar a aplicação localmente** só é obrigatório o **Docker**. Java e Maven são opcionais.

### 1.1 Requisitos da máquina

- Windows 10/11, macOS ou Linux.
- Pelo menos **8 GB de RAM** (o Oracle sozinho usa ~2 GB).
- Cerca de **5 GB livres** em disco (imagens Docker).
- Internet (na primeira execução são baixados o Oracle e as dependências Maven).

### 1.2 Instalar o Docker

**Windows**

1. Abra o PowerShell **como Administrador** e execute:
   ```powershell
   wsl --install
   ```
2. Reinicie o computador.
3. Baixe e instale o **Docker Desktop**: https://www.docker.com/products/docker-desktop/
4. Abra o Docker Desktop e aguarde aparecer **"Engine running"** no canto inferior esquerdo.

**macOS**

1. Instale o **Docker Desktop** (escolha Apple Silicon ou Intel): https://www.docker.com/products/docker-desktop/
2. Abra o Docker Desktop e aguarde ficar em execução.

**Linux (Ubuntu)**

```bash
sudo apt-get update
sudo apt-get install -y docker.io docker-compose-v2
sudo usermod -aG docker $USER
# saia e entre novamente na sessão para aplicar o grupo
```

### 1.3 Conferir a instalação

Abra um terminal (PowerShell no Windows, Terminal no macOS/Linux) e execute:

```bash
docker --version
docker compose version
```

As duas linhas devem mostrar uma versão. Se `docker compose version` falhar, atualize o Docker Desktop.

### 1.4 (Opcional) Java e Maven

Só necessários se quiser rodar os testes **sem Docker** (ver Parte 4):

- **JDK 21** (Temurin): https://adoptium.net/
- **Maven 3.9+**: https://maven.apache.org/download.cgi

Conferir: `java -version` (deve mostrar 21) e `mvn -version`.

---

## Parte 2 - Executar a aplicação localmente com Docker

### 2.1 Extrair o projeto

1. Extraia o `.zip` recebido em uma pasta **sem acentos e sem espaços** no caminho, por exemplo:
   - Windows: `C:\projetos\Projeto-ESG-DevOps`
   - macOS/Linux: `~/projetos/Projeto-ESG-DevOps`
2. Abra um terminal **dentro dessa pasta** (é a pasta onde estão `Dockerfile` e `docker-compose.yml`):
   ```powershell
   cd C:\projetos\Projeto-ESG-DevOps
   ```
   Dica (Windows): no Explorador de Arquivos, abra a pasta, clique na barra de endereço, digite `powershell` e pressione Enter.

### 2.2 Criar o arquivo de variáveis de ambiente

O projeto lê as configurações (senhas, portas) de um arquivo `.env`. Crie-o a partir do modelo:

**Windows (PowerShell)**
```powershell
Copy-Item .env.example .env
```

**macOS/Linux**
```bash
cp .env.example .env
```

Não é necessário alterar nada no `.env` para rodar localmente.

### 2.3 Subir a aplicação

Com o Docker Desktop aberto, execute:

```bash
docker compose up -d --build
```

O que acontece:

1. O Docker constrói a imagem da API usando o `Dockerfile` (compila o projeto com Maven dentro do container).
2. Baixa a imagem do Oracle.
3. Sobe o banco, aguarda ele ficar saudável e só então sobe a API.
4. A API cria as tabelas e insere dados de exemplo automaticamente (Flyway).

> A **primeira execução demora** (5 a 15 minutos, dependendo da internet). As próximas são bem mais rápidas.

### 2.4 Acompanhar a inicialização

```bash
docker compose ps
```

Aguarde até as duas linhas mostrarem `healthy` na coluna STATUS:

```
NAME                  SERVICE     STATUS
esg-local-app-1       app         Up 2 minutes (healthy)
esg-local-oracle-db-1 oracle-db   Up 4 minutes (healthy)
```

Para ver os logs da API em tempo real (Ctrl+C para sair):

```bash
docker compose logs -f app
```

A API está pronta quando aparecer a linha `Started EsgComplianceApiApplication`.

---

## Parte 3 - Testar a aplicação

### 3.1 Pelo navegador (mais fácil)

| O que | Endereço |
|---|---|
| Saúde da aplicação | http://localhost:8080/actuator/health → deve mostrar `{"status":"UP",...}` |
| Ambiente e versão | http://localhost:8080/actuator/info → deve mostrar `"environment":"local"` |
| Swagger (testar endpoints) | http://localhost:8080/swagger-ui.html |

**Usando o Swagger:**

1. Acesse http://localhost:8080/swagger-ui.html
2. Clique no botão **Authorize** (cadeado, canto superior direito).
3. Informe usuário `admin` e senha `Admin@123` → **Authorize** → **Close**.
4. Abra um endpoint, por exemplo `GET /api/licenses` → **Try it out** → **Execute**.
5. A resposta (lista de licenças de exemplo) aparece logo abaixo.

### 3.2 Pelo terminal

**Windows (PowerShell)** - use `curl.exe` (no PowerShell, `curl` sozinho é outro comando):

```powershell
curl.exe http://localhost:8080/actuator/health
curl.exe -u analyst:Analyst@123 http://localhost:8080/api/dashboard/summary
curl.exe -u admin:Admin@123 http://localhost:8080/api/licenses
```

**macOS/Linux:**

```bash
curl http://localhost:8080/actuator/health
curl -u analyst:Analyst@123 http://localhost:8080/api/dashboard/summary
```

Sem usuário/senha a API deve responder **401** (isso é esperado - é a segurança funcionando):

```bash
curl.exe -i http://localhost:8080/api/licenses
```

### 3.3 Endpoints disponíveis

| Método | Endpoint | Quem pode |
|---|---|---|
| GET | `/api/licenses` e `/api/licenses/{id}` | todos |
| POST / PUT / DELETE | `/api/licenses` | admin |
| GET | `/api/audits` | todos |
| POST | `/api/audits` | admin, auditor |
| GET | `/api/alerts` e `/api/alerts/open` | todos |
| POST | `/api/alerts` | admin, auditor |
| PATCH | `/api/alerts/{id}/resolve` | admin, auditor |
| GET | `/api/dashboard/summary` | todos |

### 3.4 Parar a aplicação

```bash
docker compose down        # para os containers e MANTÉM os dados do banco
docker compose down -v     # para e APAGA os dados do banco (recomeça do zero)
```

Para subir de novo: `docker compose up -d` (o `--build` só é necessário se o código mudar).

---

## Parte 4 - Rodar os testes automatizados

Os testes **não precisam do banco** (usam simulações com Mockito/MockMvc).

### Opção A - Com Java e Maven instalados

```bash
mvn test
```

### Opção B - Sem instalar Java (usando Docker)

**Windows (PowerShell)**
```powershell
docker run --rm -v "${PWD}:/app" -w /app maven:3.9.9-eclipse-temurin-21 mvn -B test
```

**macOS/Linux**
```bash
docker run --rm -v "$(pwd):/app" -w /app maven:3.9.9-eclipse-temurin-21 mvn -B test
```

Resultado esperado no final:

```
[INFO] Tests run: 21, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

Os relatórios ficam em `target/surefire-reports/`.

---

## Parte 5 - Colocar o projeto no GitHub (pipeline CI/CD)

O pipeline fica em `.github/workflows/` e roda automaticamente no **GitHub Actions**.

### 5.1 Criar o repositório

1. Acesse https://github.com e faça login.
2. Clique em **+** (canto superior direito) → **New repository**.
3. Nesta entrega, o repositório já foi criado como `PauloOliver12/Projeto-ESG-DevOps`, com visibilidade **Private**. Ao reproduzir em outro repositório, escolha o nome e mantenha a visibilidade privada.
4. **Não** marque "Add a README" (o projeto já tem). Clique em **Create repository**.

### 5.2 Enviar o código

**Opção A - Git pelo terminal** (instale o Git: https://git-scm.com/downloads)

Dentro da pasta do projeto:

```bash
git init
git add .
git commit -m "Projeto ESG com pipeline CI/CD"
git branch -M main
git remote add origin https://github.com/SEU-USUARIO/esg-compliance-api.git
git push -u origin main
```

**Opção B - GitHub Desktop** (https://desktop.github.com/)

1. **File → Add local repository** → selecione a pasta do projeto → **create a repository**.
2. **Publish repository**.

> Importante: confira no GitHub se a pasta **`.github/workflows`** foi enviada. Ela é oculta em alguns sistemas e, sem ela, o pipeline não existe.
> O arquivo `.env` **não** deve ser enviado (ele já está no `.gitignore`).

### 5.3 Ver o pipeline rodando

1. No repositório, abra a aba **Actions**.
2. O workflow **"CI/CD - ESG Compliance API"** inicia sozinho após o push.

| Etapa | Funciona sem configurar nada? |
|---|---|
| 1. Build e testes | Sim |
| 2. Teste de integração (Docker Compose) | Sim |
| 3. Publicar imagem Docker (GHCR) | Sim |
| 4. Deploy staging | Sim - no runner do GitHub (modo **runner**) |
| 5. Deploy produção | Sim - no runner do GitHub (modo **runner**) |

Sem servidor configurado, os deploys de staging e produção são feitos no próprio runner do GitHub Actions: a stack sobe com a imagem publicada, o smoke test roda e as evidências ficam nos artefatos `evidencias-staging` e `evidencias-production` da execução. Ao configurar o servidor (Partes 6 e 7), o mesmo pipeline passa a publicar na VM automaticamente (modo **servidor**).

> O job de publicação já declara `packages: write`. Só revise permissões adicionais se uma política efetiva bloquear a publicação; não é necessário ampliar globalmente as permissões do workflow para esta entrega.

Para rodar o pipeline manualmente: **Actions → CI/CD - ESG Compliance API → Run workflow → Run workflow**.

---

## Parte 6 - Alternativa opcional: servidor para staging e produção

Nesta alternativa, os deploys são feitos em um servidor Linux com Docker, acessado via SSH. O exemplo abaixo usa Azure; é necessário conferir a elegibilidade e os custos da conta antes de provisionar recursos. Esta entrega utilizou apenas o runner do GitHub Actions.

### 6.1 Criar a máquina virtual (Azure)

1. Acesse https://portal.azure.com → **Máquinas virtuais** → **Criar** → **Máquina virtual do Azure**.
2. Preencha:

| Campo | Valor |
|---|---|
| Grupo de recursos | Criar novo: `rg-esg` |
| Nome da VM | `vm-esg` |
| Região | A mais próxima disponível (ex.: Brazil South ou East US) |
| Imagem | **Ubuntu Server 24.04 LTS** |
| Tamanho | **Standard_B2ms** (2 vCPU, 8 GB) - recomendado para staging + produção na mesma VM |
| Tipo de autenticação | **Chave pública SSH** |
| Nome de usuário | `azureuser` |
| Origem da chave | Gerar novo par de chaves (nome: `vm-esg_key`) |
| Portas de entrada | Permitir **SSH (22)** |

3. **Revisar + criar** → **Criar** → **Baixar a chave privada e criar o recurso**.
4. Guarde o arquivo **`vm-esg_key.pem`** baixado (ele é a chave privada - não compartilhe).
5. Quando terminar, abra a VM e anote o **Endereço IP público**.

> Se o tamanho B2ms não estiver disponível, use outro com 8 GB (ex.: D2s_v3). Com 4 GB é possível, mas use uma VM para cada ambiente.

### 6.2 Liberar as portas da aplicação

Na VM: **Rede (Networking)** → **Adicionar regra de porta de entrada**:

| Porta | Uso | Nome sugerido |
|---|---|---|
| 8081 | Staging | `staging` |
| 8080 | Produção | `production` |

Protocolo **TCP**, ação **Permitir**.

### 6.3 Conectar via SSH

**Windows (PowerShell)** - primeiro ajuste a permissão da chave (o SSH recusa chaves "abertas"):

```powershell
cd $HOME\Downloads
icacls vm-esg_key.pem /inheritance:r
icacls vm-esg_key.pem /grant:r "$($env:USERNAME):(R)"
ssh -i vm-esg_key.pem azureuser@IP_DA_VM
```

**macOS/Linux**

```bash
chmod 600 ~/Downloads/vm-esg_key.pem
ssh -i ~/Downloads/vm-esg_key.pem azureuser@IP_DA_VM
```

Na primeira conexão, digite `yes` para confirmar. Para sair do servidor: `exit`.

### 6.4 Preparar o servidor (instalar Docker)

No **seu computador**, dentro da pasta do projeto:

```bash
scp -i CAMINHO\vm-esg_key.pem scripts/setup-server.sh azureuser@IP_DA_VM:~
ssh -i CAMINHO\vm-esg_key.pem azureuser@IP_DA_VM "bash setup-server.sh"
```

O script instala Docker + Docker Compose, cria uma área de swap de 4 GB e as pastas `~/esg-api/staging` e `~/esg-api/production`. No final deve mostrar as versões do Docker e a mensagem **"Servidor pronto"**.

---

## Parte 7 - Configurar os ambientes no GitHub

### 7.1 Criar os environments

No repositório: **Settings → Environments → New environment**.

1. Crie o environment **`staging`**.
2. Crie o environment **`production`**. Nele, marque **Required reviewers**, adicione seu usuário e clique em **Save protection rules** (assim o deploy em produção espera sua aprovação).

### 7.2 Cadastrar secrets e variáveis

Dentro de **cada** environment (`staging` e `production`):

- **Add environment secret** para cada linha "Secret".
- **Add environment variable** para cada linha "Variable".

| Tipo | Nome | Valor em `staging` | Valor em `production` |
|---|---|---|---|
| Secret | `SSH_HOST` | IP público da VM | IP público da VM |
| Secret | `SSH_USER` | `azureuser` | `azureuser` |
| Secret | `SSH_PRIVATE_KEY` | Conteúdo completo do `vm-esg_key.pem` | Conteúdo completo do `vm-esg_key.pem` |
| Secret | `ORACLE_PASSWORD` | ex.: `OracleStg2026` | ex.: `OracleProd2026` |
| Secret | `DB_PASSWORD` | ex.: `DbStg2026` | ex.: `DbProd2026` |
| Secret | `ADMIN_PASSWORD` | ex.: `AdminStg@2026` | ex.: `AdminProd@2026` |
| Secret | `AUDITOR_PASSWORD` | ex.: `AuditorStg@2026` | ex.: `AuditorProd@2026` |
| Secret | `ANALYST_PASSWORD` | ex.: `AnalystStg@2026` | ex.: `AnalystProd@2026` |
| Variable | `APP_PORT` | `8081` | `8080` |
| Variable | `APP_URL` | `http://IP_DA_VM:8081/swagger-ui.html` | `http://IP_DA_VM:8080/swagger-ui.html` |

Observações:

- **`SSH_PRIVATE_KEY`:** abra o `.pem` no Bloco de Notas e copie **tudo**, incluindo as linhas `-----BEGIN ... KEY-----` e `-----END ... KEY-----`.
- **Senhas:** use apenas letras, números e `@` ou `_` (sem espaços, `#`, `$`, aspas). Use valores **seus**, não os exemplos.
- Variáveis opcionais: `SSH_PORT` (padrão `22`) e `JAVA_OPTS` (padrão `-Xms256m -Xmx512m`).
- Se usar **duas VMs** (uma por ambiente), basta colocar o IP/chave de cada uma no seu environment.

---

## Parte 8 - Executar o deploy em staging e produção

### 8.1 Disparar o pipeline

Faça um novo push na branch `main` **ou** vá em **Actions → CI/CD - ESG Compliance API → Run workflow**.

### 8.2 Acompanhar

1. Etapas 1, 2 e 3 rodam automaticamente (cerca de 10 minutos no total).
2. **4. Deploy staging** roda em seguida. Na primeira vez o Oracle é criado no servidor e pode levar alguns minutos - o smoke test aguarda até 10 minutos.
3. **5. Deploy producao** fica como **"Waiting"**. Clique em **Review deployments** → marque `production` → **Approve and deploy**.

Ao final, todas as etapas ficam com o ícone verde ✔.

### 8.3 Conferir os ambientes

| Ambiente | Swagger | Info (mostra o ambiente) |
|---|---|---|
| Staging | `http://IP_DA_VM:8081/swagger-ui.html` | `http://IP_DA_VM:8081/actuator/info` → `"environment":"staging"` |
| Produção | `http://IP_DA_VM:8080/swagger-ui.html` | `http://IP_DA_VM:8080/actuator/info` → `"environment":"production"` |

No Swagger de cada ambiente, use as **senhas cadastradas nos secrets** daquele ambiente (não as senhas locais).

No servidor (via SSH), para ver os containers:

```bash
docker ps
cd ~/esg-api/staging && docker compose -p esg-staging ps
cd ~/esg-api/production && docker compose -p esg-production logs --tail=50 app
```

### 8.4 Prints sugeridos para a entrega

Salve em `docs/prints/`:

1. Aba Actions com o pipeline completo verde.
2. Etapa "Build e testes" com o resumo dos testes.
3. Etapa "Teste de integração".
4. Imagem publicada (repositório → **Packages**).
5. Log do "Deploy staging" com o smoke test OK.
6. Tela de aprovação e "Deploy producao" concluído.
7. Navegador em `/actuator/info` e Swagger de **staging**.
8. Navegador em `/actuator/info` e Swagger de **produção**.
9. Terminal do servidor com `docker ps`.

---

## Parte 9 - Problemas comuns

| Problema | Causa provável | Solução |
|---|---|---|
| `docker: command not found` / `error during connect` | Docker Desktop fechado ou não instalado | Abra o Docker Desktop e aguarde "Engine running" |
| `port is already allocated` | Porta 8080 ou 1521 já em uso | No `.env`, altere `APP_PORT=8090` e/ou `DB_PORT=1522` e rode `docker compose up -d` |
| Container `oracle-db` fica `unhealthy` ou reiniciando | Pouca memória para o Docker | Docker Desktop → Settings → Resources: aumente a memória para 4 GB ou mais. Veja `docker compose logs oracle-db` |
| API reiniciando / `health` não fica UP | Banco ainda subindo ou erro de configuração | Aguarde alguns minutos; veja `docker compose logs app` |
| No PowerShell, `curl` mostra erro estranho | `curl` é apelido de outro comando | Use `curl.exe` |
| `$'\r': command not found` ao rodar `.sh` | Script salvo com quebra de linha do Windows (CRLF) | No VS Code, clique em `CRLF` na barra inferior → `LF` → salve |
| Erro 401 no Swagger | Sem login ou senha errada | Clique em **Authorize**. Em staging/produção use as senhas dos secrets |
| Erro 403 | Usuário sem permissão | Use `admin` para criar/editar/excluir |
| Quero recomeçar o banco do zero | Dados antigos no volume | `docker compose down -v` e depois `docker compose up -d` |
| Pipeline: `'SSH_HOST' nao configurado` | Secrets não cadastrados no environment | Revise a Parte 7 (nomes exatamente iguais, no environment certo) |
| Pipeline: `Permission denied (publickey)` | Chave ou usuário SSH incorretos | Confira `SSH_USER` e se `SSH_PRIVATE_KEY` contém o `.pem` completo |
| Pipeline: `denied` ao baixar a imagem no servidor | Permissão do pacote no GHCR | GitHub → seu perfil → **Packages** → `esg-compliance-api` → **Package settings** → **Manage Actions access** → adicione o repositório |
| Pipeline: smoke test estoura o tempo no deploy | VM sem memória suficiente | Use VM de 8 GB ou uma VM por ambiente; veja os logs na própria etapa que falhou |
| Pipeline: erro ao publicar imagem (`permission_denied: write_package`) | Permissão efetiva ou acesso ao pacote | Conferir `packages: write` no job, políticas aplicáveis e vínculo do pacote com o repositório |

---

## Resumo rápido (só rodar localmente)

```bash
# dentro da pasta do projeto, com o Docker Desktop aberto
cp .env.example .env              # Windows: Copy-Item .env.example .env
docker compose up -d --build
docker compose ps                 # aguarde "healthy"
# abra http://localhost:8080/swagger-ui.html  (admin / Admin@123)
docker compose down               # para encerrar
```
