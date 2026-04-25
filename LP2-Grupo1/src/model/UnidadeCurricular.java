package model;

public class UnidadeCurricular {

    // ---------- ATRIBUTOS ----------
    private String sigla;
    private String nome;
    private int anoCurricular;
    private Docente docenteResponsavel;

    private final Curso[] cursos;
    private int totalCursos;

    public UnidadeCurricular(String sigla, String nome, int anoCurricular, Docente docenteResponsavel) {
        this.sigla = sigla;
        this.nome               = nome;
        this.anoCurricular      = anoCurricular;
        this.docenteResponsavel = docenteResponsavel;
        this.cursos             = new Curso[10];
        this.totalCursos        = 0;
    }

    // ---------- GETTERS ----------
    public String  getSigla()              { return sigla; }
    public String  getNome()               { return nome; }
    public int     getAnoCurricular()      { return anoCurricular; }
    public Docente getDocenteResponsavel() { return docenteResponsavel; }
    public Curso[] getCursos()             { return cursos; }

    // ---------- SETTERS ----------
    public void setSigla(String sigla)                    { this.sigla = sigla; }
    public void setNome(String nome)                      { this.nome = nome; }
    public void setAnoCurricular(int anoCurricular)       { this.anoCurricular = anoCurricular; }

    // ---------- MÉTODOS DE LÓGICA E AÇÃO ----------

    /**
     * Regista esta Unidade Curricular como pertencente a um dado Curso.
     * * @param curso O Curso ao qual a UC passa a estar associada.
     * @return true se a associação for bem sucedida, false se o limite de cursos for atingido.
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