# Padrão Arquitetural dos Módulos (migração para SQL Server)

Este documento descreve o padrão a seguir na migração progressiva do projeto de
ficheiros CSV para SQL Server. O módulo **Login** é a implementação de referência:
é a primeira entidade migrada por não ter dependências externas.

## Visão geral

```
View  ──►  Controller  ──►  LoginDAL (interface)
                               ▲
                               │  LoginDALFactory.create()
                               │  (lê login.persistence.mode)
                  ┌────────────┴────────────┐
            LoginDALSql                LoginDALFile
                  │                          │
         ConnectionManager            logins.csv (DALUtil)
         + RowMapper<T>
                  │
            SQL Server (config.properties)
```

Regras do padrão:

1. **View** só fala com o **Controller**. Recolhe input e mostra resultados.
2. **Controller** contém a lógica mínima (validação + hashing) e delega a
   persistência na **interface DAL**. Não sabe se está em modo SQL ou ficheiros.
3. A **interface DAL** define o contrato (CRUD + `inicializar`).
4. A **Factory** lê `login.persistence.mode` do `config.properties` e devolve a
   implementação SQL ou de ficheiros.
5. As **duas implementações** garantem **paridade total**: o mesmo comportamento
   observável em ambos os modos.
6. O **ConnectionManager** é genérico e partilhado por todos os módulos SQL.

> **Nota:** o Login não tem BLL — a "lógica" resume-se a validação e hashing,
> centralizados em `common.PasswordHasher` e no `LoginController`. As outras
> entidades (Estudante, Docente, Gestor) mantêm BLL onde já existe.

## Estrutura de pacotes

```
src/
  common/
    ConfigApp.java        Lê config.properties (Properties + FileInputStream)
    PasswordHasher.java   SHA-256 + salt (hash/salt em colunas separadas)
  dal/
    db/
      ConnectionManager.java   Genérico: select / create / update + transações
      RowMapper.java           Interface funcional ResultSet -> T
      DataAccessException.java Wrapper unchecked de SQLException
  modules/
    login/
      LoginModel.java       Espelha as colunas da tabela [logins]
      LoginController.java  Chamado pela View; validação + hashing
      LoginDAL.java         INTERFACE (+ seed do admin partilhado)
      LoginDALSql.java      ConnectionManager + RowMapper<LoginModel>
      LoginDALFile.java     logins.csv via DALUtil
      LoginDALFactory.java  Devolve a implementação conforme o modo
      LoginView.java        View de autenticação (executável isoladamente)
sql/
  schema_login.sql          DDL da tabela [logins] (SQL Server)
config.properties           Configuração (pastas, BD, modo, admin)
```

## ConnectionManager — API genérica

```java
List<T> select(String sql, RowMapper<T> mapper, Object... params);
int     create(String sql, Object... params);   // INSERT, devolve a chave IDENTITY
int     update(String sql, Object... params);    // UPDATE/DELETE, devolve linhas afetadas

void    beginTransaction();
void    commit();
void    rollback();

boolean existeTabela(String nome);
void    executarScript(String ddl);
```

- Todas as queries usam `PreparedStatement` com placeholders `?`.
- Fora de transação, cada operação abre e fecha a sua própria ligação.
- Em transação, reutiliza-se a mesma ligação até `commit()`/`rollback()`.
- A configuração vem de `common.ConfigApp` (substitui o Dotenv do exemplo do professor).

## Inicialização automática (modo SQL)

No arranque, `LoginDAL.inicializar()`:
1. Se a tabela `[logins]` não existe → executa `sql/schema_login.sql`.
2. Se a tabela está vazia → tenta importar `logins.csv` (migração nice-to-have);
   se não houver CSV, popula com o admin definido em `config.properties`.

## Como migrar um novo módulo (ex.: Departamento)

1. Criar o schema em `sql/schema_<modulo>.sql` (sintaxe SQL Server).
2. Criar `modules/<modulo>/<Modulo>DAL.java` (interface com o CRUD).
3. Implementar `<Modulo>DALSql` (ConnectionManager + RowMapper) e
   `<Modulo>DALFile` (CSV via `DALUtil`) com **paridade total**.
4. Criar `<Modulo>DALFactory` a ler o modo do `config.properties`.
5. O Controller/BLL passa a depender da **interface**, não da implementação.
6. Para autenticação/credenciais, **delegar sempre no `LoginController`** —
   nunca aceder a `logins.csv` nem a `[logins]` diretamente.

## Configuração (`config.properties`)

```properties
pasta.bd=bd/

db.server=ctespbd.dei.isep.ipp.pt
db.database=2026_LP2_G1_FEIRA
db.user=2026_LP2_G1_FEIRA
db.password=COLOCAR_PASSWORD_AQUI

login.persistence.mode=sql   # sql | file

admin.email=admin@issmf.pt
admin.password.hash=<salt>:<hash>   # ver common.PasswordHasher
```

> O valor por omissão do `admin.password.hash` incluído corresponde à password
> `admin123`. Alterar para produção. Não versionar a `db.password` real.

## Driver JDBC do SQL Server

O projeto não usa Maven/Gradle — as dependências são JARs em `lib/`.

1. Descarregar o `mssql-jdbc` (Microsoft JDBC Driver for SQL Server):
   https://learn.microsoft.com/sql/connect/jdbc/download-microsoft-jdbc-driver-for-sql-server
   Escolher a variante compatível com a JDK (ex.: `mssql-jdbc-12.8.1.jre11.jar`).
2. Copiar o `.jar` para `lib/`.
3. No IntelliJ: *File → Project Structure → Libraries* → adicionar o JAR à
   biblioteca de projeto `lib` (ou *Add as Library* sobre o ficheiro).
4. O `iniciar.bat` já inclui `lib/*` no classpath, por isso a execução por linha
   de comando funciona automaticamente assim que o JAR estiver em `lib/`.

O código não depende do driver em tempo de compilação (é carregado por nome via
`Class.forName`), pelo que compila mesmo sem o JAR — mas o **modo SQL** só
funciona em runtime com o JAR presente e a `db.password` correta.

## Executar / testar o módulo Login isoladamente

```bash
# compilar
javac -d build -cp "lib/*" $(find src -name "*.java")
# correr a view do Login (usa o modo definido em config.properties)
java -cp "build;lib/*" modules.login.LoginView
```

Em `login.persistence.mode=file` cria-se/usa-se `bd/logins.csv`.
Em `login.persistence.mode=sql` cria-se/usa-se a tabela `[logins]` no SQL Server.
O comportamento das operações (autenticar, criar, atualizar, listar, eliminar) é
idêntico nos dois modos.
