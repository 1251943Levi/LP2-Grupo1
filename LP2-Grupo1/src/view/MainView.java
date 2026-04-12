package view;

import controller.MainController;
import java.util.Scanner;

/**
 * Interface de Utilizador em Consola principal do sistema.
 * Responsável por apresentar menus, recolher dados do teclado e exibir mensagens.
 * Segue o padrão View no modelo MVC, delegando o processamento ao MainController.
 */
public class MainView {
    /**
     * Scanner para leitura da entrada padrão.
     */
    private Scanner scanner;

    /**
     * Referência para o controlador que processará as ações desta vista.
     */
    private MainController controller;

    /**
     * Inicializa a vista, configurando o Scanner e instanciando o controlador associado.
     */
    public MainView() {
        this.scanner = new Scanner(System.in);
        this.controller = new MainController(this);
    }

    /**
     * Inicia o ciclo principal de vida da interface.
     * Controla o loop do menu e a navegação entre as opções principais do sistema.
     */
    public void iniciar() {
        mostrarMensagem("Bem-vindo ao Sistema do ISSMF!");
        controller.iniciarSistema();

        boolean aExecutar = true;
        while (aExecutar) {
            int opcao = mostrarMenu();

            switch (opcao) {
                case 1:
                    String email = pedirInputString("Email").trim();
                    String pass = pedirPassword("Password").trim();                    aExecutar = controller.processarLogin(email, pass, aExecutar);
                    break;

                case 2: // NOVA LÓGICA
                    String emailRecuperacao = pedirInputString("Introduza o seu Email de recuperação").trim();
                    controller.recuperarPassword(emailRecuperacao);
                    break;

                case 3:
                    controller.guardarDados();
                    mostrarMensagem("Dados guardados com sucesso.");
                    break;

                case 0:
                    controller.guardarDados();
                    mostrarMensagem("A encerrar o sistema...");
                    aExecutar = false;
                    break;

                default:
                    mostrarMensagem("Opção inválida.");
            }
        }
    }

    /**
     * Apresenta o menu principal na consola e recolhe a opção do utilizador.
     * @return int A opção escolhida pelo utilizador, ou -1 em caso de entrada inválida.
     */
    public int mostrarMenu() {
        System.out.println("\n===== SISTEMA ISSMF =====");
        System.out.println("1 - Login");
        System.out.println("2 - Recuperar Password");
        System.out.println("3 - Guardar Dados");
        System.out.println("0 - Sair");
        System.out.print("Opção: ");

        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Solicita uma cadeia de caracteres (String) ao utilizador através de uma mensagem.
     * @param mensagem O texto descritivo do que deve ser inserido.
     * @return String O texto introduzido pelo utilizador.
     */
    public String pedirInputString(String mensagem) {
        System.out.print(mensagem + ": ");
        return scanner.nextLine();
    }

    /**
     * Solicita uma password ao utilizador, ocultando os caracteres introduzidos se a consola o permitir.
     * @param mensagem O texto descritivo do que deve ser inserido.
     * @return String A password introduzida pelo utilizador.
     */
    public String pedirPassword(String mensagem) {
        System.out.print(mensagem + ": ");
        if (System.console() != null) {
            char[] passwordChars = System.console().readPassword();
            return new String(passwordChars);
        } else {
            return scanner.nextLine();
        }
    }

    /**
     * Exibe uma mensagem informativa ou de erro no ecrã.
     * @param mensagem O conteúdo da mensagem a exibir.
     */
    public void mostrarMensagem(String mensagem) {
        System.out.println(">> " + mensagem);
    }

}

