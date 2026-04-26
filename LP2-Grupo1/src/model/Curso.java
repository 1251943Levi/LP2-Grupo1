package model;

/**
 * Representa um curso académico no sistema ISSMF.
 * Gere a estrutura de Unidades Curriculares e as regras de limite por ano letivo.
 */
public class Curso {

    public static final int DURACAO_ANOS = 3;

    private String sigla;
    private String nome;
    private Departamento departamento;
    private String estado;
    private final double valorPropinaAnual;

    // ---------- CONSTRUTOR ----------
    public Curso(String sigla, String nome, Departamento departamento, double valorPropinaAnual) {
        this.sigla             = sigla;
        this.nome              = nome;
        this.departamento      = departamento;
        this.valorPropinaAnual = valorPropinaAnual;
        this.estado            = "Inativo";
    }

    // ---------- GETTERS ----------
    public String       getSigla()            { return sigla; }
    public String       getNome()             { return nome; }
    public Departamento getDepartamento()      { return departamento; }
    public double       getValorPropinaAnual() { return valorPropinaAnual; }
    public String       getEstado()           { return estado; }
    // ---------- SETTERS ----------
    public void setSigla(String sigla)              { this.sigla = sigla; }
    public void setNome(String nome)                { this.nome = nome; }
    public void setDepartamento(Departamento dep)   { this.departamento = dep; }
    public void setEstado(String estado)            { this.estado = estado; }
}
