-- ============================================================
--  Atualização da tabela [login] com as credenciais atuais
--  Fonte: bd/credenciais.csv (passwords regeradas)
--  Servidor: ctespbd.dei.isep.ipp.pt / BD 2026_LP2_G1_FEIRA
--
--  Executar UMA VEZ no SQL Server (modo BD) para sincronizar
--  os logins com as novas palavras-passe. Idempotente:
--   - atualiza salt/hash de quem já existe;
--   - insere quem ainda não existir na tabela.
-- ============================================================

SET NOCOUNT ON;

MERGE [login] AS alvo
USING (VALUES
    (N'20260001@issmf.ipp.pt', N'P3inoJKLmwHJP6vTZGhGtg==', N'hSUq32h58aemciB2hHtnAc4ltc8/Zh1ozv86rLtG9a8=', N'ESTUDANTE', 1),
    (N'20260002@issmf.ipp.pt', N'nM2fZwkRAiLptwZgHN6fdg==', N'8Vbp5J+HNiVt5SHIawKbfPynUsvfW/Z1J7nzjPhpEBU=', N'ESTUDANTE', 1),
    (N'20260003@issmf.ipp.pt', N'zLoY/C2zztsMo3qOUhkKSw==', N'KJXjz3oB2TJS49841j1Om1Zw4D5ubqbXMmuIAAgNJd0=', N'ESTUDANTE', 1),
    (N'20260004@issmf.ipp.pt', N'yiaXP3IsZc9j4ZOJYjSbTQ==', N'Ksdn/BNZjUfLunOGN9KzKR6ymRJGPFd2+SlPW4n53d8=', N'ESTUDANTE', 1),
    (N'abc@issmf.ipp.pt', N'XCU20SyJGBig4tFG9oSegg==', N'rIR+LnLxEAR00eMywRfm8oefZ4RPACL1+2EfnE/dk44=', N'DOCENTE', 1),
    (N'xyz@issmf.ipp.pt', N'8FmP7fPmLyng9nobPk8W1A==', N'PBip4xW5ipCId27dFnawnzRmDwWQB9Zv2RV+BwSDiPs=', N'DOCENTE', 1),
    (N'mno@issmf.ipp.pt', N'awJi/2dRX8yJcGQ5G8moTQ==', N'MewNWcJizZo9X8/HFUONxTwrNCGjzXCpZ1raV6VSYxA=', N'DOCENTE', 1),
    (N'20260005@issmf.ipp.pt', N'ZVB0zBSM/u6OBDA2rbMxDA==', N'1vmg56o6u8BeZ6dVraAZpg6pDAowPeOAPFEUCcZ2JUM=', N'ESTUDANTE', 1),
    (N'20260006@issmf.ipp.pt', N'pRSQoQ2GoP6RpYMxHTbTcA==', N'DdlMtsW58VwZQp1zfj2We/5a655GHbRM9S7uUB6lzLY=', N'ESTUDANTE', 1),
    (N'20260007@issmf.ipp.pt', N'00KLGWI02/xjWiKIK8UWfw==', N'tzKOznBQ4TnCHrsAtX0VnEVPL/oPe8HwoEn4raiSkKk=', N'ESTUDANTE', 1),
    (N'20260008@issmf.ipp.pt', N'ZuZynhKLF+xOJgCwNxpLlg==', N'6T+aY6IBa3c+QXXjxfpQ31UxdLDf1dne2BUVH4XztRM=', N'ESTUDANTE', 1),
    (N'20260009@issmf.ipp.pt', N'ub6/zYHvSd+/QHkbKs066w==', N'sjcNlOr1PMdEI8onVN8AfF1EnGorI5PEXy9GeBihAIk=', N'ESTUDANTE', 1),
    (N'20260010@issmf.ipp.pt', N'NcMebLWdt68eNd42yZXVLw==', N'QNjbcOwVr6cJpp4XTLQOjklxvm3MiZ4oGHCFWohj1Ro=', N'ESTUDANTE', 1),
    (N'mfp@issmf.ipp.pt', N'j9kk7LmZUcFwLpVMJQkA5A==', N'QW8du1gUoUliUc3xUTpvfd6aV1HwVgrFmWOze2aDgxk=', N'DOCENTE', 1),
    (N'avn@issmf.ipp.pt', N'Mus7HQhe5ZjNxPunOz7ojw==', N'rkF7FtKo9n7volCXuOV25lZ9cFLgQG0pCKo5pW4Fuzg=', N'DOCENTE', 1),
    (N'ava@issmf.ipp.pt', N'GZiWrF5wBd1RGM6xG5MArg==', N'dlNwtdmrWnBbarF4GNBR0UFLvVyAqJ01M3zFq4kcl4Q=', N'DOCENTE', 1),
    (N'20260012@issmf.ipp.pt', N'6DO/0cO0mq+k6+PcCv3qpA==', N'zOQ0rcHp500N3So/BdDXPR8aQB+hPogiM3akR3xkFMQ=', N'ESTUDANTE', 1),
    (N'20260013@issmf.ipp.pt', N'hIyjNqrmF8jisTTUD8GksA==', N'Wd7NmzGmxIhP5/zcu+Re7IH+g+mR3cEFX2j/D5Ihas8=', N'ESTUDANTE', 1),
    (N'20260014@issmf.ipp.pt', N'eFt0jtZ/BadUZybsNNrtCg==', N'JZ6+ehY6+03mYuOLS4b5KOtpR5i/ucNR0G5FLJazcOY=', N'ESTUDANTE', 1),
    (N'20260015@issmf.ipp.pt', N'UqfL2EWqrny01l2kMFBW4g==', N'SqKMjrLH+AjAjdo86pzR7RI8Ibh8ZfjqvJZth15h6Ok=', N'ESTUDANTE', 1),
    (N'20260016@issmf.ipp.pt', N'UNyRUCWQQRFW8166jDZTPQ==', N'dsP0i7xgcROVPE9scH7IDL8eSiesfMXVj+OxXJPa6rs=', N'ESTUDANTE', 1),
    (N'20260017@issmf.ipp.pt', N'5m16AAR0/pXcoJqkI2A/Sg==', N'+MFpnu0K6F1Eulhigi4HNykgk2NKfXDGDFC0Cdt1K88=', N'ESTUDANTE', 1),
    (N'20260018@issmf.ipp.pt', N'KlvxsaiMlGIXhDh+PzlptQ==', N'Ts/61AM/48r7oxHvs8MJdk6PlzobAbuJD/ascKgWLK0=', N'ESTUDANTE', 1),
    (N'jos@issmf.ipp.pt', N'KX6adQPIiNMi0GrZE3ohdw==', N'otGfAb6oyiWO/fkEeQ0Szf9hL4phbom+NvaSFbJAyvg=', N'DOCENTE', 1),
    (N'backoffice@issmf.ipp.pt', N'LxU2v8fm4fCx9IzxQ7ioFA==', N'W8eh4Qs5jH9666HZdcxeZ9sFe6otGbBhmYwZK16mIps=', N'GESTOR', 1)
) AS origem (email, passwordSalt, passwordHash, tipoUtilizador, ativo)
ON alvo.email = origem.email
WHEN MATCHED THEN
    UPDATE SET alvo.passwordSalt   = origem.passwordSalt,
               alvo.passwordHash   = origem.passwordHash,
               alvo.tipoUtilizador = origem.tipoUtilizador,
               alvo.ativo          = origem.ativo,
               alvo.updatedAt      = SYSDATETIME()
WHEN NOT MATCHED BY TARGET THEN
    INSERT (email, passwordHash, passwordSalt, tipoUtilizador, ativo)
    VALUES (origem.email, origem.passwordHash, origem.passwordSalt, origem.tipoUtilizador, origem.ativo);

PRINT 'Logins sincronizados: ' + CAST(@@ROWCOUNT AS NVARCHAR(10)) + ' linha(s) afetada(s).';
GO
