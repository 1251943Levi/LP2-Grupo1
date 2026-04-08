package controller;

import model.*;
import view.DocenteView;
import utils.ImportadorCSV;
import utils.ExportadorCSV;

/**
 * Controlador responsável por gerir o painel do Docente.
 * Permite ao professor lançar notas a alunos pesquisando-os diretamente
 * no disco e guardando as avaliações de forma eficiente (On-Demand).
 */
public class DocenteController {

    /** Repositório usado para manter a sessão ativa. */
    private RepositorioDados repo;

    /** O docente logado no sistema. */
    private Docente docente;

    /** A vista associada ao docente. */
    private DocenteView view;

    /** Caminho da diretoria da base de dados. */
    private static final String PASTA_BD = "LP2-Grupo1/bd";

    /**
     * Construtor do DocenteController.
     * @param repo Repositório de sessão.
     * @param docente Objeto do docente ativo.
     */
    public DocenteController(RepositorioDados repo, Docente docente) {
        this.repo = repo;
        this.docente = docente;
        this.view = new DocenteView();
    }

    /**
     * Inicia o ciclo principal do menu do Docente.
     */
    public void iniciar() {

        boolean correr = true;

        while (correr) {

            int opcao = view.mostrarMenu();

            switch (opcao) {

                case 1:
                    view.mostrarMensagem("A listar UCs lecionadas... (Funcionalidade em desenvolvimento)");
                    break;

                case 2:
                    view.mostrarMensagem("\n--- LANÇAMENTO DE NOTAS ---");

                    int numAluno = Integer.parseInt(view.pedirInput("Nº Aluno"));
                    String siglaUc = view.pedirInput("Sigla UC");
                    int anoLetivo = Integer.parseInt(view.pedirInput("Ano Letivo (ex: 2026)"));

                    double nNormal = Double.parseDouble(view.pedirInput("Nota Normal (ou -1 se faltou)"));
                    double nRecurso = Double.parseDouble(view.pedirInput("Nota Recurso (ou -1 se faltou)"));
                    double nEspecial = Double.parseDouble(view.pedirInput("Nota Especial (ou -1 se faltou)"));

                    Estudante aluno = ImportadorCSV.procurarEstudantePorNumMec(numAluno, PASTA_BD);

                    if (aluno != null) {

                        UnidadeCurricular uc = new UnidadeCurricular(siglaUc, "UC Lançada", 1, docente);
                        Avaliacao aval = new Avaliacao(uc, anoLetivo);

                        aval.adicionarResultado(nNormal);
                        aval.adicionarResultado(nRecurso);
                        aval.adicionarResultado(nEspecial);

                        ExportadorCSV.adicionarAvaliacao(aval, aluno.getNumeroMecanografico(), PASTA_BD);

                        view.mostrarMensagem("Notas lançadas e guardadas com sucesso na base de dados!");

                    } else {
                        view.mostrarMensagem("ERRO: Aluno com o número " + numAluno + " não encontrado.");
                    }

                    break;

                case 0:
                    correr = false;
                    break;

                default:
                    view.mostrarMensagem("Opção inválida.");
            }
        }
    }
}