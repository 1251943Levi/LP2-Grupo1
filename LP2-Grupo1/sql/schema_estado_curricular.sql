-- ============================================================
--  Tabela lookup [EstadoCurricular] (Card 1)
--  Estados partilhados por Curso e UC, com descrição.
--  Deve existir ANTES de [curso] e [uc] (FK estado -> EstadoCurricular.nome).
-- ============================================================

CREATE TABLE [EstadoCurricular] (
    nome      NVARCHAR(20)  NOT NULL PRIMARY KEY,
    descricao NVARCHAR(200) NULL
);

INSERT INTO [EstadoCurricular] (nome, descricao) VALUES
    ('ATIVO',         'Reúne todas as condições e está em funcionamento.'),
    ('INATIVO',       'Desativado manualmente pelo gestor.'),
    ('SEM_CONDICOES', 'Criado mas não reúne as condições mínimas para abrir.');
