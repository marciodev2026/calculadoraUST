# Docker e Deploy — UST Gov Calculator

## Visão Geral

| Arquivo | Uso |
|---------|-----|
| `docker-compose.yml` | Stack completa (produção): Nginx, frontend, backend, PostgreSQL, Mailpit |
| `docker-compose.dev.yml` | PostgreSQL + Mailpit para desenvolvimento local |
| `docker-compose.db.yml` | PostgreSQL + Flyway standalone (conectado à rede do backend) |

## Arquitetura (Produção)

```
Internet :80
    │
    ▼
┌─────────┐     ┌──────────┐     ┌─────────┐
│  Nginx  │────▶│ Frontend │     │ Backend │
│ (proxy) │     │  (Vue)   │     │ (Java)  │
│         │────▶│          │     │         │
└─────────┘     └──────────┘     └────┬────┘
                                      │
                         ┌────────────┼────────────┐
                         ▼            ▼            ▼
                  ┌────────────┐ ┌─────────┐ ┌─────────┐
                  │ PostgreSQL │ │ Mailpit │ │ Storage │
                  │     17     │ │ SMTP/UI │ │ PDF/Logo│
                  └────────────┘ └─────────┘ └─────────┘
```

- **Nginx** — porta 80 pública; proxy `/api` → backend, `/` → frontend
- **Frontend** — build Vue 3 + Nginx servindo arquivos estáticos
- **Backend** — Spring Boot 21; Flyway na inicialização
- **PostgreSQL** — dados persistentes em volume Docker
- **Mailpit** — captura de e-mails (SMTP 1025, UI 8025)

## Portas por ambiente

| Ambiente | Frontend | API | Mailpit |
|----------|----------|-----|---------|
| **Docker** (`docker compose up`) | http://localhost | http://localhost/api | http://localhost:8025 |
| **Dev local** (`start-dev.ps1`) | http://localhost:5173 | http://localhost:8080 | http://localhost:8025 |

## Pré-requisitos

- Docker 24+
- Docker Compose v2+
- 4 GB RAM disponível (build)

## Deploy Completo

### 1. Configurar variáveis

```bash
cp .env.example .env
# Edite .env — altere JWT_SECRET e DB_PASSWORD em produção
```

### 2. Subir a stack

```bash
docker compose up -d --build
```

### 3. Acessar

| Recurso | URL |
|---------|-----|
| Aplicação | http://localhost |
| API Health | http://localhost/api/health |
| Swagger | http://localhost/swagger-ui.html |
| Mailpit | http://localhost:8025 |

### 4. Credenciais padrão

| E-mail | Senha | Perfil |
|--------|-------|--------|
| admin@ust.gov.br | admin123 | ADMIN |
| gestor@ust.gov.br | gestor123 | GESTOR |
| analista@ust.gov.br | analista123 | ANALISTA |

## Desenvolvimento Local

### Opção A — Banco + Mailpit via Docker

```bash
docker compose -f docker-compose.dev.yml up -d
cd backend && mvn spring-boot:run
cd frontend && npm run dev
```

- Frontend: http://localhost:5173
- Backend: http://localhost:8080
- PostgreSQL: localhost:5432
- Mailpit: http://localhost:8025

### Opção B — Banco + Flyway standalone

```bash
docker compose -f docker-compose.db.yml up -d
cd backend && mvn spring-boot:run
cd frontend && npm run dev
```

> O `docker-compose.db.yml` mantém o PostgreSQL na rede `calculadoragov_ust-network` para compatibilidade com o backend Docker.

## Comandos Úteis

```bash
# Ver logs
docker compose logs -f backend

# Parar stack
docker compose down

# Parar e remover volumes (CUIDADO: apaga dados)
docker compose down -v

# Rebuild forçado
docker compose up -d --build --force-recreate
```

## Volumes

| Volume | Conteúdo |
|--------|----------|
| `postgres_data` | Dados PostgreSQL |
| `reports_data` | Relatórios PDF/Excel gerados |
| `branding_data` | Logo do órgão |

## Estrutura Docker

```
docker/
├── backend/Dockerfile    # Multi-stage Maven + JRE 21
├── frontend/Dockerfile   # Multi-stage Node + Nginx
├── frontend/nginx.conf   # SPA Vue (try_files)
├── nginx/nginx.conf      # Reverse proxy principal
└── postgres/init.sql     # Init opcional
```

## Variáveis de Ambiente

| Variável | Padrão | Descrição |
|----------|--------|-----------|
| `APP_PORT` | 80 | Porta pública do Nginx |
| `DB_NAME` | ust_calculator | Nome do banco |
| `DB_USER` | ust_user | Usuário PostgreSQL |
| `DB_PASSWORD` | ust_pass | Senha PostgreSQL |
| `JWT_SECRET` | (ver .env.example) | Chave JWT HS256 |
| `REPORTS_STORAGE_PATH` | /app/storage/relatorios | Armazenamento de relatórios |
| `MAIL_HOST` | mailpit | Servidor SMTP |
| `MAIL_PORT` | 1025 | Porta SMTP |
| `MAILPIT_UI_URL` | http://localhost:8025 | Interface Mailpit |

## Troubleshooting

### Backend não inicia

```bash
docker compose logs backend
```

Verifique se o PostgreSQL está healthy e as credenciais coincidem.

### Backend não conecta ao PostgreSQL (`UnknownHostException: postgres`)

Ocorre quando o PostgreSQL foi iniciado fora da rede Docker do backend. Solução:

```bash
docker network connect --alias postgres calculadoragov_ust-network ust-postgres
docker restart ust-backend
```

Ou suba tudo com `docker compose up -d` em vez de apenas o banco.

### Porta 1025 em uso (Mailpit)

Pare o Mailpit local antes de subir o Docker:

```powershell
Get-Process -Name mailpit -ErrorAction SilentlyContinue | Stop-Process -Force
```

### Erro de build Maven/Node

```bash
docker compose build --no-cache backend
docker compose build --no-cache frontend
```

### Porta 80 em uso

Altere `APP_PORT=8080` no `.env` e acesse http://localhost:8080
