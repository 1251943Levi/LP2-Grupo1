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
                    case 0:
                        view.mostrarMensagem("A sair do portal do estudante...");
                        repositorio.limparSessao(); // Garante que a sessão é limpa ao sair
                        aExecutar = false;
                        break;
                    default:
                        view.mostrarMensagem("Opção inválida. Tente novamente.");
                }
            } catch (Exception e) {
                view.mostrarMensagem("Erro na leitura da opção. Por favor, insira um número válido.");
            }
        }
    }

    private void visualizarDadosPessoais() {
        view.mostrarMensagem("\n--- DADOS PESSOAIS ---");
        view.mostrarMensagem("Nome: " + estudanteAtivo.getNome());
        view.mostrarMensagem("Email: " + estudanteAtivo.getEmail());
        view.mostrarMensagem("NIF: " + estudanteAtivo.getNif());
        view.mostrarMensagem("Morada: " + estudanteAtivo.getMorada());
        view.mostrarMensagem("Data de Nascimento: " + estudanteAtivo.getDataNascimento());
    }

    private void atualizarDadosPessoais() {
        view.mostrarMensagem("\n--- ATUALIZAR DADOS ---");
        String novaMorada = view.pedirInputString("Introduza a nova Morada (ou prima Enter para manter a atual)");

        if (!novaMorada.trim().isEmpty()) {
            estudanteAtivo.setMorada(novaMorada);
            ExportadorCSV.atualizarEstudante(estudanteAtivo, PASTA_BD);
            view.mostrarMensagem("Morada atualizada com sucesso e guardada no sistema!");
        } else {
            view.mostrarMensagem("Nenhuma alteração efetuada na morada.");
        }
    }

    private void alterarPassword() {
        view.mostrarMensagem("\n--- ALTERAR PASSWORD ---");
        String novaPass = view.pedirPassword("Introduza a nova Password (ou prima Enter para cancelar)");

        if (!novaPass.trim().isEmpty()) {
            String passSegura = SegurancaPasswords.gerarCredencialMista(novaPass);

            estudanteAtivo.setPassword(passSegura);
            ExportadorCSV.atualizarPasswordCentralizada(estudanteAtivo.getEmail(), passSegura, PASTA_BD);

            view.mostrarMensagem("Password alterada com sucesso!");
        } else {
            view.mostrarMensagem("Operação cancelada. A password não foi alterada.");
        }
    }
}