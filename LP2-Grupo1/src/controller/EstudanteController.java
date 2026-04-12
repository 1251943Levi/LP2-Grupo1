package controller;

import model.Estudante;
import model.RepositorioDados;
import view.EstudanteView;
import utils.ExportadorCSV;
import utils.SegurancaPasswords;

/**
 * Controlador responsável por gerir o painel do Estudante.
 */
public class EstudanteController {

    private RepositorioDados repositorio;
    private Estudante estudanteAtivo;
    private EstudanteView view;

    /** Caminho atualizado para a raiz do projeto */
    private static final String PASTA_BD = "LP2-Grupo1/bd";

    public EstudanteController(RepositorioDados repositorio, Estudante estudanteAtivo) {
        this.repositorio = repositorio;
        this.estudanteAtivo = estudanteAtivo;
        this.view = new EstudanteView();
    }

    public void iniciar() {
        boolean aExecutar = true;
        while (aExecutar) {
            int opcao = view.mostrarMenuPrincipal();
            switch (opcao) {
                case 1: mostrarDadosPessoais(); break;
                case 2: atualizarDadosPerfil(); break;
                case 0:
                    view.mostrarDespedida();;
                    aExecutar = false;
                    break;
                default:
                    view.mostrarOpcaoInvalida();
            }
        }
    }

    /**
     * Extraído do Case 1: Exibe as informações do perfil.
     */
    private void mostrarDadosPessoais() {
        view.mostrarMensagem("\n--- DADOS PESSOAIS ---");
        view.mostrarMensagem("Nome: " + estudanteAtivo.getNome());
        view.mostrarMensagem("Email: " + estudanteAtivo.getEmail());
        view.mostrarMensagem("NIF: " + estudanteAtivo.getNif());
        view.mostrarMensagem("Morada: " + estudanteAtivo.getMorada());
        view.mostrarMensagem("Data de Nascimento: " + estudanteAtivo.getDataNascimento());
    }

    /**
     * Extraído do Case 2: Gere a atualização de morada e password.
     */
    private void atualizarDadosPerfil() {
        view.mostrarMensagem("\n--- ATUALIZAR DADOS ---");

        // Atualização de Morada
        String novaMorada = view.pedirInputString("Introduza a nova Morada (ou prima Enter para manter)");
        if (!novaMorada.trim().isEmpty()) {
            estudanteAtivo.setMorada(novaMorada);
        }

        // Atualização de Password
        String novaPass = view.pedirPassword("Introduza a nova Password (ou prima Enter para manter)");
        if (!novaPass.trim().isEmpty()) {
            String passSegura = SegurancaPasswords.gerarCredencialMista(novaPass);
            estudanteAtivo.setPassword(passSegura);

            // Grava na tabela central de credenciais
            ExportadorCSV.atualizarPasswordCentralizada(estudanteAtivo.getEmail(), passSegura, PASTA_BD);
        }

        // Grava os dados do perfil no ficheiro de estudantes
        ExportadorCSV.atualizarEstudante(estudanteAtivo, PASTA_BD);
        view.mostrarMensagem("Dados atualizados com sucesso e guardados no sistema!");
                /**
                 * US01 - Gestão Financeira:
                 * Permite ao estudante consultar a sua dívida atual e efetuar pagamentos totais ou parciais.
                 * O sistema valida defensivamente se o valor inserido não excede a dívida real.
                 * Em caso de sucesso matemático, deduz o valor e atualiza imediatamente o ficheiro
                 * estudantes.csv usando o ExportadorCSV (Abordagem On-Demand).
                 */

                case 3:
                    view.mostrarMensagem("\n--- DADOS FINANCEIROS ---");
                    view.mostrarMensagem("Saldo Devedor Atual: " + String.format("%.2f", estudanteAtivo.getSaldoDevedor()) + "€");

                    if (estudanteAtivo.getSaldoDevedor() > 0) {
                        String input = view.pedirInputString("Introduza o valor a pagar (ou 0 para cancelar)");
                        try {
                            double pagamento = Double.parseDouble(input);
                            if (pagamento > 0 && pagamento <= estudanteAtivo.getSaldoDevedor()) {
                                estudanteAtivo.setSaldoDevedor(estudanteAtivo.getSaldoDevedor() - pagamento);
                                ExportadorCSV.atualizarEstudante(estudanteAtivo, PASTA_BD);
                                view.mostrarMensagem("Pagamento efetuado com sucesso!");
                                view.mostrarMensagem("Novo Saldo Devedor: " + String.format("%.2f", estudanteAtivo.getSaldoDevedor()) + "€");
                            } else if (pagamento > estudanteAtivo.getSaldoDevedor()) {
                                view.mostrarMensagem("O valor introduzido é superior à dívida atual.");
                            }
                        } catch (NumberFormatException e) {
                            view.mostrarMensagem("Valor inválido.");
                        }
                    }
                    break;

    }
}