package view;

import model.Avaliacao;
import model.Estudante;
import model.Pagamento;
import java.util.Scanner;

/**
 * Interface de utilizador do portal do Estudante.
 * Responsável exclusivamente por mostrar informação e recolher inputs.
 * Não contém lógica de negócio.
 */
public class EstudanteView {

    private final Scanner scanner;

    public EstudanteView() {
        this.scanner = new Scanner(System.in);
    }

    public int mostrarMenuPrincipal() {
        System.out.println("\n=== MENU ESTUDANTE ===");
        System.out.println("1 - Ver Dados Pessoais e Notas");
        System.out.println("2 - Atualizar Morada");
        System.out.println("3 - Alterar Password");
        System.out.println("4 - Dados Financeiros / Propinas");
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
        System.out.println(">> Nº Mecanográfico:  " + estudante.getNumeroMecanografico());
        System.out.println(">> Nome:              " + estudante.getNome());
        System.out.println(">> Email:             " + estudante.getEmail());
        System.out.println(">> NIF:               " + estudante.getNif());
        System.out.println(">> Data Nascimento:   " + estudante.getDataNascimento());
        System.out.println(">> Morada:            " + estudante.getMorada());
        System.out.println(">> Curso:             " + estudante.getSiglaCurso());
        System.out.println(">> Ano Curricular:    " + estudante.getAnoCurricular() + "º Ano");

        System.out.println("\n--- AVALIAÇÕES ---");
        int totalAvaliacoes = estudante.getPercurso().getTotalAvaliacoes();
        if (totalAvaliacoes == 0) {
            System.out.println(">> Sem avaliações registadas.");
        } else {
            Avaliacao[] historico = estudante.getPercurso().getHistoricoAvaliacoes();
            for (int i = 0; i < totalAvaliacoes; i++) {
                Avaliacao av = historico[i];
                if (av == null || av.getUc() == null) continue;
                String estado = av.isAprovado() ? "APROVADO" : "REPROVADO";
                double[] notas = av.getResultados();
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < av.getTotalAvaliacoesLancadas(); j++) {
                    if (j > 0) sb.append(" | ");
                    sb.append(String.format("%.1f", notas[j]));
                }
                System.out.printf("   %-8s | %-30s | Ano: %d | Notas: %-18s | %s%n",
                        av.getUc().getSigla(),
                        av.getUc().getNome(),
                        av.getAnoLetivo(),
                        sb,
                        estado);
            }
        }
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

    /**
     * Mostra o cabeçalho da secção financeira, o histórico de pagamentos
     * e o saldo devedor atual.
     */
    public void mostrarDadosFinanceiros(Estudante estudante) {
        System.out.println("\n--- DADOS FINANCEIROS ---");

        int total = estudante.getTotalPagamentos();
        if (total == 0) {
            System.out.println(">> Histórico de pagamentos: Sem pagamentos registados.");
        } else {
            System.out.println(">> Histórico de pagamentos:");
            double totalPago = 0;
            for (int i = 0; i < total; i++) {
                Pagamento p = estudante.getHistoricoPagamentos()[i];
                if (p != null) {
                    System.out.printf("   [%d] %.2f€  em  %s%n", i + 1, p.getValorPago(), p.getDataPagamento());
                    totalPago += p.getValorPago();
                }
            }
            System.out.printf(">> Total já pago: %.2f€%n", totalPago);
        }
        System.out.printf(">> Saldo devedor atual: %.2f€%n", estudante.getSaldoDevedor());
    }

    public int pedirTipoPagamento(double divida) {
        System.out.println("\n--- OPÇÕES DE PAGAMENTO ---");
        System.out.printf("1 - Pagamento Total (%.2f€)%n", divida);
        System.out.println("2 - Pagamento Parcial");
        System.out.println("0 - Cancelar");
        System.out.print("Escolha uma opção: ");
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public double pedirValorPagamentoParcial(double dividaAtual) {
        System.out.printf("Valor a pagar (máx. %.2f€): ", dividaAtual);
        try {
            return Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1.0;
        }
    }

    public void mostrarErroValorInvalido() {
        System.out.println(">> Erro: valor inválido ou superior ao saldo devedor.");
    }

    public void mostrarSucessoPagamento(double valorPago, double novoSaldo) {
        System.out.printf(">> Pagamento de %.2f€ efetuado com sucesso!%n", valorPago);
        if (novoSaldo <= 0) {
            System.out.println(">> Propina totalmente liquidada. Obrigado!");
        } else {
            System.out.printf(">> Saldo devedor restante: %.2f€%n", novoSaldo);
        }
    }

    public void mostrarSemPagamentosPendentes() {
        System.out.println(">> Não tem propinas em atraso. Bom trabalho!");
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