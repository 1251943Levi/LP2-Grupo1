package model;

public class Estudante extends Utilizador {

    // ---------- ATRIBUTOS ----------
    private final int numeroMecanografico;
    private final int anoPrimeiraInscricao;
    private int anoCurricular;
    private int anoFrequencia;
    private PercursoAcademico percurso;
    private double saldoDevedor;

    // ---------- CONSTRUTOR ----------
    public Estudante(int numeroMecanografico, String email, String password, String nome, String nif, String morada, String dataNascimento, int anoPrimeiraInscricao) {
        super(email, password, nome, nif, morada, dataNascimento);

        this.numeroMecanografico = numeroMecanografico;
        this.anoPrimeiraInscricao = anoPrimeiraInscricao;

        this.anoCurricular = 1;
        this.anoFrequencia = 1;
        this.percurso = new PercursoAcademico();
    }

    // ---------- GETTERS ----------
    public int getNumeroMecanografico() { return numeroMecanografico; }
    public int getAnoPrimeiraInscricao() { return anoPrimeiraInscricao; }
    public int getAnoCurricular() { return anoCurricular; }
    public int getAnoFrequencia() { return anoFrequencia; }
    public PercursoAcademico getPercurso() { return percurso; }
    public double getSaldoDevedor() { return saldoDevedor; }

    // ---------- SETTERS ----------
    public void setAnoCurricular(int anoCurricular) { this.anoCurricular = anoCurricular; }
    public void setAnoFrequencia(int anoFrequencia) { this.anoFrequencia = anoFrequencia; }
    public void setSaldoDevedor(double saldoDevedor){ this.saldoDevedor = saldoDevedor; }

    // ---------- MÉTODOS ----------
    @Override
    public String toString() {
        return numeroMecanografico + " - " + nome;
    }

    public void efetuarPagamento(double valor) {
        this.saldoDevedor -= valor;
    }

}