package bll;

import model.Curso;
import model.Estudante;
import model.RepositorioDados;
import utils.ExportadorCSV;
import utils.ImportadorCSV;
import view.GestorView;

public class GestorBLL {

    private static final String PASTA_BD = "bd";

    /**
     * Executa a lógica de negócio para avançar o ano letivo.
     * Valida quóruns de cursos e impede a transição de alunos com dívidas.
     */
    public void avancarAnoLetivo(RepositorioDados repo, GestorView view) {
        view.mostrarCabecalhoArranqueAnoLetivo();

        String[] cursos = ImportadorCSV.obterListaCursos(PASTA_BD);
        if (cursos.length == 0){
            view.mostrarErroCarregarDados("Cursos");
            return;
        }

        view.mostrarVerificacaoQuorum();

        for (String c : cursos) {
            String siglaCurso = c.split(" - ")[0];
            Curso curso = ImportadorCSV.procurarCurso(siglaCurso, PASTA_BD);
            if (curso == null) continue;

            int alunos1oAno = ExportadorCSV.contarEstudantesPorCursoEAno(siglaCurso, 1, PASTA_BD);
            int alunos2oAno = ExportadorCSV.contarEstudantesPorCursoEAno(siglaCurso, 2, PASTA_BD);
            int alunos3oAno = ExportadorCSV.contarEstudantesPorCursoEAno(siglaCurso, 3, PASTA_BD);

            if (alunos1oAno < 5 && alunos1oAno > 0) {
                view.mostrarErroQuorum(siglaCurso, alunos1oAno);
                curso.setEstado("Inativo");
            } else if (alunos1oAno >= 5 || alunos2oAno >= 1 || alunos3oAno >= 1) {
                view.mostrarSucessoQuorum(siglaCurso);
                curso.setEstado("Ativo");
            } else {
                curso.setEstado("Inativo");
            }
            ExportadorCSV.atualizarCurso(curso, PASTA_BD);
        }


        view.mostrarProcessamentoTransicoes();  // Transição de Ano dos Estudantes
        Estudante[] estudantes = ImportadorCSV.carregarTodosEstudantes(PASTA_BD);

        for (Estudante e : estudantes) {
            if (e == null) continue;

            // IGNORAR AUMENTO DE ANO CURRICULAR SE HOUVER DÍVIDA PENDENTE
            if (e.getSaldoDevedor() != 0.0) {
                // Aluno retido - Mostra o bloqueio e NÃO aumenta o anoCurricular
                view.mostrarBloqueioDivida(e.getNumeroMecanografico(), e.getNome(), e.getAnoCurricular(), e.getSaldoDevedor());
            } else {

                if (e.getAnoCurricular() < 3) {
                    e.setAnoCurricular(e.getAnoCurricular() + 1);
                    view.mostrarTransicaoSucedida(e.getNumeroMecanografico(), e.getAnoCurricular());    // Aluno sem dívidas - Pode transitar
                } else {
                    view.mostrarConclusaoCurso(e.getNumeroMecanografico());
                }
                ExportadorCSV.atualizarEstudante(e, PASTA_BD);
            }
        }
        repo.setAnoAtual(repo.getAnoAtual() + 1);   // Atualizar o ano global no repositório
        view.mostrarSucessoAvancoAno(repo.getAnoAtual());
    }
}
