package controller;

import model.Estudante;
import model.RepositorioDados;
import view.EstudanteView;
import bll.EstudanteBLL;
import utils.CancelamentoException;

/**
 * Controlador que gere o fluxo de ecrãs e interações do Estudante.
 */
public class EstudanteController {

    private RepositorioDados repositorio;
    private Estudante estudanteAtivo;
    private EstudanteView view;
    private EstudanteBLL bll;

    public EstudanteController(RepositorioDados repositorio, Estudante estudanteAtivo) {
        this.repositorio = repositorio;
        this.estudanteAtivo = estudanteAtivo;
        this.view = new EstudanteView();
        this.bll = new EstudanteBLL();
    }

    /**
     * Inicia o loop do menu principal do estudante.
     */
    public void iniciar() {
        boolean aExecutar = true;
        while (aExecutar) {
            try {
                int opcao = view.mostrarMenuPrincipal();
                switch (opcao) {
                    case 1: visualizarDadosPessoais(); break;
                    case 2: atualizarDadosPessoais(); break;
                    case 3: alterarPassword(); break;
                    case 4: consultarDadosFinanceiros(); break;
                    case 0:
                        view.mostrarDespedida();
                        repositorio.limparSessao();
                        aExecutar = false;
                        break;
                    default: view.mostrarOpcaoInvalida();
                }
            } catch (Exception e) {
                view.mostrarErroLeitura();
            }
        }
    }

    private void visualizarDadosPessoais() {
        view.mostrarDadosPessoais(estudanteAtivo);
    }

    /**
     * Gere o fluxo de alteração de morada através da BLL.
     * A operação pode ser cancelada durante a introdução da nova morada.
     */
    private void atualizarDadosPessoais() {
        try {
            String novaMorada = view.pedirNovaMorada();
            if (!novaMorada.isEmpty()) {
                bll.atualizarMorada(estudanteAtivo, novaMorada);
                view.mostrarSucessoAtualizacaoMorada();
            } else {
                view.mostrarSemAlteracaoMorada();
            }
        } catch (CancelamentoException e) {
            view.mostrarOperacaoCancelada();
        }
    }

    /**
     * Gere o fluxo de alteração de password.
     */
    private void alterarPassword() {
        String novaPass = view.pedirNovaPassword();
        if (!novaPass.isEmpty()) {
            bll.alterarPassword(estudanteAtivo, novaPass);
            view.mostrarSucessoAtualizacaoPassword();
        } else {
            view.mostrarCancelamentoPassword();
        }
    }

    /**
     * Coordena a lógica de consulta e pagamento de propinas.
     * A operação de pagamento parcial pode ser cancelada durante a introdução do valor.
     */
    private void consultarDadosFinanceiros() {
        double divida = estudanteAtivo.getSaldoDevedor();
        view.mostrarSaldoDevedor(divida);

        if (divida > 0) {
            int opcao = view.pedirTipoPagamento();
            double valorAPagar = 0.0;

            if (opcao == 1) {
                valorAPagar = divida;
            } else if (opcao == 2) {
                try {
                    valorAPagar = view.pedirValorPagamentoParcial(divida);
                } catch (CancelamentoException e) {
                    view.mostrarOperacaoCancelada();
                    return;
                }
            } else {
                return;
            }

            if (bll.processarPagamento(estudanteAtivo, valorAPagar)) {
                view.mostrarSucessoPagamento();
            } else {
                view.mostrarErroValorInvalido();
            }
        } else {
            view.mostrarSemPagamentosPendentes();
        }
    }
}