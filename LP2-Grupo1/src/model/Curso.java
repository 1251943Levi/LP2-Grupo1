package model;

public class Curso {

    // ---------- ATRIBUTOS ----------
    private String sigla;
    private String nome;
    private Departamento departamento;
    private Docente docenteResponsavel;
    private final int duracaoAnos = 3;
    private String estado;

    private UnidadeCurricular[] unidadesCurriculares;
    private int totalUCs;

    private double valorPropinaAnual;

    // ---------- CONSTRUTOR ----------
    public Curso(String sigla, String nome, Departamento departamento, double valorPropinaAnual) {
        this.sigla = sigla;
        this.nome = nome;
        this.departamento = departamento;
        this.valorPropinaAnual = valorPropinaAnual;
        this.unidadesCurriculares = new UnidadeCurricular[15];
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
     * Associa uma nova Unidade Curricular à estrutura do Curso.
     * * @param uc A Unidade Curricular a ser adicionada.
     * @return true se foi adicionada com sucesso, false se o limite do curso foi atingido.
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
     * Verifica se o curso ainda tem vagas para UCs num determinado ano curricular.
     * Regra de negócio: Máximo de 5 UCs por ano.
     * @param anoCurricular O ano a verificar (1, 2 ou 3).
     * @return true se ainda tiver menos de 5 UCs, false se o limite foi atingido.
     */
    public boolean podeAdicionarUcNoAno(int anoCurricular) {
        int contadorUcsNesteAno = 0;

        for (int i = 0; i < totalUCs; i++) {
            if (unidadesCurriculares[i].getAnoCurricular() == anoCurricular) {
                contadorUcsNesteAno++;
            }
        }

        return contadorUcsNesteAno < 5;
    }
}