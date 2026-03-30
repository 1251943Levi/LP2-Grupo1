package view;

import java.util.Scanner;

/**
 * Classe responsável pela interface com o utilizador (View) na perspetiva do Estudante.
 * Lida exclusivamente com a apresentação de menus e mensagens no terminal,
 * bem como a leitura dos dados inseridos pelo utilizador.
 */
public class EstudanteView {

    // Objeto Scanner usado para ler o que o utilizador escreve na consola
    private Scanner scanner;

    /**
     * Construtor da classe.
     * Inicializa o Scanner para ler a partir do Standard Input (teclado).
     */
    public EstudanteView() {
        this.scanner = new Scanner(System.in);
    }

    /**
     * Apresenta o menu principal com as funcionalidades do estudante e lê a opção desejada.
     * * @return O número da opção escolhida, ou -1 caso o utilizador insira um formato inválido.
     */
    public int mostrarMenuPrincipal() {
        System.out.println("\n=== MENU ESTUDANTE ===");
        System.out.println("1 - Ver Dados Pessoais");
        System.out.println("2 - Atualizar Dados");
        System.out.println("3 - Percurso Académico");
        System.out.println("0 - Sair / Logout");
        System.out.print("Escolha uma opção: ");

        /* * Lê a linha toda e tenta converter para inteiro.
         * Usar nextLine() e Integer.parseInt() evita o famoso "bug" de quebras
         * de linha (Enter) que fica preso no buffer ao usar apenas scanner.nextInt().
         */
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            // Captura o erro caso o utilizador escreva letras/símbolos em vez de números.
            // O controlador (Controller) que chamar este método deve tratar o -1 como "Opção Inválida".
            return -1;
        }
    }

    /**
     * Método utilitário reaproveitável para pedir uma informação em texto ao utilizador.
     * * @param mensagem A pergunta ou indicação a apresentar (ex: "Insira o novo email")
     * @return A resposta de texto inserida pelo utilizador
     */
    public String pedirInputString(String mensagem) {
        System.out.print(mensagem + ": ");
        return scanner.nextLine();
    }

    /**
     * Método utilitário para padronizar as mensagens de feedback do sistema.
     * * @param mensagem O texto de informação/sucesso/erro a exibir (ex: "Atualização feita com sucesso")
     */
    public void mostrarMensagem(String mensagem) {
        // Usa o prefixo ">> " para ajudar a distinguir visualmente o que é output do sistema
        // do que são os menus normais.
        System.out.println(">> " + mensagem);
    }
}