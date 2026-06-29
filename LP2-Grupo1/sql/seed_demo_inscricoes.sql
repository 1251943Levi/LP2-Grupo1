-- ============================================================
--  SEED para a demo — inscrições em falta (NÃO destrutivo, idempotente)
--  Inscreve os 7 estudantes de EEC nas UCs do 1.º ano (ALG, EST, FIS, MAT)
--  no ano letivo ATIVO = 2027 (o que está INICIADO).
--  Resolve o "alunos não listados para notas" (F5) com os dados reais.
--  Re-executável: só insere o que ainda não existir.
-- ============================================================

SET NOCOUNT ON;

INSERT INTO [inscricao] (numMec, siglaUC, anoLetivo, anoLetivoRealizacao)
SELECT a.numMec, u.siglaUC, 2027, 2027
FROM (VALUES (20260012),(20260013),(20260014),(20260015),(20260016),(20260017),(20260018)) AS a(numMec)
CROSS JOIN (VALUES ('ALG'),('EST'),('FIS'),('MAT')) AS u(siglaUC)
WHERE NOT EXISTS (
    SELECT 1 FROM [inscricao] i
    WHERE i.numMec = a.numMec AND i.siglaUC = u.siglaUC AND i.anoLetivo = 2027
);

PRINT 'Inscricoes criadas/garantidas (7 alunos x 4 UCs = 28 no ano 2027).';

-- Verificacao
SELECT numMec, siglaUC, anoLetivo, anoLetivoRealizacao
FROM [inscricao]
WHERE anoLetivo = 2027
ORDER BY numMec, siglaUC;
