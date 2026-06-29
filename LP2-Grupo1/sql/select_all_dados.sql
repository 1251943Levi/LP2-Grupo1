-- ============================================================
--  SELECT * de todas as tabelas de dados (apenas LEITURA)
--  BD: 2026_LP2_G1_FEIRA. Não altera nada.
--  Cada SELECT abre um grid próprio no SSMS/Azure Data Studio.
-- ============================================================

SET NOCOUNT ON;

-- 1) Resumo rápido: nº de linhas por tabela
SELECT 'anoLetivo'          AS tabela, COUNT(*) AS linhas FROM [anoLetivo]
UNION ALL SELECT 'anoLetivoHistorico', COUNT(*) FROM [anoLetivoHistorico]
UNION ALL SELECT 'avaliacao',          COUNT(*) FROM [avaliacao]
UNION ALL SELECT 'curso',              COUNT(*) FROM [curso]
UNION ALL SELECT 'curso_uc',           COUNT(*) FROM [curso_uc]
UNION ALL SELECT 'departamento',       COUNT(*) FROM [departamento]
UNION ALL SELECT 'docente',            COUNT(*) FROM [docente]
UNION ALL SELECT 'estado_curso',       COUNT(*) FROM [estado_curso]
UNION ALL SELECT 'EstadoCurricular',   COUNT(*) FROM [EstadoCurricular]
UNION ALL SELECT 'estudante',          COUNT(*) FROM [estudante]
UNION ALL SELECT 'gestor',             COUNT(*) FROM [gestor]
UNION ALL SELECT 'historicoAcademico', COUNT(*) FROM [historicoAcademico]
UNION ALL SELECT 'inscricao',          COUNT(*) FROM [inscricao]
UNION ALL SELECT 'login',              COUNT(*) FROM [login]
UNION ALL SELECT 'momento',            COUNT(*) FROM [momento]
UNION ALL SELECT 'nota',               COUNT(*) FROM [nota]
UNION ALL SELECT 'pagamento',          COUNT(*) FROM [pagamento]
UNION ALL SELECT 'tipo_momento',       COUNT(*) FROM [tipo_momento]
UNION ALL SELECT 'uc',                 COUNT(*) FROM [uc]
ORDER BY tabela;

-- 2) Conteúdo de cada tabela
SELECT * FROM [anoLetivo];
SELECT * FROM [anoLetivoHistorico];
SELECT * FROM [avaliacao];
SELECT * FROM [curso];
SELECT * FROM [curso_uc];
SELECT * FROM [departamento];
SELECT * FROM [docente];
SELECT * FROM [estado_curso];
SELECT * FROM [EstadoCurricular];
SELECT * FROM [estudante];
SELECT * FROM [gestor];
SELECT * FROM [historicoAcademico];
SELECT * FROM [inscricao];
SELECT * FROM [login];
SELECT * FROM [momento];
SELECT * FROM [nota];
SELECT * FROM [pagamento];
SELECT * FROM [tipo_momento];
SELECT * FROM [uc];
