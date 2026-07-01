package model;

/**
 * Nota de um estudante num momento de avaliação (Card 5).
 * A nota final de uma UC é a média ponderada das notas dos seus momentos,
 * com base nos pesos definidos em {@link Momento}.
 */
public class Nota {

    private int numMec;
    private int idMomento;
    private String siglaUC;
    private double valor;   // 0–20

    public Nota(int numMec, int idMomento, String siglaUC, double valor) {
        this.numMec = numMec;
        this.idMomento = idMomento;
        this.siglaUC = siglaUC;
        this.valor = valor;
    }

    public int getNumMec()      { return numMec; }
    public int getIdMomento()   { return idMomento; }
    public String getSiglaUC()  { return siglaUC; }
    public double getValor()    { return valor; }
    public void setValor(double valor) { this.valor = valor; }

    @Override
    public String toString() {
        return String.format("Momento %d (%s): %.1f", idMomento, siglaUC, valor);
    }
}
