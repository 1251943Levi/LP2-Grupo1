package controller;

import view.EstudanteView;

/**
 * O Controlador (Controller) faz a ponte entre a View (Ecrã/Menus) e o Model (Dados).
 * É aqui que definimos a lógica: o que acontece quando o utilizador escolhe uma opção.
 */
public class EstudanteController {

    private EstudanteView view;

    /**
     * Construtor do Controller.
     * Inicializa a View para poder comunicar com o utilizador.
     */
    public EstudanteController() {
        this.view = new EstudanteView();
    }

    /**
     * Método principal que arranca o menu do Estudante.
     * Mantém o programa a correr num ciclo (loop) até o utilizador escolher "Sair" (0).
     */
    public void iniciar() {
        boolean aExecutar = true;

        while (aExecutar) {
            // 1. Pede à View para mostrar o menu e devolver a opção escolhida
            int opcao = view.mostrarMenuPrincipal();

            // 2. O Controller decide que método chamar com base na opção
            switch (opcao) {
                case 1:
                    verDadosPessoais();
                    break;
                case 2:
                    atualizarDados();
                    break;
                case 3:
                    verPercursoAcademico();
                    break;
                case 0:
                    view.mostrarMensagem("A sair do menu do estudante... Até logo!");
                    aExecutar = false; // Quebra o ciclo while e termina o menu
                    break;
                case -1:
                    //  Aqui o Controller trata esse erro.
                    view.mostrarMensagem("Erro: Por favor, insira um número válido.");
                    break;
                default:
                    // Para qualquer número que não esteja no menu (ex: 9)
                    view.mostrarMensagem("Opção inválida. Escolha um número do menu.");
                    break;
            }
        }
    }

    // --- MÉTODOS DE LÓGICA DO CONTROLLER ---

    // exemplos nada defenido
    private void verDadosPessoais() {
        // Exemplo: O Controller iria pedir ao Model os dados do aluno e mandar a View mostrar.
        view.mostrarMensagem("A carregar os seus dados pessoais...");
    }

    private void atualizarDados() {
        // O Controller usa a View para pedir um dado novo...
        String novoEmail = view.pedirInputString("Insira o seu novo email");

        //  (no futuro) mandaria o Model guardar esse novo email.
        view.mostrarMensagem("Email atualizado com sucesso para: " + novoEmail);
    }

    private void verPercursoAcademico() {
        view.mostrarMensagem("A carregar o seu percurso académico...");
    }
}