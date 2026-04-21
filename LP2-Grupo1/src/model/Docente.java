package model;

/**
 * Classe Model que representa um Docente.
 * Gere as Unidades Curriculares que o docente leciona ou das quais é regente.
 */
public class Docente extends Utilizador {

    private String sigla;
    private UnidadeCurricular[] ucsLecionadas;
    private int totalUcsLecionadas;
    private UnidadeCurricular[] ucsResponsavel;
    private int totalUcsResponsavel;

    /**
     * Construtor completo do Docente.
     */
    public Docente(String sigla, String email, String password, String nome, String nif, String morada, String dataNascimento) {
        super(email, password, nome, nif, morada, dataNascimento);
        this.sigla = sigla;
        this.ucsLecionadas = new UnidadeCurricular[20];
        this.totalUcsLecionadas = 0;
        this.ucsResponsavel = new UnidadeCurricular[20];
        this.totalUcsResponsavel = 0;
    }

    // --- Getters ---
    public String getSigla() { return sigla; }
    public UnidadeCurricular[] getUcsLecionadas() { return ucsLecionadas; }
    public int getTotalUcsLecionadas() { return totalUcsLecionadas; }
    public UnidadeCurricular[] getUcsResponsavel() { return ucsResponsavel; }
    public int getTotalUcsResponsavel() { return totalUcsResponsavel; }

    // --- Setters ---
    public void setSigla(String sigla) { this.sigla = sigla; }

    /**
     * Adiciona uma UC à lista de lecionação do docente.
     * @param uc Objeto Unidade Curricular.
     * @return true se adicionado, false se atingiu o limite de 20 UCs.
     */
    public boolean adicionarUcLecionada(UnidadeCurricular uc) {
        if (totalUcsLecionadas < ucsLecionadas.length) {
            ucsLecionadas[totalUcsLecionadas] = uc;
            totalUcsLecionadas++;
            return true;
        }
        return false;
    }

    public boolean adicionarUcResponsavel(UnidadeCurricular uc) {
        if (totalUcsResponsavel < ucsResponsavel.length) {
            ucsResponsavel[totalUcsResponsavel] = uc;
            totalUcsResponsavel++;
            return true;
        }
        return false;
    }
}