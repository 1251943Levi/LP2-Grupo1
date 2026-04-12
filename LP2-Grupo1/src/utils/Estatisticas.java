package utils;

import model.Avaliacao;
import model.Estudante;

public class Estatisticas {

    private Estatisticas() {}

    public static String calcularMediaGlobal(String pastaBase) {
        Estudante[] estudantes = ImportadorCSV.carregarTodosEstudantes(pastaBase);
        double soma = 0;
        int totalNotas = 0;

        for (Estudante e : estudantes) {
            if (e == null) continue;
            for (int i = 0; i < e.getPercurso().getTotalAvaliacoes(); i++) {
                Avaliacao av = e.getPercurso().getHistoricoAvaliacoes()[i];
                for (int j = 0; j < av.getTotalAvaliacoesLancadas(); j++) {
                    soma += av.getResultados()[j];
                    totalNotas++;
                }
            }
        }

        if (totalNotas == 0) return "Sem notas registadas.";
        return "Média Global Institucional: " + String.format("%.2f", (soma / totalNotas));
    }

    public static String obterMelhorAluno(String pastaBase) {
        Estudante[] estudantes = ImportadorCSV.carregarTodosEstudantes(pastaBase);
        Estudante melhor = null;
        double maiorMedia = -1;

        for (Estudante e : estudantes) {
            if (e == null || e.getPercurso().getTotalAvaliacoes() == 0) continue;
            double somaMedias = 0;
            for (int i = 0; i < e.getPercurso().getTotalAvaliacoes(); i++) {
                somaMedias += e.getPercurso().getHistoricoAvaliacoes()[i].calcularMedia();
            }
            double mediaAluno = somaMedias / e.getPercurso().getTotalAvaliacoes();
            if (mediaAluno > maiorMedia) {
                maiorMedia = mediaAluno;
                melhor = e;
            }
        }

        if (melhor != null) return "Melhor Aluno: " + melhor.getNome() + " | Média: " + String.format("%.2f", maiorMedia);
        return "Nenhum aluno avaliado.";
    }

}