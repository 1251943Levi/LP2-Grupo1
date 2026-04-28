package view;

import controller.MainController;
import utils.Consola;

/**
 * View do ecrã principal: login, recuperação de password e auto-matrícula.
 * Main.java instancia esta View e chama iniciar() — a View cria o MainController.
 */
public class MainView {

    /**
     * Ponto de entrada da aplicação.
     * Cria o MainController, passa-se a si própria, e delega o arranque.
     */
    public void iniciar() {
        new MainController(this).iniciar();
    }


    // ---------- MENU ----------

    public int mostrarMenu() {
        Consola.imprimirCabecalho("Sistema ISSMF - 2025/2026");
        Consola.imprimirMenu(new String[]{
                "Login",
                "Recuperar Password",
                "Matricular Estudante"
        }, "Sair");
        return Consola.lerOpcaoMenu();
    }

    // ---------- INPUTS ----------

    /** Lê um campo de texto — "0" lança CancelamentoException. */
    public String pedirInputString(String mensagem) {
        return Consola.lerString(mensagem);
    }

    /**
     * Lê uma password com mascaramento. Delega para Consola.lerPassword().
     * "0" lança CancelamentoException.
     */
    public String pedirPassword(String mensagem) {
        return Consola.lerPassword(mensagem);
    }

    /**
     * Apresenta lista numerada de cursos e pede seleção.
     * Captura CancelamentoException internamente e devolve -1.
     */
    public int pedirOpcaoCurso(int max) {
        while (true) {
            try {
                int opcao = Consola.lerInt("Número do Curso (1-" + max + ")");
                if (opcao >= 1 && opcao <= max) return opcao;
                Consola.imprimirErro("Opção fora do intervalo.");
            } catch (utils.CancelamentoException e) {
                return -1;
            }
        }
    }

    // ---------- LISTAGENS ----------

    /**
     * Apresenta uma lista numerada de cursos disponíveis.
     * @param cursos Array de strings no formato "sigla - nome".
     */
    public void mostrarListaCursosDisponiveis(String[] cursos) {
        Consola.imprimirTitulo("Cursos Disponíveis");
        for (int i = 0; i < cursos.length; i++)
            System.out.println("  [" + (i + 1) + "] " + cursos[i]);
        Consola.imprimirLinha();
    }

    // ---------- MENSAGENS ----------

    public void mostrarBemVindo() {
        Consola.imprimirCabecalho("Bem-vindo ao Sistema ISSMF");
        Consola.imprimirInfo("Instituto Superior de Santa Maria da Feira");
        Consola.imprimirLinha();
    }
    public void mostrarPastaCriada()           { Consola.imprimirInfo("Pasta de base de dados criada."); }
    public void mostrarErroLoginSufixo()       { Consola.imprimirErro("O e-mail deve terminar em '@issmf.ipp.pt'."); }
    public void mostrarLoginGestor()           { Consola.imprimirSucesso("Login de Gestor efetuado!"); }
    public void mostrarLoginEstudante()        { Consola.imprimirSucesso("Login de Estudante efetuado!"); }
    public void mostrarLoginDocente()          { Consola.imprimirSucesso("Login de Docente efetuado!"); }
    public void mostrarCredenciaisInvalidas()  { Consola.imprimirErro("Credenciais inválidas."); }
    public void mostrarErroEmailInvalido()     { Consola.imprimirErro("E-mail não reconhecido pelo sistema."); }
    public void mostrarSucessoRecuperacao(String email) { Consola.imprimirSucesso("Password enviada para: " + email); }
    public void mostrarDespedida()             { Consola.imprimirInfo("A encerrar o sistema. Até breve!"); }
    public void mostrarOpcaoInvalida()         { Consola.imprimirErro("Opção inválida."); }
    public void mostrarTituloAutoMatricula() {
        Consola.imprimirCabecalho("Auto-Matrícula");
    }
    public void mostrarErroNomeInvalido()      { Consola.imprimirErro("Nome inválido (apenas letras)."); }
    public void mostrarErroNifInvalido()       { Consola.imprimirErro("NIF inválido (9 dígitos)."); }
    public void mostrarErroNifDuplicado()      { Consola.imprimirErro("NIF já registado no sistema."); }
    public void mostrarErroDataInvalida()      { Consola.imprimirErro("Formato de data inválido (DD-MM-AAAA)."); }
    public void mostrarErroSemCursos()         { Consola.imprimirErro("Não existem cursos ativos no sistema."); }

    public void mostrarSucessoAutoMatricula(String email) {
        Consola.imprimirSucesso("Matrícula concluída com sucesso!");
        Consola.imprimirInfo("Email institucional: " + email);
        Consola.imprimirInfo("As credenciais foram enviadas para o seu email.");
        Consola.pausar();
    }

    public void mostrarOperacaoCancelada() { Consola.imprimirInfo("Operação cancelada. A regressar ao menu..."); }
}