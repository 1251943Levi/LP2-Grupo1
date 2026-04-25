package dal;

import model.Pagamento;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Responsável pelas operações de acesso a dados dos Pagamentos de propinas.
 * Persiste e lê do ficheiro pagamentos.csv.
 * Formato CSV:  numMec ; valorPago ; dataPagamento
 */
public class PagamentoDAL {

    private static final String NOME_FICHEIRO = "pagamentos.csv";
    private static final String CABECALHO = "numMec;valorPago;dataPagamento";

    /**
     * Regista um novo pagamento no ficheiro pagamentos.csv.
     */
    public static void adicionarPagamento(int numMec, double valorPago,
                                          String dataPagamento, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        DALUtil.garantirFicheiroECabecalho(caminho, CABECALHO);

        String linha = numMec + ";" + valorPago + ";" + dataPagamento;
        DALUtil.adicionarLinhaCSV(caminho, linha);
    }

    /**
     * Carrega todos os pagamentos de um estudante específico,
     * ordenados pela ordem em que foram registados.
     * @param numMec     Número mecanográfico do estudante.
     * @param pastaBase  Caminho da pasta da base de dados.
     * @return Lista de objetos Pagamento do aluno.
     */
    public static List<Pagamento> carregarPagamentosPorAluno(int numMec, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        List<Pagamento> pagamentos = new ArrayList<>();

        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 3) {
                try {
                    int idAluno = Integer.parseInt(dados[0].trim());
                    if (idAluno == numMec) {
                        double valor = Double.parseDouble(dados[1].trim());
                        String data = dados[2].trim();
                        pagamentos.add(new Pagamento(idAluno, valor, data));
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return pagamentos;
    }
}