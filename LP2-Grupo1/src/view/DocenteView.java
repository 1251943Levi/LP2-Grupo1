package view;

import java.util.Scanner;

public class DocenteView {
    private Scanner scanner = new Scanner(System.in);

    public int mostrarMenu() {
        System.out.println("\n=== MENU DOCENTE ===");
        System.out.println("1 - Ver Lista de Alunos");
        System.out.println("2 - Lançar Notas");
        System.out.println("3 - Alterar Password");
        System.out.println("0 - Sair / Logout");
        System.out.print("Opção: ");
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (Exception e) {
            return -1;
        }
    }

    // --- MÉTODOS DE LISTAGEM DE ALUNOS ---

    public void mostrarListaAlunos(String[] listaAlunosFormatada) {
        System.out.println("\n--- LISTA DE ESTUDANTES ---");

        // Verifica se a lista está nula ou vazia
        if (listaAlunosFormatada == null || listaAlunosFormatada.length == 0) {
            System.out.println(">> Ainda não existem estudantes matriculados nas suas unidades curriculares.");
        } else {
            // Se existirem alunos, imprime cada uma das linhas
            for (String linha : listaAlunosFormatada) {
                if (linha != null) {
                    System.out.println(">> " + linha);
                }
            }
        }
    }

    // --- MÉTODOS DE LANÇAMENTO DE NOTAS ---

    public void mostrarCabecalhoLancamentoNotas() {
        System.out.println("\n--- LANÇAMENTO DE NOTAS ---");
    }

    public int pedirNumeroAluno() {
        System.out.print("Nº Aluno: ");
        return Integer.parseInt(scanner.nextLine().trim());
    }

    public String pedirSiglaUc() {
        System.out.print("Sigla UC: ");
        return scanner.nextLine().trim();
    }

    public int pedirAnoLetivo() {
        System.out.print("Ano Letivo (ex: 2026): ");
        return Integer.parseInt(scanner.nextLine().trim());
    }

    public double pedirNotaNormal() {
        System.out.print("Nota Normal (ou -1 se faltou): ");
        return Double.parseDouble(scanner.nextLine().trim());
    }

    public double pedirNotaRecurso() {
        System.out.print("Nota Recurso (ou -1 se faltou): ");
        return Double.parseDouble(scanner.nextLine().trim());
    }

    public double pedirNotaEspecial() {
        System.out.print("Nota Especial (ou -1 se faltou): ");
        return Double.parseDouble(scanner.nextLine().trim());
    }

    public void mostrarSucessoLancamento() {
        System.out.println(">> Notas lançadas e guardadas com sucesso na base de dados!");
    }

    public void mostrarErroAlunoNaoEncontrado(int numAluno) {
        System.out.println(">> ERRO: Aluno com o número " + numAluno + " não encontrado.");
    }

    // --- MÉTODOS DE ALTERAÇÃO DE PASSWORD ---

    public void mostrarCabecalhoAlterarPassword() {
        System.out.println("\n--- ALTERAR PASSWORD ---");
    }

    public String pedirNovaPassword() {
        System.out.print("Introduza a nova Password (ou prima Enter para cancelar): ");
        if (System.console() != null) {
            char[] passwordChars = System.console().readPassword();
            return new String(passwordChars).trim();
        } else {
            return scanner.nextLine().trim();
        }
    }

    public void mostrarSucessoAlteracaoPassword() {
        System.out.println(">> Password alterada com sucesso!");
    }

    public void mostrarCancelamentoPassword() {
        System.out.println(">> Operação cancelada. A password não foi alterada.");
    }

    // --- MENSAGENS GENÉRICAS ---

    public void mostrarOpcaoInvalida() {
        System.out.println(">> Opção inválida.");
    }

    public void mostrarErroLeituraOpcao() {
        System.out.println(">> Erro de leitura ou formato inválido. Tente novamente.");
    }
}