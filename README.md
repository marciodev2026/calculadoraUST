# UST Gov

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![Vue.js](https://img.shields.io/badge/Vue.js-3-42b883?logo=vuedotjs)](https://vuejs.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-336791?logo=postgresql)](https://www.postgresql.org/)

Sistema web para **simulação de esforço e custo em UST** (Unidade de Serviço Técnico), voltado a órgãos governamentais. Permite montagem de squads, cálculo por **FCP** (Fator de Complexidade Profissional), gestão de projetos e sustentações, dashboard executivo, visualização de relatórios em tela e exportação PDF/Excel.

---

## Início Rápido (Docker)

```bash
cp .env.example .env
docker compose up -d --build
```

Acesse **http://localhost** (porta 80 via Docker) e faça login:

> **Docker** usa http://localhost (porta 80). **Desenvolvimento local** usa http://localhost:5173 (Vite). São ambientes diferentes — não rode os dois ao mesmo tempo.

| E-mail | Senha | Perfil |
|--------|-------|--------|
| admin@ust.gov.br | admin123 | Administrador |
| gestor@ust.gov.br | gestor123 | Gestor |
| analista@ust.gov.br | analista123 | Analista |

> Em produção, altere `JWT_SECRET` e `DB_PASSWORD` no arquivo `.env`.

---

## Fluxo da Aplicação

```
Etapa 1 — Identificação     →  Simulação (solicitante, órgão, data)
Etapa 4 — Projetos          →  Projetos, sustentações, evoluções
Etapa 5 — Montagem de Squad →  Perfis + quantidades → cálculo UST
Dashboard                   →  Indicadores e gráficos consolidados
Relatórios                  →  Visualização em tela → PDF / Excel
Configurações               →  UST, identidade (logo e nome do órgão)
```

### Passo a passo

1. **Login** como Analista, Gestor ou Administrador
2. **Simulações** → Nova Simulação (dados do solicitante)
3. Cadastre **projetos** (semanas, horas/semana, tipo)
4. Monte a **squad** (perfis e quantidades)
5. Confira totais de **horas**, **UST** e **valor**
6. Acesse o **Dashboard** para visão consolidada
7. Em **Relatórios**, visualize os dados por tipo, exporte PDF ou Excel, ou **envie por e-mail** (com logo do órgão no corpo HTML)
8. (Admin) Em **Configurações**, defina parâmetros UST, **nome do órgão** e **logo** (arrastar e soltar ou selecionar arquivo)

---

## Fórmulas de Cálculo

```
Horas por membro  = Quantidade × Horas Semanais × Semanas
UST por membro    = Horas × FCP (snapshot no momento do save)
Valor do projeto  = Total UST × Valor UST × (1 + Encargos%) × (1 + BDI%)
```

Os parâmetros financeiros (valor UST, encargos, BDI) são configurados pelo Administrador em **Configurações**.

---

## Perfis de Acesso

| Perfil | Permissões |
|--------|------------|
| **Administrador** | Configurações (UST, logo, órgão), perfis FCP, todos os usuários, todas as simulações, relatórios |
| **Gestor** | Criar usuários **analistas**, simulações próprias, projetos, squads e relatórios. **Sem** menu Administração |
| **Analista** | Criar e editar simulações próprias, projetos, squads e relatórios |
| **Consulta** | Visualização somente leitura (dashboard, simulações, relatórios) |

---

## Stack Tecnológica

| Camada | Tecnologia |
|--------|------------|
| Backend | Java 21, Spring Boot 3.5, Spring Security + JWT, JPA, Flyway, OpenPDF, Apache POI |
| Frontend | Vue.js 3, Vite, Pinia, Vue Router, PrimeVue, Chart.js |
| Banco | PostgreSQL 17 |
| Infra | Docker, Docker Compose, Nginx |

---

## Desenvolvimento Local

### Pré-requisitos

- Java 21+, Maven 3.8+
- Node.js 18+
- Docker (para PostgreSQL) ou perfil `local` com H2

### Perfil local (H2, sem Docker)

```powershell
# Tudo de uma vez (Mailpit + backend + frontend)
.\scripts\start-dev.ps1
```

Ou manualmente:

```powershell
# Mailpit (captura de e-mails em desenvolvimento)
.\scripts\start-mailpit.ps1

# Backend
cd backend
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-25.0.0.36-hotspot"
mvn spring-boot:run "-Dspring-boot.run.profiles=local"

# Frontend (outro terminal)
cd frontend
npm install
npm run dev
```

| Recurso | URL |
|---------|-----|
| Aplicação (Docker) | http://localhost |
| SPA (dev local) | http://localhost:5173 |
| API (dev local) | http://localhost:8080 |
| Mailpit (e-mails) | http://localhost:8025 |

### Com PostgreSQL

```bash
docker compose -f docker-compose.dev.yml up -d
cd backend && mvn spring-boot:run
cd frontend && npm run dev
```

---

## Deploy em Produção

Consulte [docs/docker.md](docs/docker.md).

```bash
cp .env.example .env
docker compose up -d --build
```

| Recurso | URL |
|---------|-----|
| Aplicação | http://localhost |
| API Health | http://localhost/api/health |
| Swagger | http://localhost/swagger-ui.html |
| Mailpit | http://localhost:8025 |

---

## Estrutura do Repositório

```
calculadoraGOV/
├── backend/                 # API REST Spring Boot
│   └── src/main/resources/db/migration/   # Flyway (V1–V7)
├── frontend/                # SPA Vue 3 (UST Gov)
├── docker/                  # Dockerfiles e configs Nginx
├── docs/                    # Documentação técnica
├── docker-compose.yml
├── .env.example
└── README.md
```

---

## API REST (resumo)

Prefixo: `/api` — Autenticação: `Bearer JWT`

| Grupo | Endpoints principais |
|-------|---------------------|
| Auth | `POST /auth/login`, `GET /auth/me` |
| Usuários | `GET/POST /usuarios` (ADMIN, GESTOR*) |
| Perfis FCP | CRUD `/perfis` (ADMIN) |
| Config UST | `GET/PUT /configuracoes/ust` |
| Config institucional | `GET/PUT /configuracoes/institucional`, `POST /logo` (ADMIN) |
| Simulações | CRUD `/simulacoes` |
| Projetos / Squads | CRUD aninhado |
| Dashboard | `GET /dashboard` |
| Relatórios | `GET .../preview`, `POST .../pdf`, `POST .../excel`, `POST .../email`, download |
| E-mail | `GET /mail/config` (status Mailpit) |

\* Gestor: apenas listar/criar usuários com perfil ANALISTA.

Lista completa: [docs/backend.md](docs/backend.md) e Swagger.

---

## Documentação Técnica

| Documento | Conteúdo |
|---------|----------|
| [architecture.md](docs/architecture.md) | Diagramas, camadas, perfis |
| [er-model.md](docs/er-model.md) | Modelo entidade-relacionamento |
| [database.md](docs/database.md) | Migrations Flyway e seed |
| [backend.md](docs/backend.md) | Endpoints, JWT, execução |
| [frontend.md](docs/frontend.md) | Rotas, componentes, UI |
| [docker.md](docs/docker.md) | Deploy, volumes, troubleshooting |

---

## Migrations Flyway

| Versão | Conteúdo |
|--------|----------|
| V1 | Schema inicial (8 tabelas) |
| V2 | Índices |
| V3 | Seed (admin, analista, perfis, UST) |
| V4 | `configuracoes_institucionais` (nome do órgão, logo) |
| V5 | Perfil `GESTOR` em `usuarios.role` |
| V6 | `relatorio_envios` (histórico de envio por e-mail) |
| V7 | Seed do usuário `gestor@ust.gov.br` no PostgreSQL |

---

## Configuração UST Padrão (seed)

| Parâmetro | Valor |
|-----------|-------|
| Valor UST | R$ 180,00 |
| Carga horária mês | 160 h |
| Encargos | 75% |
| BDI | 25% |

11 perfis profissionais com FCP pré-cadastrados.

---

## Licença

Uso interno — órgãos governamentais.
