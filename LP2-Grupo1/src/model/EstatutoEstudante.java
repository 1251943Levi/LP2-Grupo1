package model;

/**
 * Representa um estatuto de estudante (ex.: Estudante atleta, Estudante
 * trabalhador, Estudante pai, Matrimónio, Baixa médica) que pode ser
 * atribuído a um estudante e usado nos seus pedidos de justificação.
 */
public class EstatutoEstudante {
    private int id;
    private String nome;
    private String descricao;

    public EstatutoEstudante() {}

    public EstatutoEstudante(int id, String nome, String descricao) {
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
