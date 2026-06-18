# Backend — UST Gov

API REST Spring Boot 3.5 com autenticação JWT.

## Stack

- Java 21+
- Spring Boot 3.5.3
- Spring Security + JWT (jjwt 0.12)
- Spring Data JPA + Flyway
- PostgreSQL 17
- OpenPDF (relatórios PDF), Apache POI (Excel)
- Spring Mail (envio de relatórios por e-mail via Mailpit)
- Lombok, OpenAPI / Swagger (springdoc)

## Endpoints

| Método | Endpoint | Auth | Descrição |
|--------|----------|------|-----------|
| GET | `/api/health` | Público | Health check |
| POST | `/api/auth/login` | Público | Login (JWT) |
| GET | `/api/auth/me` | Bearer | Usuário autenticado |
| GET | `/api/usuarios` | ADMIN, GESTOR | Listar usuários * |
| GET | `/api/usuarios/{id}` | ADMIN, GESTOR | Buscar usuário * |
| POST | `/api/usuarios` | ADMIN, GESTOR | Criar usuário * |
| GET/POST/PUT/DELETE | `/api/perfis` | Leitura: todos; escrita: ADMIN | Perfis FCP |
| GET | `/api/configuracoes/ust` | Todos | Configuração UST ativa |
| GET | `/api/configuracoes/ust/historico` | ADMIN | Histórico UST |
| PUT | `/api/configuracoes/ust` | ADMIN | Salvar configuração UST |
| GET | `/api/configuracoes/institucional` | Todos | Nome do órgão / flag logo |
| PUT | `/api/configuracoes/institucional` | ADMIN | Salvar nome do órgão |
| POST | `/api/configuracoes/institucional/logo` | ADMIN | Upload logo (multipart) |
| DELETE | `/api/configuracoes/institucional/logo` | ADMIN | Remover logo |
| GET | `/api/configuracoes/institucional/logo` | Todos | Download da imagem do logo |
| CRUD | `/api/simulacoes` | Leitura: todos; escrita: ADMIN/GESTOR/ANALISTA | Simulações |
| CRUD | `/api/simulacoes/{id}/projetos` | Idem | Projetos |
| GET/PUT | `/api/simulacoes/{id}/projetos/{pid}/squad` | Idem | Squad |
| GET | `/api/dashboard` | Todos | Indicadores |
| GET | `/api/simulacoes/{id}/relatorios` | Todos | Listar arquivos gerados |
| GET | `/api/simulacoes/{id}/relatorios/preview` | Todos | Dados para exibição em tela |
| POST | `/api/simulacoes/{id}/relatorios/pdf` | ADMIN/GESTOR/ANALISTA | Gerar e baixar PDF |
| POST | `/api/simulacoes/{id}/relatorios/excel` | ADMIN/GESTOR/ANALISTA | Gerar e baixar Excel |
| POST | `/api/simulacoes/{id}/relatorios/email` | ADMIN/GESTOR/ANALISTA | Gerar relatório e enviar por e-mail |
| GET | `/api/simulacoes/{id}/relatorios/envios` | Todos | Histórico de envios por e-mail (API) |
| GET | `/api/simulacoes/{id}/relatorios/{rid}/download` | Todos | Download do arquivo |
| GET | `/api/mail/config` | Todos | Status do serviço de e-mail e URL do Mailpit |

\* **GESTOR**: lista e cria apenas usuários `ANALISTA`; **ADMIN**: qualquer perfil.

## Swagger

http://localhost:8080/swagger-ui.html

## Execução

### PostgreSQL

```bash
docker compose -f docker-compose.db.yml up -d
cd backend
mvn spring-boot:run
```

### Perfil local (H2)

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-25.0.0.36-hotspot"
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```

Flyway desabilitado; `ddl-auto: update`; seed em `LocalDataSeeder`.

## Variáveis de ambiente

| Variável | Padrão | Descrição |
|----------|--------|-----------|
| `JWT_SECRET` | (application.yml) | Chave JWT HS256 |
| `REPORTS_STORAGE_PATH` | `./storage/relatorios` | PDF/Excel gerados |
| `BRANDING_STORAGE_PATH` | `./storage/branding` | Logo do órgão |
| `MAIL_ENABLED` | `true` | Habilita envio de e-mail |
| `MAIL_HOST` | `localhost` | Servidor SMTP (Mailpit em dev) |
| `MAIL_PORT` | `1025` | Porta SMTP |
| `MAIL_FROM` | `UST Gov <noreply@ust.gov.br>` | Remetente |
| `MAILPIT_UI_URL` | `http://localhost:8025` | URL da interface Mailpit |
| `SPRING_PROFILES_ACTIVE` | dev | Perfil Spring |

## Perfis Spring

| Perfil | Uso |
|--------|-----|
| dev | PostgreSQL local |
| local | H2 em arquivo, sem Flyway |
| prod | Docker / produção |

## Segurança

- Senhas: BCrypt
- JWT stateless (24h)
- Roles: `ROLE_ADMIN`, `ROLE_GESTOR`, `ROLE_ANALISTA`, `ROLE_CONSULTA`
- CORS habilitado para desenvolvimento

## Serviços de relatório

| Classe | Função |
|--------|--------|
| `RelatorioDataService` | Agrega simulação, projetos, squads, config UST |
| `RelatorioPreviewMapper` | DTO JSON agrupado por `ProjetoTipo` |
| `RelatorioPdfGenerator` | PDF com logo e nome do órgão |
| `RelatorioExcelGenerator` | Excel técnico (4 abas) |
| `ConfiguracaoInstitucionalService` | CRUD nome + upload logo |
| `EmailService` | Envio SMTP com anexo e imagem inline |
| `RelatorioEmailComposer` | Template HTML institucional (logo, totais, anexo) |

## Migrations

| Versão | Arquivo | Conteúdo |
|--------|---------|----------|
| V1–V3 | Schema, índices, seed | Base |
| V4 | `V4__create_configuracoes_institucionais.sql` | Nome órgão + caminho logo |
| V5 | `V5__add_gestor_role.sql` | Role GESTOR |
| V6 | `V6__create_relatorio_envios.sql` | Histórico de envio por e-mail |
| V7 | `V7__seed_gestor_user.sql` | Usuário gestor de demonstração |

## E-mail em desenvolvimento

O [Mailpit](https://github.com/axllent/mailpit) captura e-mails sem enviá-los de fato:

```powershell
.\scripts\start-mailpit.ps1
```

- SMTP: `localhost:1025`
- Interface web: http://localhost:8025

O script tenta o binário local (`scripts/mailpit.exe`) e, se ausente, o container Docker `ust-mailpit`.

## Status

Backend completo com preview de relatórios, branding institucional, perfil gestor e envio de relatórios por e-mail.
