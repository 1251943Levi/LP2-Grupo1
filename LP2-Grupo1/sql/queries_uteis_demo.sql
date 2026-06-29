-- ============================================================
--  QUERIES ÚTEIS PARA A DEMO/TESTES — alteração de campos
--  BD: 2026_LP2_G1_FEIRA
--  Corre só a(s) linha(s) que precisas. As DESTRUTIVAS estão marcadas [!].
--  Faz sempre backup antes de mexer em massa.
-- ============================================================
SET NOCOUNT ON;

-- =======================================================
-- ANO LETIVO  (estado: PLANEAMENTO | INICIADO | FECHADO)
-- =======================================================
-- Ver:
SELECT ano, estado FROM [anoLetivo] ORDER BY ano;
-- Voltar 2027 a INICIADO (p/ lançar notas):
-- UPDATE [anoLetivo] SET estado = 'INICIADO' WHERE ano = 2027;
-- Pôr 2027 em PLANEAMENTO (p/ definir momentos / registar):
-- UPDATE [anoLetivo] SET estado = 'PLANEAMENTO' WHERE ano = 2027;
-- Fechar 2027:
-- UPDATE [anoLetivo] SET estado = 'FECHADO' WHERE ano = 2027;
-- Criar um ano novo em PLANEAMENTO (ex.: 2028):
-- INSERT INTO [anoLetivo](ano, estado) VALUES (2028, 'PLANEAMENTO');

-- =======================================================
-- CURSO  (estado: ATIVO | INATIVO | SEM_CONDICOES)
-- =======================================================
SELECT sigla, nome, propina, estado FROM [curso] ORDER BY sigla;
-- UPDATE [curso] SET estado  = 'ATIVO'   WHERE sigla = 'EEC';
-- UPDATE [curso] SET estado  = 'INATIVO' WHERE sigla = 'EER';
-- UPDATE [curso] SET propina = 1000.00  WHERE sigla = 'LEI';

-- =======================================================
-- UC  (estado + numMomentos + docente responsável)
-- =======================================================
SELECT sigla, nome, anoCurricular, siglaDocente, siglaCurso, numMomentos, estado FROM [uc] ORDER BY sigla;
-- Definir nº de momentos de uma UC (ex.: 1):
-- UPDATE [uc] SET numMomentos = 1 WHERE sigla IN ('ALG','EST','FIS','MAT');
-- Pôr UC ATIVA / mudar docente:
-- UPDATE [uc] SET estado = 'ATIVO'  WHERE sigla = 'ALG';
-- UPDATE [uc] SET siglaDocente = 'MFP' WHERE sigla = 'ALG';

-- =======================================================
-- ESTUDANTE  (saldo de propina, ano curricular, curso)
-- =======================================================
SELECT numMec, nome, siglaCurso, anoCurricular, saldoDevedor FROM [estudante] ORDER BY numMec;
-- "Pagar" propinas (saldo 0) p/ poder fechar/avançar o ano:
-- UPDATE [estudante] SET saldoDevedor = 0 WHERE numMec BETWEEN 20260012 AND 20260018;
-- Repor dívida (ex.: 1200):
-- UPDATE [estudante] SET saldoDevedor = 1200 WHERE numMec = 20260013;
-- Forçar o ano curricular de um aluno:
-- UPDATE [estudante] SET anoCurricular = 1 WHERE numMec = 20260013;

-- =======================================================
-- INSCRIÇÕES  (numMec, siglaUC, anoLetivo, anoLetivoRealizacao)
-- =======================================================
SELECT * FROM [inscricao] WHERE anoLetivo = 2027 ORDER BY numMec, siglaUC;
-- Inscrever um aluno numa UC:
-- INSERT INTO [inscricao](numMec, siglaUC, anoLetivo, anoLetivoRealizacao) VALUES (20260012, 'ALG', 2027, 2027);
-- Remover uma inscrição:
-- DELETE FROM [inscricao] WHERE numMec = 20260012 AND siglaUC = 'ALG' AND anoLetivo = 2027;   -- [!]

-- =======================================================
-- LOGIN  (ativar/desativar acesso)
-- =======================================================
SELECT id, email, tipoUtilizador, ativo FROM [login] ORDER BY id;
-- UPDATE [login] SET ativo = 1 WHERE email = 'mfp@issmf.ipp.pt';

-- =======================================================
-- REPOR para repetir a demo (apagar dados transacionais, SEM dropar tabelas)  [!]
-- =======================================================
-- Apagar todas as notas/avaliações:
-- DELETE FROM [avaliacao];                              -- [!]
-- DELETE FROM [nota];                                   -- [!]
-- Apagar presenças/justificações/aulas da Parte III:
-- DELETE FROM [presenca];                               -- [!]
-- DELETE FROM [justificacao];                           -- [!]
-- DELETE FROM [aula];                                   -- [!]
-- Desfazer o avanço de ano (apagar 2028 e inscrições novas):
-- DELETE FROM [inscricao] WHERE anoLetivo = 2028;       -- [!]
-- DELETE FROM [anoLetivo] WHERE ano = 2028;             -- [!]
-- Limpar histórico académico:
-- DELETE FROM [historicoAcademico];                     -- [!]
