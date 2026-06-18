# Frontend — UST Gov Calculator

SPA Vue.js 3 com Vite, Pinia, PrimeVue e Vue Router.

## Execução

```bash
cd frontend
npm install
npm run dev
```

Acesse: http://localhost:5173

> O backend deve estar rodando em `localhost:8080` (proxy Vite encaminha `/api`).

### Ambiente Docker

Com `docker compose up`, o frontend é servido via Nginx em **http://localhost** (porta 80) — não use a porta 5173 nesse modo.

## Credenciais de teste

| E-mail | Senha | Perfil |
|--------|-------|--------|
| admin@ust.gov.br | admin123 | ADMIN |
| gestor@ust.gov.br | gestor123 | GESTOR |
| analista@ust.gov.br | analista123 | ANALISTA |

## Estrutura

```
src/
├── assets/styles/     # CSS global
├── components/
│   ├── common/        # Componentes reutilizáveis
│   └── layout/        # AppLayout, Sidebar, Topbar
├── views/
│   ├── auth/          # Login
│   ├── admin/         # Configurações, Perfis, Usuários
│   ├── dashboard/
│   ├── simulacoes/
│   ├── projetos/
│   ├── squads/
│   └── relatorios/    # Preview, exportação e envio por e-mail
├── stores/            # Pinia (auth)
├── router/            # Rotas + guards
└── services/          # API axios (relatorioService, mailService, etc.)
```

## Rotas principais

| Rota | Perfil | Descrição |
|------|--------|-----------|
| `/login` | Público | Autenticação |
| `/dashboard` | Todos | Dashboard executivo |
| `/simulacoes` | Todos | Simulações |
| `/relatorios` | Todos | Preview, PDF/Excel e envio por e-mail |
| `/usuarios` | ADMIN, GESTOR | Gestão de usuários |
| `/admin/*` | ADMIN | Configurações administrativas |

## Build produção

```bash
npm run build
npm run preview
```

Documentação completa: [docs/frontend.md](../docs/frontend.md)

## Status

Projeto completo — UI UST Gov, dashboard, relatórios com preview, envio por e-mail e identidade institucional.
