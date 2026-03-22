package model;

public class Estudante extends Utilizador {

    // ---------- ATRIBUTOS ESPECÍFICOS ----------
    private final int numeroMecanografico;
    private final int anoPrimeiraInscricao;

    // ---------- CONSTRUTOR ----------
    public Estudante(int numeroMecanografico, String email, String password, String nome, String nif, String morada, String dataNascimento, int anoPrimeiraInscricao) {
        super(email, password, nome, nif, morada, dataNascimento);

        this.numeroMecanografico = numeroMecanografico;
        this.anoPrimeiraInscricao = anoPrimeiraInscricao;
    }

    // ---------- GETTERS ----------
    public int getNumeroMecanografico() { return numeroMecanografico; }
    public int getAnoPrimeiraInscricao() { return anoPrimeiraInscricao; }
}