-- ============================================================
--  Tabela intermédia [curso_uc] (Card 2) — relação M:N Curso<->UC
--  PK composta (siglaCurso, siglaUC, anoLetivo).
--  Deve ser criada DEPOIS de [curso] (FK siglaCurso -> curso.sigla).
-- ============================================================

CREATE TABLE [curso_uc] (
    siglaCurso    NVARCHAR(10) NOT NULL REFERENCES [curso](sigla),
    siglaUC       NVARCHAR(10) NOT NULL,
    anoCurricular INT          NOT NULL,
    anoLetivo     INT          NOT NULL,
    PRIMARY KEY (siglaCurso, siglaUC, anoLetivo)
);
