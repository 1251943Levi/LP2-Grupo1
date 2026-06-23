// dal/AulaDALSql.java
package dal;

import common.ConfigApp;
import dal.db.ConnectionManager;
import dal.db.RowMapper;
import model.Aula;
import java.time.DayOfWeek;
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
        // Não há importação de CSV porque é uma entidade nova sem dados iniciais.
    }

    private String lerSchema() {
        return "CREATE TABLE [aula] (\n"
                + "    id            INT IDENTITY(1,1) PRIMARY KEY,\n"
                + "    anoLetivo     INT NOT NULL REFERENCES [anoLetivo](ano),\n"
                + "    siglaUC       NVARCHAR(10) NOT NULL,\n"
                + "    siglaCurso    NVARCHAR(10) NOT NULL,\n"
                + "    siglaDocente  NVARCHAR(10) NOT NULL REFERENCES [docente](sigla),\n"
                + "    diaSemana     NVARCHAR(10) NOT NULL,\n"
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
        a.setDiaSemana(DayOfWeek.valueOf(rs.getString("diaSemana").toUpperCase()));
        a.setHoraInicio(rs.getTime("horaInicio").toLocalTime());
        a.setHoraFim(rs.getTime("horaFim").toLocalTime());
        a.setBloco(rs.getInt("bloco"));
        return a;
    };

    @Override
    public void adicionar(Aula aula) {
        cm.update("INSERT INTO [aula] (anoLetivo, siglaUC, siglaCurso, siglaDocente, diaSemana, horaInicio, horaFim, bloco) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                aula.getAnoLetivo(), aula.getSiglaUC(), aula.getSiglaCurso(),
                aula.getSiglaDocente(), aula.getDiaSemana().name(),
                aula.getHoraInicio(), aula.getHoraFim(), aula.getBloco());
        // Obter o id gerado (opcional)
    }

    @Override
    public void atualizar(Aula aula) {
        cm.update("UPDATE [aula] SET anoLetivo=?, siglaUC=?, siglaCurso=?, siglaDocente=?, diaSemana=?, horaInicio=?, horaFim=?, bloco=? "
                        + "WHERE id=?",
                aula.getAnoLetivo(), aula.getSiglaUC(), aula.getSiglaCurso(),
                aula.getSiglaDocente(), aula.getDiaSemana().name(),
                aula.getHoraInicio(), aula.getHoraFim(), aula.getBloco(),
                aula.getId());
    }

    @Override
    public boolean remover(int id) {
        return cm.update("DELETE FROM [aula] WHERE id=?", id) > 0;
    }

    @Override
    public Aula buscarPorId(int id) {
        List<Aula> r = cm.select("SELECT * FROM [aula] WHERE id=?", mapper, id);
        return r.isEmpty() ? null : r.get(0);
    }

    @Override
    public List<Aula> listarPorAnoLetivo(int anoLetivo) {
        return cm.select("SELECT * FROM [aula] WHERE anoLetivo=? ORDER BY diaSemana, horaInicio", mapper, anoLetivo);
    }

    @Override
    public List<Aula> listarPorUC(String siglaUC, int anoLetivo) {
        return cm.select("SELECT * FROM [aula] WHERE siglaUC=? AND anoLetivo=? ORDER BY diaSemana, horaInicio",
                mapper, siglaUC, anoLetivo);
    }

    @Override
    public List<Aula> listarPorDocente(String siglaDocente, int anoLetivo) {
        return cm.select("SELECT * FROM [aula] WHERE siglaDocente=? AND anoLetivo=? ORDER BY diaSemana, horaInicio",
                mapper, siglaDocente, anoLetivo);
    }

    @Override
    public List<Aula> listarPorDocenteEDia(String siglaDocente, DayOfWeek dia, int anoLetivo) {
        return cm.select("SELECT * FROM [aula] WHERE siglaDocente=? AND diaSemana=? AND anoLetivo=? ORDER BY horaInicio",
                mapper, siglaDocente, dia.name(), anoLetivo);
    }

    @Override
    public List<Aula> listarTodas() {
        return cm.select("SELECT * FROM [aula] ORDER BY anoLetivo, diaSemana, horaInicio", mapper);
    }
}