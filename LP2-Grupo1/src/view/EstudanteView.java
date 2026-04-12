package view;

import java.util.Scanner;

public class EstudanteView {
    private Scanner scanner;

    public EstudanteView() {
        this.scanner = new Scanner(System.in);
    }

    public int mostrarMenuPrincipal() {
        System.out.println("\n=== MENU ESTUDANTE ===");
        System.out.println("1 - Ver Dados Pessoais");
        System.out.println("2 - Atualizar Dados");
        System.out.println("0 - Sair / Logout");
        System.out.print("Escolha uma opção: ");

        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public String pedirInputString(String mensagem) {
        System.out.print(mensagem + ": ");
        return scanner.nextLine();
    }

    public void mostrarMensagem(String mensagem) {
        System.out.println(">> " + mensagem);
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
    public void mostrarDespedida() {
        mostrarMensagem("A sair do portal do estudante...");
    }

    public void mostrarOpcaoInvalida() {
        mostrarMensagem("Opção inválida. Tente novamente.");
    }
}
