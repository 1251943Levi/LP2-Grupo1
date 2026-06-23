CREATE TABLE [inscricao] (
    numMec              INT          NOT NULL REFERENCES [estudante](numMec),
    siglaUC             NVARCHAR(10) NOT NULL,
    anoLetivo           INT          NOT NULL REFERENCES [anoLetivo](ano),
    anoLetivoRealizacao INT          NULL,   -- A1: ano em que o aluno frequentou originalmente a UC
    PRIMARY KEY (numMec, siglaUC, anoLetivo)
);
