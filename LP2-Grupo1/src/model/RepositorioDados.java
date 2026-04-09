package model;

/**
 * Repositório atua de forma leve (Lazy Loading).
 * É usado para manter o estado da sessão atual.
 */
public class RepositorioDados {

    private Utilizador utilizadorLogado;

    public RepositorioDados() {
        this.utilizadorLogado = null;
    }

    public Utilizador getUtilizadorLogado() {
        return utilizadorLogado;
    }

    public void setUtilizadorLogado(Utilizador utilizadorLogado) {
        this.utilizadorLogado = utilizadorLogado;
    }

    public void limparSessao() {
        this.utilizadorLogado = null;
    }
}