package model;

public class Docente extends Utilizador {

    // ---------- ATRIBUTOS ESPECÍFICOS ----------
    private String sigla;

    // ---------- CONSTRUTOR ----------
    public Docente(String sigla, String email, String password, String nome, String nif, String morada, String dataNascimento) {
        super(email, password, nome, nif, morada, dataNascimento);
        this.sigla = sigla;
    }

    // ---------- GETTER ----------
    public String getSigla() { return sigla; }

    // ---------- SETTER ----------
    public void setSigla(String sigla) { this.sigla = sigla; }
}