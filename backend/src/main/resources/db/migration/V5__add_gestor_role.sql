ALTER TABLE usuarios DROP CONSTRAINT ck_usuarios_role;

ALTER TABLE usuarios
    ADD CONSTRAINT ck_usuarios_role
    CHECK (role IN ('ADMIN', 'GESTOR', 'ANALISTA', 'CONSULTA'));
