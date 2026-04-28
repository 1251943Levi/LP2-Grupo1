package dal;

import model.Avaliacao;
import model.UnidadeCurricular;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Acesso aos dados de avaliações armazenados em avaliacoes.csv.
 * Formato das colunas: numMec; siglaUC; anoLetivo; nota1; nota2; nota3.
 * Cada linha regista as notas de um estudante numa UC num dado ano letivo.
 */
public class AvaliacaoDAL {

    private static final String NOME_FICHEIRO = "avaliacoes.csv";
    private static final String CABECALHO = "numMec;siglaUC;anoLetivo;nota1;nota2;nota3";


    /**
     * Verifica se já existe um registo de avaliação para a combinação indicada.
     * @param numMec    Número mecanográfico do estudante.
     * @param siglaUc   Sigla da UC.
     * @param anoLetivo Ano letivo a verificar.
     * @param pastaBase Caminho da pasta de dados.
     * @return true se já existir um registo; false caso contrário.
     */
    public static boolean existeAvaliacao(int numMec, String siglaUc, int anoLetivo, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);

        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 3) {
                try {
                    if (Integer.parseInt(dados[0].trim()) == numMec
                            && dados[1].trim().equalsIgnoreCase(siglaUc)
                            && Integer.parseInt(dados[2].trim()) == anoLetivo) {
                        return true;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return false;
    }


    /**
     * Persiste um registo de avaliação no ficheiro CSV.
     * @param avaliacao Avaliação com as notas a guardar.
     * @param numMec    Número mecanográfico do estudante avaliado.
     * @param pastaBase Caminho da pasta de dados.
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
     * Carrega todas as avaliações de um estudante.
     * @param numMec    Número mecanográfico do estudante.
     * @param pastaBase Caminho da pasta de dados.
     * @return Lista de avaliações; vazia se não existirem registos.
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

    /**
     * Procura e devolve uma avaliação específica de um aluno num determinado ano.
     */
    public static Avaliacao obterAvaliacao(int numMec, String siglaUc, int ano, String pastaBd) {
        String caminho = pastaBd + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);

        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;

            String[] dados = linha.split(";", -1);
            if (dados.length >= 3) {
                try {
                    int mecLido = Integer.parseInt(dados[0].trim());
                    int anoLido = Integer.parseInt(dados[2].trim());

                    if (mecLido == numMec && dados[1].trim().equalsIgnoreCase(siglaUc) && anoLido == ano) {
                        UnidadeCurricular uc = UcDAL.procurarUC(siglaUc, pastaBd);
                        Avaliacao av = new Avaliacao(uc, ano);

                        for (int i = 3; i < dados.length && i < 6; i++) {
                            if (!dados[i].trim().isEmpty()) {
                                double nota = Double.parseDouble(dados[i].trim().replace(",", "."));
                                av.adicionarResultado(nota);
                            }
                        }
                        return av;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return null;
    }

    /**
     * Atualiza uma avaliação existente (substitui a linha antiga no CSV pela nova com mais notas).
     */
    public static void atualizarAvaliacao(Avaliacao aval, int numMec, String pastaBd) {
        String caminho = pastaBd + "/avaliacoes.csv";
        java.util.List<String> linhas = DALUtil.lerFicheiro(caminho);
        java.util.List<String> novasLinhas = new java.util.ArrayList<>();

        for (String linha : linhas) {
            String[] dados = linha.split(";");

            if (dados.length >= 3 && Integer.parseInt(dados[0]) == numMec
                    && dados[1].equalsIgnoreCase(aval.getUc().getSigla())
                    && Integer.parseInt(dados[2]) == aval.getAnoLetivo()) {

                StringBuilder novaLinha = new StringBuilder();
                novaLinha.append(numMec).append(";")
                        .append(aval.getUc().getSigla()).append(";")
                        .append(aval.getAnoLetivo());

                for (int i = 0; i < aval.getTotalAvaliacoesLancadas(); i++) {
                    novaLinha.append(";").append(aval.getResultados()[i]);
                }
                novasLinhas.add(novaLinha.toString());
            } else {
                novasLinhas.add(linha);
            }
        }
        DALUtil.reescreverFicheiro(caminho, novasLinhas);
    }
}