package dal;

import dal.db.ConnectionManager;
import dal.db.RowMapper;
import model.Justificacao;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public class JustificacaoDALSql implements JustificacaoDAL {
    private static final String TABELA = "justificacao";
    // Colunas explicitas - evita SELECT * (revisao de queries, Cartao 1).
    private static final String COLUNAS =
            "id, numMec, idAula, idTipoJustificacao, estado, dataCriacao, dataResposta, observacao";
    private final ConnectionManager cm;

    public JustificacaoDALSql() {
        this.cm = new ConnectionManager();
    }

    private RowMapper<Justificacao> mapper = rs -> {
        Justificacao j = new Justificacao();
        j.setId(rs.getInt("id"));
        j.setNumMec(rs.getInt("numMec"));
        j.setIdAula(rs.getInt("idAula"));
        j.setIdTipoJustificacao(rs.getInt("idTipoJustificacao"));
        j.setEstado(rs.getString("estado"));
        Timestamp ts = rs.getTimestamp("dataCriacao");
        if (ts != null) j.setDataCriacao(ts.toLocalDateTime());
        ts = rs.getTimestamp("dataResposta");
        if (ts != null) j.setDataResposta(ts.toLocalDateTime());
        j.setObservacao(rs.getString("observacao"));
        return j;
    };

    @Override
    public void inicializar() {
        if (!cm.existeTabela(TABELA)) {
            cm.executarScript(lerSchema());
        }
    }

    private String lerSchema() {
        return "CREATE TABLE justificacao (\n"
                + "    id INT IDENTITY(1,1) PRIMARY KEY,\n"
                + "    numMec INT NOT NULL REFERENCES estudante(numMec),\n"
                + "    idAula INT NOT NULL REFERENCES aula(id),\n"
                + "    idTipoJustificacao INT NOT NULL REFERENCES tipo_justificacao(id),\n"
                + "    estado NVARCHAR(20) NOT NULL DEFAULT 'PENDENTE',\n"
                + "    dataCriacao DATETIME2 NOT NULL DEFAULT GETDATE(),\n"
                + "    dataResposta DATETIME2 NULL,\n"
                + "    observacao NVARCHAR(500) NULL\n"
                + ");\n";
    }

    @Override
    public void adicionar(Justificacao justificacao) {
        if (justificacao == null) return;
        int id = cm.create(
                "INSERT INTO " + TABELA + " (numMec, idAula, idTipoJustificacao, estado, dataCriacao) "
                        + "VALUES (?, ?, ?, ?, ?)",
                justificacao.getNumMec(),
                justificacao.getIdAula(),
                justificacao.getIdTipoJustificacao(),
                justificacao.getEstado(),
                justificacao.getDataCriacao()
        );
        justificacao.setId(id);
    }

    @Override
    public void atualizar(Justificacao justificacao) {
        if (justificacao == null) return;
        cm.update("UPDATE " + TABELA + " SET estado = ?, dataResposta = ?, observacao = ? WHERE id = ?",
                justificacao.getEstado(),
                justificacao.getDataResposta(),
                justificacao.getObservacao(),
                justificacao.getId()
        );
    }

    @Override
    public Justificacao buscarPorId(int id) {
        List<Justificacao> r = cm.select("SELECT " + COLUNAS + " FROM " + TABELA + " WHERE id = ?", mapper, id);
        return r.isEmpty() ? null : r.get(0);
    }

    @Override
    public List<Justificacao> listarPorAluno(int numMec) {
        return cm.select("SELECT " + COLUNAS + " FROM " + TABELA + " WHERE numMec = ? ORDER BY dataCriacao DESC", mapper, numMec);
    }

    @Override
    public List<Justificacao> listarPorAula(int idAula) {
        return cm.select("SELECT " + COLUNAS + " FROM " + TABELA + " WHERE idAula = ?", mapper, idAula);
    }

    @Override
    public List<Justificacao> listarPendentes() {
        return cm.select("SELECT " + COLUNAS + " FROM " + TABELA + " WHERE estado = 'PENDENTE' ORDER BY dataCriacao", mapper);
    }

    @Override
    public List<Justificacao> listarTodas() {
        return cm.select("SELECT " + COLUNAS + " FROM " + TABELA + " ORDER BY dataCriacao DESC", mapper);
    }

    @Override
    public void removerPorAula(int idAula) {
        cm.update("DELETE FROM " + TABELA + " WHERE idAula = ?", idAula);
    }
}