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
                    view.mostrarMensagem("A sair do portal do estudante...");
                    aExecutar = false;
                    break;
                default: view.mostrarMensagem("Opção inválida.");
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
    }
}