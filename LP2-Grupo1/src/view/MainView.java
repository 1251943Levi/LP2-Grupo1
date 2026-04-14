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
        mostrarBemVindo();
        controller.iniciarSistema();

        boolean aExecutar = true;
        while (aExecutar) {
            int opcao = mostrarMenu();

            switch (opcao) {
                case 1:
                    String email = pedirInputString("Email");
                    // Validação imediata de sufixo antes de prosseguir
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
                    controller.guardarDados();
                    mostrarDadosGuardados();
                    break;
                case 4:
                    controller.executarAutoMatricula();
                    break;
                case 0:
                    controller.guardarDados();
                    mostrarDespedida();
                    aExecutar = false;
                    break;
                default:
                    mostrarOpcaoInvalida();
            }
        }
    }

    public int mostrarMenu() {
        System.out.println("\n===== SISTEMA ISSMF =====");
        System.out.println("1 - Login");
        System.out.println("2 - Recuperar Password");
        System.out.println("3 - Guardar Dados");
        System.out.println("4 - Matricular Estudante");
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
        return scanner.nextLine().trim();
    }

    public String pedirPassword(String mensagem) {
        System.out.print(mensagem + ": ");
        // Requisito: Password oculta na consola
        if (System.console() != null) {
            char[] passwordChars = System.console().readPassword();
            return new String(passwordChars).trim();
        }
        return scanner.nextLine().trim();
    }

    public void mostrarBemVindo() { System.out.println(">> Bem-vindo ao Sistema do ISSMF!"); }
    public void mostrarPastaCriada() { System.out.println(">> Pasta de base de dados criada."); }
    public void mostrarErroLoginSufixo() { System.out.println(">> ERRO: O e-mail deve conter '@issmf.ipp.pt'."); }
    public void mostrarLoginGestor() { System.out.println(">> Login de Gestor efetuado!"); }
    public void mostrarLoginEstudante() { System.out.println(">> Login de Estudante efetuado!"); }
    public void mostrarLoginDocente() { System.out.println(">> Login de Docente efetuado!"); }
    public void mostrarCredenciaisInvalidas() { System.out.println(">> Credenciais inválidas."); }
    public void mostrarErroEmailInvalido() { System.out.println(">> E-mail não reconhecido pelo sistema."); }
    public void mostrarSucessoRecuperacao(String email) { System.out.println(">> Password enviada para: " + email); }
    public void mostrarDadosGuardados() { System.out.println(">> Dados guardados com sucesso."); }
    public void mostrarDespedida() { System.out.println(">> A encerrar o sistema..."); }
    public void mostrarOpcaoInvalida() { System.out.println(">> Opção inválida."); }
    public void mostrarTituloAutoMatricula() { System.out.println("\n--- AUTO-MATRÍCULA ---"); }
    public void mostrarErroNomeInvalido() { System.out.println(">> Nome inválido (apenas letras)."); }
    public void mostrarErroNifInvalido() { System.out.println(">> NIF inválido (9 dígitos)."); }
    public void mostrarErroNifDuplicado() { System.out.println(">> Erro: NIF já registado."); }
    public void mostrarErroDataInvalida() { System.out.println(">> Formato de data inválido (DD-MM-AAAA)."); }
    public void mostrarErroSemCursos() { System.out.println(">> Não existem cursos ativos."); }

    public void mostrarListaCursosDisponiveis(String[] cursos) {
        System.out.println("\n--- CURSOS DISPONÍVEIS ---");
        for (int i = 0; i < cursos.length; i++) {
            System.out.println((i + 1) + " - " + cursos[i]);
        }
    }

    public int pedirOpcaoCurso(int max) {
        System.out.print("Selecione o Curso (1-" + max + "): ");
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (Exception e) {
            return -1;
        }
    }

    public void mostrarSucessoAutoMatricula(String email, String pass) {
        System.out.println("\n>> MATRÍCULA CONCLUÍDA!");
        System.out.println(">> E-mail Institucional: " + email);
    }
}