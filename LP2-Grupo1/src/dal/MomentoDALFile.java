package dal;

import common.ConfigApp;
import model.Momento;
import model.TipoMomento;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação em ficheiro (CSV) de {@link MomentoDAL}.
 * Ficheiro: momento.csv — id;siglaUC;nome;tipo;peso;dataRealizacao.
 */
public class MomentoDALFile implements MomentoDAL {

    private static final String NOME_FICHEIRO = "momento.csv";
    private static final String CABECALHO = "id;siglaUC;nome;tipo;peso;dataRealizacao";

    private String caminho() {
        return ConfigApp.PASTA_BD + File.separator + NOME_FICHEIRO;
    }

    @Override
    public void inicializar() {
        DALUtil.garantirFicheiroECabecalho(caminho(), CABECALHO);
    }

    @Override
    public int adicionar(Momento m) {
        if (m == null) return -1;
        DALUtil.garantirFicheiroECabecalho(caminho(), CABECALHO);
        int id = proximoId();
        m.setId(id);
        String tipo = (m.getTipo() != null) ? m.getTipo().etiqueta() : "";
        DALUtil.adicionarLinhaCSV(caminho(),
                id + ";" + m.getSiglaUC() + ";" + m.getNome() + ";" + tipo + ";"
                        + m.getPeso() + ";" + m.getDataRealizacao());
        return id;
    }

    @Override
    public List<Momento> listarPorUc(String siglaUC) {
        List<Momento> lista = new ArrayList<>();
        for (String linha : DALUtil.lerFicheiro(caminho())) {
            if (isCabecalho(linha)) continue;
            Momento m = parse(linha);
            if (m != null && m.getSiglaUC().equalsIgnoreCase(siglaUC)) lista.add(m);
        }
        return lista;
    }

    @Override
    public Momento procurarPorId(int id) {
        for (String linha : DALUtil.lerFicheiro(caminho())) {
            if (isCabecalho(linha)) continue;
            Momento m = parse(linha);
            if (m != null && m.getId() == id) return m;
        }
        return null;
    }

    @Override
    public double somaPesos(String siglaUC) {
        double soma = 0;
        for (Momento m : listarPorUc(siglaUC)) soma += m.getPeso();
        return soma;
    }

    @Override
    public boolean remover(int id) {
        List<String> linhas = DALUtil.lerFicheiro(caminho());
        List<String> novas = new ArrayList<>();
        boolean removeu = false;
        for (String linha : linhas) {
            if (isCabecalho(linha)) { novas.add(linha); continue; }
            Momento m = parse(linha);
            if (m != null && m.getId() == id) { removeu = true; continue; }
            novas.add(linha);
        }
        if (removeu) DALUtil.reescreverFicheiro(caminho(), novas);
        return removeu;
    }

    // ------------------------------------------------------------------

    private int proximoId() {
        int max = 0;
        for (String linha : DALUtil.lerFicheiro(caminho())) {
            if (isCabecalho(linha)) continue;
            String[] d = linha.split(";", -1);
            try { max = Math.max(max, Integer.parseInt(d[0].trim())); }
            catch (NumberFormatException ignored) {}
        }
        return max + 1;
    }

    private boolean isCabecalho(String linha) {
        return linha == null || linha.trim().toLowerCase().startsWith("id;");
    }

    private Momento parse(String linha) {
        String[] d = linha.split(";", -1);
        if (d.length < 6) return null;
        try {
            int id = Integer.parseInt(d[0].trim());
            double peso = d[4].trim().isEmpty() ? 0 : Double.parseDouble(d[4].trim());
            return new Momento(id, d[1].trim(), d[2].trim(),
                    TipoMomento.fromString(d[3].trim()), peso, d[5].trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
