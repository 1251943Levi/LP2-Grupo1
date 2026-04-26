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

    private final Avaliacao[] historicoAvaliacoes;
    private int totalAvaliacoes;


    // ---------- CONSTRUTOR ----------
    public PercursoAcademico() {
        this.ucsInscrito = new UnidadeCurricular[15];
        this.totalUcsInscrito = 0;
        this.historicoAvaliacoes = new Avaliacao[100];
        this.totalAvaliacoes  = 0;
    }

    // ---------- MÉTODOS DE LÓGICA E INTEGRIDADE ----------

    /**
     * Inscreve o estudante numa Unidade Curricular, garantindo que não há duplicados.
     */
    public void inscreverEmUc(UnidadeCurricular uc) {
        for (int i = 0; i < totalUcsInscrito; i++) {
            if (ucsInscrito[i].getSigla().equals(uc.getSigla())) return;
        }
        if (totalUcsInscrito < ucsInscrito.length) {
            ucsInscrito[totalUcsInscrito] = uc;
            totalUcsInscrito++;
        }
    }
    /**
     * Regista uma nova avaliação no histórico permanente do estudante.
     */
    public void registarAvaliacao(Avaliacao avaliacao) {
        if (totalAvaliacoes < historicoAvaliacoes.length) {
            historicoAvaliacoes[totalAvaliacoes] = avaliacao;
            totalAvaliacoes++;
        }
    }

    /**
     * Verifica se o estudante tem aproveitamento suficiente (> 60%) para
     * transitar de ano letivo.
     * Regra: para cada UC em que o estudante está inscrito, verifica se
     * existe pelo menos uma avaliação positiva (>= 9.5) no histórico.
     * Se a proporção de UCs aprovadas for superior a 60%, o estudante
     * pode transitar.
     * @return true se o aproveitamento for estritamente superior a 60%.
     */
    public boolean temAproveitamentoSuficiente() {
        if (totalUcsInscrito == 0) return false;

        int aprovadas = 0;
        for (int i = 0; i < totalUcsInscrito; i++) {
            if (ucsInscrito[i] == null) continue;
            String siglaUc = ucsInscrito[i].getSigla();
            for (int j = 0; j < totalAvaliacoes; j++) {
                Avaliacao av = historicoAvaliacoes[j];
                if (av != null && av.getUc() != null
                        && av.getUc().getSigla().equalsIgnoreCase(siglaUc)
                        && av.isAprovado()) {
                    aprovadas++;
                    break;
                }
            }
        }
        return (double) aprovadas / totalUcsInscrito >= 0.60;
    }

    /**
     * Calcula a percentagem de aproveitamento atual (para apresentação ao utilizador).
     * @return Valor entre 0.0 e 1.0.
     */
    public double calcularPercentagemAproveitamento() {
        if (totalUcsInscrito == 0) return 0.0;
        int aprovadas = 0;
        for (int i = 0; i < totalUcsInscrito; i++) {
            if (ucsInscrito[i] == null) continue;
            String siglaUc = ucsInscrito[i].getSigla();
            for (int j = 0; j < totalAvaliacoes; j++) {
                Avaliacao av = historicoAvaliacoes[j];
                if (av != null && av.getUc() != null
                        && av.getUc().getSigla().equalsIgnoreCase(siglaUc)
                        && av.isAprovado()) {
                    aprovadas++;
                    break;
                }
            }
        }
        return (double) aprovadas / totalUcsInscrito;
    }

    /**
     * Limpa as inscrições do ano corrente (usado na transição de ano letivo).
     */
    public void limparInscricoesAtivas() {
        this.ucsInscrito      = new UnidadeCurricular[15];
        this.totalUcsInscrito = 0;
    }

    public Avaliacao[] getHistoricoAvaliacoes() { return historicoAvaliacoes; }
    public int         getTotalAvaliacoes()      { return totalAvaliacoes; }}