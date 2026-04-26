package utils;

import bll.EstudanteBLL;
import model.Avaliacao;
import model.Estudante;

import java.util.List;

/**
 * Motor de cálculo de estatísticas institucionais.
 *
 * Responsabilidades:
 *   - Calcular médias e identificar o melhor aluno.
 *   - Devolver DADOS BRUTOS (números e objetos) — nunca Strings formatadas.
 *   - A formatação para ecrã é sempre responsabilidade da GestorView.
 *
 * É a ÚNICA fonte de verdade para cálculos estatísticos.
 * A GestorBLL delega aqui e repassa os resultados ao Controller/View.
 */
public class Estatisticas {

    private Estatisticas() {}

    // ------------------------------------------------------------------ MÉDIA GLOBAL

    /**
     * Calcula a soma de todas as notas lançadas e o total de avaliações.
     *
     * @return double[] onde [0] = soma das notas, [1] = total de notas.
     *         Devolve null se não existirem estudantes.
     *         Devolve {0, 0} se existirem estudantes mas sem notas lançadas.
     */
    public static double[] calcularDadosMediaGlobal() {
        List<Estudante> estudantes = new EstudanteBLL().carregarTodosCompleto();
        if (estudantes == null || estudantes.isEmpty()) return null;

        double soma    = 0;
        int totalNotas = 0;

        for (Estudante e : estudantes) {
            if (e == null) continue;
            for (int i = 0; i < e.getPercurso().getTotalAvaliacoes(); i++) {
                Avaliacao av = e.getPercurso().getHistoricoAvaliacoes()[i];
                if (av == null) continue;
                for (int j = 0; j < av.getTotalAvaliacoesLancadas(); j++) {
                    soma += av.getResultados()[j];
                    totalNotas++;
                }
            }
        }

        return new double[]{soma, totalNotas};
    }

    // ------------------------------------------------------------------ MELHOR ALUNO

    /**
     * Determina o aluno com a maior média académica global.
     *
     * @return Object[] onde [0] = Estudante, [1] = Double (média calculada).
     *         Devolve null se não existirem alunos avaliados.
     */
    public static Object[] calcularMelhorAluno() {
        List<Estudante> estudantes = new EstudanteBLL().carregarTodosCompleto();
        if (estudantes == null || estudantes.isEmpty()) return null;

        Estudante melhor     = null;
        double    maiorMedia = -1;

        for (Estudante e : estudantes) {
            if (e == null || e.getPercurso().getTotalAvaliacoes() == 0) continue;

            double somaMedias = 0;
            int    total      = e.getPercurso().getTotalAvaliacoes();

            for (int i = 0; i < total; i++) {
                Avaliacao av = e.getPercurso().getHistoricoAvaliacoes()[i];
                if (av != null) somaMedias += av.calcularMedia();
            }

            double mediaAluno = somaMedias / total;
            if (mediaAluno > maiorMedia) {
                maiorMedia = mediaAluno;
                melhor     = e;
            }
        }

        return melhor != null ? new Object[]{melhor, maiorMedia} : null;
    }
}