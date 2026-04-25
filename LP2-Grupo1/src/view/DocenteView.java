package view;

import model.Docente;
import model.Estudante;
import model.UnidadeCurricular;
import java.util.Scanner;

/**
 * Interface de utilizador do portal do Docente.
 * Apenas mostra informação e recolhe inputs — sem lógica de negócio.
 */
public class DocenteView {

    private final Scanner scanner = new Scanner(System.in);

    public int mostrarMenu() {
        System.out.println("\n=== MENU DOCENTE ===");
        System.out.println("1 - Consultar os Meus Alunos e Médias");
        System.out.println("2 - Lançar Notas");
        System.out.println("3 - Alterar Password");
        System.out.println("4 - Ver Dados Pessoais");
        System.out.println("5 - Ver as Minhas Unidades Curriculares");
        System.out.println("0 - Sair / Logout");
        System.out.print("Opção: ");
        try { return Integer.parseInt(scanner.nextLine()); }
        catch (Exception e) { return -1; }
    }

    /**
     * Mostra a ficha completa do docente autenticado.
     */
    public void mostrarFichaDocente(Docente docente) {
        System.out.println("\n--- DADOS PESSOAIS ---");
        System.out.println(">> Sigla:            " + docente.getSigla());
        System.out.println(">> Nome:             " + docente.getNome());
        System.out.println(">> Email:            " + docente.getEmail());
        System.out.println(">> NIF:              " + docente.getNif());
        System.out.println(">> Data Nascimento:  " + docente.getDataNascimento());
        System.out.println(">> Morada:           " + docente.getMorada());
    }

    /**
     * Mostra a lista de UCs lecionadas pelo docente autenticado.
     */
    public void mostrarUcsDocente(Docente docente) {
        System.out.println("\n--- AS MINHAS UNIDADES CURRICULARES ---");
        if (docente.getTotalUcsLecionadas() == 0) {
            System.out.println(">> Não tem unidades curriculares atribuídas.");
            return;
        }
        for (int i = 0; i < docente.getTotalUcsLecionadas(); i++) {
            UnidadeCurricular uc = docente.getUcsLecionadas()[i];
            if (uc != null) {
                System.out.printf(">> [%d] %-8s | %-35s | %dº Ano%n",
                        i + 1, uc.getSigla(), uc.getNome(), uc.getAnoCurricular());
            }
        }
    }

    // --- MÉTODOS DE LISTAGEM DE ALUNOS ---

    public void mostrarCabecalhoAlunos() {
        System.out.println("\n--- OS MEUS ALUNOS ---");
    }

    public void mostrarErroCarregarAlunos() {
        System.out.println(">> Não foram encontrados alunos nas suas Unidades Curriculares.");
    }

    public void mostrarAluno(int numMecanografico, String nome) {
        System.out.println(">> Nº: " + numMecanografico + " | Aluno: " + nome);
    }

    /**
     * Mostra um aluno com a sua média académica nas UCs lecionadas por este docente.
     * @param estudante O objeto estudante.
     * @param media     A média calculada para as UCs deste docente.
     */
    public void mostrarAlunoComMedia(Estudante estudante, double media) {
        System.out.printf(">> Nº %-8d | %-25s | Média nas minhas UCs: %.2f valores%n",
                estudante.getNumeroMecanografico(),
                estudante.getNome(),
                media);
    }

    public void mostrarMedia(double media) {
        System.out.println(">> Média das suas disciplinas: " + String.format("%.2f", media));
    }

    public void mostrarSemAlunos() {
        System.out.println(">> Não tem alunos inscritos nas suas UCs.");
    }



    // --- MÉTODOS DE LANÇAMENTO DE NOTAS ---

    public void mostrarCabecalhoLancamentoNotas() {
        System.out.println("\n--- LANÇAMENTO DE NOTAS ---");
    }

    public int pedirNumeroAluno() {
        System.out.print("Nº do Aluno: ");
        return Integer.parseInt(scanner.nextLine().trim());
    }

    public String pedirSiglaUc() {
        System.out.print("Sigla da UC: ");
        return scanner.nextLine().trim();
    }

    public int pedirAnoLetivo() {
        System.out.print("Ano Letivo (ex: 2026): ");
        return Integer.parseInt(scanner.nextLine().trim());
    }

    public double pedirNotaNormal() {
        System.out.print("Nota Época Normal  (-1 se não realizou): ");
        return Double.parseDouble(scanner.nextLine().trim());
    }

    public double pedirNotaRecurso() {
        System.out.print("Nota Época Recurso (-1 se não realizou): ");
        return Double.parseDouble(scanner.nextLine().trim());
    }

    public double pedirNotaEspecial() {
        System.out.print("Nota Época Especial(-1 se não realizou): ");
        return Double.parseDouble(scanner.nextLine().trim());
    }

    public void mostrarSucessoLancamento() {
        System.out.println(">> Notas lançadas e guardadas com sucesso!");
    }

    public void mostrarErroAlunoNaoEncontrado(int numAluno) {
        System.out.println(">> ERRO: Aluno com o número " + numAluno + " não encontrado.");
    }


    // --- MÉTODOS DE ALTERAÇÃO DE PASSWORD ---

    public void mostrarCabecalhoAlterarPassword() {
        System.out.println("\n--- ALTERAR PASSWORD ---");
    }

    public String pedirNovaPassword() {
        System.out.print("Nova Password (Enter para cancelar): ");
        if (System.console() != null)
            return new String(System.console().readPassword()).trim();
        return scanner.nextLine().trim();
    }

    public void mostrarSucessoAlteracaoPassword() {
        System.out.println(">> Password alterada com sucesso!");
    }

    public void mostrarCancelamentoPassword() {
        System.out.println(">> Operação cancelada. A password não foi alterada.");
    }


    // --- MENSAGENS GENÉRICAS ---

    public void mostrarDespedida() {
        System.out.println(">> A sair do portal do docente...");
    }

    public void mostrarOpcaoInvalida() {
        System.out.println(">> Opção inválida.");
    }

    public void mostrarErroLeituraOpcao() {
        System.out.println(">> Erro de leitura. Tente novamente.");
    }
}