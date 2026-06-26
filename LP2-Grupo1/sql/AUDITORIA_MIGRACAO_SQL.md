# Auditoria de Migração e Validação SQL (Cartão 2)

Auditoria da migração da camada de ficheiros (`*DALFile`) para SQL (`*DALSql`),
fluxos executáveis em SQL puro, cenários de restrição e integridade referencial.

## 1. Cobertura `*DALFile` → `*DALSql` (todos têm par)

Os 15 DAL têm implementação **File e Sql** e a paridade de métodos públicos foi confirmada
automaticamente (nenhum método existe só num dos lados):

| DAL | File | Sql | Paridade de métodos |
|---|---|---|---|
| AnoLetivoDAL | ✅ | ✅ | igual |
| AvaliacaoDAL | ✅ | ✅ | igual |
| CursoDAL | ✅ | ✅ | igual |
| DepartamentoDAL | ✅ | ✅ | igual |
| DocenteDAL | ✅ | ✅ | igual |
| EstudanteDAL | ✅ | ✅ | igual |
| GestorDAL | ✅ | ✅ | igual |
| HistoricoAnoLetivoDAL | ✅ | ✅ | igual |
| HistoricoDAL | ✅ | ✅ | igual |
| InscricaoDAL | ✅ | ✅ | igual |
| LoginDAL | ✅ | ✅ | igual |
| PagamentoDAL | ✅ | ✅ | igual |
| UcDAL | ✅ | ✅ | igual |
| AulaDAL | ✅ | ✅ | igual |
| PresencaDAL | ✅ | ✅ | igual |

A escolha da implementação é feita em tempo de execução pela BLL com
`ConfigApp.isModoSql() ? new XxxDALSql() : new XxxDALFile()`, ambos a respeitar a mesma
interface `XxxDAL`.

## 2. Validações de negócio replicadas (não vivem só na camada File)

A lógica de negócio está na **BLL**, que é única e partilhada pelos dois modos (File e SQL).
Como a BLL chama apenas métodos da interface `XxxDAL` — presentes em ambas as implementações
(ver paridade acima) — **nenhuma operação ou validação existe exclusivamente no modo File**.
Exemplos de validações centralizadas na BLL (aplicam-se igualmente em SQL):

- quórum mínimo de curso e estado do ano letivo (`AnoLetivoBLL`, `MatriculaBLL`);
- momentos de avaliação por definir (`AnoLetivoBLL.validarMomentosUcs`);
- propinas por definir / em dívida (`AnoLetivoBLL`);
- intervalo de nota 0–20 e momentos máximos (`DocenteController` / `DocenteBLL.lancarNota`);
- unicidade de NIF / e-mail (verificada via DAL `existeNif`/lookup, presente em File e Sql).

## 3. Fluxos principais executados em SQL puro

Implementados e demonstrados em `simulacao_completa.sql` (Parte B):

| Fluxo | Secção do script |
|---|---|
| Criação e ciclo de vida do ano letivo (PLANEAMENTO → INICIADO → FECHADO) | B.1 |
| Inscrição de estudante numa UC | B.2 |
| Lançamento de notas e cálculo de nota final | B.3 |
| Reprovação / aprovação (histórico académico) | B.4 |

## 4. Cenários de restrição

| Restrição | Estado | Secção |
|---|---|---|
| Estudante sem inscrição não aparece na listagem de avaliação | ✅ simulado | B.5 |
| UC sem momentos definidos (`numMomentos = 0`) bloqueia iniciar ano | ✅ simulado | B.6 |
| Sobreposição de horário (mesmo docente, mesmo dia, horas a sobrepor) | ✅ simulado | B.7 |

## 5. Integridade referencial

FKs **declaradas** no schema e verificadas (Parte A, A.1–A.14):
`curso→departamento`, `curso→estado_curso`, `estudante→curso`, `uc→docente`, `uc→curso`,
`inscricao→estudante`, `inscricao→anoLetivo`, `avaliacao→estudante`,
`historicoAcademico→estudante`, `pagamento→estudante`,
`aula→anoLetivo`, `aula→docente`, `aula→uc(sigla,siglaCurso)`,
`presenca→aula`, `presenca→estudante`.

Nota: a tabela `[aula]` referencia `[uc]` pela PK composta `(siglaUC, siglaCurso)` — é o primeiro
sítio do schema com FK real para UC, o que valida a chave composta. Corrigiu-se também a ordem de
DROP em `recriar_bd.sql` (apagar `presenca` e `aula` antes de `uc`/`anoLetivo`/`docente`/`estudante`,
senão o DROP falha por FK).

### Lacuna conhecida — referência "soft" a UC

`inscricao.siglaUC`, `avaliacao.siglaUC` e `historicoAcademico.siglaUC` **não têm FK** para
`[uc]`, porque a PK de `[uc]` é **composta** `(sigla, siglaCurso)` — não é possível referenciar
apenas `sigla`. Estas referências são verificadas manualmente em `simulacao_completa.sql`
(A.11–A.12). A correção definitiva depende do cartão 3 (tabela intermédia `curso_uc`), que
permitiria uma chave de UC independente do curso e, com ela, FKs reais para `siglaUC`.

## 6. Como validar

1. Correr `recriar_bd.sql` (recria tabelas) e arrancar a app em modo SQL para reimportar os CSV.
2. Correr `schema_indices.sql` (índices de desempenho).
3. Correr `simulacao_completa.sql` — Parte A deve devolver **0 linhas** em todas as consultas;
   Parte B imprime os resultados de cada fluxo e termina com ROLLBACK (não altera dados reais).

## Adenda — DAL adicionais com par File/Sql

| DAL | File | Sql | Paridade |
|---|---|---|---|
| TipoJustificacaoDAL | OK | OK | igual |
| JustificacaoDAL | OK | OK | igual |
| EstatutoDAL | OK | OK | igual |

O EstatutoDAL cobre o catalogo de estatutos e a associacao estudante-estatuto
(tabela estudante_estatuto com FK para estudante e estatuto).
