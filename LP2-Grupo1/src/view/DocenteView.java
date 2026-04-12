package view;
import java.util.Scanner;

public class DocenteView {
    private Scanner scanner = new Scanner(System.in);

    public int mostrarMenu() {
        System.out.println("\n=== MENU DOCENTE ===");
        System.out.println("1 - Consultar os Meus Alunos e Estatísticas");
        System.out.println("2 - Lançar Notas");
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