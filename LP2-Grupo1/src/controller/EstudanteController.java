package controller;

import common.ConfigApp;
import bll.EstudanteBLL;
import bll.PagamentoBLL;
import bll.HorarioBLL;
import bll.PresencaBLL;
import bll.JustificacaoBLL;
import bll.EstadoInvalidoException;
import utils.CancelamentoException;
import model.Aula;
import model.Justificacao;
import model.TipoJustificacao;
import dal.HistoricoDAL;
import dal.HistoricoDALFile;
import dal.HistoricoDALSql;
import model.Estudante;
import model.RepositorioDados;
import utils.Consola;
import view.EstudanteView;

import java.util.List;

/**
 * Controlador responsável por gerir o painel do Estudante.
 * Liga a EstudanteView às BLLs correspondentes, sem aceder a DALs
 * nem a ficheiros CSV diretamente.
  */
public class EstudanteController {

    private final RepositorioDados repositorio;
    private final Estudante estudanteAtivo;
    private final EstudanteView view;
    private final EstudanteBLL estudanteBll;
    private final PagamentoBLL pagamentoBll;
    private final HistoricoDAL historicoDAL =
            ConfigApp.isModoSql() ? new HistoricoDALSql() : new HistoricoDALFile();
    private HorarioBLL horarioBll;
    private HorarioBLL horarioBll() { if (horarioBll == null) horarioBll = new HorarioBLL(); return horarioBll; }
    private PresencaBLL presencaBll;
    private PresencaBLL presencaBll() { if (presencaBll == null) presencaBll = new PresencaBLL(); return presencaBll; }
    private JustificacaoBLL justificacaoBll;
    private JustificacaoBLL justificacaoBll() { if (justificacaoBll == null) justificacaoBll = new JustificacaoBLL(); return justificacaoBll; }

    public EstudanteController(RepositorioDados repositorio, Estudante estudanteAtivo) {
        this.repositorio = repositorio;
        this.estudanteAtivo = estudanteAtivo;
        this.view = new EstudanteView();
        this.estudanteBll = new EstudanteBLL();
        this.pagamentoBll = new PagamentoBLL();
        this.historicoDAL.inicializar();
    }

    public void iniciar() {
        boolean aExecutar = true;
        while (aExecutar) {
            try {
                int opcao = view.mostrarMenuPrincipal();
                switch (opcao) {
                    case 1: visualizarDadosPessoais();
                        break;
                    case 2: atualizarMorada();
                        break;
                    case 3: alterarPassword();
                        break;
                    case 4: consultarDadosFinanceiros();
                        break;
                    case 5: verUcsInscritas();
                        break;
                    case 6: verNotas();
                        break;
                    case 7: consultarHistorico();
                        break;
                    case 8: verMeuHorario();
                        break;
                    case 9: marcarPresenca();
                        break;
                    case 10: justificarFalta();
                        break;
                    case 11: consultarJustificacoes();
                        break;
                    case 0:
                        view.mostrarDespedida();
                        repositorio.limparSessao();
                        aExecutar = false;
                        break;
                    default:
                        view.mostrarOpcaoInvalida();
                }
            } catch (Exception e) {
                view.mostrarErroLeitura();
            }
        }
    }


    /** Mostra o horário do estudante no ano letivo ativo. */
    private void verMeuHorario() {
        int ano = utils.Config.getAnoAtual();
        view.mostrarHorario(horarioBll().listarHorarioEstudante(
                estudanteAtivo.getSiglaCurso(), estudanteAtivo.getAnoCurricular(), ano));
        Consola.pausar();
    }

    /** Marca presença numa aula — só permitido depois de o docente registar a aula. */
    private void marcarPresenca() {
        try {
            int ano = utils.Config.getAnoAtual();
            List<Aula> aulas = horarioBll().listarHorarioEstudante(
                    estudanteAtivo.getSiglaCurso(), estudanteAtivo.getAnoCurricular(), ano);
            view.mostrarHorario(aulas);
            if (aulas.isEmpty()) { Consola.pausar(); return; }
            int idAula = Consola.lerInt("Id da aula em que vai marcar presenca");
            presencaBll().marcarPresencaEstudante(estudanteAtivo.getNumeroMecanografico(), idAula);
            Consola.imprimirSucesso("Presenca marcada.");
        } catch (CancelamentoException e) {
            Consola.imprimirInfo("Operacao cancelada.");
        } catch (EstadoInvalidoException e) {
            Consola.imprimirErro(e.getMessage());
        }
        Consola.pausar();
    }

    /** Submete um pedido de justificação de falta ao gestor. */
    private void justificarFalta() {
        try {
            List<TipoJustificacao> tipos = justificacaoBll().listarTiposJustificacao();
            if (tipos.isEmpty()) {
                Consola.imprimirInfo("Nao ha tipos de justificacao definidos. Contacte o gestor.");
                Consola.pausar();
                return;
            }
            int ano = utils.Config.getAnoAtual();
            List<Aula> aulas = horarioBll().listarHorarioEstudante(
                    estudanteAtivo.getSiglaCurso(), estudanteAtivo.getAnoCurricular(), ano);
            view.mostrarHorario(aulas);
            int idAula = Consola.lerInt("Id da aula a justificar");
            Consola.imprimirTitulo("Tipos de Justificacao");
            for (TipoJustificacao t : tipos) Consola.imprimirInfo(t.getId() + " - " + t.getNome());
            int idTipo = Consola.lerInt("Id do tipo de justificacao");
            justificacaoBll().criarJustificacao(estudanteAtivo.getNumeroMecanografico(), idAula, idTipo);
            Consola.imprimirSucesso("Pedido de justificacao submetido. Aguarda aprovacao do gestor.");
        } catch (CancelamentoException e) {
            Consola.imprimirInfo("Operacao cancelada.");
        } catch (EstadoInvalidoException e) {
            Consola.imprimirErro(e.getMessage());
        }
        Consola.pausar();
    }

    /** Mostra ao estudante o estado das suas justificações. */
    private void consultarJustificacoes() {
        List<Justificacao> js = justificacaoBll().listarPorAluno(estudanteAtivo.getNumeroMecanografico());
        Consola.imprimirTitulo("As Minhas Justificacoes");
        if (js.isEmpty()) {
            Consola.imprimirInfo("Ainda nao submeteu justificacoes.");
        } else {
            for (Justificacao j : js) {
                Consola.imprimirInfo("Aula #" + j.getIdAula() + " | tipo " + j.getIdTipoJustificacao()
                        + " | estado: " + j.getEstado());
            }
        }
        Consola.pausar();
    }

    /**
     * Mostra os dados pessoais e o percurso académico do estudante.
     */
    private void visualizarDadosPessoais() {
        view.mostrarDadosPessoais(estudanteAtivo);
    }

    private void atualizarMorada() {
        String novaMorada = view.pedirNovaMorada();
        if (!novaMorada.isEmpty()) {
            estudanteBll.atualizarMorada(estudanteAtivo, novaMorada);
            view.mostrarSucessoAtualizacaoMorada();
        } else {
            view.mostrarSemAlteracaoMorada();
        }
    }

    private void alterarPassword() {
        String novaPass = view.pedirNovaPassword();
        if (!novaPass.isEmpty()) {
            estudanteBll.alterarPassword(estudanteAtivo, novaPass);
            view.mostrarSucessoAtualizacaoPassword();
        } else {
            view.mostrarCancelamentoPassword();
        }
    }

    /**
     * Mostra o histórico de pagamentos e o saldo devedor.
     * Se existir dívida, oferece as opções de pagamento total ou parcial.
     */
    private void consultarDadosFinanceiros() {
        view.mostrarDadosFinanceiros(estudanteAtivo);

        double divida = estudanteAtivo.getSaldoDevedor();
        if (divida <= 0) {
            view.mostrarSemPagamentosPendentes();
            return;
        }
        int opcao = view.pedirTipoPagamento(divida);
        double valorAPagar;

        switch (opcao) {
            case 1:
                valorAPagar = divida; // pagamento total
                break;
            case 2:
                valorAPagar = view.pedirValorPagamentoParcial(divida); // pagamento parcial
                break;
            default:
                return; // cancelar
        }

        if (valorAPagar <= 0) return;

        boolean sucesso = pagamentoBll.processarPagamento(estudanteAtivo, valorAPagar);
        if (sucesso) {
            view.mostrarSucessoPagamento(valorAPagar, estudanteAtivo.getSaldoDevedor());
        } else {
            view.mostrarErroValorInvalido();
        }
    }

    private void verUcsInscritas() {
        String info = estudanteBll.obterInfoInscricoes(estudanteAtivo);
        view.mostrarInscricoes(info);
    }

    private void verNotas() {
        String notas = estudanteBll.obterNotasDoEstudante(estudanteAtivo);
        Consola.imprimirTitulo("Minhas Notas");
        System.out.println(notas);
        Consola.pausar();
    }
    private void consultarHistorico() {
        try {
            Consola.imprimirTitulo("O Meu Histórico Académico");


            int ano = utils.Consola.lerInt("Introduza o Ano Letivo que deseja consultar");

            java.util.List<String> historico = historicoDAL.consultarHistoricoPorAluno(estudanteAtivo.getNumeroMecanografico());

            boolean encontrou = false;
            for (String registo : historico) {
                String[] p = registo.split(";");
                // Só mostra se o ano do ficheiro for igual ao ano digitado!
                if (p.length >= 5 && Integer.parseInt(p[0].trim()) == ano) {
                    System.out.printf("  Ano: %-5s | UC: %-6s | Notas: %-15s | %s%n", p[0], p[2], p[3], p[4]);
                    encontrou = true;
                }
            }

            if (!encontrou) {
                System.out.println("  Não foram encontrados registos para o ano " + ano);
            }
            Consola.pausar();
        } catch (utils.CancelamentoException e) {
            view.mostrarOperacaoCancelada();
        }
    }
}