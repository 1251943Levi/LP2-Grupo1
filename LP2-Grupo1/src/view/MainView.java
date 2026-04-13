package view;

import controller.MainController;
import java.util.Scanner;

public class MainView {
    private Scanner scanner;
    private MainController controller;

    public MainView() {
        this.scanner = new Scanner(System.in);
        // A View cria o Controller e diz-lhe "eu sou a tua interface gráfica"
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

                    if (controller.validarFormatoEmailLogin(email)) {
                        String pass = pedirPassword("Password");
                        controller.processarLogin(email, pass);
                    }
                    break;

                case 2:
                    String emailRecuperacao = pedirInputString("Introduza o seu Email de recuperação");
                    controller.recuperarPassword(emailRecuperacao);
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
        if (System.console() != null) {
            char[] passwordChars = System.console().readPassword();
            return new String(passwordChars).trim();
        } else {
            return scanner.nextLine().trim();
        }
    }

    // --- MENSAGENS SEMÂNTICAS ---

    public void mostrarBemVindo() { System.out.println(">> Bem-vindo ao Sistema do ISSMF!"); }
    public void mostrarPastaCriada() { System.out.println(">> Pasta de base de dados criada."); }

    public void mostrarErroLoginSufixo() { System.out.println(">> ERRO DE LOGIN: O endereço de e-mail tem de conter obrigatoriamente o sufixo '@issmf.ipp.pt'."); }
    public void mostrarLoginGestor() { System.out.println(">> Login de Gestor detetado!"); }
    public void mostrarLoginEstudante() { System.out.println(">> Login de Estudante detetado!"); }
    public void mostrarLoginDocente() { System.out.println(">> Login de Docente detetado!"); }
    public void mostrarCredenciaisInvalidas() { System.out.println(">> Credenciais inválidas ou utilizador não encontrado."); }

    public void mostrarErroEmailInvalido() { System.out.println(">> E-mail inválido: o domínio não é reconhecido pelo sistema."); }
    public void mostrarSucessoRecuperacao(String email) {
        System.out.println(">> Processo de recuperação concluído.");
        System.out.println(">> [SISTEMA] Credenciais enviadas por e-mail para: " + email);
    }

    public void mostrarDadosGuardados() { System.out.println(">> Dados guardados com sucesso."); }
    public void mostrarDespedida() { System.out.println(">> A encerrar o sistema..."); }
    public void mostrarOpcaoInvalida() { System.out.println(">> Opção inválida."); }

    public void mostrarTituloAutoMatricula() {
        System.out.println("\n--- FORMULÁRIO DE AUTO-MATRÍCULA ---");
        System.out.println("Preencha os dados abaixo para criar o seu perfil.");
    }

    public void mostrarListaCursosDisponiveis(String[] cursos) {
        System.out.println("\n--- CURSOS COM INSCRIÇÕES ABERTAS ---");
        for (int i = 0; i < cursos.length; i++) {
            System.out.println((i + 1) + " - " + cursos[i]);
        }
    }

    public int pedirOpcaoCurso(int max) {
        System.out.print("Selecione o número do Curso: ");
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (Exception e) {
            return -1;
        }
    }

    public void mostrarSucessoAutoMatricula(String email, String pass) {
        System.out.println("\n>> SUCESSO: A sua matrícula foi realizada!");
        System.out.println(">> E-mail: " + email);
        System.out.println(">> Password Temporária: " + pass);
        System.out.println(">> Nota: Verifique o seu e-mail de contacto para mais detalhes.");
    }

    // Mensagens de Erro Específicas
    public void mostrarErroNomeInvalido() { System.out.println(">> ERRO: Nome inválido. Use apenas letras e espaços."); }
    public void mostrarErroNifInvalido() { System.out.println(">> ERRO: NIF inválido. Deve ter 9 dígitos e possuir apenas algarismos."); }
    public void mostrarErroDataInvalida() { System.out.println(">> ERRO: Formato de data inválido. Use DD-MM-AAAA."); }
    public void mostrarErroSemCursos() { System.out.println(">> AVISO: De momento não existem cursos ativos no sistema."); }
    public void mostrarErroNifDuplicado() { System.out.println(">> ERRO: Este NIF já se encontra registado num aluno existente."); }
}
