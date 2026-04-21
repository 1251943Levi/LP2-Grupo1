package model;

/**
 * Classe Model que representa o Gestor do sistema.
 */
public class Gestor extends Utilizador { // Se tiveres uma classe pai Utilizador
    private String email;
    private String password;
    private String nome;
    private String nif;
    private String morada;
    private String dataNascimento;

    public Gestor(String email, String password, String nome, String nif, String morada, String dataNascimento) {
        this.email = email;
        this.password = password;
        this.nome = nome;
        this.nif = nif;
        this.morada = morada;
        this.dataNascimento = dataNascimento;
    }

    // --- Getters e Setters Essenciais ---

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getNif() { return nif; }
    public void setNif(String nif) { this.nif = nif; }

    public String getMorada() { return morada; }
    public void setMorada(String morada) { this.morada = morada; }

    public String getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(String dataNascimento) { this.dataNascimento = dataNascimento; }
}