package dal;

import dal.db.ConnectionManager;
import dal.db.RowMapper;
import model.TipoJustificacao;
import java.util.List;

public class TipoJustificacaoDALSql implements TipoJustificacaoDAL {
    private static final String TABELA = "tipo_justificacao";
    private final ConnectionManager cm;

    public TipoJustificacaoDALSql() {
        this.cm = new ConnectionManager();
    }

    private RowMapper<TipoJustificacao> mapper = rs -> {
        TipoJustificacao t = new TipoJustificacao();
        t.setId(rs.getInt("id"));
        t.setNome(rs.getString("nome"));
        t.setDescricao(rs.getString("descricao"));
        return t;
    };

    @Override
    public void inicializar() {
        if (!cm.existeTabela(TABELA)) {
            cm.executarScript(lerSchema());
        }
        if (listarTodos().isEmpty()) {
            adicionar(new TipoJustificacao(0, "Baixa médica", "Doença/lesão com atestado médico"));
            adicionar(new TipoJustificacao(0, "Matrimónio", "Casamento"));
            adicionar(new TipoJustificacao(0, "Estudante atleta", "Desporto de alta competição"));
            adicionar(new TipoJustificacao(0, "Estudante trabalhador", "Trabalhador-estudante com horário incompatível"));
            adicionar(new TipoJustificacao(0, "Estudante pai", "Estudante com filhos"));
        }
    }

    private String lerSchema() {
        return "CREATE TABLE tipo_justificacao (\n"
                + "    id INT IDENTITY(1,1) PRIMARY KEY,\n"
                + "    nome NVARCHAR(100) NOT NULL,\n"
                + "    descricao NVARCHAR(255)\n"
                + ");\n";
    }

    @Override
    public List<TipoJustificacao> listarTodos() {
        return cm.select("SELECT * FROM " + TABELA + " ORDER BY nome", mapper);
    }

    @Override
    public TipoJustificacao buscarPorId(int id) {
        List<TipoJustificacao> r = cm.select("SELECT * FROM " + TABELA + " WHERE id = ?", mapper, id);
        return r.isEmpty() ? null : r.get(0);
    }

    @Override
    public boolean adicionar(TipoJustificacao tipo) {
        if (tipo == null) return false;
        int id = cm.create(
                "INSERT INTO " + TABELA + " (nome, descricao) VALUES (?, ?)",
                tipo.getNome(), tipo.getDescricao()
        );
        tipo.setId(id);
        return id > 0;
    }

    @Override
    public boolean atualizar(TipoJustificacao tipo) {
        if (tipo == null) return false;
        return cm.update("UPDATE " + TABELA + " SET nome = ?, descricao = ? WHERE id = ?",
                tipo.getNome(), tipo.getDescricao(), tipo.getId()) > 0;
    }

    @Override
    public boolean remover(int id) {
        return cm.update("DELETE FROM " + TABELA + " WHERE id = ?", id) > 0;
    }
}