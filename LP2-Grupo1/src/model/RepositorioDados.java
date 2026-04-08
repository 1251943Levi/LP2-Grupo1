package model;

/**
 * Repositório atua de forma leve (Lazy Loading).
 * É usado para manter o estado da sessão atual e as variáveis globais do sistema.
 */
public class RepositorioDados {

    private Utilizador utilizadorLogado;
    private int anoAtual; // Nova variável global do sistema

    public RepositorioDados() {
        this.utilizadorLogado = null;
        this.anoAtual = 2026; // Ano letivo inicial por defeito quando o sistema arranca
    }

    public Utilizador getUtilizadorLogado() {
        return utilizadorLogado;
    }

    public void setUtilizadorLogado(Utilizador utilizadorLogado) {
        this.utilizadorLogado = utilizadorLogado;
    }

    // --- Getters e Setters para o Ano Atual ---

    public int getAnoAtual() {
        return anoAtual;
    }

    public void setAnoAtual(int anoAtual) {
        this.anoAtual = anoAtual;
    }

    // ------------------------------------------

    public void limparSessao() {
        this.utilizadorLogado = null;
    }
}