package Model;

/**
 * Classe Pessoa
 * nome - Nome completo da pessoa
 * nif - Identificação fiscal
 * dataNascimento - data de nascimento da pessoa
 * morada - morada de pessoa
 * email - email de pessoa (definido nas classes de herança de aluno e docente)
 * identificador - serve para receber o input (nº mecanografico/sigla e atribui um sufixo '@issmf.pt'
 *  tornando o email standard para todos os utilizadores.
 *  username - utilizador de gestor.
 * password - password de acesso(definida nas classes de herança de aluno e docente).
 *
 */
public abstract class Pessoa {
    private String nome;
    private int nif;
    private String dataNascimento;
    private String morada;
    private String email;
    private String password;
    private String username;

    public Pessoa() {
    }

    /**
     * Construtor de uma Pessoa (Aluno ou Docente)
     */
    public Pessoa(String nome, int nif, String dataNascimento, String morada, String identificador) {
        this.nome = nome;
        setNif(nif);
        this.dataNascimento = dataNascimento;
        this.morada = morada;
        this.email = identificador.toLowerCase() + "@issmf.pt";
        this.password = String.valueOf(nif) + dataNascimento.replace("/","");
    }

    /**
     * Construtor dos gestores.
     */
    public Pessoa(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getNif() {
        return nif;
    }

    public void setNif(int nif) {
        if (String.valueOf(nif).length() == 9) {
            this.nif = nif;
        }else {
            System.out.println("Erro: O NIF deve ter obrigatoriamente 9 digitos.");
        }
    }

    public String getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(String dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getMorada() {
        return morada;
    }

    public void setMorada(String morada) {
        this.morada = morada;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
