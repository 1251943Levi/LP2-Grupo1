package view;

import model.Estudante;
import java.util.Scanner;

public class EstudanteView {
    private Scanner scanner;

    public EstudanteView() {
        this.scanner = new Scanner(System.in);
    }

    public int mostrarMenuPrincipal() {
        System.out.println("\n=== MENU ESTUDANTE ===");
        System.out.println("1 - Ver Dados Pessoais");
        System.out.println("2 - Atualizar Dados");
        System.out.println("3 - Alterar Password");
        System.out.println("4 - Consultar Dados Financeiros / Pagar");
        System.out.println("0 - Sair / Logout");
        System.out.print("Escolha uma opção: ");

        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public void mostrarDadosPessoais(Estudante estudante) {
        System.out.println("\n--- DADOS PESSOAIS ---");
        System.out.println(">> Nome: " + estudante.getNome());
        System.out.println(">> Email: " + estudante.getEmail());
        System.out.println(">> NIF: " + estudante.getNif());
        System.out.println(">> Morada: " + estudante.getMorada());
        System.out.println(">> Data de Nascimento: " + estudante.getDataNascimento());
    }

    public String pedirNovaMorada() {
        System.out.println("\n--- ATUALIZAR DADOS ---");
        System.out.print("Introduza a nova Morada (ou prima Enter para manter a atual): ");
        return scanner.nextLine().trim();
    }

    public void mostrarSucessoAtualizacaoMorada() {
        System.out.println(">> Morada atualizada com sucesso e guardada no sistema!");
    }

    public void mostrarSemAlteracaoMorada() {
        System.out.println(">> Nenhuma alteração efetuada na morada.");
    }

    public String pedirNovaPassword() {
        System.out.println("\n--- ALTERAR PASSWORD ---");
        System.out.print("Introduza a nova Password (ou prima Enter para cancelar): ");
        if (System.console() != null) {
            char[] passwordChars = System.console().readPassword();
            return new String(passwordChars).trim();
        } else {
            return scanner.nextLine().trim();
        }
    }

    public void mostrarSucessoAtualizacaoPassword() {
        System.out.println(">> Password alterada com sucesso!");
    }

    public void mostrarCancelamentoPassword() {
        System.out.println(">> Operação cancelada. A password não foi alterada.");
    }

    public void mostrarSaldoDevedor(double divida) {
        System.out.println("\n--- DADOS FINANCEIROS ---");
        System.out.println(">> O seu saldo devedor atual (propinas) é: " + divida + "€");
    }

    public String pedirConfirmacaoPagamento() {
        System.out.print("Deseja efetuar o pagamento agora? (S/N): ");
        return scanner.nextLine().trim();
    }

    public void mostrarSucessoPagamento() {
        System.out.println(">> Pagamento efetuado com sucesso. Saldo regularizado!");
    }

    public void mostrarSemPagamentosPendentes() {
        System.out.println(">> Não tem pagamentos pendentes. Bom trabalho!");
    }

    public void mostrarDespedida() {
        System.out.println(">> A sair do portal do estudante...");
    }

    public void mostrarOpcaoInvalida() {
        System.out.println(">> Opção inválida. Tente novamente.");
    }

    public void mostrarErroLeitura() {
        System.out.println(">> Erro na leitura da opção. Por favor, insira um número válido.");
    }
}