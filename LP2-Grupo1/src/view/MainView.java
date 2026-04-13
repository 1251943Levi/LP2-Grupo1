package view;

import controller.MainController;
import java.util.Scanner;

public class MainView {
    private Scanner scanner;
    private MainController controller;

    public MainView() {
        this.scanner = new Scanner(System.in);
        this.controller = new MainController(this);
    }

    public void iniciar() {
        mostrarMensagem("Bem-vindo ao Sistema do ISSMF!");
        controller.iniciarSistema();

        boolean aExecutar = true;
        while (aExecutar) {
            int opcao = mostrarMenu();

            switch (opcao) {
                case 1:
                    String email = pedirInputString("Email").trim();

                    if (!controller.validarFormatoEmailLogin(email)) {
                        break;
                    }

                    String pass = pedirPassword("Password").trim();
                    aExecutar = controller.processarLogin(email, pass, aExecutar);
                    break;

                case 2:
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

    public int mostrarMenu() {
        System.out.println("\n===== SISTEMA ISSMF =====");
        System.out.println("1 - Login");
        System.out.println("2 - Recuperar Password");
        System.out.println("3 - Guardar Dados");
        System.out.println("0 - Sair");
        System.out.print("Opção: ");

        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public String pedirInputString(String mensagem) {
        System.out.print(mensagem + ": ");
        return scanner.nextLine();
    }

    public String pedirPassword(String mensagem) {
        System.out.print(mensagem + ": ");
        if (System.console() != null) {
            char[] passwordChars = System.console().readPassword();
            return new String(passwordChars);
        } else {
            return scanner.nextLine();
        }
    }

    public void mostrarMensagem(String mensagem) {
        System.out.println(">> " + mensagem);
    }
}