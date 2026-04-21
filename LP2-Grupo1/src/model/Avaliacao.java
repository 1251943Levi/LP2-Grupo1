package model;

/**
 * Classe que representa o registo de avaliação de um estudante numa Unidade Curricular.
 * Armazena as notas das várias épocas (Normal, Recurso, Especial) e determina o sucesso académico.
 */
public class Avaliacao {

    private UnidadeCurricular uc;
    private int anoLetivo;
    private double[] resultados;
    private int totalAvaliacoesLancadas;

    /**
     * Construtor da Avaliação.
     * @param uc A Unidade Curricular a que esta avaliação se refere.
     * @param anoLetivo O ano em que a avaliação foi realizada.
     */
    public Avaliacao(UnidadeCurricular uc, int anoLetivo) {
        this.uc = uc;
        this.anoLetivo = anoLetivo;
        this.resultados = new double[3]; // Representa Época Normal, Recurso e Especial
        this.totalAvaliacoesLancadas = 0;
    }

    // ---------- MÉTODOS DE INTEGRIDADE E LÓGICA ----------

    /**
     * Adiciona uma nova nota ao histórico desta UC.
     * @param nota Valor da nota (0 a 20).
     * @return true se guardou com sucesso, false se já excedeu o limite de 3 tentativas.
     */
    public boolean adicionarResultado(double nota) {
        if (totalAvaliacoesLancadas < resultados.length) {
            resultados[totalAvaliacoesLancadas] = nota;
            totalAvaliacoesLancadas++;
            return true;
        }
        return false;
    }

    /**
     * Calcula a média aritmética de todas as tentativas realizadas pelo aluno nesta UC.
     * @return A média das notas lançadas.
     */
    public double calcularMedia() {
        if (totalAvaliacoesLancadas == 0) return 0.0;

        double soma = 0;
        for (int i = 0; i < totalAvaliacoesLancadas; i++) {
            soma += resultados[i];
        }
        return soma / totalAvaliacoesLancadas;
    }

    /**
     * Verifica se o aluno obteve aprovação na disciplina.
     * A aprovação é ditada por pelo menos um resultado igual ou superior a 9.5 (arredondado para 10).
     * @return true se aprovado em qualquer uma das épocas.
     */
    public boolean isAprovado() {
        for (int i = 0; i < totalAvaliacoesLancadas; i++) {
            if (resultados[i] >= 9.5) { // Lógica académica de arredondamento
                return true;
            }
        }
        return false;
    }

    // ---------- GETTERS ----------
    public UnidadeCurricular getUc() { return uc; }
    public int getAnoLetivo() { return anoLetivo; }
    public double[] getResultados() { return resultados; }
    public int getTotalAvaliacoesLancadas() { return totalAvaliacoesLancadas; }

    // ---------- SETTERS ----------
    public void setUc(UnidadeCurricular uc) { this.uc = uc; }
    public void setAnoLetivo(int anoLetivo) { this.anoLetivo = anoLetivo; }
}