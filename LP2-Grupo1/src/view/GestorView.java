package view;
import java.util.Scanner;

public class GestorView {
    private Scanner scanner = new Scanner(System.in);

    public int mostrarMenu() {
        System.out.println("\n=== MENU GESTOR ===");
        System.out.println("1 - Registar Novo Estudante");
        System.out.println("2 - Avançar Ano Letivo");
        System.out.println("0 - Sair / Logout");
        System.out.print("Opção: ");
        try { return Integer.parseInt(scanner.nextLine()); } catch (Exception e) { return -1; }
    }

    public String pedirInput(String msg) {
        System.out.print(msg + ": ");
        return scanner.nextLine();
    }
    public void mostrarMensagem(String msg) { System.out.println(">> " + msg); }

    // --- MÉTODOS DE PEDIDO DE DADOS ---

    public String pedirNome() {
        return pedirInput("Nome Completo");
    }

    public void mostrarErroNomeInvalido() {
        mostrarMensagem(">> Erro: O nome é inválido. Utilize apenas letras.");
    }

    public String pedirNif() {
        return pedirInput("NIF (9 dígitos)");
    }

    public void mostrarErroNifInvalido() {
        mostrarMensagem(">> Erro: O NIF deve ser constituído exatamente por 9 dígitos.");
    }

    public String pedirDataNascimento() {
        return pedirInput("Data Nasc. (DD-MM-AAAA)");
    }

    public void mostrarErroDataInvalida() {
        mostrarMensagem(">> Erro: Formato de data inválido. Por favor, utilize o formato DD-MM-AAAA.");
    }

    public void mostrarListaCursos(String[] cursos) {
        mostrarMensagem("\n--- SELEÇÃO DE CURSO ---");
        for (int i = 0; i < cursos.length; i++) {
            if (cursos[i] != null) {
                // Vai imprimir algo como: "1 - EI - Engenharia Informática"
                mostrarMensagem((i + 1) + " - " + cursos[i]);
            }
        }
    }

    public int pedirOpcaoCurso(int max) {
        while (true) {
            try {
                int opcao = Integer.parseInt(pedirInput("Selecione o número do Curso"));
                if (opcao > 0 && opcao <= max) {
                    return opcao;
                }
                mostrarMensagem(">> Erro: Opção inválida. Escolha um número entre 1 e " + max + ".");
            } catch (NumberFormatException e) {
                mostrarMensagem(">> Erro: Introduza um número válido.");
            }
        }
    }

    public void mostrarAvisoSemCursos() {
        mostrarMensagem(">> Aviso: Não foram encontrados cursos no sistema. Defina manualmente.");
    }

    public String pedirSiglaCursoManual() {
        return pedirInput("Sigla do Curso");
    }

    public void mostrarCursoSelecionado(String sigla) {
        mostrarMensagem("Curso selecionado: " + sigla);
    }

}
