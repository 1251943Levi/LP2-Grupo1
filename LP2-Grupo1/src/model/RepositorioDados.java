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

    // Só permite se existirem menos de 5 UCs
    public boolean podeAdicionarUc(String siglaCurso, int ano, String pastaBase) {
        int ucsAtuais = utils.ImportadorCSV.contarUcsPorCursoEAno(siglaCurso, ano, pastaBase);
        return ucsAtuais < 5;
    }

    // Só permite se NÃO tiver estudantes
    public boolean podeEditarCurso(String siglaCurso, String pastaBase) {
        boolean temEstudantes = utils.ImportadorCSV.existeEstudanteNoCurso(siglaCurso, pastaBase);
        return !temEstudantes;
    }
}