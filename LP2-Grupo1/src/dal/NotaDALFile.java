package dal;

import common.ConfigApp;
import model.Nota;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação em ficheiro (CSV) de {@link NotaDAL}.
 * Ficheiro: nota.csv — numMec;idMomento;siglaUC;valor.
 */
public class NotaDALFile implements NotaDAL {

    private static final String NOME_FICHEIRO = "nota.csv";
    private static final String CABECALHO = "numMec;idMomento;siglaUC;valor";

    private String caminho() {
        return ConfigApp.PASTA_BD + File.separator + NOME_FICHEIRO;
    }

    @Override
    public void inicializar() {
        DALUtil.garantirFicheiroECabecalho(caminho(), CABECALHO);
    }

    @Override
    public void guardar(Nota nota) {
        if (nota == null) return;
        DALUtil.garantirFicheiroECabecalho(caminho(), CABECALHO);
        List<String> linhas = DALUtil.lerFicheiro(caminho());
        List<String> novas = new ArrayList<>();
        boolean atualizou = false;
        for (String linha : linhas) {
            if (isCabecalho(linha)) { novas.add(linha); continue; }
            String[] d = linha.split(";", -1);
            if (d.length >= 4 && igual(d, nota.getNumMec(), nota.getIdMomento())) {
                novas.add(serializar(nota));
                atualizou = true;
            } else {
                novas.add(linha);
            }
        }
        if (!atualizou) novas.add(serializar(nota));
        DALUtil.reescreverFicheiro(caminho(), novas);
    }

    @Override
    public Nota procurar(int numMec, int idMomento) {
        for (String linha : DALUtil.lerFicheiro(caminho())) {
            if (isCabecalho(linha)) continue;
            String[] d = linha.split(";", -1);
            if (d.length >= 4 && igual(d, numMec, idMomento)) return parse(d);
        }
        return null;
    }

    @Override
    public List<Nota> listarPorAlunoEUc(int numMec, String siglaUC) {
        List<Nota> lista = new ArrayList<>();
        for (String linha : DALUtil.lerFicheiro(caminho())) {
            if (isCabecalho(linha)) continue;
            String[] d = linha.split(";", -1);
            if (d.length >= 4) {
                Nota n = parse(d);
                if (n != null && n.getNumMec() == numMec && n.getSiglaUC().equalsIgnoreCase(siglaUC)) lista.add(n);
            }
        }
        return lista;
    }

    @Override
    public boolean remover(int numMec, int idMomento) {
        List<String> linhas = DALUtil.lerFicheiro(caminho());
        List<String> novas = new ArrayList<>();
        boolean removeu = false;
        for (String linha : linhas) {
            if (isCabecalho(linha)) { novas.add(linha); continue; }
            String[] d = linha.split(";", -1);
            if (d.length >= 4 && igual(d, numMec, idMomento)) { removeu = true; continue; }
            novas.add(linha);
        }
        if (removeu) DALUtil.reescreverFicheiro(caminho(), novas);
        return removeu;
    }

    // ------------------------------------------------------------------

    private boolean igual(String[] d, int numMec, int idMomento) {
        try {
            return Integer.parseInt(d[0].trim()) == numMec
                    && Integer.parseInt(d[1].trim()) == idMomento;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isCabecalho(String linha) {
        return linha == null || linha.trim().toLowerCase().startsWith("nummec;");
    }

    private String serializar(Nota n) {
        return n.getNumMec() + ";" + n.getIdMomento() + ";" + n.getSiglaUC() + ";" + n.getValor();
    }

    private Nota parse(String[] d) {
        try {
            return new Nota(Integer.parseInt(d[0].trim()), Integer.parseInt(d[1].trim()),
                    d[2].trim(), Double.parseDouble(d[3].trim()));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
