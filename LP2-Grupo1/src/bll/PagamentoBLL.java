package bll;

import dal.EstudanteDAL;
import dal.PagamentoDAL;
import model.Estudante;
import model.Pagamento;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Lógica de negócio financeira do sistema.
 * Processa pagamentos de propinas totais ou parciais e garante
 * a consistência entre o saldo em memória e a persistência em ficheiro.
 */
public class PagamentoBLL {

    private static final String PASTA_BD = "bd";
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd-MM-yyyy");


    /**
     * Processa um pagamento de propina total ou parcial.
     * Deduz o montante do saldo do estudante, regista o pagamento
     * em memória e persiste em estudantes.csv e pagamentos.csv.
     * @param estudante Estudante que efetua o pagamento.
     * @param valor     Montante a pagar; deve ser positivo e não exceder o saldo.
     * @return true se o pagamento foi processado com sucesso.
     */
    public boolean processarPagamento(Estudante estudante, double valor) {
        if (valor <= 0 || valor > estudante.getSaldoDevedor()) {
            return false;
        }

        estudante.efetuarPagamento(valor);

        String dataHoje = LocalDate.now().format(FORMATO_DATA);
        Pagamento registo = new Pagamento(estudante.getNumeroMecanografico(), valor, dataHoje);
        estudante.adicionarPagamento(registo);
        EstudanteDAL.atualizarEstudante(estudante, PASTA_BD);
        PagamentoDAL.adicionarPagamento(estudante.getNumeroMecanografico(), valor, dataHoje, PASTA_BD);

        return true;
    }
}