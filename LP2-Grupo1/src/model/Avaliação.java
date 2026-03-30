package model;

public class Avaliacao {

    // ---------- ATRIBUTOS ----------
    private UnidadeCurricular uc;
    private int anoLetivo;

    // Array para guardar até 3 notas da mesma UC
    private double[] resultados;
    private int totalAvaliacoesLancadas;

    // ---------- CONSTRUTOR ----------
    public Avaliacao(UnidadeCurricular uc, int anoLetivo) {
        this.uc = uc;
        this.anoLetivo = anoLetivo;
        this.resultados = new double[3]; // Limite rigoroso de 3 avaliações
        this.totalAvaliacoesLancadas = 0;
    }

    // ---------- MÉTODOS DE LÓGICA ----------

    /**
     * Adiciona uma nova nota (tentativa) à UC.
     * @return true se guardou com sucesso, false se já excedeu as 3 tentativas.
     */
    public boolean adicionarResultado(double nota) {
        if (totalAvaliacoesLancadas < resultados.length) {
            resultados[totalAvaliacoesLancadas] = nota;
            totalAvaliacoesLancadas++;
            return true;
        }
        return false; // Bloqueia a 4ª tentativa!
    }

    /**
     * Calcula a média de todas as tentativas realizadas.
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
     * Verifica se o aluno obteve aprovação.
     * Na faculdade, basta que UMA das tentativas (ex: Recurso) seja >= 10 para aprovar.
     */
    public boolean isAprovado() {
        for (int i = 0; i < totalAvaliacoesLancadas; i++) {
            if (resultados[i] >= 10.0) {
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
}