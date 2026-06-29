-- ============================================================
--  simulacao_completa.sql  (Cartão 2 — Simulação e Validação SQL)
--  Servidor: ctespbd.dei.isep.ipp.pt / BD 2026_LP2_G1_FEIRA
--
--  OBJETIVO: executar em SQL puro os fluxos principais de negócio e os
--  cenários de restrição, e verificar a integridade referencial do schema.
--
--  COMO CORRER: abrir no SSMS/Azure Data Studio ligado à BD do grupo e
--  executar tudo. O script está dividido em duas partes:
--
--    PARTE A — Integridade referencial dos DADOS ATUAIS (somente leitura).
--              Cada consulta deve devolver 0 linhas. Linhas devolvidas = órfãos.
--
--    PARTE B — Simulação dos fluxos dentro de UMA transação que termina em
--              ROLLBACK. NÃO altera os dados reais: serve para provar que cada
--              fluxo é executável em SQL e que as restrições são detetáveis.
--
--  Dados de teste usam chaves reservadas que não colidem com dados reais:
--    departamento/curso/docente = 'TST'   uc = 'TSTUC' / 'TSTNM'
--    estudante numMec = 99990001 / 99990002   anoLetivo = 2999
--
--  Inclui os módulos novos: horário ([aula]) e presenças ([presenca]).
--  O cenário de SOBREPOSIÇÃO DE HORÁRIO está implementado na secção B.7.
-- ============================================================

SET NOCOUNT ON;
PRINT '====================================================================';
PRINT ' PARTE A — INTEGRIDADE REFERENCIAL DOS DADOS ATUAIS (esperado: 0 linhas)';
PRINT '====================================================================';

-- A.1  FKs declaradas no schema -------------------------------------------------
PRINT '[A.1] curso.siglaDepartamento sem departamento:';
SELECT c.sigla, c.siglaDepartamento
FROM [curso] c LEFT JOIN [departamento] d ON c.siglaDepartamento = d.sigla
WHERE d.sigla IS NULL;

PRINT '[A.2] curso.estado fora de estado_curso:';
SELECT c.sigla, c.estado
FROM [curso] c LEFT JOIN [estado_curso] e ON c.estado = e.nome
WHERE e.nome IS NULL;

PRINT '[A.3] estudante.siglaCurso sem curso:';
SELECT e.numMec, e.siglaCurso
FROM [estudante] e LEFT JOIN [curso] c ON e.siglaCurso = c.sigla
WHERE c.sigla IS NULL;

PRINT '[A.4] uc.siglaDocente sem docente (ignora NULL):';
SELECT u.sigla, u.siglaDocente
FROM [uc] u LEFT JOIN [docente] d ON u.siglaDocente = d.sigla
WHERE u.siglaDocente IS NOT NULL AND d.sigla IS NULL;

PRINT '[A.5] uc.siglaCurso sem curso:';
SELECT u.sigla, u.siglaCurso
FROM [uc] u LEFT JOIN [curso] c ON u.siglaCurso = c.sigla
WHERE c.sigla IS NULL;

PRINT '[A.6] inscricao.numMec sem estudante:';
SELECT i.numMec, i.siglaUC, i.anoLetivo
FROM [inscricao] i LEFT JOIN [estudante] e ON i.numMec = e.numMec
WHERE e.numMec IS NULL;

PRINT '[A.7] inscricao.anoLetivo sem anoLetivo:';
SELECT i.numMec, i.siglaUC, i.anoLetivo
FROM [inscricao] i LEFT JOIN [anoLetivo] a ON i.anoLetivo = a.ano
WHERE a.ano IS NULL;

PRINT '[A.8] avaliacao.numMec sem estudante:';
SELECT av.numMec, av.siglaUC, av.anoLetivo
FROM [avaliacao] av LEFT JOIN [estudante] e ON av.numMec = e.numMec
WHERE e.numMec IS NULL;

PRINT '[A.9] historicoAcademico.numMec sem estudante:';
SELECT h.anoLetivo, h.numMec, h.siglaUC
FROM [historicoAcademico] h LEFT JOIN [estudante] e ON h.numMec = e.numMec
WHERE e.numMec IS NULL;

PRINT '[A.10] pagamento.numMec sem estudante:';
SELECT p.id, p.numMec
FROM [pagamento] p LEFT JOIN [estudante] e ON p.numMec = e.numMec
WHERE e.numMec IS NULL;

-- A.11  Referências "soft" a UC (siglaUC NÃO tem FK porque a PK de [uc] é
--       composta (sigla, siglaCurso)). Verificadas manualmente aqui.
PRINT '[A.11] inscricao.siglaUC inexistente em uc:';
SELECT DISTINCT i.siglaUC
FROM [inscricao] i LEFT JOIN [uc] u ON i.siglaUC = u.sigla
WHERE u.sigla IS NULL;

PRINT '[A.12] avaliacao.siglaUC inexistente em uc:';
SELECT DISTINCT av.siglaUC
FROM [avaliacao] av LEFT JOIN [uc] u ON av.siglaUC = u.sigla
WHERE u.sigla IS NULL;

-- A.13–A.14  Módulos novos: horário (aula) e presenças (presenca)
PRINT '[A.13] aula com docente / ano letivo / uc inexistentes:';
SELECT a.id, a.siglaDocente, a.anoLetivo, a.siglaUC, a.siglaCurso
FROM [aula] a
LEFT JOIN [docente]  d ON a.siglaDocente = d.sigla
LEFT JOIN [anoLetivo] al ON a.anoLetivo = al.ano
LEFT JOIN [uc] u ON a.siglaUC = u.sigla AND a.siglaCurso = u.siglaCurso
WHERE d.sigla IS NULL OR al.ano IS NULL OR u.sigla IS NULL;

PRINT '[A.14] presenca com aula / estudante inexistentes:';
SELECT p.id, p.idAula, p.numMec
FROM [presenca] p
LEFT JOIN [aula] a ON p.idAula = a.id
LEFT JOIN [estudante] e ON p.numMec = e.numMec
WHERE a.id IS NULL OR e.numMec IS NULL;

-- A.15  Regra de negócio: nenhum docente pode ter duas aulas sobrepostas no
--       mesmo dia (verificação sobre os dados ATUAIS — esperado 0 linhas).
PRINT '[A.15] sobreposições de horário existentes (mesmo docente, mesmo dia):';
SELECT a1.siglaDocente, a1.data,
       a1.id AS aula1, a1.horaInicio AS ini1, a1.horaFim AS fim1,
       a2.id AS aula2, a2.horaInicio AS ini2, a2.horaFim AS fim2
FROM [aula] a1
JOIN [aula] a2
  ON a1.siglaDocente = a2.siglaDocente
 AND a1.data = a2.data
 AND a1.id < a2.id
 AND a1.horaInicio < a2.horaFim
 AND a2.horaInicio < a1.horaFim;


PRINT '';
PRINT '====================================================================';
PRINT ' PARTE B — SIMULAÇÃO DOS FLUXOS (em transação, termina em ROLLBACK)';
PRINT '====================================================================';

BEGIN TRY
    BEGIN TRANSACTION;

    -- Garantir que o estado de curso usado existe (idempotente)
    IF NOT EXISTS (SELECT 1 FROM [estado_curso] WHERE nome = 'Pendente')
        INSERT INTO [estado_curso] (nome) VALUES ('Pendente');

    -- ----------------------------------------------------------------
    -- B.1  CRIAÇÃO DE ANO LETIVO: PLANEAMENTO -> INICIADO -> FECHADO
    -- ----------------------------------------------------------------
    PRINT '[B.1] Criação e ciclo de vida do ano letivo 2999';
    INSERT INTO [anoLetivo] (ano, estado) VALUES (2999, 'PLANEAMENTO');
    UPDATE [anoLetivo] SET estado = 'INICIADO' WHERE ano = 2999;
    UPDATE [anoLetivo] SET estado = 'FECHADO'  WHERE ano = 2999;
    SELECT ano, estado AS estado_final FROM [anoLetivo] WHERE ano = 2999;

    -- Dados de apoio (departamento, curso, docente, uc)
    INSERT INTO [departamento] (sigla, nome) VALUES ('TST', 'Departamento de Teste');
    INSERT INTO [curso] (sigla, nome, siglaDepartamento, propina, estado)
        VALUES ('TST', 'Curso de Teste', 'TST', 1000.00, 'Pendente');
    INSERT INTO [docente] (sigla, email, nome, nif)
        VALUES ('TST', 'tst.docente@issmf.ipp.pt', 'Docente Teste', '900000001');
    -- UC COM momentos definidos (numMomentos = 2)
    INSERT INTO [uc] (sigla, nome, anoCurricular, siglaDocente, siglaCurso, numMomentos)
        VALUES ('TSTUC', 'UC de Teste', 1, 'TST', 'TST', 2);
    -- UC SEM momentos definidos (numMomentos = 0) — para o cenário B.6
    INSERT INTO [uc] (sigla, nome, anoCurricular, siglaDocente, siglaCurso, numMomentos)
        VALUES ('TSTNM', 'UC sem momentos', 1, 'TST', 'TST', 0);

    -- Estudantes: 99990001 será inscrito; 99990002 fica SEM inscrição (cenário B.5)
    INSERT INTO [estudante] (numMec, email, nome, nif, anoInscricao, siglaCurso, anoCurricular)
        VALUES (99990001, '99990001@issmf.ipp.pt', 'Aluno Inscrito', '900000002', 2999, 'TST', 1);
    INSERT INTO [estudante] (numMec, email, nome, nif, anoInscricao, siglaCurso, anoCurricular)
        VALUES (99990002, '99990002@issmf.ipp.pt', 'Aluno Sem Inscricao', '900000003', 2999, 'TST', 1);

    -- ----------------------------------------------------------------
    -- B.2  INSCRIÇÃO de um estudante numa UC
    -- ----------------------------------------------------------------
    PRINT '[B.2] Inscrição do aluno 99990001 na UC TSTUC';
    INSERT INTO [inscricao] (numMec, siglaUC, anoLetivo, anoLetivoRealizacao)
        VALUES (99990001, 'TSTUC', 2999, 2999);
    SELECT numMec, siglaUC, anoLetivo FROM [inscricao] WHERE numMec = 99990001 AND anoLetivo = 2999;

    -- ----------------------------------------------------------------
    -- B.3  LANÇAMENTO DE NOTAS (2 momentos) e nota final = média
    -- ----------------------------------------------------------------
    PRINT '[B.3] Lançamento de 2 notas e cálculo de média';
    INSERT INTO [avaliacao] (numMec, siglaUC, anoLetivo, nota1, nota2)
        VALUES (99990001, 'TSTUC', 2999, 14.0, 16.0);
    SELECT numMec, siglaUC,
           nota1, nota2,
           CAST((nota1 + nota2) / 2.0 AS DECIMAL(4,2)) AS nota_final,
           CASE WHEN (nota1 + nota2) / 2.0 >= 10 THEN 'APROVADO' ELSE 'REPROVADO' END AS resultado
    FROM [avaliacao] WHERE numMec = 99990001 AND siglaUC = 'TSTUC' AND anoLetivo = 2999;

    -- ----------------------------------------------------------------
    -- B.4  REPROVAÇÃO / APROVAÇÃO — registo no histórico académico
    -- ----------------------------------------------------------------
    PRINT '[B.4] Registo no histórico (aprovação) e exemplo de reprovação';
    INSERT INTO [historicoAcademico] (anoLetivo, numMec, siglaUC, notas, estado)
        VALUES (2999, 99990001, 'TSTUC', '14.0 16.0', 'APROVADO');
    -- exemplo de reprovação (nota < 10)
    INSERT INTO [historicoAcademico] (anoLetivo, numMec, siglaUC, notas, estado)
        VALUES (2999, 99990002, 'TSTUC', '8.0 6.0', 'REPROVADO');
    SELECT numMec, siglaUC, estado FROM [historicoAcademico] WHERE anoLetivo = 2999;

    -- ----------------------------------------------------------------
    -- B.5  RESTRIÇÃO: estudante SEM inscrição não pode receber nota
    --      (a listagem de alunos para avaliação só devolve os inscritos)
    -- ----------------------------------------------------------------
    PRINT '[B.5] Listagem de inscritos na TSTUC (99990002 NÃO deve aparecer)';
    SELECT i.numMec, e.nome
    FROM [inscricao] i JOIN [estudante] e ON i.numMec = e.numMec
    WHERE i.siglaUC = 'TSTUC' AND i.anoLetivo = 2999
    ORDER BY i.numMec;
    PRINT '   -> Aluno 99990002 ausente da lista = restrição validada.';

    -- ----------------------------------------------------------------
    -- B.6  RESTRIÇÃO: UC sem momentos definidos bloqueia o INÍCIO do ano
    --      (numMomentos = 0). Esta query é a base de validarMomentosUcs().
    -- ----------------------------------------------------------------
    PRINT '[B.6] UCs sem momentos definidos (numMomentos = 0) — bloqueiam iniciar:';
    SELECT sigla, nome, numMomentos
    FROM [uc]
    WHERE numMomentos = 0 AND siglaCurso = 'TST';
    PRINT '   -> TSTNM listada = restrição "UC sem momentos" detetada.';

    -- ----------------------------------------------------------------
    -- B.7  RESTRIÇÃO: sobreposição de horário (mesmo docente, mesmo dia)
    --      Duas aulas do docente TST no mesmo dia com horas que se sobrepõem
    --      (18:00–20:00 e 19:00–21:00). A query deve devolver o par em conflito.
    -- ----------------------------------------------------------------
    PRINT '[B.7] Sobreposição de horário do docente TST em 2999-09-15';
    INSERT INTO [aula] (anoLetivo, siglaUC, siglaCurso, siglaDocente, data, horaInicio, horaFim, bloco)
        VALUES (2999, 'TSTUC', 'TST', 'TST', '2999-09-15', '18:00', '20:00', 1);
    INSERT INTO [aula] (anoLetivo, siglaUC, siglaCurso, siglaDocente, data, horaInicio, horaFim, bloco)
        VALUES (2999, 'TSTUC', 'TST', 'TST', '2999-09-15', '19:00', '21:00', 1);
    SELECT a1.siglaDocente, a1.data,
           a1.horaInicio AS ini1, a1.horaFim AS fim1,
           a2.horaInicio AS ini2, a2.horaFim AS fim2
    FROM [aula] a1
    JOIN [aula] a2
      ON a1.siglaDocente = a2.siglaDocente
     AND a1.data = a2.data
     AND a1.id < a2.id
     AND a1.horaInicio < a2.horaFim
     AND a2.horaInicio < a1.horaFim
    WHERE a1.siglaDocente = 'TST';
    PRINT '   -> Par devolvido = sobreposição detetada (a inserção deve ser rejeitada pela BLL).';

    -- ----------------------------------------------------------------
    -- B.8  Presenças: marcar presença de um aluno numa aula
    -- ----------------------------------------------------------------
    PRINT '[B.8] Registo de presença do aluno 99990001 numa aula';
    DECLARE @idAula INT = (SELECT TOP 1 id FROM [aula]
                           WHERE siglaDocente = 'TST' AND data = '2999-09-15'
                           ORDER BY horaInicio);
    INSERT INTO [presenca] (idAula, numMec, estado, docenteMarcou)
        VALUES (@idAula, 99990001, 'PRESENTE', 1);
    SELECT idAula, numMec, estado, docenteMarcou
    FROM [presenca] WHERE numMec = 99990001;

    -- ----------------------------------------------------------------
    -- Reverter tudo: a simulação não persiste dados.
    -- ----------------------------------------------------------------
    ROLLBACK TRANSACTION;
    PRINT '';
    PRINT '>> Simulação concluída com sucesso. ROLLBACK efetuado (sem alterações reais).';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    PRINT '>> ERRO na simulação — ROLLBACK efetuado.';
    PRINT ERROR_MESSAGE();
END CATCH;
GO
