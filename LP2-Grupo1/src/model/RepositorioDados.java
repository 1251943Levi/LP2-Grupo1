package model;

/**
 * Mantém o estado da sessão atual e as variáveis globais do sistema.
 * Atua como um Singleton de facto durante a execução do programa.
 */
public class RepositorioDados {

    private Utilizador utilizadorLogado;
    private int anoAtual;

    /**
     * Inicializa o repositório com valores padrão.
     */
    public RepositorioDados() {
        this.utilizadorLogado = null;
        this.anoAtual = 2026; // Valor inicial do sistema
    }

    // ---------- GETTERS E SETTERS ----------
    public Utilizador getUtilizadorLogado() { return utilizadorLogado; }
    public void setUtilizadorLogado(Utilizador utilizadorLogado) { this.utilizadorLogado = utilizadorLogado; }

    public int getAnoAtual() { return anoAtual; }
    public void setAnoAtual(int anoAtual) { this.anoAtual = anoAtual; }

    /**
     * Termina a sessão atual, removendo a referência do utilizador.
     */
    public void limparSessao() {
        this.utilizadorLogado = null;
    }
}