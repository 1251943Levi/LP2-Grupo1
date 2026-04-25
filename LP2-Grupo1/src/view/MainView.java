package view;

import controller.MainController;
import utils.Consola;
import utils.CancelamentoException;
import java.util.Scanner;


public class MainView {
    private Scanner scanner;
    private MainController controller;

    /**
     * Construtor da MainView. Inicializa o scanner de leitura (usado apenas como fallback para passwords)
     * e instancia o controlador principal.
     */
    public MainView() {
        this.scanner = new Scanner(System.in);
        this.controller = new MainController(this);
    }

    /**
     * Inicia o ciclo principal da aplicação: exibe a mensagem de boas‑vindas,
     * garante a criação da estrutura de base de dados e apresenta o menu principal
     * até que o utilizador escolha a opção de saída.
     */
    public void iniciar() {
        mostrarBemVindo();
        controller.iniciarSistema();

        boolean aExecutar = true;
        while (aExecutar) {
            int opcao = mostrarMenu();

            switch (opcao) {
                case 1:
                    String email = pedirInputString("Email");
                    if (controller.validarFormatoEmailLogin(email)) {
                        String pass = pedirPassword("Password");
                        controller.processarLogin(email, pass);
                    }
                    break;
                case 2:
                    String emailRecup = pedirInputString("Email de recuperação");
                    controller.recuperarPassword(emailRecup);
                    break;
                case 3:
                    controller.executarAutoMatricula();
                    break;
                case 0:
                    mostrarDespedida();
                    aExecutar = false;
                    break;
                default:
                    mostrarOpcaoInvalida();
            }
        }
    }

    /**
     * Apresenta o menu principal e lê a opção do utilizador.
     * A leitura é feita com {@link Consola#lerOpcaoMenu()}, que não lança excepção
     * ao digitar opção legítima de saída.
     *
     * @return Número da opção escolhida (0, 1, 2 ou 3).
     */
    public int mostrarMenu() {
        System.out.println("\n===== SISTEMA ISSMF =====");
        System.out.println("1 - Login");
        System.out.println("2 - Recuperar Password");
        System.out.println("3 - Matricular Estudante");
        System.out.println("0 - Sair");
        System.out.print("Opção: ");
        return Consola.lerOpcaoMenu();
    }

    /**
     * Solicita uma string ao utilizador. O pedido é cancelável:
     *
     * @param mensagem Texto a exibir antes da entrada.
     * @return A string introduzida pelo utilizador.
     */
    public String pedirInputString(String mensagem) {
        return Consola.lerString(mensagem + ": ");
    }

    /**
     * Solicita uma password ao utilizador, com ocultação de caracteres quando a consola o permite.
     * Se não existir uma consola interativa (ex: execução num IDE), recorre ao scanner normal.
     * <p>
     *
     * @param mensagem Texto a exibir antes da entrada.
     * @return A password introduzida (em texto claro).
     */
    public String pedirPassword(String mensagem) {
        System.out.print(mensagem + ": ");
        if (System.console() != null) {
            char[] passwordChars = System.console().readPassword();
            return new String(passwordChars).trim();
        }
        return scanner.nextLine().trim();
    }

    /**
     * Apresenta uma lista numerada de cursos e solicita ao utilizador que seleccione um.
     * @param max Número máximo de opções (quantidade de cursos disponíveis).
     * @return O índice escolhido (1..max) ou -1 se o utilizador cancelar a operação.
     */
    public int pedirOpcaoCurso(int max) {
        while (true) {
            try {
                int opcao = Consola.lerInt("Selecione o Curso (1-" + max + "): ");
                if (opcao >= 1 && opcao <= max) return opcao;
                System.out.println(">> Opção inválida.");
            } catch (CancelamentoException e) {
                return -1;
            }
        }
    }

    // --- Métodos de mensagens ---

    /** Exibe mensagem de boas‑vindas. */
    public void mostrarBemVindo() { System.out.println(">> Bem-vindo ao Sistema do ISSMF!"); }

    /** Informa que a pasta de base de dados foi criada. */
    public void mostrarPastaCriada() { System.out.println(">> Pasta de base de dados criada."); }

    /** Alerta para o formato de e-mail incorrecto (falta sufixo institucional). */
    public void mostrarErroLoginSufixo() { System.out.println(">> ERRO: O e-mail deve conter '@issmf.ipp.pt'."); }

    /** Indica que um Gestor se autenticou com sucesso. */
    public void mostrarLoginGestor() { System.out.println(">> Login de Gestor efetuado!"); }

    /** Indica que um Estudante se autenticou com sucesso. */
    public void mostrarLoginEstudante() { System.out.println(">> Login de Estudante efetuado!"); }

    /** Indica que um Docente se autenticou com sucesso. */
    public void mostrarLoginDocente() { System.out.println(">> Login de Docente efetuado!"); }

    /** Informa que as credenciais fornecidas são inválidas. */
    public void mostrarCredenciaisInvalidas() { System.out.println(">> Credenciais inválidas."); }

    /** Informa que o e‑mail não está registado no sistema. */
    public void mostrarErroEmailInvalido() { System.out.println(">> E-mail não reconhecido pelo sistema."); }

    /**
     * Informa que a recuperação de password foi concluída e que a nova password
     * foi enviada para o e‑mail indicado.
     * @param email Endereço de e‑mail para o qual foi enviada a nova password.
     */
    public void mostrarSucessoRecuperacao(String email) { System.out.println(">> Password enviada para: " + email); }

    /** Exibe mensagem de encerramento do sistema. */
    public void mostrarDespedida() { System.out.println(">> A encerrar o sistema..."); }

    /** Exibe mensagem de opção inválida (normalmente usada quando a opção do menu não é 0,1,2,3). */
    public void mostrarOpcaoInvalida() { System.out.println(">> Opção inválida."); }

    /** Título da secção de auto‑matrícula. */
    public void mostrarTituloAutoMatricula() { System.out.println("\n--- AUTO-MATRÍCULA ---"); }

    /** Alerta que o nome introduzido é inválido (contém caracteres não‑alfabéticos). */
    public void mostrarErroNomeInvalido() { System.out.println(">> Nome inválido (apenas letras)."); }

    /** Alerta que o NIF introduzido não tem exactamente 9 dígitos. */
    public void mostrarErroNifInvalido() { System.out.println(">> NIF inválido (9 dígitos)."); }

    /** Alerta que o NIF introduzido já se encontra registado noutro estudante. */
    public void mostrarErroNifDuplicado() { System.out.println(">> Erro: NIF já registado."); }

    /** Alerta que a data de nascimento não respeita o formato DD-MM-AAAA ou é uma data inválida. */
    public void mostrarErroDataInvalida() { System.out.println(">> Formato de data inválido (DD-MM-AAAA)."); }

    /** Informa que não existem cursos activos no sistema para apresentar. */
    public void mostrarErroSemCursos() { System.out.println(">> Não existem cursos ativos."); }

    /**
     * Apresenta uma lista numerada de cursos disponíveis.
     * @param cursos Array de strings no formato "sigla - nome".
     */
    public void mostrarListaCursosDisponiveis(String[] cursos) {
        System.out.println("\n--- CURSOS DISPONÍVEIS ---");
        for (int i = 0; i < cursos.length; i++) {
            System.out.println((i + 1) + " - " + cursos[i]);
        }
    }

    /**
     * Informa que a auto‑matrícula foi concluída e mostra o e‑mail institucional gerado.
     * @param email E‑mail institucional atribuído ao novo estudante.
     * @param pass  Password temporária gerada pelo sistema.
     */
    public void mostrarSucessoAutoMatricula(String email, String pass) {
        System.out.println("\n>> MATRÍCULA CONCLUÍDA!");
        System.out.println(">> E-mail Institucional: " + email);
    }

    /** Mensagem exibida quando o utilizador cancela uma operação (digitando '0' durante um pedido de dados). */
    public void mostrarOperacaoCancelada() {
        System.out.println(">> Operação cancelada. A regressar ao menu...");
    }
}