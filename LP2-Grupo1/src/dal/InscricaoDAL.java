package dal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Acesso aos dados de inscrições em UCs armazenados em inscricoes.csv.
 * Formato das colunas: numMec; siglaUC.
 * Cada linha associa um estudante a uma unidade curricular ativa.
 * As inscrições refletem as UCs do ano corrente, incluindo UCs
 * de anos anteriores ainda não aprovadas.
 */
public class InscricaoDAL {
    private static final String NOME_FICHEIRO = "inscricoes.csv";
    private static final String CABECALHO = "numMec;siglaUC";

    /**
     * Regista a inscrição de um estudante numa unidade curricular.
     * @param numMec    Número mecanográfico do estudante.
     * @param siglaUC   Sigla da UC em que o estudante se inscreve.
     * @param pastaBase Caminho da pasta de dados.
     */
    public static void adicionarInscricao(int numMec, String siglaUC, String pastaBase) {
        if (siglaUC == null || siglaUC.trim().isEmpty()) return;
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        DALUtil.garantirFicheiroECabecalho(caminho, CABECALHO);
        DALUtil.adicionarLinhaCSV(caminho, numMec + ";" + siglaUC.trim());
    }

    /**
     * Remove a inscrição de um estudante numa unidade curricular.
     * Chamado na transição de ano para eliminar inscrições em UCs já aprovadas.
     * @param numMec    Número mecanográfico do estudante.
     * @param siglaUC   Sigla da UC a remover.
     * @param pastaBase Caminho da pasta de dados.
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
     * Devolve as siglas de todas as UCs em que um estudante está inscrito.
     * @param numMec    Número mecanográfico do estudante.
     * @param pastaBase Caminho da pasta de dados.
     * @return Lista de siglas; vazia se não houver inscrições.
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