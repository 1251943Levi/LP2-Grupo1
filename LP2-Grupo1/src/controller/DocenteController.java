package controller;

import model.*;
import view.DocenteView;
import bll.DocenteBLL;
import utils.CancelamentoException;
import java.util.List;

/**
 * Controlador responsável por gerir as interações do Docente.
 * Atua como intermediário entre a interface (DocenteView) e a lógica de negócio (DocenteBLL).
 *
 * Regras MVC respeitadas:
 *   - NÃO acede a qualquer DAL diretamente.
 *   - NÃO referencia ImportadorCSV nem ExportadorCSV.
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
            view.mostrarAlunoComMedia(e.getNumeroMecanografico(), e.getNome(), media);
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
     * Fluxo de recolha de notas e envio para a DocenteBLL processar o registo.
     * Inclui validação de UC, duplicados e pertença ao docente.
     */
    private void executarLancamentoNotas() {
        view.mostrarCabecalhoLancamentoNotas();
        try {
            int numMec     = view.pedirNumeroAluno();
            String siglaUc = view.pedirSiglaUc();
            int ano        = view.pedirAnoLetivo();
            double n1      = view.pedirNotaNormal();
            double n2      = view.pedirNotaRecurso();
            double n3      = view.pedirNotaEspecial();

            String erro = docenteBll.lancarNota(numMec, siglaUc, ano, n1, n2, n3, docente);
            if (erro == null) {
                view.mostrarSucessoLancamento();
            } else {
                System.out.println(">> " + erro);
            }
        } catch (CancelamentoException e) {
            view.mostrarOperacaoCancelada();
        } catch (Exception e) {
            view.mostrarErroLeituraOpcao();
        }
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