package model;

/**
 * Representa um Departamento dentro da instituição de ensino.
 * Atua como uma unidade orgânica que agrega diversos cursos e possui um
 * docente responsável pela sua coordenação.
 */
public class Departamento {

    // ---------- ATRIBUTOS ----------
    private String sigla;
    private String nome;
    private Docente docenteResponsavel;

    private Curso[] cursos;
    private int totalCursos;

    // ---------- CONSTRUTOR ----------
    /**
     * Cria um novo departamento com uma capacidade máxima de 10 cursos.
     * @param sigla Sigla identificadora (ex: "DEI").
     * @param nome Nome por extenso (ex: "Departamento de Engenharia Informática").
     */
    public Departamento(String sigla, String nome) {
        this.sigla = sigla;
        this.nome = nome;
        this.cursos = new Curso[10]; // Limite fixo por departamento
        this.totalCursos = 0;
    }

    // ---------- GETTERS ----------
    public String getSigla() { return sigla; }
    public String getNome() { return nome; }
    public Docente getDocenteResponsavel() { return docenteResponsavel; }
    public Curso[] getCursos() { return cursos; }
    public int getTotalCursos() { return totalCursos; }

    // ---------- SETTERS ----------
    public void setSigla(String sigla) { this.sigla = sigla; }
    public void setNome(String nome) { this.nome = nome; }
    /**
     * Define o docente regente/responsável por este departamento.
     * @param docenteResponsavel Objeto Docente.
     */
    public void setDocenteResponsavel(Docente docenteResponsavel) { this.docenteResponsavel = docenteResponsavel; }

    // ---------- MÉTODOS DE LÓGICA E AÇÃO ----------

    /**
     * Adiciona um curso à lista de cursos geridos por este departamento.
     * Garante a integridade do array interno, verificando se há espaço disponível.
     * * @param curso O objeto Curso a associar.
     * @return true se o curso foi adicionado, false se o limite de 10 cursos foi atingido.
     */
    public boolean adicionarCurso(Curso curso) {
        if (totalCursos < cursos.length) {
            cursos[totalCursos] = curso;
            totalCursos++;
            return true;
        }
        return false;
    }
}