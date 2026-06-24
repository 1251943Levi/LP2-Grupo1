package dal;

import common.ConfigApp;
import dal.db.ConnectionManager;
import dal.db.RowMapper;
import model.Presenca;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public class PresencaDALSql implements PresencaDAL {

    private static final String TABELA = "presenca";
    private final ConnectionManager cm;

    public PresencaDALSql() {
        this.cm = new ConnectionManager();
    }

    @Override
    public void inicializar() {
        if (!cm.existeTabela(TABELA)) {
            cm.executarScript(lerSchema());
        } else {
            // Verificar se a coluna statusDocente existe, senão adicionar
            if (!colunaExiste("statusDocente")) {
                cm.update("ALTER TABLE [presenca] ADD statusDocente NVARCHAR(20) NULL");
            }
        }
    }

    private boolean colunaExiste(String coluna) {
        List<Integer> r = cm.select(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ? AND COLUMN_NAME = ?",
                rs -> rs.getInt(1), TABELA, coluna);
        return !r.isEmpty() && r.get(0) > 0;
    }

    private String lerSchema() {
        return "CREATE TABLE [presenca] (\n"
                + "    id            INT IDENTITY(1,1) PRIMARY KEY,\n"
                + "    idAula        INT NOT NULL REFERENCES [aula](id),\n"
                + "    numMec        INT NOT NULL REFERENCES [estudante](numMec),\n"
                + "    estado        NVARCHAR(20) NOT NULL DEFAULT 'PENDENTE',\n"
                + "    docenteMarcou BIT NOT NULL DEFAULT 0,\n"
                + "    statusDocente NVARCHAR(20) NULL,\n"
                + "    dataHoraRegisto DATETIME2 NOT NULL DEFAULT GETDATE()\n"
                + ");\n";
    }

    private RowMapper<Presenca> mapper = rs -> {
        Presenca p = new Presenca();
        p.setId(rs.getInt("id"));
        p.setIdAula(rs.getInt("idAula"));
        p.setNumMec(rs.getInt("numMec"));
        p.setEstado(rs.getString("estado"));
        p.setDocenteMarcou(rs.getBoolean("docenteMarcou"));
        p.setStatusDocente(rs.getString("statusDocente"));
        Timestamp ts = rs.getTimestamp("dataHoraRegisto");
        p.setDataHoraRegisto(ts != null ? ts.toLocalDateTime() : null);
        return p;
    };

    @Override
    public void adicionar(Presenca presenca) {
        cm.update("INSERT INTO [presenca] (idAula, numMec, estado, docenteMarcou, statusDocente, dataHoraRegisto) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                presenca.getIdAula(), presenca.getNumMec(), presenca.getEstado(),
                presenca.isDocenteMarcou(), presenca.getStatusDocente(), presenca.getDataHoraRegisto());
    }

    @Override
    public void atualizar(Presenca presenca) {
        cm.update("UPDATE [presenca] SET idAula=?, numMec=?, estado=?, docenteMarcou=?, statusDocente=?, dataHoraRegisto=? WHERE id=?",
                presenca.getIdAula(), presenca.getNumMec(), presenca.getEstado(),
                presenca.isDocenteMarcou(), presenca.getStatusDocente(), presenca.getDataHoraRegisto(),
                presenca.getId());
    }

    @Override
    public boolean remover(int id) {
        return cm.update("DELETE FROM [presenca] WHERE id=?", id) > 0;
    }

    @Override
    public Presenca buscarPorId(int id) {
        List<Presenca> r = cm.select("SELECT id, idAula, numMec, estado, docenteMarcou, statusDocente, dataHoraRegisto FROM [presenca] WHERE id=?", mapper, id);
        return r.isEmpty() ? null : r.get(0);
    }

    @Override
    public List<Presenca> listarPorAula(int idAula) {
        return cm.select("SELECT id, idAula, numMec, estado, docenteMarcou, statusDocente, dataHoraRegisto FROM [presenca] WHERE idAula=? ORDER BY numMec", mapper, idAula);
    }

    @Override
    public List<Presenca> listarPorAluno(int numMec) {
        return cm.select("SELECT id, idAula, numMec, estado, docenteMarcou, statusDocente, dataHoraRegisto FROM [presenca] WHERE numMec=? ORDER BY idAula", mapper, numMec);
    }

    @Override
    public List<Presenca> listarPorAlunoEAula(int numMec, int idAula) {
        return cm.select("SELECT id, idAula, numMec, estado, docenteMarcou, statusDocente, dataHoraRegisto FROM [presenca] WHERE numMec=? AND idAula=?", mapper, numMec, idAula);
    }

    @Override
    public boolean existePresencaAluno(int numMec, int idAula) {
        List<Integer> r = cm.select("SELECT COUNT(*) FROM [presenca] WHERE numMec=? AND idAula=?", rs -> rs.getInt(1), numMec, idAula);
        return !r.isEmpty() && r.get(0) > 0;
    }

    @Override
    public boolean docenteJaMarcou(int idAula) {
        List<Integer> r = cm.select("SELECT COUNT(*) FROM [presenca] WHERE idAula=? AND numMec=-1", rs -> rs.getInt(1), idAula);
        return !r.isEmpty() && r.get(0) > 0;
    }

    @Override
    public void marcarDocente(int idAula) {
        // Não usado diretamente na nova lógica; mantido por compatibilidade.
    }

    @Override
    public void removerPorAulaEDocente(int idAula) {
        cm.update("DELETE FROM [presenca] WHERE idAula=? AND numMec=-1", idAula);
    }

    @Override
    public void removerPorAula(int idAula) {
        cm.update("DELETE FROM [presenca] WHERE idAula=?", idAula);
    }
}