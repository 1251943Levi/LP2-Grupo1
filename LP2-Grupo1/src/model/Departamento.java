package model;

/**
 * Representa um Departamento.
 * Contém os dados base e a capacidade de guardar vários Cursos.
 */
public class Departamento {

    // ---------- ATRIBUTOS ----------
    private String sigla;
    private String nome;

    // Array para guardar os cursos e um contador para saber quantos já foram adicionados
    private Curso[] cursos;
    private int totalCursos;

    // ---------- CONSTRUTOR ----------
    public Departamento(String sigla, String nome) {
        this.sigla = sigla;
        this.nome = nome;

        // Inicializamos o array com um tamanho fixo (ex: 10) para esta fase
        this.cursos = new Curso[10];
        this.totalCursos = 0;
    }

    // ---------- GETTERS ----------
    public String getSigla() { return sigla; }
    public String getNome() { return nome; }
    public Curso[] getCursos() { return cursos; }
    public int getTotalCursos() { return totalCursos; }

    // ---------- SETTERS ----------
    public void setSigla(String sigla) { this.sigla = sigla; }
    public void setNome(String nome) { this.nome = nome; }

    // ---------- MÉTODOS DE LIGAR AS PEÇAS ----------

    /**
     * Adiciona um Curso ao array do Departamento.
     */
    public boolean adicionarCurso(Curso curso) {
        // Verifica se ainda há espaço no array
        if (totalCursos < cursos.length) {
            cursos[totalCursos] = curso;
            totalCursos++; // Aumenta a contagem
            return true;
        }
        return false; // Retorna falso se o array estiver cheio
    }
}