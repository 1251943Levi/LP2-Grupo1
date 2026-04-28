package dal;

import model.Pagamento;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Acesso ao histórico de pagamentos de propinas em pagamentos.csv.
 * Formato das colunas: numMec; valorPago; dataPagamento.
 */
public class PagamentoDAL {

    private static final String NOME_FICHEIRO = "pagamentos.csv";
    private static final String CABECALHO = "numMec;valorPago;dataPagamento";

    /**
     * Regista um novo pagamento de propina no ficheiro.
     * @param numMec        Número mecanográfico do estudante.
     * @param valorPago     Montante pago em euros.
     * @param dataPagamento Data do pagamento (DD-MM-AAAA).
     * @param pastaBase     Caminho da pasta de dados.
     */
    public static void adicionarPagamento(int numMec, double valorPago,
                                          String dataPagamento, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        DALUtil.garantirFicheiroECabecalho(caminho, CABECALHO);

        String linha = numMec + ";" + valorPago + ";" + dataPagamento;
        DALUtil.adicionarLinhaCSV(caminho, linha);
    }

    /**
     * Carrega todos os pagamentos de um estudante por ordem de registo.
     * @param numMec    Número mecanográfico do estudante.
     * @param pastaBase Caminho da pasta de dados.
     * @return Lista de pagamentos do estudante.
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