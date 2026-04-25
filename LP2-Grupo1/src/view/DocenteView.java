package view;

import utils.Consola;
import utils.CancelamentoException;

public class DocenteView {

    /**
     * Apresenta o menu principal do docente e lê a opção escolhida.
     *
     * @return Número da opção selecionada (0 a 3).
     */
    public int mostrarMenu() {
        System.out.println("\n=== MENU DOCENTE ===");
        System.out.println("1 - Consultar os Meus Alunos e Estatísticas");
        System.out.println("2 - Lançar Notas");
        System.out.println("3 - Alterar Password");
        System.out.println("0 - Sair / Logout");
        System.out.print("Opção: ");
        return Consola.lerOpcaoMenu();
    }

    // --- MÉTODOS DE LISTAGEM DE ALUNOS ---

    /** Exibe o cabeçalho da lista de alunos. */
    public void mostrarCabecalhoAlunos() {
        System.out.println("\n--- OS MEUS ALUNOS ---");
    }

    /** Informa que ocorreu um erro ao carregar os alunos. */
    public void mostrarErroCarregarAlunos() {
        System.out.println(">> Erro ao carregar a lista de estudantes.");
    }

    /**
     * Exibe um aluno (número mecanográfico e nome).
     *
     * @param numMecanografico Número do aluno.
     * @param nome             Nome do aluno.
     */
    public void mostrarAluno(int numMecanografico, String nome) {
        System.out.println(">> Nº: " + numMecanografico + " | Aluno: " + nome);
    }

    /** Informa que o docente não tem alunos inscritos nas suas UCs. */
    public void mostrarSemAlunos() {
        System.out.println(">> Não tem alunos inscritos nas suas UCs.");
    }

    /**
     * Exibe a média das disciplinas do docente.
     *
     * @param media Valor da média calculada.
     */
    public void mostrarMedia(double media) {
        System.out.println(">> Média das suas disciplinas: " + String.format("%.2f", media));
    }

    // --- MÉTODOS DE LANÇAMENTO DE NOTAS ---

    /** Exibe o cabeçalho da secção de lançamento de notas. */
    public void mostrarCabecalhoLancamentoNotas() {
        System.out.println("\n--- LANÇAMENTO DE NOTAS ---");
    }

    /**
     * Solicita o número do aluno.
     */
    public int pedirNumeroAluno() {
        return Consola.lerInt("Nº Aluno: ");
    }

    /**
     * Solicita a sigla da Unidade Curricular.
     *
     * @return Sigla da UC (ex: "POO").
     */
    public String pedirSiglaUc() {
        return Consola.lerString("Sigla UC: ");
    }

    /**
     * Solicita o ano letivo.
     *
     * @return Ano letivo (ex: 2026).
     */
    public int pedirAnoLetivo() {
        return Consola.lerInt("Ano Letivo (ex: 2026): ");
    }

    /**
     * Solicita a nota da época normal.
     * @return Nota introduzida (pode ser -1 para faltou).
     */
    public double pedirNotaNormal() {
        return Consola.lerDouble("Nota Normal (ou -1 se faltou): ");
    }

    /**
     * Solicita a nota da época de recurso.
     *
     * @return Nota introduzida (pode ser -1 para faltou).
     */
    public double pedirNotaRecurso() {
        return Consola.lerDouble("Nota Recurso (ou -1 se faltou): ");
    }

    /**
     * Solicita a nota da época especial.
     *
     * @return Nota introduzida (pode ser -1 para faltou).
     */
    public double pedirNotaEspecial() {
        return Consola.lerDouble("Nota Especial (ou -1 se faltou): ");
    }

    /** Informa que as notas foram lançadas e guardadas com sucesso. */
    public void mostrarSucessoLancamento() {
        System.out.println(">> Notas lançadas e guardadas com sucesso na base de dados!");
    }

    /**
     * Informa que o aluno com o número fornecido não foi encontrado.
     *
     * @param numAluno Número mecanográfico do aluno não encontrado.
     */
    public void mostrarErroAlunoNaoEncontrado(int numAluno) {
        System.out.println(">> ERRO: Aluno com o número " + numAluno + " não encontrado.");
    }

    // --- MÉTODOS DE ALTERAÇÃO DE PASSWORD ---

    /** Exibe o cabeçalho para alteração de password. */
    public void mostrarCabecalhoAlterarPassword() {
        System.out.println("\n--- ALTERAR PASSWORD ---");
    }

    /**
     * Solicita a nova password ao utilizador, com ocultação de caracteres quando a consola o permite.
     * <p>
     * O cancelamento é feito premindo Enter sem introduzir texto, o que retorna uma string vazia.
     *
     * @return A nova password introduzida, ou uma string vazia se o utilizador premir Enter.
     */
    public String pedirNovaPassword() {
        System.out.print("Introduza a nova Password (ou prima Enter para cancelar): ");
        if (System.console() != null) {
            char[] passwordChars = System.console().readPassword();
            return new String(passwordChars).trim();
        } else {
            // Fallback para quando não existe consola interativa (ex: execução num IDE)
            return new java.util.Scanner(System.in).nextLine().trim();
        }
    }

    /** Informa que a password foi alterada com sucesso. */
    public void mostrarSucessoAlteracaoPassword() {
        System.out.println(">> Password alterada com sucesso!");
    }

    /** Informa que a operação de alteração de password foi cancelada. */
    public void mostrarCancelamentoPassword() {
        System.out.println(">> Operação cancelada. A password não foi alterada.");
    }

    // --- MENSAGENS GENÉRICAS ---

    /** Mensagem exibida quando o utilizador escolhe uma opção inválida no menu. */
    public void mostrarOpcaoInvalida() {
        System.out.println(">> Opção inválida.");
    }

    /** Mensagem de erro genérica para leitura da opção (quando a entrada não é numérica). */
    public void mostrarErroLeituraOpcao() {
        System.out.println(">> Erro de leitura ou formato inválido. Tente novamente.");
    }

    /** Mensagem exibida quando o utilizador cancela uma operação (digitando '0' durante um pedido de dados). */
    public void mostrarOperacaoCancelada() {
        System.out.println(">> Operação cancelada. A regressar ao menu...");
    }
}