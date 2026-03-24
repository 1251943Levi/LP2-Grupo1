package model;

public class Docente extends Utilizador {

    // ---------- ATRIBUTOS ----------
    private String sigla;

    private UnidadeCurricular[] ucsLecionadas;
    private int totalUcsLecionadas;

    private UnidadeCurricular[] ucsResponsavel;
    private int totalUcsResponsavel;

    // ---------- CONSTRUTOR ----------
    public Docente(String sigla, String email, String password, String nome, String nif, String morada, String dataNascimento) {
        super(email, password, nome, nif, morada, dataNascimento);
        this.sigla = sigla;

        this.ucsLecionadas = new UnidadeCurricular[20];
        this.totalUcsLecionadas = 0;

        this.ucsResponsavel = new UnidadeCurricular[20];
        this.totalUcsResponsavel = 0;
    }

    // ---------- GETTERS ----------
    public String getSigla() { return sigla; }
    public UnidadeCurricular[] getUcsLecionadas() { return ucsLecionadas; }
    public int getTotalUcsLecionadas() { return totalUcsLecionadas; }
    public UnidadeCurricular[] getUcsResponsavel() { return ucsResponsavel; }
    public int getTotalUcsResponsavel() { return totalUcsResponsavel; }

    // ---------- SETTERS ----------
    public void setSigla(String sigla) { this.sigla = sigla; }

    // ---------- MÉTODOS DE LÓGICA E AÇÃO ----------

    /**
     * Associa uma Unidade Curricular à lista de UCs lecionadas pelo docente.
     * * @param uc A Unidade Curricular a adicionar.
     * @return true se adicionada com sucesso, false se o limite do docente foi atingido.
     */
    public boolean adicionarUcLecionada(UnidadeCurricular uc) {
        if (totalUcsLecionadas < ucsLecionadas.length) {
            ucsLecionadas[totalUcsLecionadas] = uc;
            totalUcsLecionadas++;
            return true;
        }
        return false;
    }

    /**
     * Regista o docente como regente/responsável de uma determinada Unidade Curricular.
     * * @param uc A Unidade Curricular a ser coordenada.
     * @return true se adicionada com sucesso, false se o limite do docente foi atingido.
     */
    public boolean adicionarUcResponsavel(UnidadeCurricular uc) {
        if (totalUcsResponsavel < ucsResponsavel.length) {
            ucsResponsavel[totalUcsResponsavel] = uc;
            totalUcsResponsavel++;
            return true;
        }
        return false;
    }
}