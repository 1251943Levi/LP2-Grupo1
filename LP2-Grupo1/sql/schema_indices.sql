-- ============================================================
--  Índices de desempenho (Cartão 1 — Otimização de Queries)
--  Servidor: ctespbd.dei.isep.ipp.pt / BD 2026_LP2_G1_FEIRA
--
--  PORQUÊ: as PK criam um índice clustered e as colunas UNIQUE
--  (email, nif) já têm índice próprio. Faltam índices nas colunas
--  usadas em filtros (WHERE) e chaves estrangeiras que NÃO são
--  prefixo de uma PK — sem eles o SQL Server faz table scan.
--
--  Mapeamento query -> índice:
--   * EstudanteDALSql.contarEstudantesPorCursoEAno  -> estudante(siglaCurso, anoCurricular)
--   * UcDALSql.listarPorCurso / obterUcsDoCurso      -> uc(siglaCurso)
--   * UcDALSql.obterUcsPorDocente                    -> uc(siglaDocente)
--   * InscricaoDALSql.obterAlunosPorUc               -> inscricao(siglaUC, anoLetivo)
--   * HistoricoDALSql.listarPorEstudante             -> historicoAcademico(numMec)
--   * PagamentoDALSql (consulta por aluno)           -> pagamento(numMec)
--   * CursoDALSql (FK departamento)                  -> curso(siglaDepartamento)
--   * AnoLetivoBLL.validarNotasPendentes (por UC)    -> avaliacao(siglaUC, anoLetivo)
--
--  Idempotente: cada CREATE só corre se o índice ainda não existir.
--  Executar UMA VEZ em modo SQL, depois de as tabelas existirem.
-- ============================================================

SET NOCOUNT ON;

-- estudante: filtros por curso + ano curricular (quórum, contagens)
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_estudante_curso_ano')
    CREATE NONCLUSTERED INDEX IX_estudante_curso_ano
        ON [estudante] (siglaCurso, anoCurricular);

-- uc: listagens por curso
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_uc_siglaCurso')
    CREATE NONCLUSTERED INDEX IX_uc_siglaCurso
        ON [uc] (siglaCurso);

-- uc: UCs de um docente
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_uc_siglaDocente')
    CREATE NONCLUSTERED INDEX IX_uc_siglaDocente
        ON [uc] (siglaDocente);

-- inscricao: alunos inscritos numa UC num dado ano (PK começa em numMec,
-- por isso filtrar por siglaUC/anoLetivo não usa a PK)
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_inscricao_uc_ano')
    CREATE NONCLUSTERED INDEX IX_inscricao_uc_ano
        ON [inscricao] (siglaUC, anoLetivo);

-- historicoAcademico: histórico de um estudante (PK começa em anoLetivo)
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_historico_numMec')
    CREATE NONCLUSTERED INDEX IX_historico_numMec
        ON [historicoAcademico] (numMec);

-- pagamento: pagamentos de um aluno (FK numMec)
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_pagamento_numMec')
    CREATE NONCLUSTERED INDEX IX_pagamento_numMec
        ON [pagamento] (numMec);

-- curso: FK para departamento
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_curso_departamento')
    CREATE NONCLUSTERED INDEX IX_curso_departamento
        ON [curso] (siglaDepartamento);

-- avaliacao: notas pendentes por UC + ano (PK começa em numMec)
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_avaliacao_uc_ano')
    CREATE NONCLUSTERED INDEX IX_avaliacao_uc_ano
        ON [avaliacao] (siglaUC, anoLetivo);

-- aula: horário por ano letivo (AulaDALSql.listarPorAnoLetivo)
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_aula_ano')
    CREATE NONCLUSTERED INDEX IX_aula_ano
        ON [aula] (anoLetivo);

-- aula: aulas de uma UC num ano (listarPorUc)
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_aula_uc_ano')
    CREATE NONCLUSTERED INDEX IX_aula_uc_ano
        ON [aula] (siglaUC, anoLetivo);

-- aula: deteção de sobreposição de horário do docente (WHERE data = ? AND siglaDocente = ?)
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_aula_docente_data')
    CREATE NONCLUSTERED INDEX IX_aula_docente_data
        ON [aula] (siglaDocente, data);

-- presenca: presenças de uma aula (PresencaDALSql.listarPorAula)
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_presenca_idAula')
    CREATE NONCLUSTERED INDEX IX_presenca_idAula
        ON [presenca] (idAula);

-- presenca: presenças de um estudante (listarPorEstudante)
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_presenca_numMec')
    CREATE NONCLUSTERED INDEX IX_presenca_numMec
        ON [presenca] (numMec);

PRINT 'Indices de desempenho criados/confirmados.';
GO
