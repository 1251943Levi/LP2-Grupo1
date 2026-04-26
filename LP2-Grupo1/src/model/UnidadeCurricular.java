package model;

public class UnidadeCurricular {

    /** Valor de ECTS comum a todas as UCs do ISSMF (enunciado v1.0, pág. 2). */
    public static final int ECTS_PADRAO = 6;

    // ---------- ATRIBUTOS ----------
    private String sigla;
    private String nome;
    private int anoCurricular;
    private int ects;
    private Docente docenteResponsavel;

    private final Curso[] cursos;
    private int totalCursos;

    /** Construtor completo — usado pela DAL ao carregar UCs com ECTS persistido. */
    public UnidadeCurricular(String sigla, String nome, int anoCurricular,
                             Docente docenteResponsavel, int ects) {
        this.sigla              = sigla;
        this.nome               = nome;
        this.anoCurricular      = anoCurricular;
        this.docenteResponsavel = docenteResponsavel;
        this.ects               = ects;
        this.cursos             = new Curso[10];
        this.totalCursos        = 0;
    }

    /** Construtor de conveniência — aplica o valor padrão de ECTS. */
    public UnidadeCurricular(String sigla, String nome, int anoCurricular,
                             Docente docenteResponsavel) {
        this(sigla, nome, anoCurricular, docenteResponsavel, ECTS_PADRAO);
    }

    // ---------- GETTERS ----------
    public String  getSigla()              { return sigla; }
    public String  getNome()               { return nome; }
    public int     getAnoCurricular()      { return anoCurricular; }
    public int     getEcts()               { return ects; }
    public Docente getDocenteResponsavel() { return docenteResponsavel; }
    public Curso[] getCursos()             { return cursos; }

    // ---------- SETTERS ----------
    public void setSigla(String sigla)              { this.sigla = sigla; }
    public void setNome(String nome)                { this.nome = nome; }
    public void setAnoCurricular(int anoCurricular) { this.anoCurricular = anoCurricular; }
    public void setEcts(int ects)                   { this.ects = ects; }

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