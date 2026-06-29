-- ============================================================
--  Tabela lookup [tipo_momento] (Card 3) — tipos de momento de avaliação.
--  Deve existir ANTES de [momento] (FK tipo -> tipo_momento.nome).
-- ============================================================

CREATE TABLE [tipo_momento] (
    nome      NVARCHAR(20)  NOT NULL PRIMARY KEY,
    descricao NVARCHAR(100) NULL
);

INSERT INTO [tipo_momento] (nome, descricao) VALUES
    ('EXAME',      'Exame'),
    ('TRABALHO',   'Trabalho'),
    ('MINI_TESTE', 'Mini-teste'),
    ('PROJETO',    'Projeto');
