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
     * Regista a inscrição de um estudante numa Unidade Curricular.
     * Chamado ao criar um novo estudante ou ao avançar de ano letivo.
     */
    public static void adicionarInscricao(int numMec, String siglaUC, String pastaBase) {
        if (siglaUC == null || siglaUC.trim().isEmpty()) return;
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        DALUtil.garantirFicheiroECabecalho(caminho, CABECALHO);
        DALUtil.adicionarLinhaCSV(caminho, numMec + ";" + siglaUC.trim());
    }

    /**
     * Remove a inscrição de um estudante numa UC específica.
     * Usado ao avançar de ano para limpar UCs aprovadas (não precisam de ser repetidas).
     */
    public static void removerInscricao(int numMec, String siglaUC, String pastaBase) {
        if (siglaUC == null || siglaUC.trim().isEmpty()) return;
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        List<String> novas  = new ArrayList<>();

        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) { novas.add(linha); continue; }
            String[] dados = linha.split(";", -1);
            if (dados.length >= 2) {
                try {
                    if (Integer.parseInt(dados[0].trim()) == numMec
                            && dados[1].trim().equalsIgnoreCase(siglaUC.trim())) {
                        continue; // ignora esta linha → apaga a inscrição
                    }
                } catch (NumberFormatException ignored) {}
            }
            novas.add(linha);
        }
        DALUtil.reescreverFicheiro(caminho, novas);
    }

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