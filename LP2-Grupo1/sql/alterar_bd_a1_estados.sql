-- ============================================================
--  Alterações NÃO destrutivas à BD existente (sem apagar dados)
--  Servidor: ctespbd.dei.isep.ipp.pt / BD 2026_LP2_G1_FEIRA
--
--  Aplica:
--    * A1     -> coluna [inscricao].anoLetivoRealizacao
--    * Card 1 -> tabela [EstadoCurricular] (nome+descricao) com 3 estados
--                (ATIVO, INATIVO, SEM_CONDICOES) + FK [curso].estado e [uc].estado
--
--  Idempotente; trata também quem já tinha a tabela antiga [estado_curso].
--  NÃO apaga dados de negócio.
-- ============================================================

SET NOCOUNT ON;

-- A1: coluna do ano de realização original (só se ainda não existir)
IF COL_LENGTH('inscricao', 'anoLetivoRealizacao') IS NULL
    ALTER TABLE [inscricao] ADD anoLetivoRealizacao INT NULL;
GO

-- Largar artefactos antigos (versão [estado_curso]), se existirem
IF EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_curso_estado')
    ALTER TABLE [curso] DROP CONSTRAINT FK_curso_estado;
GO
IF OBJECT_ID('estado_curso', 'U') IS NOT NULL
    DROP TABLE [estado_curso];
GO

-- Card 1: tabela lookup [EstadoCurricular] (a app também a cria sozinha)
IF OBJECT_ID('EstadoCurricular', 'U') IS NULL
    CREATE TABLE [EstadoCurricular] (nome NVARCHAR(20) NOT NULL PRIMARY KEY, descricao NVARCHAR(200) NULL);
GO
IF NOT EXISTS (SELECT 1 FROM [EstadoCurricular] WHERE nome = 'ATIVO')
    INSERT INTO [EstadoCurricular] (nome, descricao) VALUES ('ATIVO', 'Reúne todas as condições e está em funcionamento.');
IF NOT EXISTS (SELECT 1 FROM [EstadoCurricular] WHERE nome = 'INATIVO')
    INSERT INTO [EstadoCurricular] (nome, descricao) VALUES ('INATIVO', 'Desativado manualmente pelo gestor.');
IF NOT EXISTS (SELECT 1 FROM [EstadoCurricular] WHERE nome = 'SEM_CONDICOES')
    INSERT INTO [EstadoCurricular] (nome, descricao) VALUES ('SEM_CONDICOES', 'Criado mas não reúne as condições mínimas para abrir.');
GO

-- Normalizar valores antigos de [curso].estado para os nomes canónicos
UPDATE [curso] SET estado = 'ATIVO'         WHERE estado IN ('Ativo','ativo');
UPDATE [curso] SET estado = 'INATIVO'       WHERE estado IN ('Inativo','inativo');
UPDATE [curso] SET estado = 'SEM_CONDICOES' WHERE estado IN ('Pendente','pendente');
GO

-- FK [curso].estado -> [EstadoCurricular](nome)
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_curso_estado')
    ALTER TABLE [curso] ADD CONSTRAINT FK_curso_estado FOREIGN KEY (estado) REFERENCES [EstadoCurricular](nome);
GO

-- Card 1: coluna [uc].estado (so se ainda nao existir) + FK -> [EstadoCurricular]
-- O codigo (UcDALSql) le/escreve esta coluna; uma tabela [uc] antiga pode nao a ter.
IF COL_LENGTH('uc', 'estado') IS NULL
    ALTER TABLE [uc] ADD estado NVARCHAR(20) NOT NULL CONSTRAINT DF_uc_estado DEFAULT 'SEM_CONDICOES';
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_uc_estado')
    ALTER TABLE [uc] ADD CONSTRAINT FK_uc_estado FOREIGN KEY (estado) REFERENCES [EstadoCurricular](nome);
GO

PRINT 'Alteracoes aplicadas (nao destrutivas).';
GO
