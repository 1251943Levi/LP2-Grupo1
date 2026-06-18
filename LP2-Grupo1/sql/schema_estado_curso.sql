-- ============================================================
--  Tabela lookup dos estados de curso (F1/F2)
--  Valores canónicos usados por model.EstadoCurso.etiqueta().
--  Deve ser criada ANTES de [curso] (FK [curso].estado -> [estado_curso].nome).
-- ============================================================

CREATE TABLE [estado_curso] (
    nome NVARCHAR(20) NOT NULL PRIMARY KEY
);

INSERT INTO [estado_curso] (nome) VALUES ('Ativo'), ('Inativo'), ('Pendente');
