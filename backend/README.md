# Backend — UST Gov Calculator

API REST desenvolvida com Java 21 e Spring Boot 3.5+.

## Execução rápida

```bash
# 1. Banco de dados + migrations
docker compose -f docker-compose.db.yml up -d

# 2. API (perfil dev — PostgreSQL)
cd backend
mvn spring-boot:run
```

Perfil `local` (H2, sem Docker):

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```

Swagger: http://localhost:8080/swagger-ui.html

Documentação completa: [docs/backend.md](../docs/backend.md)

## Migrations Flyway

| Versão | Arquivo | Descrição |
|--------|---------|-----------|
| V1 | `V1__create_schema.sql` | 8 tabelas + constraints |
| V2 | `V2__create_indexes.sql` | Índices |
| V3 | `V3__seed_initial_data.sql` | Admin, analista, perfis, UST |
| V4 | `V4__create_configuracoes_institucionais.sql` | Nome do órgão + logo |
| V5 | `V5__add_gestor_role.sql` | Role GESTOR |
| V6 | `V6__create_relatorio_envios.sql` | Histórico de envio por e-mail |
| V7 | `V7__seed_gestor_user.sql` | Usuário gestor de demonstração |

## Credenciais de teste

| E-mail | Senha | Perfil |
|--------|-------|--------|
| admin@ust.gov.br | admin123 | ADMIN |
| gestor@ust.gov.br | gestor123 | GESTOR |
| analista@ust.gov.br | analista123 | ANALISTA |

## Status

Projeto completo — API REST, relatórios PDF/Excel, envio por e-mail (Mailpit), branding institucional e perfil gestor.
