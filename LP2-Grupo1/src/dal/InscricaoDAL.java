package dal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Responsável pelo acesso aos dados do ficheiro inscricoes.csv
 */
public class InscricaoDAL {
    private static final String NOME_FICHEIRO = "inscricoes.csv";
    private static final String CABECALHO = "numMec;siglaUC";

    /**
     * Lê o ficheiro e devolve uma lista apenas com as siglas das UCs do aluno.
     */
    public static List<String> obterSiglasUcsPorAluno(int numMec, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        List<String> siglas = new ArrayList<>();

        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;

            String[] dados = linha.split(";", -1);
            if (dados.length >= 2) {
                try {
                    if (Integer.parseInt(dados[0].trim()) == numMec) {
                        siglas.add(dados[1].trim());
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return siglas;
    }
}