package model;

public class Pagamento {

    private int idAluno;
    private double valorTotal;
    private double valorPago;

    public Pagamento(int idAluno, double valorTotal, double valorPago) {
        this.idAluno = idAluno;
        this.valorTotal = valorTotal;
        this.valorPago = valorPago;
    }

    // Getters e Setters
    public int getIdAluno() { return idAluno; }
    public void setIdAluno(int idAluno) { this.idAluno = idAluno; }

    public double getValorTotal() { return valorTotal; }
    public void setValorTotal(double valorTotal) { this.valorTotal = valorTotal; }

    public double getValorPago() { return valorPago; }
    public void setValorPago(double valorPago) { this.valorPago = valorPago; }

    @Override
    public String toString() {
        return "Pagamento [Aluno=" + idAluno + ", Dívida=" + valorTotal + ", Pago=" + valorPago + "]";
    }
}
