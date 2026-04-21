package model;

/**
 * Classe Model que representa um Estudante no sistema.
 * Estende a classe Utilizador para herdar dados pessoais e credenciais.
 */
public class Estudante extends Utilizador {

    private final int numeroMecanografico;
    private final int anoPrimeiraInscricao;
    private int anoCurricular;
    private int anoFrequencia;
    private PercursoAcademico percurso;
    private double saldoDevedor;
    private String siglaCurso;

    /**
     * Construtor completo para o Estudante.
     */
    public Estudante(int numeroMecanografico, String email, String password, String nome,
                     String nif, String morada, String dataNascimento, int anoPrimeiraInscricao) {
        super(email, password, nome, nif, morada, dataNascimento);
        this.numeroMecanografico = numeroMecanografico;
        this.anoPrimeiraInscricao = anoPrimeiraInscricao;
        this.anoCurricular = 1;
        this.anoFrequencia = 1;
        this.percurso = new PercursoAcademico();
    }

    // --- Getters ---
    public int getNumeroMecanografico() { return numeroMecanografico; }
    public int getAnoPrimeiraInscricao() { return anoPrimeiraInscricao; }
    public int getAnoCurricular() { return anoCurricular; }
    public int getAnoFrequencia() { return anoFrequencia; }
    public PercursoAcademico getPercurso() { return percurso; }
    public double getSaldoDevedor() { return saldoDevedor; }
    public String getSiglaCurso() { return siglaCurso; }

    // --- Setters ---
    public void setAnoCurricular(int anoCurricular) { this.anoCurricular = anoCurricular; }
    public void setAnoFrequencia(int anoFrequencia) { this.anoFrequencia = anoFrequencia; }
    public void setSaldoDevedor(double saldoDevedor) { this.saldoDevedor = saldoDevedor; }
    public void setSiglaCurso(String siglaCurso) { this.siglaCurso = siglaCurso; }

    @Override
    public String toString() {
        return numeroMecanografico + " - " + getNome();
    }

    /**
     * Deduz um valor ao saldo devedor do estudante.
     * @param valor Valor a ser subtraído da dívida.
     */
    public void efetuarPagamento(double valor) {
        this.saldoDevedor -= valor;
    }
}