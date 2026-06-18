-- Usuário gestor de demonstração (PostgreSQL)
-- Senha: gestor123 (BCrypt strength 10)
INSERT INTO usuarios (nome_completo, email, senha_hash, orgao, departamento, role, ativo, created_by)
SELECT
    'Gestor Demonstração',
    'gestor@ust.gov.br',
    '$2y$10$wTbJU2DY4D.meoregp0PeeDvcjFkFGt95hB.jNjQatqA8DJrdGgEa',
    'Governo Federal',
    'Gestão de Projetos',
    'GESTOR',
    TRUE,
    'a0000000-0000-4000-8000-000000000001'
WHERE NOT EXISTS (
    SELECT 1 FROM usuarios WHERE email = 'gestor@ust.gov.br'
);
