package controller;

import model.Estudante;
import model.RepositorioDados;
import view.EstudanteView;
import utils.ExportadorCSV;
import utils.SegurancaPasswords;

/**
 * Controlador responsável por gerir o painel do Estudante.
 * Permite a visualização de dados pessoais e a atualização do perfil e credenciais,
 * gravando as alterações diretamente nos ficheiros correspondentes (On-Demand).
 */
public class EstudanteController {

    private RepositorioDados repositorio;
    private Estudante estudanteAtivo;
    private EstudanteView view;
    private static final String PASTA_BD = "bd";

    public EstudanteController(RepositorioDados repositorio, Estudante estudanteAtivo) {
        this.repositorio = repositorio;
        this.estudanteAtivo = estudanteAtivo;
        this.view = new EstudanteView();
    }

    public void iniciar() {
        boolean aExecutar = true;

        while (aExecutar) {
            try {
                int opcao = view.mostrarMenuPrincipal();

                switch (opcao) {
                    case 1:
                        visualizarDadosPessoais();
                        break;
                    case 2:
                        atualizarDadosPessoais();
                        break;
                    case 3:
                        alterarPassword();
                        break;
                    case 4:
                        consultarDadosFinanceiros();
                        break;
                    case 0:
                        view.mostrarDespedida();
                        repositorio.limparSessao(); // Garante que a sessão é limpa ao sair
                        aExecutar = false;
                        break;
                    default:
                        view.mostrarOpcaoInvalida();
                }
            } catch (Exception e) {
                view.mostrarErroLeitura();
            }
        }
    }

    private void visualizarDadosPessoais() {
        // Envia o modelo diretamente para a View formatar
        view.mostrarDadosPessoais(estudanteAtivo);
    }

    private void atualizarDadosPessoais() {
        String novaMorada = view.pedirNovaMorada();

        if (!novaMorada.isEmpty()) {
            estudanteAtivo.setMorada(novaMorada);
            ExportadorCSV.atualizarEstudante(estudanteAtivo, PASTA_BD);
            view.mostrarSucessoAtualizacaoMorada();
        } else {
            view.mostrarSemAlteracaoMorada();
        }
    }

    private void alterarPassword() {
        String novaPass = view.pedirNovaPassword();

        if (!novaPass.isEmpty()) {
            String passSegura = SegurancaPasswords.gerarCredencialMista(novaPass);

            estudanteAtivo.setPassword(passSegura);
            ExportadorCSV.atualizarPasswordCentralizada(estudanteAtivo.getEmail(), passSegura, PASTA_BD);

            view.mostrarSucessoAtualizacaoPassword();
        } else {
            view.mostrarCancelamentoPassword();
        }
    }

    private void consultarDadosFinanceiros() {
        double divida = estudanteAtivo.getSaldoDevedor();
        view.mostrarSaldoDevedor(divida);

        if (divida > 0) {
            String resp = view.pedirConfirmacaoPagamento();
            if (resp.equalsIgnoreCase("S")) {
                estudanteAtivo.efetuarPagamento(divida);
                ExportadorCSV.atualizarEstudante(estudanteAtivo, PASTA_BD);
                view.mostrarSucessoPagamento();
            }
        } else {
            view.mostrarSemPagamentosPendentes();
        }
    }
}