package View;

import java.util.Scanner;

public class MainView {

    // ---------- ATRIBUTOS ----------
    private Scanner scanner;

    // ---------- CONSTRUTOR ----------
    public MainView() {
        this.scanner = new Scanner(System.in);
    }

    // ---------- MÉTODOS DE APRESENTAÇÃO E LEITURA ----------

    /**
     * Apresenta o menu de entrada principal da aplicação.
     * @return A opção numérica escolhida pelo utilizador. Retorna -1 em caso de input inválido.
     */
    public int mostrarMenu() {
        System.out.println();
        System.out.println("===== SISTEMA ISSMF =====");
        System.out.println("1 - Login");
        System.out.println("2 - Criar Estudante");
        System.out.println("3 - Sair");

        System.out.print("Opção: ");

        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Imprime uma mensagem no ecrã e aguarda que o utilizador introduza uma resposta em texto.
     * @param mensagem Texto a ser exibido antes do pedido de input.
     * @return A string inserida pelo utilizador.
     */
    public String pedirInputString(String mensagem) {
        System.out.print(mensagem + ": ");
        return scanner.nextLine();
    }

    /**
     * Apresenta uma mensagem padronizada de feedback no ecrã.
     * @param mensagem O conteúdo a exibir.
     */
    public void mostrarMensagem(String mensagem) {
        System.out.println(">> " + mensagem);
    }
}