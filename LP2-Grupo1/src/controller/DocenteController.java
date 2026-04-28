package controller;

import model.*;
import utils.Consola;
import view.DocenteView;
import bll.DocenteBLL;
import utils.CancelamentoException;

import java.util.ArrayList;
import java.util.List;
import dal.InscricaoDAL;
import dal.EstudanteDAL;
import model.Estudante;

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
                    case 1: listarMeusAlunos(); break;
                    case 2: executarLancamentoNotas(); break;
                    case 3: executarLancamentoNotasLote(); break;
                    case 4: alterarPassword(); break;
                    case 5: verDadosPessoais(); break;
                    case 6: verMinhasUcs(); break;
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
     * Fluxo de recolha de uma única nota e envio para a DocenteBLL processar o registo.
     * Inclui validação de UC, limites de 3 avaliações e pertença ao docente.
     */
    private void executarLancamentoNotas() {
        view.mostrarCabecalhoLancamentoNotas();
        try {
            int numMec     = view.pedirNumeroAluno();
            String siglaUc = view.pedirSiglaUc();
            int ano        = view.pedirAnoLetivo();

            double notaMomento = view.pedirNotaMomento();

            String erro = docenteBll.lancarNota(numMec, siglaUc, ano, notaMomento, docente);

            if (erro != null) {
                System.out.println("  >> " + erro);
            } else {
                view.mostrarSucessoLancamento();
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

    private void executarLancamentoNotasLote() {
        view.mostrarCabecalhoLancamentoNotasLote();
        try {
            String siglaUc = view.pedirSiglaUc();
            if (!docenteBll.lecionaEstaUC(docente, siglaUc)) {
                view.mostrarErro("Não lecciona a UC " + siglaUc);
                return;
            }
            List<String> alunos = docenteBll.obterAlunosInscritosNaUc(siglaUc);
            if (alunos.isEmpty()) {
                view.mostrarErro("Nenhum aluno inscrito nesta UC.");
                return;
            }
            view.mostrarListaAlunosParaLote(siglaUc, alunos);
            if (!Consola.lerSimNao("Iniciar lançamento sequencial de notas para esta UC?")) {
                view.mostrarOperacaoCancelada();
                return;
            }
            int anoLetivo = view.pedirAnoLetivo();

            // Função que pergunta a nota para cada aluno
            java.util.function.Function<Integer, Double> obterNota = (numMec) -> {
                Estudante e = EstudanteDAL.procurarPorNumMec(numMec, "bd");
                String nome = (e != null) ? e.getNome() : "Desconhecido";
                view.mostrarPedidoNotaParaAluno(numMec, nome);
                try {
                    double nota = view.pedirNotaMomento();
                    return nota;
                } catch (CancelamentoException ex) {
                    return null; // indica salto
                }
            };

            String resultado = docenteBll.lancarNotasEmLote(siglaUc, anoLetivo, docente, obterNota);
            view.mostrarResultadoLote(resultado);
        } catch (CancelamentoException e) {
            view.mostrarOperacaoCancelada();
        } catch (Exception e) {
            e.printStackTrace();
            view.mostrarErroLeituraOpcao();
        }
    }
}