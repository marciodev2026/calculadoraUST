-- ============================================================
-- UST Gov Calculator — Dados iniciais
-- ============================================================

-- Usuário administrador padrão
-- Senha: admin123 (BCrypt strength 10)
INSERT INTO usuarios (id, nome_completo, email, senha_hash, orgao, departamento, role, ativo)
VALUES (
    'a0000000-0000-4000-8000-000000000001',
    'Administrador do Sistema',
    'admin@ust.gov.br',
    '$2y$10$hESOE5u8Szv8arF42eSuluQZHf5ip3RbkNgW3/b.rjTV3ESIo.1PG',
    'Governo Federal',
    'TI',
    'ADMIN',
    TRUE
);

-- Perfis profissionais com FCP (órgãos federais)
INSERT INTO perfis (nome, descricao, fcp, created_by) VALUES
    ('Desenvolvedor Junior',  'Desenvolvedor com experiência inicial',           1.00, 'a0000000-0000-4000-8000-000000000001'),
    ('Desenvolvedor Pleno',    'Desenvolvedor com experiência intermediária',     1.30, 'a0000000-0000-4000-8000-000000000001'),
    ('Desenvolvedor Senior',    'Desenvolvedor com experiência avançada',        1.80, 'a0000000-0000-4000-8000-000000000001'),
    ('Especialista',          'Especialista técnico em área específica',       2.20, 'a0000000-0000-4000-8000-000000000001'),
    ('Arquiteto',             'Arquiteto de software/soluções',                2.50, 'a0000000-0000-4000-8000-000000000001'),
    ('Scrum Master',          'Facilitador ágil de equipes',                   2.00, 'a0000000-0000-4000-8000-000000000001'),
    ('Product Owner',         'Responsável pelo produto e backlog',            2.10, 'a0000000-0000-4000-8000-000000000001'),
    ('Gerente de Projeto',    'Gestão e coordenação de projetos',              2.80, 'a0000000-0000-4000-8000-000000000001'),
    ('DBA',                   'Administrador de banco de dados',               2.00, 'a0000000-0000-4000-8000-000000000001'),
    ('DevOps',                'Operações e integração contínua',               2.20, 'a0000000-0000-4000-8000-000000000001'),
    ('UX/UI',                 'Design de experiência e interface',             1.80, 'a0000000-0000-4000-8000-000000000001');

-- Configuração UST padrão
INSERT INTO configuracoes (
    valor_ust,
    carga_horaria_mes,
    encargos_percentual,
    bdi_percentual,
    vigente_desde,
    ativo,
    created_by
) VALUES (
    180.00,
    160,
    75.00,
    25.00,
    CURRENT_DATE,
    TRUE,
    'a0000000-0000-4000-8000-000000000001'
);

-- Usuário analista de demonstração
-- Senha: analista123 (BCrypt strength 10)
INSERT INTO usuarios (nome_completo, email, senha_hash, orgao, departamento, role, ativo, created_by)
VALUES (
    'Analista Demonstração',
    'analista@ust.gov.br',
    '$2y$10$GyvaVe/OYfYRwq5XmZV0x.oY2fZzccn9xo5mRGjewokMU9d578n76',
    'Exército Brasileiro',
    'Centro de Desenvolvimento',
    'ANALISTA',
    TRUE,
    'a0000000-0000-4000-8000-000000000001'
);
