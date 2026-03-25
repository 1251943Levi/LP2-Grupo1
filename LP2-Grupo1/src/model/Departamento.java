package model;

public class Departamento {

    // ---------- ATRIBUTOS ----------
    private String sigla;
    private String nome;
    private Docente docenteResponsavel;

    private Curso[] cursos;
    private int totalCursos;

    // ---------- CONSTRUTOR ----------
    public Departamento(String sigla, String nome) {
        this.sigla = sigla;
        this.nome = nome;
        this.cursos = new Curso[10];
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
    public void setDocenteResponsavel(Docente docenteResponsavel) { this.docenteResponsavel = docenteResponsavel; }

    // ---------- MÉTODOS DE LÓGICA E AÇÃO ----------

    /**
     * Associa um novo Curso a este Departamento.
     * * @param curso O Curso a ser associado.
     * @return true se foi adicionado com sucesso, false se o limite departamental foi atingido.
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