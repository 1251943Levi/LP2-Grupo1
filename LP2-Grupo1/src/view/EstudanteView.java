package view;

import model.Estudante;
import utils.Consola;
import utils.CancelamentoException;

public class EstudanteView {

    public EstudanteView() {
    }

    /**
     * Apresenta o menu principal do estudante e lê a opção escolhida.
     *
     * @return Número da opção selecionada (0 a 4).
     */
    public int mostrarMenuPrincipal() {
        System.out.println("\n=== MENU ESTUDANTE ===");
        System.out.println("1 - Ver Dados Pessoais");
        System.out.println("2 - Atualizar Dados");
        System.out.println("3 - Alterar Password");
        System.out.println("4 - Consultar Dados Financeiros / Pagar");
        System.out.println("0 - Sair / Logout");
        System.out.print("Escolha uma opção: ");
        return Consola.lerOpcaoMenu();
    }

    /**
     * Exibe os dados pessoais do estudante.
     *
     * @param estudante Objeto {@link Estudante} cujos dados serão mostrados.
     */
    public void mostrarDadosPessoais(Estudante estudante) {
        System.out.println("\n--- DADOS PESSOAIS ---");
        System.out.println(">> Nome: " + estudante.getNome());
        System.out.println(">> Email: " + estudante.getEmail());
        System.out.println(">> NIF: " + estudante.getNif());
        System.out.println(">> Morada: " + estudante.getMorada());
        System.out.println(">> Data de Nascimento: " + estudante.getDataNascimento());
    }

    /**
     * Solicita a nova morada ao utilizador.
     * O pedido é cancelável
     *
     * @return A nova morada introduzida, ou uma string vazia se o utilizador
     *         premir Enter sem introduzir texto.
     */
    public String pedirNovaMorada() {
        System.out.println("\n--- ATUALIZAR DADOS ---");
        return Consola.lerString("Introduza a nova Morada (ou prima Enter para manter a atual): ");
    }

    /** Informa que a morada foi atualizada com sucesso. */
    public void mostrarSucessoAtualizacaoMorada() {
        System.out.println(">> Morada atualizada com sucesso e guardada no sistema!");
    }

    /** Informa que nenhuma alteração foi efectuada na morada. */
    public void mostrarSemAlteracaoMorada() {
        System.out.println(">> Nenhuma alteração efetuada na morada.");
    }

    /**
     * Solicita a nova password ao utilizador, com ocultação de caracteres quando a consola o permite.
     * <p>
     * Nota: Este método NÃO é cancelável com '0' (o '0' pode fazer parte da password).
     * O cancelamento é feito premindo Enter sem introduzir texto, o que retorna uma string vazia.
     *
     * @return A nova password introduzida, ou uma string vazia se o utilizador
     *         premir Enter (cancelamento).
     */
    public String pedirNovaPassword() {
        System.out.println("\n--- ALTERAR PASSWORD ---");
        System.out.print("Introduza a nova Password (ou prima Enter para cancelar): ");
        if (System.console() != null) {
            char[] passwordChars = System.console().readPassword();
            return new String(passwordChars).trim();
        } else {
            return new java.util.Scanner(System.in).nextLine().trim();
        }
    }

    /** Informa que a password foi alterada com sucesso. */
    public void mostrarSucessoAtualizacaoPassword() {
        System.out.println(">> Password alterada com sucesso!");
    }

    /** Informa que a operação de alteração de password foi cancelada pelo utilizador. */
    public void mostrarCancelamentoPassword() {
        System.out.println(">> Operação cancelada. A password não foi alterada.");
    }

    /**
     * Exibe o saldo devedor atual do estudante.
     *
     * @param divida Valor da dívida (propinas em atraso).
     */
    public void mostrarSaldoDevedor(double divida) {
        System.out.println("\n--- DADOS FINANCEIROS ---");
        System.out.println(">> O seu saldo devedor atual (propinas) é: " + divida + "€");
    }

    /**
     * Apresenta as opções de pagamento e lê a escolha do utilizador.
     *
     * @return Opção escolhida: 1 (Pagamento Total), 2 (Pagamento Parcial) ou 0 (Cancelar).
     */
    public int pedirTipoPagamento() {
        System.out.println("\n--- OPÇÕES DE PAGAMENTO ---");
        System.out.println("1 - Pagamento Total");
        System.out.println("2 - Pagamento Parcial");
        System.out.println("0 - Cancelar");
        System.out.print("Escolha uma opção: ");
        return Consola.lerOpcaoMenu();
    }

    /**
     * Solicita o valor a pagar no pagamento parcial.
     *
     * @param dividaAtual Valor total da dívida (utilizado apenas para mostrar o máximo)
     */
    public double pedirValorPagamentoParcial(double dividaAtual) {
        return Consola.lerDouble("Introduza o valor a pagar (Máx: " + dividaAtual + "€): ");
    }

    /** Informa que o valor introduzido é inválido (negativo, zero ou superior à dívida). */
    public void mostrarErroValorInvalido() {
        System.out.println(">> Erro: O valor introduzido é inválido ou excede o montante em dívida.");
    }

    /** Informa que o pagamento foi efetuado com sucesso. */
    public void mostrarSucessoPagamento() {
        System.out.println(">> Pagamento efetuado com sucesso. Saldo regularizado!");
    }

    /** Informa que o estudante não tem pagamentos pendentes. */
    public void mostrarSemPagamentosPendentes() {
        System.out.println(">> Não tem pagamentos pendentes.");
    }

    /** Mensagem exibida ao sair do portal do estudante (logout). */
    public void mostrarDespedida() {
        System.out.println(">> A sair do portal do estudante...");
    }

    /** Mensagem exibida quando o utilizador escolhe uma opção inválida no menu. */
    public void mostrarOpcaoInvalida() {
        System.out.println(">> Opção inválida. Tente novamente.");
    }

    /** Mensagem de erro genérica para leitura da opção (quando a entrada não é numérica). */
    public void mostrarErroLeitura() {
        System.out.println(">> Erro na leitura da opção. Por favor, insira um número válido.");
    }

    /** Mensagem exibida quando o utilizador cancela uma operação (digitando '0' durante um pedido de dados). */
    public void mostrarOperacaoCancelada() {
        System.out.println(">> Operação cancelada. A regressar ao menu...");
    }
}