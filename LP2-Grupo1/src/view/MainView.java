package view;

import java.util.Scanner;

public class MainView {
    private Scanner scanner;

    public MainView() {
        this.scanner = new Scanner(System.in);
    }

    public int mostrarMenu() {
        System.out.println();
        System.out.println("===== SISTEMA ISSMF =====");
        System.out.println("1 - Login");
        System.out.println("2 - Criar Estudante");
        System.out.println("0 - Sair");
        System.out.print("Opção: ");

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
}

