package model;

/**
 * Mantém o estado da sessão atual e as variáveis globais do sistema.
 * Atua como um Singleton de facto durante a execução do programa.
 */
public class RepositorioDados {

    private Utilizador utilizadorLogado;
    private int anoAtual;

    public RepositorioDados() {
        this.utilizadorLogado = null;
        this.anoAtual = 2026;
    }

    public void setUtilizadorLogado(Utilizador u) { this.utilizadorLogado = u; }
    public int  getAnoAtual() { return anoAtual; }
    public void setAnoAtual(int ano) { this.anoAtual = ano; }

    public void limparSessao() {
        this.utilizadorLogado = null;
    }
}