package controller;

import model.Estudante;
import model.RepositorioDados;

import view.EstudanteView;

public class EstudanteController {
    private RepositorioDados repositorio;
    private Estudante estudanteAtivo;
    private EstudanteView view;

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
                    String novaMorada = view.pedirInputString("Introduza a nova Morada");
                    estudanteAtivo.setMorada(novaMorada);

                    String novaPass = view.pedirInputString("Introduza a nova Password (ou deixe em branco para manter a atual)");
                    if (!novaPass.trim().isEmpty()) {
                        estudanteAtivo.setPassword(novaPass);
                    }

                    view.mostrarMensagem("Dados atualizados com sucesso!");
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
