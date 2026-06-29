package model;

/**
 * Momento de avaliação de uma UC (Card 3).
 * Tem um tipo ({@link TipoMomento}), um peso (%) e uma data de realização.
 * A nota final de uma UC é a média ponderada dos seus momentos pelos pesos.
 */
public class Momento {

    private int id;                 // 0 = ainda não persistido (atribuído pela DAL)
    private String siglaUC;
    private String nome;
    private TipoMomento tipo;
    private double peso;            // percentagem (0–100)
    private String dataRealizacao;  // DD-MM-AAAA

    public Momento(int id, String siglaUC, String nome, TipoMomento tipo, double peso, String dataRealizacao) {
        this.id = id;
        this.siglaUC = siglaUC;
        this.nome = nome;
        this.tipo = tipo;
        this.peso = peso;
        this.dataRealizacao = dataRealizacao;
    }

    public Momento(String siglaUC, String nome, TipoMomento tipo, double peso, String dataRealizacao) {
        this(0, siglaUC, nome, tipo, peso, dataRealizacao);
    }

    public int getId()                  { return id; }
    public void setId(int id)           { this.id = id; }
    public String getSiglaUC()          { return siglaUC; }
    public String getNome()             { return nome; }
    public TipoMomento getTipo()        { return tipo; }
    public double getPeso()             { return peso; }
    public String getDataRealizacao()   { return dataRealizacao; }

    @Override
    public String toString() {
        return String.format("[%d] %s (%s) — %.0f%% — %s",
                id, nome, tipo != null ? tipo.descricao() : "?", peso, dataRealizacao);
    }
}
