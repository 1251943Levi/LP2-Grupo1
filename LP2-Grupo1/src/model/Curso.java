package model;

/**
 * Representa um curso académico no sistema ISSMF.
 * Gere a estrutura de Unidades Curriculares e as regras de limite por ano letivo.
 */
public class Curso {

    // ---------- ATRIBUTOS ----------
    private String sigla;
    private String nome;
    private Departamento departamento;
    private Docente docenteResponsavel;
    private final int duracaoAnos = 3;
    private String estado; // Ex: "Ativo", "Inativo"

    private UnidadeCurricular[] unidadesCurriculares;
    private int totalUCs;

    private double valorPropinaAnual;

    // ---------- CONSTRUTOR ----------
    /**
     * Cria um novo curso com estado inicial "Inativo".
     * @param sigla Sigla única (ex: "LEI").
     * @param nome Nome completo do curso.
     * @param departamento Departamento ao qual o curso pertence.
     * @param valorPropinaAnual Custo anual para o estudante.
     */
    public Curso(String sigla, String nome, Departamento departamento, double valorPropinaAnual) {
        this.sigla = sigla;
        this.nome = nome;
        this.departamento = departamento;
        this.valorPropinaAnual = valorPropinaAnual;
        this.unidadesCurriculares = new UnidadeCurricular[15]; // Limite total do curso
        this.totalUCs = 0;
        this.estado = "Inativo";
    }

    // ---------- GETTERS ----------
    public String getSigla() { return sigla; }
    public String getNome() { return nome; }
    public Departamento getDepartamento() { return departamento; }
    public Docente getDocenteResponsavel() { return docenteResponsavel; }
    public int getDuracaoAnos() { return duracaoAnos; }
    public UnidadeCurricular[] getUnidadesCurriculares() { return unidadesCurriculares; }
    public int getTotalUCs() { return totalUCs; }
    public double getValorPropinaAnual() { return valorPropinaAnual; }
    public String getEstado() { return estado; }

    // ---------- SETTERS ----------
    public void setSigla(String sigla) { this.sigla = sigla; }
    public void setNome(String nome) { this.nome = nome; }
    public void setDepartamento(Departamento departamento) { this.departamento = departamento; }
    public void setDocenteResponsavel(Docente docenteResponsavel) { this.docenteResponsavel = docenteResponsavel; }
    public void setEstado(String estado) { this.estado = estado; }

    // ---------- MÉTODOS DE LÓGICA E AÇÃO ----------

    /**
     * Adiciona uma UC à matriz do curso, respeitando o limite físico do array.
     * @param uc Objeto Unidade Curricular.
     * @return true se adicionado, false se excedeu o limite total.
     */
    public boolean adicionarUnidadeCurricular(UnidadeCurricular uc) {
        if (totalUCs < unidadesCurriculares.length) {
            unidadesCurriculares[totalUCs] = uc;
            totalUCs++;
            return true;
        }
        return false;
    }

    /**
     * Valida uma regra de negócio específica: Cada ano curricular (1, 2 ou 3)
     * não pode ter mais de 5 Unidades Curriculares.
     * @param anoCurricular O ano a validar.
     * @return true se ainda for possível adicionar UCs a esse ano.
     */
    public boolean podeAdicionarUcNoAno(int anoCurricular) {
        int contadorUcsNesteAno = 0;

        for (int i = 0; i < totalUCs; i++) {
            if (unidadesCurriculares[i] != null && unidadesCurriculares[i].getAnoCurricular() == anoCurricular) {
                contadorUcsNesteAno++;
            }
        }

        return contadorUcsNesteAno < 5;
    }
}