package model;

public class PercursoAcademico {

    // ---------- ATRIBUTOS ----------
    private UnidadeCurricular[] ucsInscrito;
    private int totalUcsInscrito;

    private Avaliacao[] historicoAvaliacoes;
    private int totalAvaliacoes;

    // ---------- CONSTRUTOR ----------
    public PercursoAcademico() {
        this.ucsInscrito = new UnidadeCurricular[15];
        this.totalUcsInscrito = 0;

        this.historicoAvaliacoes = new Avaliacao[100];
        this.totalAvaliacoes = 0;
    }

    // ---------- MÉTODOS DE LÓGICA ----------

    public boolean inscreverEmUc(UnidadeCurricular uc) {
        for (int i = 0; i < totalUcsInscrito; i++) {
            if (ucsInscrito[i].getSigla().equals(uc.getSigla())) {
                return false;
            }
        }

        if (totalUcsInscrito < ucsInscrito.length) {
            ucsInscrito[totalUcsInscrito] = uc;
            totalUcsInscrito++;
            return true;
        }
        return false;
    }

    public boolean registarAvaliacao(Avaliacao avaliacao) {
        if (totalAvaliacoes < historicoAvaliacoes.length) {
            historicoAvaliacoes[totalAvaliacoes] = avaliacao;
            totalAvaliacoes++;
            return true;
        }
        return false;
    }

    /**
     * Limpa as inscrições atuais.
     * Muito útil para quando o Controller executar a "Passagem de Ano" no sistema!
     */
    public void limparInscricoesAtivas() {
        this.ucsInscrito = new UnidadeCurricular[15];
        this.totalUcsInscrito = 0;
    }

    // ---------- GETTERS ----------
    public UnidadeCurricular[] getUcsInscrito() { return ucsInscrito; }
    public int getTotalUcsInscrito() { return totalUcsInscrito; }
    public Avaliacao[] getHistoricoAvaliacoes() { return historicoAvaliacoes; }
    public int getTotalAvaliacoes() { return totalAvaliacoes; }
}