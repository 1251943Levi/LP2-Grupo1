package model;

/**
 * Representa o registo histórico e atual de um estudante.
 * Gere as inscrições ativas em Unidades Curriculares e armazena todas
 * as avaliações obtidas ao longo do tempo.
 */
public class PercursoAcademico {

    // ---------- ATRIBUTOS ----------
    private UnidadeCurricular[] ucsInscrito;
    private int totalUcsInscrito;

    private Avaliacao[] historicoAvaliacoes;
    private int totalAvaliacoes;

    // ---------- CONSTRUTOR ----------
    /**
     * Inicializa um novo percurso académico com limites para inscrições anuais e histórico.
     */
    public PercursoAcademico() {
        this.ucsInscrito = new UnidadeCurricular[15]; // Limite de UCs por ano letivo
        this.totalUcsInscrito = 0;

        this.historicoAvaliacoes = new Avaliacao[100]; // Histórico total do curso
        this.totalAvaliacoes = 0;
    }

    // ---------- MÉTODOS DE LÓGICA E INTEGRIDADE ----------

    /**
     * Inscreve o estudante numa Unidade Curricular, garantindo que não há duplicados.
     * @param uc A Unidade Curricular a inscrever.
     * @return true se a inscrição foi bem-sucedida; false se já estiver inscrito ou se o limite foi atingido.
     */
    public boolean inscreverEmUc(UnidadeCurricular uc) {
        // Validação de duplicados
        for (int i = 0; i < totalUcsInscrito; i++) {
            if (ucsInscrito[i].getSigla().equals(uc.getSigla())) {
                return false;
            }
        }

        // Validação de limite físico
        if (totalUcsInscrito < ucsInscrito.length) {
            ucsInscrito[totalUcsInscrito] = uc;
            totalUcsInscrito++;
            return true;
        }
        return false;
    }

    /**
     * Regista uma nova avaliação no histórico permanente do estudante.
     * @param avaliacao O objeto de avaliação a registar.
     * @return true se gravado com sucesso no histórico.
     */
    public boolean registarAvaliacao(Avaliacao avaliacao) {
        if (totalAvaliacoes < historicoAvaliacoes.length) {
            historicoAvaliacoes[totalAvaliacoes] = avaliacao;
            totalAvaliacoes++;
            return true;
        }
        return false;
    }

    /**
     * Limpa as inscrições do ano corrente.
     * Este método é essencial para o processo de transição de ano letivo,
     * permitindo que o estudante comece um novo ano com a lista de UCs vazia.
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