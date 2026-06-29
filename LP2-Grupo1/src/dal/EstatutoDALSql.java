package dal;

import dal.db.ConnectionManager;
import dal.db.RowMapper;
import model.EstatutoEstudante;

import java.util.List;

/**
 * Implementação SQL Server do {@link EstatutoDAL}.
 * <ul>
 *   <li>{@code estatuto} &mdash; catálogo (id, nome, descricao)</li>
 *   <li>{@code estudante_estatuto} &mdash; atribuições, com FK para
 *       {@code estudante} e {@code estatuto}</li>
 * </ul>
 * Todas as queries usam {@link java.sql.PreparedStatement} (parâmetros ?) e
 * colunas explícitas (sem SELECT *), via {@link ConnectionManager}.
 */
public class EstatutoDALSql implements EstatutoDAL {

    private static final String TABELA = "estatuto";
    private static final String TABELA_ATRIB = "estudante_estatuto";
    private static final String COLUNAS = "id, nome, descricao";

    private final ConnectionManager cm;

    public EstatutoDALSql() {
        this.cm = new ConnectionManager();
    }

    private final RowMapper<EstatutoEstudante> mapper = rs -> {
        EstatutoEstudante e = new EstatutoEstudante();
        e.setId(rs.getInt("id"));
        e.setNome(rs.getString("nome"));
        e.setDescricao(rs.getString("descricao"));
        return e;
    };

    @Override
    public void inicializar() {
        if (!cm.existeTabela(TABELA)) {
            cm.executarScript(schemaCatalogo());
        }
        if (!cm.existeTabela(TABELA_ATRIB)) {
            cm.executarScript(schemaAtribuicoes());
        }
        if (listarTodos().isEmpty()) {
            adicionar(new EstatutoEstudante(0, "Estudante atleta", "Desporto de alta competição"));
            adicionar(new EstatutoEstudante(0, "Estudante trabalhador", "Trabalhador-estudante"));
            adicionar(new EstatutoEstudante(0, "Estudante pai", "Estudante com filhos"));
            adicionar(new EstatutoEstudante(0, "Matrimónio", "Casamento"));
            adicionar(new EstatutoEstudante(0, "Baixa médica", "Doença/lesão com atestado médico"));
        }
    }

    private String schemaCatalogo() {
        return "CREATE TABLE [estatuto] (\n"
                + "    id        INT IDENTITY(1,1) PRIMARY KEY,\n"
                + "    nome      NVARCHAR(100) NOT NULL,\n"
                + "    descricao NVARCHAR(255)\n"
                + ");\n";
    }

    private String schemaAtribuicoes() {
        return "CREATE TABLE [estudante_estatuto] (\n"
                + "    numMec     INT NOT NULL REFERENCES [estudante](numMec),\n"
                + "    idEstatuto INT NOT NULL REFERENCES [estatuto](id),\n"
                + "    CONSTRAINT PK_estudante_estatuto PRIMARY KEY (numMec, idEstatuto)\n"
                + ");\n";
    }

    // ---------------------------- Catálogo ----------------------------

    @Override
    public List<EstatutoEstudante> listarTodos() {
        return cm.select("SELECT " + COLUNAS + " FROM " + TABELA + " ORDER BY nome", mapper);
    }

    @Override
    public EstatutoEstudante buscarPorId(int id) {
        List<EstatutoEstudante> r =
                cm.select("SELECT " + COLUNAS + " FROM " + TABELA + " WHERE id = ?", mapper, id);
        return r.isEmpty() ? null : r.get(0);
    }

    @Override
    public boolean adicionar(EstatutoEstudante estatuto) {
        if (estatuto == null) return false;
        int id = cm.create(
                "INSERT INTO " + TABELA + " (nome, descricao) VALUES (?, ?)",
                estatuto.getNome(), estatuto.getDescricao()
        );
        estatuto.setId(id);
        return id > 0;
    }

    @Override
    public boolean atualizar(EstatutoEstudante estatuto) {
        if (estatuto == null) return false;
        return cm.update("UPDATE " + TABELA + " SET nome = ?, descricao = ? WHERE id = ?",
                estatuto.getNome(), estatuto.getDescricao(), estatuto.getId()) > 0;
    }

    @Override
    public boolean remover(int id) {
        // Remove primeiro as atribuições (respeita a FK).
        cm.update("DELETE FROM " + TABELA_ATRIB + " WHERE idEstatuto = ?", id);
        return cm.update("DELETE FROM " + TABELA + " WHERE id = ?", id) > 0;
    }

    // -------------------------- Atribuições ---------------------------

    @Override
    public boolean atribuir(int numMec, int idEstatuto) {
        List<Integer> existe = cm.select(
                "SELECT numMec FROM " + TABELA_ATRIB + " WHERE numMec = ? AND idEstatuto = ?",
                rs -> rs.getInt("numMec"), numMec, idEstatuto);
        if (!existe.isEmpty()) return false;
        return cm.update("INSERT INTO " + TABELA_ATRIB + " (numMec, idEstatuto) VALUES (?, ?)",
                numMec, idEstatuto) > 0;
    }

    @Override
    public boolean removerAtribuicao(int numMec, int idEstatuto) {
        return cm.update("DELETE FROM " + TABELA_ATRIB + " WHERE numMec = ? AND idEstatuto = ?",
                numMec, idEstatuto) > 0;
    }

    @Override
    public List<EstatutoEstudante> listarPorEstudante(int numMec) {
        return cm.select(
                "SELECT e.id, e.nome, e.descricao FROM " + TABELA + " e "
                        + "JOIN " + TABELA_ATRIB + " ee ON ee.idEstatuto = e.id "
                        + "WHERE ee.numMec = ? ORDER BY e.nome",
                mapper, numMec);
    }
}
