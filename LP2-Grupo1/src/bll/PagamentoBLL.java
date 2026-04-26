package bll;

import dal.EstudanteDAL;
import dal.PagamentoDAL;
import model.Estudante;
import model.Pagamento;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Lógica de negócio financeira.
 * Valida, processa e regista pagamentos de propinas (total ou parcial).
 *
 * Enunciado v1.1:
 *  - Propina pode ser paga de forma total ou parcial.
 *  - Estudante tem acesso ao valor em débito e ao histórico de pagamentos.
 *  - Só pode transitar/concluir se a propina estiver totalmente paga.
 */
public class PagamentoBLL {

    private static final String PASTA_BD = "bd";
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd-MM-yyyy");


    /**
     * Processa um pagamento total ou parcial de propina.
     * Se válido: deduz o valor do saldo, regista no histórico em memória
     * e persiste em estudantes.csv e pagamentos.csv.
     *
     * @param estudante O estudante que efetua o pagamento.
     * @param valor     Valor a pagar (> 0 e <= saldo devedor).
     * @return true se o pagamento foi processado com sucesso; false se o valor for inválido.
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