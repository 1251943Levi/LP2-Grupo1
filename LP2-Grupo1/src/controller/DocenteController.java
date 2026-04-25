package controller;

import model.*;
import view.DocenteView;
import bll.DocenteBLL;
import utils.ImportadorCSV;
import utils.ExportadorCSV;
import utils.SegurancaPasswords;
import utils.CancelamentoException;

public class DocenteController {
    private RepositorioDados repo;
    private Docente docente;
    private DocenteView view;
    private DocenteBLL bll;

    private static final String PASTA_BD = "bd";

    public DocenteController(RepositorioDados repo, Docente docente) {
        this.repo = repo;
        this.docente = docente;
        this.view = new DocenteView();
        this.bll = new DocenteBLL();
        // Inicializa as UCs que o docente leciona carregando do CSV
        ImportadorCSV.carregarUcsDoDocente(this.docente, PASTA_BD);
    }

    public void iniciar() {
        boolean correr = true;
        while (correr) {
            try {
                int opcao = view.mostrarMenu();
                switch (opcao) {
                    case 1: listarMeusAlunos(); break;
                    case 2: executarLancamentoNotas(); break;
                    case 3: alterarPassword(); break;
                    case 0: correr = false; break;
                    default: view.mostrarOpcaoInvalida();
                }
            } catch (Exception e) {
                view.mostrarErroLeituraOpcao();
            }
        }
    }

    private void listarMeusAlunos() {
        view.mostrarCabecalhoAlunos();
        Estudante[] todos = ImportadorCSV.carregarTodosEstudantes(PASTA_BD);
        double somaDocente = 0;
        int totalNotasDocente = 0;
        boolean encontrou = false;

        if (todos == null) {
            view.mostrarErroCarregarAlunos();
            return;
        }

        for (Estudante e : todos) {
            if (e == null || e.getPercurso() == null) continue;
            boolean alunoDoDocente = false;

            for (int i = 0; i < e.getPercurso().getTotalUcsInscrito(); i++) {
                if (bll.lecionaEstaUC(docente, e.getPercurso().getUcsInscrito()[i].getSigla())) {
                    alunoDoDocente = true;
                    break;
                }
            }

            if (alunoDoDocente) {
                encontrou = true;
                view.mostrarAluno(e.getNumeroMecanografico(), e.getNome());

                for (int i = 0; i < e.getPercurso().getTotalAvaliacoes(); i++) {
                    Avaliacao av = e.getPercurso().getHistoricoAvaliacoes()[i];
                    if (av != null && bll.lecionaEstaUC(docente, av.getUc().getSigla())) {
                        for (int j = 0; j < av.getTotalAvaliacoesLancadas(); j++) {
                            somaDocente += av.getResultados()[j];
                            totalNotasDocente++;
                        }
                    }
                }
            }
        }

        if (!encontrou) view.mostrarSemAlunos();
        else if (totalNotasDocente > 0) view.mostrarMedia(somaDocente / totalNotasDocente);
    }

    /**
     * Fluxo de recolha de notas e envio para a BLL processar o registo.
     * A operação pode ser cancelada durante a introdução dos dados.
     */
    private void executarLancamentoNotas() {
        view.mostrarCabecalhoLancamentoNotas();

        try {
            int numMec = view.pedirNumeroAluno();
            String siglaUc = view.pedirSiglaUc();
            int ano = view.pedirAnoLetivo();
            double n1 = view.pedirNotaNormal();
            double n2 = view.pedirNotaRecurso();
            double n3 = view.pedirNotaEspecial();

            if (bll.lancarNota(numMec, siglaUc, ano, n1, n2, n3, docente)) {
                view.mostrarSucessoLancamento();
            } else {
                view.mostrarErroAlunoNaoEncontrado(numMec);
            }
        } catch (CancelamentoException e) {
            view.mostrarOperacaoCancelada();
        } catch (Exception e) {
            view.mostrarErroLeituraOpcao();
        }
    }

    private void alterarPassword() {
        String novaPass = view.pedirNovaPassword();
        if (!novaPass.trim().isEmpty()) {
            bll.alterarPassword(docente, novaPass);
            view.mostrarSucessoAlteracaoPassword();
        } else {
            view.mostrarCancelamentoPassword();
        }
    }
}