package dal;

import common.ConfigApp;
import model.TipoJustificacao;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TipoJustificacaoDALFile implements TipoJustificacaoDAL {
    private static final String NOME_FICHEIRO = "tipos_justificacao.csv";
    private static final String CABECALHO = "id;nome;descricao";
    private final String pastaBase;

    public TipoJustificacaoDALFile() {
        this.pastaBase = ConfigApp.PASTA_BD;
    }

    private String caminho() {
        return pastaBase + File.separator + NOME_FICHEIRO;
    }

    @Override
    public void inicializar() {
        DALUtil.garantirFicheiroECabecalho(caminho(), CABECALHO);
        if (listarTodos().isEmpty()) {
            adicionar(new TipoJustificacao(1, "Baixa médica", "Doença/lesão com atestado médico"));
            adicionar(new TipoJustificacao(2, "Matrimónio", "Casamento"));
            adicionar(new TipoJustificacao(3, "Estudante atleta", "Desporto de alta competição"));
            adicionar(new TipoJustificacao(4, "Estudante trabalhador", "Trabalhador-estudante com horário incompatível"));
            adicionar(new TipoJustificacao(5, "Estudante pai", "Estudante com filhos"));
        }
    }

    private int proximoId() {
        return listarTodos().stream().mapToInt(TipoJustificacao::getId).max().orElse(0) + 1;
    }

    @Override
    public List<TipoJustificacao> listarTodos() {
        List<TipoJustificacao> lista = new ArrayList<>();
        for (String linha : DALUtil.lerFicheiro(caminho())) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] d = linha.split(";", -1);
            if (d.length >= 3) {
                try {
                    TipoJustificacao t = new TipoJustificacao(
                            Integer.parseInt(d[0].trim()),
                            d[1].trim(),
                            d[2].trim()
                    );
                    lista.add(t);
                } catch (NumberFormatException ignored) {}
            }
        }
        return lista;
    }

    @Override
    public TipoJustificacao buscarPorId(int id) {
        for (TipoJustificacao t : listarTodos()) {
            if (t.getId() == id) return t;
        }
        return null;
    }

    @Override
    public boolean adicionar(TipoJustificacao tipo) {
        if (tipo == null) return false;
        DALUtil.garantirFicheiroECabecalho(caminho(), CABECALHO);
        int id = proximoId();
        tipo.setId(id);
        DALUtil.adicionarLinhaCSV(caminho(), id + ";" + tipo.getNome() + ";" + tipo.getDescricao());
        return true;
    }

    @Override
    public boolean atualizar(TipoJustificacao tipo) {
        List<String> linhas = DALUtil.lerFicheiro(caminho());
        List<String> novas = new ArrayList<>();
        boolean encontrou = false;
        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) { novas.add(linha); continue; }
            String[] d = linha.split(";", -1);
            if (d.length >= 1 && Integer.parseInt(d[0].trim()) == tipo.getId()) {
                novas.add(tipo.getId() + ";" + tipo.getNome() + ";" + tipo.getDescricao());
                encontrou = true;
            } else {
                novas.add(linha);
            }
        }
        if (encontrou) DALUtil.reescreverFicheiro(caminho(), novas);
        return encontrou;
    }

    @Override
    public boolean remover(int id) {
        List<String> linhas = DALUtil.lerFicheiro(caminho());
        List<String> novas = new ArrayList<>();
        boolean removido = false;
        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) { novas.add(linha); continue; }
            String[] d = linha.split(";", -1);
            if (d.length >= 1 && Integer.parseInt(d[0].trim()) == id) {
                removido = true;
            } else {
                novas.add(linha);
            }
        }
        if (removido) DALUtil.reescreverFicheiro(caminho(), novas);
        return removido;
    }
}