package model;

/**
 * Representa um Curso.
 * Contém os dados base e a capacidade de guardar várias Unidades Curriculares.
 */
public class Curso {

    // ---------- ATRIBUTOS ----------
    private String sigla;
    private String nome;
    private int duracao; // Duração em anos

    // Array para guardar as UCs e um contador para saber quantas já foram adicionadas
    private UnidadeCurricular[] unidadesCurriculares;
    private int totalUCs;

    // ---------- CONSTRUTOR ----------
    public Curso(String sigla, String nome, int duracao) {
        this.sigla = sigla;
        this.nome = nome;
        this.duracao = duracao;

        // Inicializamos o array com um tamanho fixo (ex: 15) para esta fase
        this.unidadesCurriculares = new UnidadeCurricular[15];
        this.totalUCs = 0;
    }

    // ---------- GETTERS ----------
    public String getSigla() { return sigla; }
    public String getNome() { return nome; }
    public int getDuracao() { return duracao; }
    public UnidadeCurricular[] getUnidadesCurriculares() { return unidadesCurriculares; }
    public int getTotalUCs() { return totalUCs; }

    // ---------- SETTERS ----------
    public void setSigla(String sigla) { this.sigla = sigla; }
    public void setNome(String nome) { this.nome = nome; }
    public void setDuracao(int duracao) { this.duracao = duracao; }

    // ---------- MÉTODOS DE LIGAR AS PEÇAS ----------

    /**
     * Adiciona uma Unidade Curricular ao array do Curso.
     */
    public boolean adicionarUnidadeCurricular(UnidadeCurricular uc) {
        // Verifica se ainda há espaço no array
        if (totalUCs < unidadesCurriculares.length) {
            unidadesCurriculares[totalUCs] = uc;
            totalUCs++; // Aumenta a contagem
            return true;
        }
        return false; // Retorna falso se o array estiver cheio
    }
}