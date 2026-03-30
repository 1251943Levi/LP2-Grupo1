package model;

public class PercursoAcademico {

    // ---------- ATRIBUTOS ----------
    // UCs que o aluno está a frequentar no ano atual
    private UnidadeCurricular[] ucsInscrito;
    private int totalUcsInscrito;

    // Histórico de todas as avaliações (pautas) do aluno ao longo do curso
    private Avaliacao[] historicoAvaliacoes;
    private int totalAvaliacoes;

    // ---------- CONSTRUTOR ----------
    public PercursoAcademico() {
        this.ucsInscrito = new UnidadeCurricular[15]; // Limite de inscrições ativas
        this.totalUcsInscrito = 0;

        this.historicoAvaliacoes = new Avaliacao[100]; // Limite de histórico do curso
        this.totalAvaliacoes = 0;
    }

    // ---------- MÉTODOS DE LÓGICA ----------

    public boolean inscreverEmUc(UnidadeCurricular uc) {
        // Regra de segurança: Verificar se o aluno já não está inscrito na mesma UC
        for (int i = 0; i < totalUcsInscrito; i++) {
            if (ucsInscrito[i].getSigla().equals(uc.getSigla())) {
                return false; // Já está inscrito!
            }
        }

        if (totalUcsInscrito < ucsInscrito.length) {
            ucsInscrito[totalUcsInscrito] = uc;
            totalUcsInscrito++;
            return true;
        }
        return false; // Atingiu o limite máximo de inscrições
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