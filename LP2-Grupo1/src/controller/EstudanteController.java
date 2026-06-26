package controller;

import bll.*;
import common.ConfigApp;
import dal.HistoricoDAL;
import dal.HistoricoDALFile;
import dal.HistoricoDALSql;
import model.*;
import utils.Config;
import utils.Consola;
import view.EstudanteView;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
                    case 8: verHorario(); break;
                    case 9: marcarPresenca(); break;
                    case 10: menuJustificacoes(); break;
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

// ============================================================
// =========== Horários, Presenças e Justificações ============
// ============================================================

    /**
     * Mostra o horário semanal do estudante.
     */
    private void verHorario() {
        try {
            int anoAtual = Config.getAnoAtual();
            String siglaCurso = estudanteAtivo.getSiglaCurso();
            int anoCurricular = estudanteAtivo.getAnoCurricular();

            Consola.imprimirInfo("Ver horário:");
            Consola.imprimirMenu(new String[]{"Semana atual", "Escolher intervalo"}, "Cancelar");
            int opcao = Consola.lerOpcaoMenu();

            HorarioBLL horarioBll = new HorarioBLL();
            List<Aula> aulas;

            if (opcao == 1) {
                LocalDate hoje = LocalDate.now();
                LocalDate inicio = hoje.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                LocalDate fim = hoje.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
                aulas = horarioBll.listarHorarioEstudante(siglaCurso, anoCurricular, anoAtual, inicio, fim);
            } else if (opcao == 2) {
                LocalDate[] intervalo = Consola.pedirIntervaloDatas();
                aulas = horarioBll.listarHorarioEstudante(siglaCurso, anoCurricular, anoAtual, intervalo[0], intervalo[1]);
            } else {
                return;
            }

            view.mostrarHorario(aulas);
        } catch (Exception e) {
            Consola.imprimirErro(e.getMessage());
        }
    }

    /**
     * Marca presença do estudante numa aula.
     */
    private void marcarPresenca() {
        try {
            Consola.imprimirTitulo("Marcar Presença");
            int ano = Config.getAnoAtual();
            String siglaCurso = estudanteAtivo.getSiglaCurso();
            int anoCurricular = estudanteAtivo.getAnoCurricular();

            HorarioBLL horarioBll = new HorarioBLL();

            // 1. Listar todas as aulas do estudante
            List<Aula> todasAulas = horarioBll.listarHorarioEstudante(siglaCurso, anoCurricular, ano);
            if (todasAulas.isEmpty()) {
                Consola.imprimirInfo("Não tem aulas agendadas.");
                return;
            }

            // 2. Extrair UCs únicas
            Set<String> siglasSet = new HashSet<>();
            for (Aula a : todasAulas) siglasSet.add(a.getSiglaUC());
            List<String> siglasUC = new ArrayList<>(siglasSet);

            Consola.imprimirTitulo("Suas Unidades Curriculares");
            for (int i = 0; i < siglasUC.size(); i++) {
                System.out.println("  [" + (i + 1) + "] " + siglasUC.get(i));
            }
            int idx = Consola.lerInt("Escolha a UC") - 1;
            if (idx < 0 || idx >= siglasUC.size()) return;
            String siglaUC = siglasUC.get(idx);

            // 3. Pedir data
            LocalDate data = Consola.pedirData("Data da aula (DD-MM-YYYY)");

            // 4. Listar aulas dessa UC nesse dia
            List<Aula> aulasDia = horarioBll.listarPorUCEData(siglaUC, ano, data);
            if (aulasDia.isEmpty()) {
                Consola.imprimirInfo("Não há aulas da UC " + siglaUC + " nesse dia.");
                return;
            }

            // 5. Mostrar aulas com ID
            view.mostrarAulasParaPresenca(aulasDia);
            int idAula = Consola.lerInt("ID da Aula para marcar presença");

            // 6. Validar se o docente já abriu
            PresencaBLL presencaBll = new PresencaBLL();
            if (presencaBll.presencasEstaoAbertas(idAula)) {
                // Estudante pode marcar
                if (presencaBll.estudanteMarcouPresenca(estudanteAtivo.getNumeroMecanografico(), idAula)) {
                    Consola.imprimirInfo("Já marcou presença para esta aula.");
                    return;
                }
                presencaBll.marcarPresencaEstudante(estudanteAtivo.getNumeroMecanografico(), idAula);
                Consola.imprimirSucesso("Presença registada com sucesso!");
            } else if (presencaBll.docenteJaMarcou(idAula)) {
                Consola.imprimirErro("As presenças para esta aula já foram fechadas pelo docente.");
            } else {
                Consola.imprimirErro("O docente ainda não abriu as presenças para esta aula.");
            }
        } catch (Exception e) {
            Consola.imprimirErro(e.getMessage());
        }
    }

    private void menuJustificacoes() {
        boolean voltar = false;
        while (!voltar) {
            int opcao = view.mostrarSubMenuJustificacoes();
            switch (opcao) {
                case 1: justificarFaltas(); break;
                case 2: verJustificacoes(); break;
                case 0: voltar = true; break;
                default: view.mostrarOpcaoInvalida();
            }
        }
    }

    private void justificarFaltas() {
        try {
            Consola.imprimirTitulo("Justificar Faltas");

            // Obter todas as aulas do estudante (passadas)
            HorarioBLL horarioBll = new HorarioBLL();
            int anoAtual = Config.getAnoAtual();
            List<Aula> todasAulas = horarioBll.listarHorarioEstudante(
                    estudanteAtivo.getSiglaCurso(),
                    estudanteAtivo.getAnoCurricular(),
                    anoAtual
            );

            if (todasAulas.isEmpty()) {
                Consola.imprimirInfo("Não tem aulas agendadas.");
                return;
            }

            // Filtrar aulas passadas e verificar presença
            LocalDate hoje = LocalDate.now();
            PresencaBLL presencaBll = new PresencaBLL();
            JustificacaoBLL justificacaoBll = new JustificacaoBLL();

            List<Aula> faltas = new ArrayList<>();
            for (Aula a : todasAulas) {
                if (a.getData().isAfter(hoje)) continue; // futura
                if (!presencaBll.estudanteMarcouPresenca(estudanteAtivo.getNumeroMecanografico(), a.getId())) {
                    // Verificar se já tem justificação pendente ou aceite
                    List<Justificacao> justs = justificacaoBll.listarPorAluno(estudanteAtivo.getNumeroMecanografico());
                    boolean justificada = false;
                    for (Justificacao j : justs) {
                        if (j.getIdAula() == a.getId() && ("PENDENTE".equals(j.getEstado()) || "APROVADA".equals(j.getEstado()))) {
                            justificada = true;
                            break;
                        }
                    }
                    if (!justificada) {
                        faltas.add(a);
                    }
                }
            }

            if (faltas.isEmpty()) {
                Consola.imprimirInfo("Não tem faltas para justificar.");
                return;
            }

            // Mostrar faltas
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            System.out.println("  Aulas com falta (pode justificar):");
            System.out.printf("  %-6s | %-8s | %-12s%n", "ID", "UC", "Data");
            for (Aula a : faltas) {
                System.out.printf("  %-6d | %-8s | %-12s%n", a.getId(), a.getSiglaUC(), a.getData().format(fmt));
            }

            int idAula = Consola.lerInt("ID da aula a justificar (0 para cancelar)");
            if (idAula == 0) return;

            // Verificar se a aula está na lista de faltas
            Aula escolhida = null;
            for (Aula a : faltas) {
                if (a.getId() == idAula) {
                    escolhida = a;
                    break;
                }
            }
            if (escolhida == null) {
                Consola.imprimirErro("Aula não encontrada ou não tem falta.");
                return;
            }

            // Listar tipos de justificação
            // Estatutos do estudante (ou disponiveis) - mostrados ao submeter justificacao
            List<model.EstatutoEstudante> meusEstatutos =
                    justificacaoBll.listarEstatutosDoEstudante(estudanteAtivo.getNumeroMecanografico());
            if (!meusEstatutos.isEmpty()) {
                Consola.imprimirInfo("Os teus estatutos:");
                for (model.EstatutoEstudante est : meusEstatutos) {
                    System.out.println("    - " + est.getNome());
                }
            } else {
                List<model.EstatutoEstudante> disponiveis = justificacaoBll.listarEstatutosDisponiveis();
                if (!disponiveis.isEmpty()) {
                    Consola.imprimirInfo("Estatutos disponiveis (pede ao gestor para te atribuir):");
                    for (model.EstatutoEstudante est : disponiveis) {
                        System.out.println("    - " + est.getNome());
                    }
                }
            }

            List<TipoJustificacao> tipos = justificacaoBll.listarTiposJustificacao();
            if (tipos.isEmpty()) {
                Consola.imprimirErro("Não existem tipos de justificação. Contacte o gestor.");
                return;
            }
            Consola.imprimirTitulo("Tipos de Justificação");
            for (int i = 0; i < tipos.size(); i++) {
                System.out.println("  [" + (i + 1) + "] " + tipos.get(i));
            }
            int idxTipo = Consola.lerInt("Escolha o tipo") - 1;
            if (idxTipo < 0 || idxTipo >= tipos.size()) return;
            int idTipo = tipos.get(idxTipo).getId();

            justificacaoBll.criarJustificacao(estudanteAtivo.getNumeroMecanografico(), idAula, idTipo);
            Consola.imprimirSucesso("Justificação enviada para aprovação.");

        } catch (Exception e) {
            Consola.imprimirErro(e.getMessage());
        }
    }

    private void verJustificacoes() {
        try {
            Consola.imprimirTitulo("Minhas Justificações");
            JustificacaoBLL bll = new JustificacaoBLL();
            List<Justificacao> justs = bll.listarPorAluno(estudanteAtivo.getNumeroMecanografico());

            if (justs.isEmpty()) {
                Consola.imprimirInfo("Não tem justificações.");
                return;
            }

            System.out.printf("  %-6s | %-8s | %-15s | %-10s%n", "Aula", "UC", "Data", "Estado");
            for (Justificacao j : justs) {
                Aula a = new HorarioBLL().buscarPorId(j.getIdAula());
                if (a == null) continue;
                String data = a.getData().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                System.out.printf("  %-6d | %-8s | %-15s | %-10s%n",
                        j.getIdAula(), a.getSiglaUC(), data, j.getEstado());
            }
            Consola.pausar();
        } catch (Exception e) {
            Consola.imprimirErro(e.getMessage());
        }
    }
}