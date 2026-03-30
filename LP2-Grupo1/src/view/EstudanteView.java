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
        System.out.println("3 - Percurso Académico");
        System.out.println("0 - Sair / Logout");
        System.out.print("Escolha uma opção: ");

        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1; // Devolve -1 se o utilizador escrever letras em vez de números
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



