package dal;

import dal.db.ConnectionManager;
import model.Nota;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Implementação SQL Server de {@link NotaDAL} (Card 5).
 * Tabela [nota] — PK (numMec, idMomento); FK numMec -> estudante, idMomento -> momento.
 */
public class NotaDALSql implements NotaDAL {

    private static final String TABELA = "nota";
    private final ConnectionManager cm;

    public NotaDALSql() { this(new ConnectionManager()); }
    public NotaDALSql(ConnectionManager cm) { this.cm = cm; }

    @Override
    public void inicializar() {
        if (!cm.existeTabela(TABELA)) {
            cm.executarScript(
                    "CREATE TABLE [nota] (\n"
                    + "    numMec    INT          NOT NULL REFERENCES [estudante](numMec),\n"
                    + "    idMomento INT          NOT NULL REFERENCES [momento](id),\n"
                    + "    siglaUC   NVARCHAR(10) NOT NULL,\n"
                    + "    valor     DECIMAL(4,2) NOT NULL,\n"
                    + "    PRIMARY KEY (numMec, idMomento)\n"
                    + ");\n");
        }
    }

    @Override
    public void guardar(Nota nota) {
        if (nota == null) return;
        if (procurar(nota.getNumMec(), nota.getIdMomento()) != null) {
            cm.update("UPDATE [nota] SET valor = ?, siglaUC = ? WHERE numMec = ? AND idMomento = ?",
                    nota.getValor(), nota.getSiglaUC(), nota.getNumMec(), nota.getIdMomento());
        } else {
            cm.update("INSERT INTO [nota] (numMec, idMomento, siglaUC, valor) VALUES (?, ?, ?, ?)",
                    nota.getNumMec(), nota.getIdMomento(), nota.getSiglaUC(), nota.getValor());
        }
    }

    @Override
    public Nota procurar(int numMec, int idMomento) {
        List<Nota> r = cm.select("SELECT * FROM [nota] WHERE numMec = ? AND idMomento = ?",
                this::map, numMec, idMomento);
        return r.isEmpty() ? null : r.get(0);
    }

    @Override
    public List<Nota> listarPorAlunoEUc(int numMec, String siglaUC) {
        return cm.select("SELECT * FROM [nota] WHERE numMec = ? AND siglaUC = ?", this::map, numMec, siglaUC);
    }

    @Override
    public boolean remover(int numMec, int idMomento) {
        return cm.update("DELETE FROM [nota] WHERE numMec = ? AND idMomento = ?", numMec, idMomento) > 0;
    }

    private Nota map(ResultSet rs) throws SQLException {
        return new Nota(rs.getInt("numMec"), rs.getInt("idMomento"),
                rs.getString("siglaUC"), rs.getDouble("valor"));
    }
}
