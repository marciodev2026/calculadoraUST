-- Histórico de envio de relatórios por e-mail
CREATE TABLE relatorio_envios (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    simulacao_id    UUID          NOT NULL REFERENCES simulacoes (id) ON DELETE CASCADE,
    relatorio_id    UUID          REFERENCES relatorios (id) ON DELETE SET NULL,
    destinatario    VARCHAR(150)  NOT NULL,
    assunto         VARCHAR(200)  NOT NULL,
    tipo            VARCHAR(10)   NOT NULL,
    status          VARCHAR(20)   NOT NULL DEFAULT 'ENVIADO',
    mensagem_erro   TEXT,
    enviado_em      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID REFERENCES usuarios (id),
    CONSTRAINT ck_relatorio_envios_tipo CHECK (tipo IN ('PDF', 'EXCEL')),
    CONSTRAINT ck_relatorio_envios_status CHECK (status IN ('ENVIADO', 'FALHA'))
);

CREATE INDEX idx_relatorio_envios_simulacao_id ON relatorio_envios (simulacao_id);
CREATE INDEX idx_relatorio_envios_enviado_em ON relatorio_envios (enviado_em DESC);
