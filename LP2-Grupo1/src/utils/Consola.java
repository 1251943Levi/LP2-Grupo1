package utils;

import java.util.Scanner;

/**
 * Classe utilitária centralizada para leitura de dados da consola.
 * Interceta o valor "0" para cancelar operações em curso (exceto em menus).
 */
public class Consola {

    private static final Scanner scanner = new Scanner(System.in);

    /**
     * Lê uma String. Se o utilizador introduzir "0", aborta a operação.
     * @param mensagem O texto a perguntar ao utilizador.
     * @return A string introduzida (nunca vazia se for válida).
     * @throws CancelamentoException se o utilizador digitar exatamente "0".
     */
    public static String lerString(String mensagem) {
        System.out.print(mensagem);
        String input = scanner.nextLine().trim();

        if (input.equals("0")) {
            throw new CancelamentoException();
        }
        return input;
    }

    /**
     * Lê um valor decimal (double). Se o utilizador digitar "0", a operação é cancelada.
     * @param mensagem Texto a exibir.
     * @return O valor double introduzido.
     * @throws CancelamentoException se o utilizador digitar "0".
     */
    public static double lerDouble(String mensagem) {
        while (true) {
            String input = lerString(mensagem);  // "0" aqui lança exceção
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println(">> Erro: Formato numérico inválido. Introduza um valor válido (ex: 10.5) ou '0' para cancelar.");
            }
        }
    }

    /**
     * Lê um valor inteiro. Se o utilizador digitar "0", a operação é cancelada.
     * @param mensagem Texto a exibir.
     * @return O valor inteiro introduzido.
     * @throws CancelamentoException se o utilizador digitar "0".
     */
    public static int lerInt(String mensagem) {
        while (true) {
            String input = lerString(mensagem);
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println(">> Erro: Formato inteiro inválido. Introduza um número válido ou '0' para cancelar.");
            }
        }
    }

    /**
     * Método EXCLUSIVO para ler opções de menus.
     * Não lança CancelamentoException porque "0" é uma opção legítima de saída do menu.
     * @return O número da opção escolhida, ou -1 se a entrada for inválida.
     */
    public static int lerOpcaoMenu() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}