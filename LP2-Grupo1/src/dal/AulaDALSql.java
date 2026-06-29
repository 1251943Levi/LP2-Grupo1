package dal;

import common.ConfigApp;
import dal.db.ConnectionManager;
import dal.db.RowMapper;
import model.Aula;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class AulaDALSql implements AulaDAL {

    private static final String TABELA = "aula";
    private final ConnectionManager cm;

    public AulaDALSql() {
        this.cm = new ConnectionManager();
    }

    @Override
    public void inicializar() {
        if (!cm.existeTabela(TABELA)) {
            cm.executarScript(lerSchema());
        }
    }

    private String lerSchema() {
        return "CREATE TABLE [aula] (\n"
                + "    id            INT IDENTITY(1,1) PRIMARY KEY,\n"
                + "    anoLetivo     INT NOT NULL REFERENCES [anoLetivo](ano),\n"
                + "    siglaUC       NVARCHAR(10) NOT NULL,\n"
                + "    siglaCurso    NVARCHAR(10) NOT NULL,\n"
                + "    siglaDocente  NVARCHAR(10) NOT NULL REFERENCES [docente](sigla),\n"
                + "    data          DATE NOT NULL,\n"
                + "    horaInicio    TIME NOT NULL,\n"
                + "    horaFim       TIME NOT NULL,\n"
                + "    bloco         INT NOT NULL,\n"
                + "    CONSTRAINT FK_aula_uc FOREIGN KEY (siglaUC, siglaCurso) REFERENCES [uc](sigla, siglaCurso)\n"
                + ");\n";
    }

    private RowMapper<Aula> mapper = rs -> {
        Aula a = new Aula();
        a.setId(rs.getInt("id"));
        a.setAnoLetivo(rs.getInt("anoLetivo"));
        a.setSiglaUC(rs.getString("siglaUC"));
        a.setSiglaCurso(rs.getString("siglaCurso"));
        a.setSiglaDocente(rs.getString("siglaDocente"));
        a.setData(rs.getDate("data").toLocalDate());
        a.setHoraInicio(rs.getTime("horaInicio").toLocalTime());
        a.setHoraFim(rs.getTime("horaFim").toLocalTime());
        a.setBloco(rs.getInt("bloco"));
        return a;
    };

    @Override
    public void adicionar(Aula aula) {
        cm.update(
                "INSERT INTO [aula] (anoLetivo, siglaUC, siglaCurso, siglaDocente, data, horaInicio, horaFim, bloco) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                aula.getAnoLetivo(),
                aula.getSiglaUC(),
                aula.getSiglaCurso(),
                aula.getSiglaDocente(),
                Date.valueOf(aula.getData()),
                aula.getHoraInicio(),
                aula.getHoraFim(),
                aula.getBloco()
        );
    }

    @Override
    public void atualizar(Aula aula) {
        cm.update(
                "UPDATE [aula] SET anoLetivo=?, siglaUC=?, siglaCurso=?, siglaDocente=?, data=?, horaInicio=?, horaFim=?, bloco=? "
                        + "WHERE id=?",
                aula.getAnoLetivo(),
                aula.getSiglaUC(),
                aula.getSiglaCurso(),
                aula.getSiglaDocente(),
                Date.valueOf(aula.getData()),
                aula.getHoraInicio(),
                aula.getHoraFim(),
                aula.getBloco(),
                aula.getId()
        );
    }

    @Override
    public boolean remover(int id) {
        return cm.update("DELETE FROM [aula] WHERE id=?", id) > 0;
    }

    @Override
    public Aula buscarPorId(int id) {
        List<Aula> r = cm.select("SELECT id, anoLetivo, siglaUC, siglaCurso, siglaDocente, data, horaInicio, horaFim, bloco FROM [aula] WHERE id=?", mapper, id);
        return r.isEmpty() ? null : r.get(0);
    }

    @Override
    public List<Aula> listarPorAnoLetivo(int anoLetivo) {
        return cm.select("SELECT id, anoLetivo, siglaUC, siglaCurso, siglaDocente, data, horaInicio, horaFim, bloco FROM [aula] WHERE anoLetivo=? ORDER BY data, horaInicio", mapper, anoLetivo);
    }

    @Override
    public List<Aula> listarPorUC(String siglaUC, int anoLetivo) {
        return cm.select("SELECT id, anoLetivo, siglaUC, siglaCurso, siglaDocente, data, horaInicio, horaFim, bloco FROM [aula] WHERE siglaUC=? AND anoLetivo=? ORDER BY data, horaInicio",
                mapper, siglaUC, anoLetivo);
    }

    @Override
    public List<Aula> listarPorDocente(String siglaDocente, int anoLetivo) {
        return cm.select("SELECT id, anoLetivo, siglaUC, siglaCurso, siglaDocente, data, horaInicio, horaFim, bloco FROM [aula] WHERE siglaDocente=? AND anoLetivo=? ORDER BY data, horaInicio",
                mapper, siglaDocente, anoLetivo);
    }

    @Override
    public List<Aula> listarPorDataEDocente(LocalDate data, String siglaDocente) {
        return cm.select("SELECT id, anoLetivo, siglaUC, siglaCurso, siglaDocente, data, horaInicio, horaFim, bloco FROM [aula] WHERE data=? AND siglaDocente=? ORDER BY horaInicio",
                mapper, Date.valueOf(data), siglaDocente);
    }

    @Override
    public List<Aula> listarTodas() {
        return cm.select("SELECT id, anoLetivo, siglaUC, siglaCurso, siglaDocente, data, horaInicio, horaFim, bloco FROM [aula] ORDER BY anoLetivo, data, horaInicio", mapper);
    }
}