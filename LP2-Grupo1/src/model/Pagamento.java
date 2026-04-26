package model;

/**
 * Representa um registo individual de pagamento de propina.
 * Cada vez que um estudante paga (total ou parcialmente), é criado um objeto Pagamento.
 */
public class Pagamento {

    private int idAluno;
    private double valorPago;
    private String dataPagamento; // Formato DD-MM-AAAA

    public Pagamento(int idAluno, double valorPago, String dataPagamento) {
        this.idAluno = idAluno;
        this.valorPago = valorPago;
        this.dataPagamento = dataPagamento;
    }

    // ---------- GETTERS / SETTERS ----------
    public double getValorPago()       { return valorPago; }
    public String getDataPagamento()   { return dataPagamento; }


    @Override
    public String toString() {
        return String.format("%.2f€ em %s", valorPago, dataPagamento);
    }
}