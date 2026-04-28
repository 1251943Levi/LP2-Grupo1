package model;

/**
 * Repositório de estado da sessão em curso.
 * Mantém o utilizador autenticado e o ano letivo ativo
 * durante toda a execução da aplicação.
 */
public class RepositorioDados {

    private Utilizador utilizadorLogado;
    private int anoAtual;

    /** Inicializa o repositório sem utilizador autenticado e com o ano letivo 2026. */
    public RepositorioDados() {
        this.utilizadorLogado = null;
        this.anoAtual = 2026;
    }

    /**
     * Define o utilizador autenticado na sessão atual.
     * @param u Utilizador que acabou de fazer login.
     */
    public void setUtilizadorLogado(Utilizador u) { this.utilizadorLogado = u; }

    /** @return Ano letivo ativo no sistema. */
    public int  getAnoAtual() { return anoAtual; }

    /**
     * Atualiza o ano letivo após avançar o ciclo académico.
     * @param ano Novo ano letivo.
     */
    public void setAnoAtual(int ano) { this.anoAtual = ano; }

    /** Termina a sessão removendo o utilizador autenticado. */
    public void limparSessao() {
        this.utilizadorLogado = null;
    }
}