package model;

public abstract class Utilizador {

    // ---------- ATRIBUTOS ----------
    protected String email;
    protected String password;
    protected String nome;
    protected String nif;
    protected String morada;
    protected String dataNascimento;

    // ---------- CONSTRUTOR ----------
    public Utilizador() {}

    public Utilizador(String email, String password, String nome, String nif, String morada, String dataNascimento) {
        this.email = email;
        this.password = password;
        this.nome = nome;
        this.nif = nif;
        this.morada = morada;
        this.dataNascimento = dataNascimento;
    }

    // ---------- GETTERS ----------
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getNome() { return nome; }
    public String getNif() { return nif; }
    public String getMorada() { return morada; }
    public String getDataNascimento() { return dataNascimento; }

    // ---------- SETTERS ----------
    public void setPassword(String password) { this.password = password; }
    public void setNome(String nome) { this.nome = nome; }
    public void setNif(String nif) { this.nif = nif; }
    public void setMorada(String morada) { this.morada = morada; }
    public void setDataNascimento(String dataNascimento) { this.dataNascimento = dataNascimento; }
}