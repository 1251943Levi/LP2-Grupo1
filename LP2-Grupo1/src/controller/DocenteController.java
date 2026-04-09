package controller;

import model.*;
import view.DocenteView;
import utils.ImportadorCSV;
import utils.ExportadorCSV;

/**
 * Controlador responsável por gerir o painel do Docente.
 */
public class DocenteController {

    private RepositorioDados repo;
    private Docente docente;
    private DocenteView view;

    /** Caminho atualizado para a raiz do projeto */
    private static final String PASTA_BD = "bd";

    public DocenteController(RepositorioDados repo, Docente docente) {
        this.repo = repo;
        this.docente = docente;
        this.view = new DocenteView();
    }

    public void iniciar() {
        boolean correr = true;
        while (correr) {
            int opcao = view.mostrarMenu();
            switch (opcao) {
                case 1:
                    view.mostrarMensagem("A listar UCs lecionadas... (Funcionalidade em desenvolvimento)");
                    break;
                case 2:
                    executarLancamentoNotas();
                    break;
                case 0:
                    correr = false;
                    break;
                default:
                    view.mostrarMensagem("Opção inválida.");
            }
        }
    }

    /**
     * Extraído do Case 2: Gere o processo de procura de aluno e gravação de notas.
     */
    private void executarLancamentoNotas() {
        view.mostrarMensagem("\n--- LANÇAMENTO DE NOTAS ---");

        int numAluno = Integer.parseInt(view.pedirInput("Nº Aluno"));
        String siglaUc = view.pedirInput("Sigla UC");
        int anoLetivo = Integer.parseInt(view.pedirInput("Ano Letivo (ex: 2026)"));

        double nNormal = Double.parseDouble(view.pedirInput("Nota Normal (ou -1 se faltou)"));
        double nRecurso = Double.parseDouble(view.pedirInput("Nota Recurso (ou -1 se faltou)"));
        double nEspecial = Double.parseDouble(view.pedirInput("Nota Especial (ou -1 se faltou)"));

        // Procura o aluno no disco
        Estudante aluno = ImportadorCSV.procurarEstudantePorNumMec(numAluno, PASTA_BD);

        if (aluno != null) {
            // Cria os objetos de domínio para a avaliação
            UnidadeCurricular uc = new UnidadeCurricular(siglaUc, "UC Lançada", 1, docente);
            Avaliacao aval = new Avaliacao(uc, anoLetivo);

            // Adiciona as notas (método da classe Avaliacao)
            aval.adicionarResultado(nNormal);
            aval.adicionarResultado(nRecurso);
            aval.adicionarResultado(nEspecial);

            // Grava diretamente no ficheiro avaliacoes.csv
            ExportadorCSV.adicionarAvaliacao(aval, aluno.getNumeroMecanografico(), PASTA_BD);

            view.mostrarMensagem("Notas lançadas e guardadas com sucesso na base de dados!");
        } else {
            view.mostrarMensagem("ERRO: Aluno com o número " + numAluno + " não encontrado.");
        }
    }
}