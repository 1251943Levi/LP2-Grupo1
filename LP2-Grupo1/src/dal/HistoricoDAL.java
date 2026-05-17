package dal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class HistoricoDAL {

    private static final String NOME_FICHEIRO = "historico_academico.csv";
    private static final String CABECALHO = "anoLetivo;numMec;siglaUC;notas;estado";

    public static void guardarRegistoHistorico(int anoLetivo, int numMec, String siglaUC, String notas, String estado, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        DALUtil.garantirFicheiroECabecalho(caminho, CABECALHO);
        String linha = anoLetivo + ";" + numMec + ";" + siglaUC + ";" + notas + ";" + estado;
        DALUtil.adicionarLinhaCSV(caminho, linha);
    }

    public static List<String> consultarHistoricoPorAno(int anoLetivo, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        List<String> resultados = new ArrayList<>();

        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 1) {
                try {
                    if (Integer.parseInt(dados[0].trim()) == anoLetivo) {
                        resultados.add(linha);
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return resultados;
    }

    public static List<String> consultarHistoricoPorAluno(int numMec, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        List<String> resultados = new ArrayList<>();

        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 2) {
                try {
                    if (Integer.parseInt(dados[1].trim()) == numMec) {
                        resultados.add(linha);
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return resultados;
    }
}