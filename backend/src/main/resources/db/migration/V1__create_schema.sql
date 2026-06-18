-- ============================================================
-- UST Gov Calculator — Schema inicial
-- PostgreSQL 17 | UUID + Auditoria
-- ============================================================

-- ------------------------------------------------------------
-- usuarios
-- ------------------------------------------------------------
CREATE TABLE usuarios (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome_completo   VARCHAR(200)  NOT NULL,
    email           VARCHAR(150)  NOT NULL,
    senha_hash      VARCHAR(255)  NOT NULL,
    orgao           VARCHAR(200),
    departamento    VARCHAR(200),
    telefone        VARCHAR(20),
    role            VARCHAR(20)   NOT NULL,
    ativo           BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP,
    created_by      UUID,
    updated_by      UUID,
    CONSTRAINT uk_usuarios_email UNIQUE (email),
    CONSTRAINT ck_usuarios_role CHECK (role IN ('ADMIN', 'ANALISTA', 'CONSULTA'))
);

ALTER TABLE usuarios
    ADD CONSTRAINT fk_usuarios_created_by FOREIGN KEY (created_by) REFERENCES usuarios (id),
    ADD CONSTRAINT fk_usuarios_updated_by FOREIGN KEY (updated_by) REFERENCES usuarios (id);

-- ------------------------------------------------------------
-- perfis (perfis profissionais com FCP)
-- ------------------------------------------------------------
CREATE TABLE perfis (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome        VARCHAR(100)  NOT NULL,
    descricao   VARCHAR(500),
    fcp         DECIMAL(4, 2) NOT NULL,
    ativo       BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP,
    created_by  UUID REFERENCES usuarios (id),
    updated_by  UUID REFERENCES usuarios (id),
    CONSTRAINT uk_perfis_nome UNIQUE (nome),
    CONSTRAINT ck_perfis_fcp CHECK (fcp > 0)
);

-- ------------------------------------------------------------
-- configuracoes (parâmetros UST)
-- ------------------------------------------------------------
CREATE TABLE configuracoes (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    valor_ust           DECIMAL(10, 2) NOT NULL,
    carga_horaria_mes   INTEGER        NOT NULL,
    encargos_percentual DECIMAL(5, 2)  NOT NULL,
    bdi_percentual      DECIMAL(5, 2)  NOT NULL,
    vigente_desde       DATE           NOT NULL,
    ativo               BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP,
    created_by          UUID REFERENCES usuarios (id),
    updated_by          UUID REFERENCES usuarios (id),
    CONSTRAINT ck_configuracoes_valor_ust CHECK (valor_ust > 0),
    CONSTRAINT ck_configuracoes_carga_horaria CHECK (carga_horaria_mes > 0),
    CONSTRAINT ck_configuracoes_encargos CHECK (encargos_percentual >= 0),
    CONSTRAINT ck_configuracoes_bdi CHECK (bdi_percentual >= 0)
);

-- ------------------------------------------------------------
-- simulacoes
-- ------------------------------------------------------------
CREATE TABLE simulacoes (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id      UUID          NOT NULL REFERENCES usuarios (id),
    nome_completo   VARCHAR(200)  NOT NULL,
    email           VARCHAR(150)  NOT NULL,
    orgao           VARCHAR(200)  NOT NULL,
    departamento    VARCHAR(200)  NOT NULL,
    telefone        VARCHAR(20),
    data_simulacao  DATE          NOT NULL,
    status          VARCHAR(20)   NOT NULL DEFAULT 'RASCUNHO',
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP,
    created_by      UUID REFERENCES usuarios (id),
    updated_by      UUID REFERENCES usuarios (id),
    CONSTRAINT ck_simulacoes_status CHECK (status IN ('RASCUNHO', 'FINALIZADA'))
);

-- ------------------------------------------------------------
-- projetos
-- ------------------------------------------------------------
CREATE TABLE projetos (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    simulacao_id    UUID          NOT NULL REFERENCES simulacoes (id) ON DELETE CASCADE,
    nome            VARCHAR(200)  NOT NULL,
    tipo            VARCHAR(20)   NOT NULL,
    semanas         INTEGER       NOT NULL,
    horas_semanais  INTEGER       NOT NULL DEFAULT 40,
    descricao       TEXT,
    status          VARCHAR(20)   NOT NULL DEFAULT 'ATIVO',
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP,
    created_by      UUID REFERENCES usuarios (id),
    updated_by      UUID REFERENCES usuarios (id),
    CONSTRAINT ck_projetos_tipo CHECK (tipo IN ('PROJETO', 'SUSTENTACAO', 'EVOLUCAO', 'CORRECAO')),
    CONSTRAINT ck_projetos_semanas CHECK (semanas > 0),
    CONSTRAINT ck_projetos_horas_semanais CHECK (horas_semanais > 0),
    CONSTRAINT ck_projetos_status CHECK (status IN ('ATIVO', 'CONCLUIDO', 'CANCELADO'))
);

-- ------------------------------------------------------------
-- squads
-- ------------------------------------------------------------
CREATE TABLE squads (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    projeto_id   UUID          NOT NULL REFERENCES projetos (id) ON DELETE CASCADE,
    total_horas  DECIMAL(12, 2),
    total_ust    DECIMAL(12, 2),
    valor_total  DECIMAL(14, 2),
    created_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP,
    created_by   UUID REFERENCES usuarios (id),
    updated_by   UUID REFERENCES usuarios (id),
    CONSTRAINT uk_squads_projeto_id UNIQUE (projeto_id)
);

-- ------------------------------------------------------------
-- squad_membros
-- ------------------------------------------------------------
CREATE TABLE squad_membros (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    squad_id          UUID          NOT NULL REFERENCES squads (id) ON DELETE CASCADE,
    perfil_id         UUID          NOT NULL REFERENCES perfis (id),
    quantidade        INTEGER       NOT NULL,
    fcp_aplicado      DECIMAL(4, 2) NOT NULL,
    horas_calculadas  DECIMAL(12, 2),
    ust_calculada     DECIMAL(12, 2),
    created_at        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP,
    created_by          UUID REFERENCES usuarios (id),
    updated_by          UUID REFERENCES usuarios (id),
    CONSTRAINT uk_squad_membros_squad_perfil UNIQUE (squad_id, perfil_id),
    CONSTRAINT ck_squad_membros_quantidade CHECK (quantidade > 0),
    CONSTRAINT ck_squad_membros_fcp CHECK (fcp_aplicado > 0)
);

-- ------------------------------------------------------------
-- relatorios
-- ------------------------------------------------------------
CREATE TABLE relatorios (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    simulacao_id    UUID          NOT NULL REFERENCES simulacoes (id) ON DELETE CASCADE,
    tipo            VARCHAR(10)   NOT NULL,
    nome_arquivo    VARCHAR(255)  NOT NULL,
    caminho_arquivo VARCHAR(500)  NOT NULL,
    tamanho_bytes   BIGINT,
    gerado_em       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID REFERENCES usuarios (id),
    CONSTRAINT ck_relatorios_tipo CHECK (tipo IN ('PDF', 'EXCEL'))
);
