package model;

/**
 * Representa uma Unidade Curricular (UC).
 * Contém apenas os dados base pedidos nesta fase.
 */
public class UnidadeCurricular {

    // ---------- ATRIBUTOS ----------
    private String sigla;
    private String nome;
    private int anoCurricular;

    // ---------- CONSTRUTOR ----------
    public UnidadeCurricular(String sigla, String nome, int anoCurricular) {
        this.sigla = sigla;
        this.nome = nome;
        this.anoCurricular = anoCurricular;
    }

    // ---------- GETTERS ----------
    public String getSigla() { return sigla; }
    public String getNome() { return nome; }
    public int getAnoCurricular() { return anoCurricular; }

    // ---------- SETTERS ----------
    public void setSigla(String sigla) { this.sigla = sigla; }
    public void setNome(String nome) { this.nome = nome; }
    public void setAnoCurricular(int anoCurricular) { this.anoCurricular = anoCurricular; }
}