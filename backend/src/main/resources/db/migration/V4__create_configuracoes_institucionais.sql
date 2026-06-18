-- Configurações institucionais (logo e nome do órgão)
CREATE TABLE configuracoes_institucionais (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome_organizacao VARCHAR(200) NOT NULL,
    logo_caminho     VARCHAR(500),
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP,
    created_by       UUID REFERENCES usuarios (id),
    updated_by       UUID REFERENCES usuarios (id)
);

INSERT INTO configuracoes_institucionais (nome_organizacao)
VALUES ('Governo Federal');
