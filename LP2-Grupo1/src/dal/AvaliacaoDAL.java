package dal;

import model.Avaliacao;
import model.UnidadeCurricular;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Responsável pelo acesso ao ficheiro avaliacoes.csv.
 */
public class AvaliacaoDAL {

    private static final String NOME_FICHEIRO = "avaliacoes.csv";
    private static final String CABECALHO = "numMec;siglaUC;anoLetivo;nota1;nota2;nota3";

    /**
     * Adiciona uma nova avaliação ao ficheiro CSV.
     */
    public static void adicionarAvaliacao(Avaliacao avaliacao, int numMec, String pastaBase) {
        if (avaliacao == null || avaliacao.getUc() == null) return;

        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        DALUtil.garantirFicheiroECabecalho(caminho, CABECALHO);

        double[] notas = avaliacao.getResultados();
        String nota1 = (notas.length > 0) ? String.valueOf(notas[0]) : "";
        String nota2 = (notas.length > 1) ? String.valueOf(notas[1]) : "";
        String nota3 = (notas.length > 2) ? String.valueOf(notas[2]) : "";

        String linha = numMec + ";" +
                avaliacao.getUc().getSigla() + ";" +
                avaliacao.getAnoLetivo() + ";" +
                nota1 + ";" + nota2 + ";" + nota3;

        DALUtil.adicionarLinhaCSV(caminho, linha);
    }

    /**
     * Retorna a lista de todas as avaliações de um determinado aluno.
     */
    public static List<Avaliacao> obterAvaliacoesPorAluno(int numMec, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        List<Avaliacao> avaliacoesDoAluno = new ArrayList<>();

        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;

            String[] dados = linha.split(";", -1);
            if (dados.length >= 4) {
                try {
                    if (Integer.parseInt(dados[0].trim()) == numMec) {
                        UnidadeCurricular uc = UcDAL.procurarUC(dados[1].trim(), pastaBase);
                        if (uc != null) {
                            Avaliacao av = new Avaliacao(uc, Integer.parseInt(dados[2].trim()));

                            if (!dados[3].trim().isEmpty()) av.adicionarResultado(Double.parseDouble(dados[3].trim()));
                            if (dados.length > 4 && !dados[4].trim().isEmpty()) av.adicionarResultado(Double.parseDouble(dados[4].trim()));
                            if (dados.length > 5 && !dados[5].trim().isEmpty()) av.adicionarResultado(Double.parseDouble(dados[5].trim()));

                            avaliacoesDoAluno.add(av);
                        }
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return avaliacoesDoAluno;
    }
}