package model;
/**
 * Representa um Departamento dentro da instituição de ensino.
 */
public class Departamento {

    private String sigla;
    private String nome;

    public Departamento(String sigla, String nome) {
        this.sigla = sigla;
        this.nome  = nome;
    }

    public String getSigla() { return sigla; }
    public String getNome()  { return nome; }

    public void setSigla(String sigla) { this.sigla = sigla; }
    public void setNome(String nome)   { this.nome = nome; }
}