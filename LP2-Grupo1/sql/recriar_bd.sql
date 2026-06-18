-- ============================================================
--  Recriar a base de dados ISSMF (aplicar o schema mais recente)
--  Servidor: ctespbd.dei.isep.ipp.pt / BD 2026_LP2_G1_FEIRA
--
--  PORQUÊ: as alterações de schema mais recentes não se aplicam a
--  tabelas já existentes —
--    * [estado_curso] (lookup) + FK [curso].estado -> [estado_curso](nome)
--    * coluna [inscricao].anoLetivoRealizacao (A1)
--  Apagar as tabelas faz com que a aplicação as RECRIE com o schema
--  novo e REIMPORTE dos CSV no próximo arranque em modo SQL.
--
--  ATENÇÃO: isto apaga TODOS os dados nas tabelas. Os dados voltam a
--  ser carregados a partir dos ficheiros CSV em bd/ no arranque.
--
--  Ordem de DROP = inversa às chaves estrangeiras (filhos antes dos pais).
-- ============================================================

SET NOCOUNT ON;

DROP TABLE IF EXISTS [historicoAcademico];
DROP TABLE IF EXISTS [pagamento];
DROP TABLE IF EXISTS [avaliacao];
DROP TABLE IF EXISTS [inscricao];
DROP TABLE IF EXISTS [uc];
DROP TABLE IF EXISTS [estudante];
DROP TABLE IF EXISTS [curso];
DROP TABLE IF EXISTS [estado_curso];
DROP TABLE IF EXISTS [anoLetivoHistorico];
DROP TABLE IF EXISTS [anoLetivo];
DROP TABLE IF EXISTS [departamento];
DROP TABLE IF EXISTS [docente];
DROP TABLE IF EXISTS [gestor];
DROP TABLE IF EXISTS [login];

PRINT 'Tabelas removidas. Arranque a aplicacao em modo SQL para as recriar e reimportar dos CSV.';
GO
