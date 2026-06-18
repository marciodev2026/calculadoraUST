-- ============================================================
-- UST Gov Calculator — Índices e constraints adicionais
-- ============================================================

CREATE INDEX idx_usuarios_email ON usuarios (email);
CREATE INDEX idx_usuarios_role ON usuarios (role) WHERE ativo = TRUE;

CREATE INDEX idx_perfis_ativo ON perfis (ativo) WHERE ativo = TRUE;

CREATE UNIQUE INDEX uk_configuracoes_ativo ON configuracoes (ativo) WHERE ativo = TRUE;

CREATE INDEX idx_simulacoes_usuario_id ON simulacoes (usuario_id);
CREATE INDEX idx_simulacoes_data ON simulacoes (data_simulacao);
CREATE INDEX idx_simulacoes_status ON simulacoes (status);

CREATE INDEX idx_projetos_simulacao_id ON projetos (simulacao_id);
CREATE INDEX idx_projetos_tipo ON projetos (tipo);
CREATE INDEX idx_projetos_status ON projetos (status);

CREATE INDEX idx_squads_projeto_id ON squads (projeto_id);

CREATE INDEX idx_squad_membros_squad_id ON squad_membros (squad_id);
CREATE INDEX idx_squad_membros_perfil_id ON squad_membros (perfil_id);

CREATE INDEX idx_relatorios_simulacao_id ON relatorios (simulacao_id);
CREATE INDEX idx_relatorios_tipo ON relatorios (tipo);
