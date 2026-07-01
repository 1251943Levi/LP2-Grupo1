-- ============================================================
--  Tabela [momento] (Card 3) — momentos de avaliação de uma UC.
--  Cada momento tem um tipo (FK -> tipo_momento), um peso (%) e uma data.
-- ============================================================

CREATE TABLE [momento] (
    id             INT IDENTITY(1,1) PRIMARY KEY,
    siglaUC        NVARCHAR(10)  NOT NULL,
    nome           NVARCHAR(100) NOT NULL,
    tipo           NVARCHAR(20)  NOT NULL REFERENCES [tipo_momento](nome),
    peso           DECIMAL(5,2)  NOT NULL DEFAULT 0,
    dataRealizacao NVARCHAR(10)  NULL
);
