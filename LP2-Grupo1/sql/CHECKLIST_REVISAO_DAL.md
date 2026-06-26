# Checklist de Revisão da Camada DAL (Cartão 1 — Otimização de Queries e Gestão de Ligações à BD)

Revisão de todos os ficheiros `*DALSql` quanto a: abertura/fecho de ligação, parametrização,
ausência de `SELECT *`, produtos cartesianos e índices usados.

## Gestão de ligações — centralizada em `dal/db/ConnectionManager.java`

Nenhum `*DALSql` abre ligações por conta própria (zero `DriverManager`/`getConnection`/`createStatement`
fora do `ConnectionManager`). Todas as operações passam por `cm.select()`, `cm.create()` ou `cm.update()`.
Isto garante, num único sítio, que cada método:

- **Abre, executa e fecha em bloco controlado** — cada operação fora de transação abre a sua ligação e
  fecha-a no `finally` (`fecharSeNaoTransacional`), mesmo em caso de exceção.
- **Nunca deixa ligação aberta numa exceção** — o `PreparedStatement`/`ResultSet` está em
  `try (...)` (try-with-resources) e a `Connection` é fechada no `finally`.
- **Usa sempre `PreparedStatement` com parâmetros `?`** — `ligarParametros()` faz `ps.setObject(i+1, ...)`;
  não há concatenação de valores de utilizador em SQL (sem risco de injeção).

## Estado por DAL

Legenda: ✅ conforme · n/a não aplicável

| DAL (SQL) | Abre/fecha via CM | Só `PreparedStatement` | Sem `SELECT *` | JOIN / produto cartesiano | Índices relevantes |
|---|---|---|---|---|---|
| AnoLetivoDALSql | ✅ | ✅ | ✅ (`ano, estado`) | n/a (sem JOIN) | PK `ano` |
| AvaliacaoDALSql | ✅ | ✅ | ✅ (colunas explícitas) | n/a | PK `(numMec, siglaUC, anoLetivo)` · `IX_avaliacao_uc_ano` |
| CursoDALSql | ✅ | ✅ | ✅ (`sigla, nome, siglaDepartamento, propina, estado`) | n/a | PK `sigla` · `IX_curso_departamento` |
| DepartamentoDALSql | ✅ | ✅ | ✅ (`sigla, nome`) | n/a | PK `sigla` |
| DocenteDALSql | ✅ | ✅ | ✅ (`sigla, email, nome, nif, morada, dataNascimento`) | n/a | PK `sigla` · UNIQUE `email`, `nif` |
| EstudanteDALSql | ✅ | ✅ | ✅ (10 colunas explícitas) | n/a | PK `numMec` · UNIQUE `email`, `nif` · `IX_estudante_curso_ano` |
| GestorDALSql | ✅ | ✅ | ✅ (`email, nome, nif, morada, dataNascimento`) | n/a | PK `email` · UNIQUE `nif` |
| HistoricoAnoLetivoDALSql | ✅ | ✅ | ✅ (`ano, estado, dataArquivo`) | n/a | PK `ano` |
| HistoricoDALSql | ✅ | ✅ | ✅ (`anoLetivo, numMec, siglaUC, notas, estado`) | n/a | PK `(anoLetivo, numMec, siglaUC)` · `IX_historico_numMec` |
| InscricaoDALSql | ✅ | ✅ | ✅ (colunas explícitas) | n/a | PK `(numMec, siglaUC, anoLetivo)` · `IX_inscricao_uc_ano` |
| LoginDALSql | ✅ | ✅ | ✅ (8 colunas explícitas) | n/a | PK `id` · UNIQUE `email` |
| PagamentoDALSql | ✅ | ✅ | ✅ (colunas explícitas) | n/a | PK `id` · `IX_pagamento_numMec` |
| UcDALSql | ✅ | ✅ | ✅ (`sigla, nome, anoCurricular, siglaDocente, siglaCurso, numMomentos`) | n/a | PK `(sigla, siglaCurso)` · `IX_uc_siglaCurso`, `IX_uc_siglaDocente` |
| AulaDALSql | ✅ | ✅ | ✅ (`id, anoLetivo, siglaUC, siglaCurso, siglaDocente, data, horaInicio, horaFim, bloco`) | n/a | PK `id` · `IX_aula_ano`, `IX_aula_uc_ano`, `IX_aula_docente_data` |
| PresencaDALSql | ✅ | ✅ | ✅ (`id, idAula, numMec, estado, docenteMarcou, statusDocente, dataHoraRegisto`) | n/a | PK `id` · `IX_presenca_idAula`, `IX_presenca_numMec` |

## Notas

- **`SELECT *` eliminado.** Todas as ocorrências (incl. `AulaDALSql` e `PresencaDALSql`, os
  módulos novos de horário/presenças) foram substituídas por listas de colunas explícitas,
  alinhadas com os nomes que cada `RowMapper` lê (`rs.getString("...")`). Verificação automática:
  zero `SELECT *`/`SELECT TOP 1 *` em todos os `*DALSql`.
- **Sem JOINs SQL.** A aplicação não usa JOINs entre tabelas (as composições são feitas em memória na
  BLL). Não existe risco de produto cartesiano. Caso se introduzam JOINs no futuro, garantir condição de
  junção em todas as FKs e índices nas colunas de junção.
- **Colunas dinâmicas em `AvaliacaoDALSql`** (INSERT/UPDATE): os nomes de coluna são construídos a partir
  de uma lista interna fixa (não de input do utilizador) e os **valores** continuam ligados por `?`.
  Parametrização segura.
- **Índices novos:** ver `schema_indices.sql`. Cobrem as colunas filtradas em `WHERE` que não são prefixo
  de uma PK nem já têm índice por `UNIQUE`. Script idempotente — correr uma vez em modo SQL.

## Adenda — revisao de novos DAL (estatutos e justificacoes)

| DAL (SQL) | Abre/fecha via CM | So PreparedStatement | Sem SELECT * | Indices/PK |
|---|---|---|---|---|
| TipoJustificacaoDALSql | OK | OK | OK (colunas explicitas) | PK id |
| JustificacaoDALSql | OK | OK | OK (colunas explicitas) | PK id; FK numMec/idAula/idTipoJustificacao |
| EstatutoDALSql | OK | OK | OK (colunas explicitas) | PK id; estudante_estatuto PK(numMec,idEstatuto) |

Nota: os SELECT * em TipoJustificacaoDALSql e JustificacaoDALSql foram substituidos por
listas de colunas explicitas (constante COLUNAS).
