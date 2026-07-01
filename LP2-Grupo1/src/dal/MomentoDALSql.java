package dal;

import dal.db.ConnectionManager;
import model.Momento;
import model.TipoMomento;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Implementação SQL Server de {@link MomentoDAL} (Card 3).
 * Tabelas: [tipo_momento] (lookup) e [momento] (id IDENTITY, FK tipo -> tipo_momento).
 */
public class MomentoDALSql implements MomentoDAL {

    private static final String TABELA = "momento";
    private final ConnectionManager cm;

    public MomentoDALSql() { this(new ConnectionManager()); }
    public MomentoDALSql(ConnectionManager cm) { this.cm = cm; }

    @Override
    public void inicializar() {
        garantirTabelaTipos();
        if (!cm.existeTabela(TABELA)) {
            cm.executarScript(
                    "CREATE TABLE [momento] (\n"
                    + "    id             INT IDENTITY(1,1) PRIMARY KEY,\n"
                    + "    siglaUC        NVARCHAR(10)  NOT NULL,\n"
                    + "    nome           NVARCHAR(100) NOT NULL,\n"
                    + "    tipo           NVARCHAR(20)  NOT NULL REFERENCES [tipo_momento](nome),\n"
                    + "    peso           DECIMAL(5,2)  NOT NULL DEFAULT 0,\n"
                    + "    dataRealizacao NVARCHAR(10)  NULL\n"
                    + ");\n");
        }
    }

    /** Cria e semeia a lookup [tipo_momento] com os tipos base. Idempotente. */
    private void garantirTabelaTipos() {
        if (!cm.existeTabela("tipo_momento")) {
            cm.executarScript("CREATE TABLE [tipo_momento] ("
                    + "nome NVARCHAR(20) NOT NULL PRIMARY KEY, descricao NVARCHAR(100) NULL);");
        }
        for (TipoMomento t : TipoMomento.values()) {
            List<Integer> r = cm.select(
                    "SELECT COUNT(*) AS c FROM [tipo_momento] WHERE nome = ?", rs -> rs.getInt("c"), t.etiqueta());
            if (r.isEmpty() || r.get(0) == 0) {
                cm.update("INSERT INTO [tipo_momento] (nome, descricao) VALUES (?, ?)", t.etiqueta(), t.descricao());
            }
        }
    }

    @Override
    public int adicionar(Momento m) {
        if (m == null) return -1;
        int id = cm.create(
                "INSERT INTO [momento] (siglaUC, nome, tipo, peso, dataRealizacao) VALUES (?, ?, ?, ?, ?)",
                m.getSiglaUC(), m.getNome(),
                (m.getTipo() != null ? m.getTipo().etiqueta() : null),
                m.getPeso(), m.getDataRealizacao());
        m.setId(id);
        return id;
    }

    @Override
    public List<Momento> listarPorUc(String siglaUC) {
        return cm.select("SELECT * FROM [momento] WHERE siglaUC = ? ORDER BY id", this::map, siglaUC);
    }

    @Override
    public Momento procurarPorId(int id) {
        List<Momento> r = cm.select("SELECT * FROM [momento] WHERE id = ?", this::map, id);
        return r.isEmpty() ? null : r.get(0);
    }

    @Override
    public double somaPesos(String siglaUC) {
        List<Double> r = cm.select(
                "SELECT ISNULL(SUM(peso), 0) AS s FROM [momento] WHERE siglaUC = ?",
                rs -> rs.getDouble("s"), siglaUC);
        return r.isEmpty() ? 0 : r.get(0);
    }

    @Override
    public boolean remover(int id) {
        return cm.update("DELETE FROM [momento] WHERE id = ?", id) > 0;
    }

    private Momento map(ResultSet rs) throws SQLException {
        return new Momento(
                rs.getInt("id"),
                rs.getString("siglaUC"),
                rs.getString("nome"),
                TipoMomento.fromString(rs.getString("tipo")),
                rs.getDouble("peso"),
                rs.getString("dataRealizacao"));
    }
}
