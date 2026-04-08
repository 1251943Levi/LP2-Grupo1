package view;
import java.util.Scanner;

public class GestorView {
    private Scanner scanner = new Scanner(System.in);

    public int mostrarMenu() {
        System.out.println("\n=== MENU GESTOR ===");
        System.out.println("1 - Registar Novo Estudante");
        System.out.println("2 - Avançar Ano Letivo");
        System.out.println("3 - Ver Média Global da Instituição");
        System.out.println("4 - Identificar Melhor Aluno");
        System.out.println("0 - Sair / Logout");
        System.out.print("Opção: ");
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (Exception e) {
            return -1;
        }
    }

    public String pedirInput(String msg) {
        System.out.print(msg + ": ");
        return scanner.nextLine();
    }

    public void mostrarMensagem(String msg) {
        System.out.println(">> " + msg);
    }
}