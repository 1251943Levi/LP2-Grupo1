-- ============================================================
--  Tabela [nota] (Card 5) — nota de um estudante num momento de avaliação.
--  PK (numMec, idMomento). FK para estudante e momento.
--  Deve ser criada DEPOIS de [estudante] e [momento].
-- ============================================================

CREATE TABLE [nota] (
    numMec    INT          NOT NULL REFERENCES [estudante](numMec),
    idMomento INT          NOT NULL REFERENCES [momento](id),
    siglaUC   NVARCHAR(10) NOT NULL,
    valor     DECIMAL(4,2) NOT NULL,
    PRIMARY KEY (numMec, idMomento)
);
