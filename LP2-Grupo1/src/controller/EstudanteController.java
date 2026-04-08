package controller;

import model.Estudante;
import model.RepositorioDados;
import view.EstudanteView;
import utils.ExportadorCSV;

/**
 * Controlador responsável por gerir o painel do Estudante.
 * Permite a visualização de dados pessoais e a atualização do perfil e credenciais,
 * gravando as alterações diretamente nos ficheiros correspondentes (On-Demand).
 */
public class EstudanteController {

    /** Repositório usado para manter a sessão (logout). */
    private RepositorioDados repositorio;

    /** O estudante que tem a sessão atualmente iniciada. */
    private Estudante estudanteAtivo;

    /** A interface de visualização do estudante. */
    private EstudanteView view;

    /** Caminho da diretoria da base de dados. */
    private static final String PASTA_BD = "LP2-Grupo1/bd";

    /**
     * Construtor do EstudanteController.
     * @param repositorio Repositório central de sessão.
     * @param estudanteAtivo O objeto Estudante logado.
     */
    public EstudanteController(RepositorioDados repositorio, Estudante estudanteAtivo) {
        this.repositorio = repositorio;
        this.estudanteAtivo = estudanteAtivo;
        this.view = new EstudanteView();
    }

    /**
     * Inicia o ciclo principal do menu do Estudante.
     */
    public void iniciar() {
        boolean aExecutar = true;

        while (aExecutar) {
            int opcao = view.mostrarMenuPrincipal();

            switch (opcao) {
                case 1:
                    view.mostrarMensagem("\n--- DADOS PESSOAIS ---");
                    view.mostrarMensagem("Nome: " + estudanteAtivo.getNome());
                    view.mostrarMensagem("Email: " + estudanteAtivo.getEmail());
                    view.mostrarMensagem("NIF: " + estudanteAtivo.getNif());
                    view.mostrarMensagem("Morada: " + estudanteAtivo.getMorada());
                    view.mostrarMensagem("Data de Nascimento: " + estudanteAtivo.getDataNascimento());
                    break;

                case 2:
                    view.mostrarMensagem("\n--- ATUALIZAR DADOS ---");
                    String novaMorada = view.pedirInputString("Introduza a nova Morada (ou prima Enter para manter)");
                    if (!novaMorada.trim().isEmpty()) {
                        estudanteAtivo.setMorada(novaMorada);
                    }

                    String novaPass = view.pedirPassword("Introduza a nova Password (ou prima Enter para manter)");
                    if (!novaPass.trim().isEmpty()) {
                        String passSegura = utils.SegurancaPasswords.gerarCredencialMista(novaPass);
                        estudanteAtivo.setPassword(passSegura);

                        ExportadorCSV.atualizarPasswordCentralizada(estudanteAtivo.getEmail(), passSegura, PASTA_BD);
                    }

                    ExportadorCSV.atualizarEstudante(estudanteAtivo, PASTA_BD);

                    view.mostrarMensagem("Dados atualizados com sucesso e guardados no sistema!");
                    break;

                case 0:
                    view.mostrarMensagem("A sair do portal do estudante...");
                    aExecutar = false;
                    break;

                default:
                    view.mostrarMensagem("Opção inválida. Tente novamente.");
            }
        }
    }
}