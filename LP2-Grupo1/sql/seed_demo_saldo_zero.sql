-- Opcional para a demo: marca as propinas dos alunos de EEC como pagas (saldo 0),
-- para o 'fechar/avançar' do ano não bloquear por dívida. NÃO destrutivo.
SET NOCOUNT ON;
UPDATE [estudante] SET saldoDevedor = 0
WHERE numMec IN (20260012,20260013,20260014,20260015,20260016,20260017,20260018);
PRINT 'Saldos a 0 para os alunos da demo.';
SELECT numMec, nome, saldoDevedor FROM [estudante]
WHERE numMec BETWEEN 20260012 AND 20260018 ORDER BY numMec;
