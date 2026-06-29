package model;

public class TipoJustificacao {
    private int id;
    private String nome;
    private String descricao;

    public TipoJustificacao() {}

    public TipoJustificacao(int id, String nome, String descricao) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
    }

    // Getters e setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    @Override
    public String toString() {
        return nome + (descricao != null && !descricao.isEmpty() ? " (" + descricao + ")" : "");
    }
}