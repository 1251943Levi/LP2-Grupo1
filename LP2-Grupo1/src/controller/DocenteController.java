package controller;

import model.*;
import view.DocenteView;
import bll.DocenteBLL;
import utils.CancelamentoException;
import java.util.List;

/**
 * Controlador responsável por gerir as interações do Docente.
 * Atua como intermediário entre a interface (DocenteView) e a lógica de negócio (DocenteBLL).
 */
public class DocenteController {

    private final RepositorioDados repo;
    private final Docente docente;
    private final DocenteView view;
    private final DocenteBLL docenteBll;

    public DocenteController(RepositorioDados repo, Docente docente) {
        this.repo        = repo;
        this.docente     = docente;
        this.view        = new DocenteView();
        this.docenteBll  = new DocenteBLL();
    }

    public void iniciar() {
        boolean correr = true;
        while (correr) {
            try {
                int opcao = view.mostrarMenu();
                switch (opcao) {
                    case 1: listarMeusAlunos();          break;
                    case 2: executarLancamentoNotas();   break;
                    case 3: alterarPassword();           break;
                    case 4: verDadosPessoais();          break;
                    case 5: verMinhasUcs();              break;
                    case 0:
                        view.mostrarDespedida();
                        repo.limparSessao();
                        correr = false;
                        break;
                    default:
                        view.mostrarOpcaoInvalida();
                }
            } catch (Exception e) {
                view.mostrarErroLeituraOpcao();
            }
        }
    }

    /**
     * Lista os alunos do docente com a respetiva média académica.
     * Toda a matemática e filtragem é feita na DocenteBLL.
     */

    private void listarMeusAlunos() {
        view.mostrarCabecalhoAlunos();
        List<Object[]> alunos = docenteBll.obterAlunosDoDocenteComMedia(docente);

        if (alunos.isEmpty()) {
            view.mostrarErroCarregarAlunos();
            return;
        }
        for (Object[] par : alunos) {
            Estudante e  = (Estudante) par[0];
            double media = (double)    par[1];
            String ucs   = (String)    par[2];

            view.mostrarAlunoComMedia(e.getNumeroMecanografico(), e.getNome(), media, ucs);
        }
    }

    /**
     * Mostra a ficha pessoal do docente autenticado.
     */
    private void verDadosPessoais() {
        view.mostrarFichaDocente(docente);
    }

    /**
     * Lista as Unidades Curriculares atribuídas ao docente.
     */
    private void verMinhasUcs() {
        view.mostrarUcsDocente(docente);
    }

    /**
     * Fluxo de recolha de uma única nota e envio para a DocenteBLL processar o registo.
     * Inclui validação de UC, limites de 3 avaliações e pertença ao docente.
     */
    private void executarLancamentoNotas() {
        view.mostrarCabecalhoLancamentoNotas();
        try {
            if (utils.Consola.lerSimNao("Deseja listar as suas Unidades Curriculares disponíveis?")) {
                verMinhasUcs();
            }
            String siglaUc = view.pedirSiglaUc();

            if (!docenteBll.lecionaEstaUC(docente, siglaUc)) {
                System.out.println("  [ERRO] Não leciona a UC '" + siglaUc + "'.");
                return;
            }

            if (utils.Consola.lerSimNao("Deseja listar os alunos inscritos em " + siglaUc + "?")) {
                listarAlunosPorUC(siglaUc);
            }

            int numMec = -1;
            boolean alunoValido = false;
            while (!alunoValido) {
                numMec = view.pedirNumeroAluno();
                if (dal.EstudanteDAL.procurarPorNumMec(numMec, "bd") != null) {
                    alunoValido = true;
                    break;
                } else {
                    System.out.println("  [ERRO] Aluno com nº " + numMec + " não encontrado. Tente novamente.");
                }
            }

            int anoAtivo = repo.getAnoAtual();
            System.out.println("  Ano Letivo: " + anoAtivo + " (Assumido pelo sistema)");

            double notaMomento = -1;
            boolean notaValida = false;
            while (!notaValida) {
                notaMomento = view.pedirNotaMomento();
                if ((notaMomento >= 0 && notaMomento <= 20) || notaMomento == -1) {
                    notaValida = true;
                    break;
                } else {
                    System.out.println("  [ERRO] Nota inválida. Insira um valor entre 0 e 20 (ou -1 para falta).");
                }
            }

            String erro = docenteBll.lancarNota(numMec, siglaUc, anoAtivo, notaMomento, docente);

            if (erro != null) {
                System.out.println("  >> " + erro);
            } else {
                view.mostrarSucessoLancamento();
            }

        } catch (utils.CancelamentoException e) {
            view.mostrarOperacaoCancelada();
        } catch (Exception e) {
            view.mostrarErroLeituraOpcao();
        }
    }

    /**
     * Método auxiliar para listar apenas os alunos inscritos na UC selecionada.
     */
    private void listarAlunosPorUC(String siglaUC) {
        List<Object[]> todosAlunos = docenteBll.obterAlunosDoDocenteComMedia(docente);
        boolean encontrou = false;

        System.out.println("\n  --- Alunos Inscritos em " + siglaUC.toUpperCase() + " ---");
        for (Object[] par : todosAlunos) {
            model.Estudante e = (model.Estudante) par[0];
            String ucsInscritas = (String) par[2];

            if (ucsInscritas.toUpperCase().contains(siglaUC.toUpperCase())) {
                view.mostrarAlunoSimples(e.getNumeroMecanografico(), e.getNome());
                encontrou = true;
            }
        }

        if (!encontrou) {
            System.out.println("  Nenhum aluno encontrado inscrito nesta Unidade Curricular.");
        }
        System.out.println();
    }

    private void alterarPassword() {
        try {
            String novaPass = view.pedirNovaPassword();
            docenteBll.alterarPassword(docente, novaPass);
            view.mostrarSucessoAlteracaoPassword();
        } catch (CancelamentoException e) {
            view.mostrarCancelamentoPassword();
        }
    }
}