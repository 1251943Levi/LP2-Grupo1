package model;

/**
 * Repositório atua de forma leve (Lazy Loading).
 * É usado para manter o estado da sessão atual e as variáveis globais do sistema.
 */
public class RepositorioDados {

    private Utilizador utilizadorLogado;
    private int anoAtual;

    public RepositorioDados() {
        this.utilizadorLogado = null;
        this.anoAtual = 2026;
    }

    public Utilizador getUtilizadorLogado() {
        return utilizadorLogado;
    }

    public void setUtilizadorLogado(Utilizador utilizadorLogado) {
        this.utilizadorLogado = utilizadorLogado;
    }

    public int getAnoAtual() {
        return anoAtual;
    }

    public void setAnoAtual(int anoAtual) {
        this.anoAtual = anoAtual;
    }

    public void limparSessao() {
        this.utilizadorLogado = null;
    }

    public boolean podeAdicionarUc(String siglaCurso, int ano, String pastaBase) {
        int ucsAtuais = utils.ImportadorCSV.contarUcsPorCursoEAno(siglaCurso, ano, pastaBase);
        return ucsAtuais < 5;
    }

    /**
     * Validação de integridade: Impede a edição ou remoção de um curso
     * se este já tiver estudantes matriculados ou UCs (e docentes) associados.
     */
    public boolean podeEditarCurso(String siglaCurso, String pastaBD) {
        int alunosAno1 = utils.ExportadorCSV.contarEstudantesPorCursoEAno(siglaCurso, 1, pastaBD);
        int alunosAno2 = utils.ExportadorCSV.contarEstudantesPorCursoEAno(siglaCurso, 2, pastaBD);
        int alunosAno3 = utils.ExportadorCSV.contarEstudantesPorCursoEAno(siglaCurso, 3, pastaBD);

        if ((alunosAno1 + alunosAno2 + alunosAno3) > 0) {
            return false;
        }
        String ucsDoCurso = utils.ImportadorCSV.listarUcsPorCurso(siglaCurso, pastaBD);

            if (ucsDoCurso != null && !ucsDoCurso.contains("Não existem UCs") && !ucsDoCurso.contains("Não foram encontradas")) {
                return false;
            }
            return true;
    }
}
