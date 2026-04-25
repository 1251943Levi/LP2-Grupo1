package model;

/**
 * Classe Model que representa um Docente.
 * Gere as Unidades Curriculares que o docente leciona ou das quais é regente.
 */
public class Docente extends Utilizador {

    private String sigla;
    private final UnidadeCurricular[] ucsLecionadas;
    private int totalUcsLecionadas;


    /**
     * Construtor completo do Docente.
     */
    public Docente(String sigla, String email, String password,
                   String nome, String nif, String morada, String dataNascimento) {
        super(email, password, nome, nif, morada, dataNascimento);
        this.sigla             = sigla;
        this.ucsLecionadas     = new UnidadeCurricular[20];
        this.totalUcsLecionadas = 0;
    }

    // --- Getters ---
    public String getSigla()                      { return sigla; }
    public UnidadeCurricular[] getUcsLecionadas() { return ucsLecionadas; }
    public int getTotalUcsLecionadas()            { return totalUcsLecionadas; }

    // --- Setters ---
    public void setSigla(String sigla) { this.sigla = sigla; }

    /**
     * Adiciona uma UC à lista de lecionação do docente.
     */
    public void adicionarUcLecionada(UnidadeCurricular uc) {
        if (totalUcsLecionadas < ucsLecionadas.length) {
            ucsLecionadas[totalUcsLecionadas] = uc;
            totalUcsLecionadas++;
        }
    }
}