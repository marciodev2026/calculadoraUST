# Frontend — UST Gov

SPA Vue.js 3 com Vite, Pinia, PrimeVue e Vue Router.

## Stack

- Vue.js 3.5
- Vite 7
- Pinia 3
- Vue Router 4
- PrimeVue 4 (tema Aura)
- Axios
- Chart.js — gráficos do Dashboard

## Execução

### Pré-requisitos

- Node.js 18+
- Backend rodando em `localhost:8080`
- Mailpit em `localhost:1025` (para envio de e-mails em desenvolvimento)

### Desenvolvimento

```bash
cd frontend
npm install
npm run dev
```

Acesse: http://localhost:5173

> **Docker** serve o frontend em http://localhost (porta 80). Use 5173 apenas no modo desenvolvimento com `npm run dev`.

O Vite faz proxy de `/api` → `http://localhost:8080`.

### Build produção

```bash
npm run build
npm run preview
```

## Identidade visual

- Nome do sistema: **UST Gov**
- Login em layout dividido (painel institucional + formulário)
- Sidebar com marca e menu por perfil
- Cores principais em `src/assets/styles/main.css` (`--ust-primary`, etc.)

## Autenticação

- Login via `POST /api/auth/login`
- Token JWT em `localStorage`
- Interceptor Axios: `Authorization: Bearer`
- Redirect para `/login` em 401

### Credenciais de teste (perfil `local` / seed)

| E-mail | Senha | Perfil |
|--------|-------|--------|
| admin@ust.gov.br | admin123 | ADMIN |
| gestor@ust.gov.br | gestor123 | GESTOR |
| analista@ust.gov.br | analista123 | ANALISTA |

## Layout

```
┌─────────────┬──────────────────────────────┐
│  Sidebar    │  Topbar (título + logout)    │
│  - Dashboard├──────────────────────────────┤
│  - Simulações│  Conteúdo (router-view)     │
│  - Relatórios│                              │
│  - Usuários*│                              │
│  - Admin ** │                              │
└─────────────┴──────────────────────────────┘
```

\* Menu **Usuários** no menu principal — apenas `GESTOR` (cadastro de analistas).

\** Seção **Administração** — apenas `ADMIN` (Configurações, Perfis, Usuários).

## Rotas

| Rota | Guard | Descrição |
|------|-------|-----------|
| `/login` | Público | Tela de login (UST Gov) |
| `/dashboard` | Auth | Dashboard executivo |
| `/simulacoes` | Auth | Simulações |
| `/simulacoes/:id/projetos` | Auth | Projetos por tipo |
| `/simulacoes/:id/projetos/:pid/squad` | Auth | Montagem de squad |
| `/relatorios` | Auth | Preview em tela + exportação PDF/Excel |
| `/usuarios` | ADMIN, GESTOR | Gestão de usuários |
| `/admin/configuracao-ust` | ADMIN | Configurações (UST + logo/órgão) |
| `/admin/perfis` | ADMIN | Perfis FCP |

## Telas principais

### Relatórios (`RelatorioView.vue`)

1. Selecionar simulação
2. Visualizar identificação, totais e abas por tipo (Projeto, Sustentação, Evolução, Correção)
3. Expandir projeto para ver composição do squad
4. Exportar PDF executivo ou Excel técnico (download automático)
5. **Enviar por e-mail** — destinatário, formato (PDF/Excel), assunto e mensagem opcionais; link para abrir o Mailpit

### Configurações (`ConfiguracaoUstView.vue`)

- **Identidade institucional** (ADMIN): nome do órgão; logo com **drag-and-drop** ou seleção de arquivo (PNG/JPG/WEBP, máx. 2 MB)
- **Parâmetros UST**: valor, carga horária, encargos, BDI
- Histórico de configurações UST (ADMIN)

### Usuários (`UsuariosView.vue`)

- **ADMIN**: listar todos; criar ADMIN, GESTOR, ANALISTA ou CONSULTA
- **GESTOR**: listar e criar apenas ANALISTA

## Estrutura de pastas

```
src/
├── assets/styles/main.css
├── components/layout/     AppLayout, AppSidebar, AppTopbar
├── views/
│   ├── auth/LoginView.vue
│   ├── admin/ConfiguracaoUstView, PerfisProfissionaisView, UsuariosView
│   ├── dashboard/, simulacoes/, projetos/, squads/, relatorios/
├── stores/auth.js         isAdmin, isGestor, canWrite, canManageUsers
├── router/index.js
└── services/
    ├── api.js, authService.js, simulacaoService.js
    ├── relatorioService.js          (+ preview, download, enviarEmail)
    ├── mailService.js               (config Mailpit)
    ├── configuracaoUstService.js
    ├── configuracaoInstitucionalService.js
    └── usuarioService.js
```

## Store de autenticação

| Computed | Descrição |
|----------|-----------|
| `isAdmin` | Perfil ADMIN |
| `isGestor` | Perfil GESTOR |
| `canWrite` | ADMIN, GESTOR ou ANALISTA |
| `canManageUsers` | ADMIN ou GESTOR |

## Status

Funcionalidades implementadas: base, perfis FCP, configuração UST, simulações, projetos, squads, dashboard, relatórios com preview, envio por e-mail, identidade institucional, perfil gestor, UI UST Gov.

Ver [README.md](../README.md) para guia completo.
