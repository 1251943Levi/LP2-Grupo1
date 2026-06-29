package controller;

import common.ConfigApp;
import model.*;
import utils.*;
import view.GestorView;
import dal.UcDAL;
import dal.UcDALFile;
import dal.UcDALSql;
import bll.GestorBLL;
import bll.EstudanteBLL;
import bll.UcBLL;
import bll.DocenteBLL;
import bll.DepartamentoBLL;
import bll.CursoBLL;
import bll.HorarioBLL;
import bll.JustificacaoBLL;
import bll.EstadoInvalidoException;
import utils.CancelamentoException;
import utils.Validador;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Controlador responsável por gerir as interações e permissões do Gestor.
 * Liga a GestorView às BLLs correspondentes.
 */
public class GestorController {

    private final RepositorioDados repo;
    private final Gestor gestor;
    private final GestorView view;
    private final GestorBLL gestorBll;
    private final EstudanteBLL estudanteBll;
    private final UcBLL ucBll;
    private final DocenteBLL docenteBll = new DocenteBLL();
    private final UcDAL ucDAL = ConfigApp.isModoSql() ? new UcDALSql() : new UcDALFile();
    private HorarioBLL horarioBll;

    /** Instancia a HorarioBLL apenas quando necessário. */
    private HorarioBLL horarioBll() {
        if (horarioBll == null) horarioBll = new HorarioBLL();
        return horarioBll;
    }
    private JustificacaoBLL justificacaoBll;
    private JustificacaoBLL justificacaoBll() {
        if (justificacaoBll == null) justificacaoBll = new JustificacaoBLL();
        return justificacaoBll;
    }

    public GestorController(RepositorioDados repo, Gestor gestor) {
        this.repo = repo;
        this.gestor = gestor;
        this.view = new GestorView();
        this.gestorBll = new GestorBLL();
        this.estudanteBll = new EstudanteBLL();
        this.ucBll  = new UcBLL();
    }

    /**
     * Inicia o ciclo principal de execução do menu do Gestor.
     * Gere a navegação principal e o logout.
     */
    public void iniciar() {
        boolean correr = true;
        while (correr) {
            try {
                int opcao = view.mostrarMenu();
                switch (opcao) {
                    case 1: menuGerirEstudante(); break;
                    case 2: menuGerirDocente(); break;
                    case 3: menuGerirDepartamento(); break;
                    case 4: menuGerirUcs(); break;
                    case 5: menuGerirCurso(); break;
                    case 6: menuEstatisticas(); break;
                    case 7: menuAnoLetivo(); break;
                    case 8: consultarHistoricoAno(); break;
                    case 9: listarDevedores(); break;
                    case 10: alterarPassword(); break;
                    case 11: menuGerirHorarios(); break;
                    case 12: menuGerirTiposJustificacao(); break;
                    case 13: aprovarJustificacoes(); break;
                    case 14: menuGerirEstatutos(); break;
                    case 0:
                        view.mostrarDespedida();
                        repo.limparSessao();
                        correr = false;
                        break;
                    default:
                        view.mostrarOpcaoInvalida();
                }
            } catch (Exception e) {
                view.mostrarErroLeituraOpcao();
            }
        }
    }


    /** Submenu CRUD da gestão de horários (aulas) do ano letivo. */
    private void menuGerirHorarios() {
        boolean voltar = false;
        while (!voltar) {
            int opcao = view.mostrarSubMenuHorarios();
            switch (opcao) {
                case 1: definirHorario(); break;   // Adicionar Aula
                case 2: listarHorario(); break;    // Listar Aulas
                case 3: removerAula(); break;      // Remover Aula
                case 4: editarAula(); break;       // Editar Aula
                case 0: voltar = true; break;
                default: view.mostrarOpcaoInvalida();
            }
        }
    }

    /** Cria aulas num intervalo de datas (um dia da semana fixo), com as validações da BLL. */
    private void definirHorario() {
        try {
            Consola.imprimirTitulo("Definir Horário");
            Consola.imprimirInfo("Regras: 18h-23h30 - pausa 20h-20h30 - blocos de 1h ou 2h - max 5h/dia - sem sobreposicao de docente.");
            int anoLetivo = utils.Config.getAnoAtual();
            if (anoLetivo <= 0) {
                Consola.imprimirErro("Nao ha ano letivo ativo. Crie/inicie um ano letivo primeiro.");
                Consola.pausar();
                return;
            }

            oferecerLista("Deseja ver a lista de UCs?", ucBll.obterListaUcs());
            String siglaUc = Consola.lerString("Sigla da UC").toUpperCase();
            UnidadeCurricular uc = ucBll.procurarUCCompleta(siglaUc);
            if (uc == null) { view.mostrarErroNaoEncontrado("UC"); Consola.pausar(); return; }
            if (uc.getDocenteResponsavel() == null) {
                Consola.imprimirErro("A UC " + siglaUc + " nao tem docente responsavel atribuido.");
                Consola.pausar();
                return;
            }
            String siglaDocente = uc.getDocenteResponsavel().getSigla();
            oferecerLista("Deseja ver a lista de cursos?", gestorBll.obterListaCursos());
            String siglaCurso = Consola.lerString("Sigla do Curso a que pertence este horario").toUpperCase();

            Consola.imprimirInfo("Ano letivo: " + anoLetivo + " | Docente responsavel: " + siglaDocente);
            LocalDate dataInicio = lerData("Data de inicio (DD-MM-AAAA)");
            LocalDate dataFim    = lerData("Data de fim (DD-MM-AAAA)");
            int dia              = Consola.lerInt("Dia da semana (1=Seg, 2=Ter, 3=Qua, 4=Qui, 5=Sex)");
            LocalTime horaInicio = lerHora("Hora de inicio (HH:MM)");
            int bloco            = Consola.lerInt("Bloco em horas (1 ou 2)");

            List<Aula> criadas = horarioBll().criarAulasEmIntervalo(
                    siglaUc, siglaCurso, siglaDocente, dataInicio, dataFim, dia, horaInicio, bloco, anoLetivo);
            Consola.imprimirSucesso(criadas.size() + " aula(s) criada(s) para " + siglaUc + ".");
            if (!criadas.isEmpty()) {
                int horas = horarioBll().horasSemanaisUC(siglaUc, anoLetivo, criadas.get(0).getData());
                if (horas < 2) {
                    Consola.imprimirInfo("Aviso: a UC " + siglaUc + " tem apenas " + horas
                            + "h/semana (recomendado: no minimo 2h, no maximo 6h).");
                }
            }
        } catch (CancelamentoException e) {
            Consola.imprimirInfo("Operacao cancelada.");
        } catch (EstadoInvalidoException e) {
            Consola.imprimirErro(e.getMessage());
        } catch (Exception e) {
            Consola.imprimirErro("Erro ao definir horario: " + e.getMessage());
        }
        Consola.pausar();
    }

    /** Lista as aulas do ano letivo ativo (todas, ou de uma UC à escolha). */
    private void listarHorario() {
        try {
            int anoLetivo = utils.Config.getAnoAtual();
            if (Consola.lerSimNao("Filtrar por uma UC específica?")) {
                oferecerLista("Deseja ver a lista de UCs?", ucBll.obterListaUcs());
                String siglaUc = Consola.lerString("Sigla da UC").toUpperCase();
                view.mostrarHorario(horarioBll().listarPorUC(siglaUc, anoLetivo));
            } else {
                view.mostrarHorario(horarioBll().listarPorAnoLetivo(anoLetivo));
            }
        } catch (CancelamentoException e) {
            Consola.imprimirInfo("Operacao cancelada.");
        }
        Consola.pausar();
    }

    /** Remove uma aula pelo seu id. */
    private void removerAula() {
        try {
            int anoLetivo = utils.Config.getAnoAtual();
            view.mostrarHorario(horarioBll().listarPorAnoLetivo(anoLetivo));
            int id = Consola.lerInt("Id da aula a remover");
            if (horarioBll().removerAula(id)) Consola.imprimirSucesso("Aula removida.");
            else Consola.imprimirErro("Aula nao encontrada.");
        } catch (CancelamentoException e) {
            Consola.imprimirInfo("Operacao cancelada.");
        }
        Consola.pausar();
    }

    /** Edita uma aula existente: data, hora de início e/ou bloco (Enter mantém o valor atual). */
    private void editarAula() {
        try {
            int anoLetivo = utils.Config.getAnoAtual();
            view.mostrarHorario(horarioBll().listarPorAnoLetivo(anoLetivo));
            int id = Consola.lerInt("Id da aula a editar");
            Aula aula = horarioBll().buscarPorId(id);
            if (aula == null) { Consola.imprimirErro("Aula nao encontrada."); Consola.pausar(); return; }

            Consola.imprimirInfo("Aula atual: " + aula.getData() + " | "
                    + aula.getHoraInicio() + "-" + aula.getHoraFim()
                    + " | UC " + aula.getSiglaUC() + " | Bloco " + aula.getBloco() + "h");
            Consola.imprimirInfo("Deixe em branco (Enter) para manter o valor atual.");

            String sData = Consola.lerStringOpcional("Nova data (DD-MM-AAAA)");
            if (!sData.isEmpty()) {
                String[] p = sData.split("[-/]");
                aula.setData(LocalDate.of(Integer.parseInt(p[2].trim()),
                        Integer.parseInt(p[1].trim()), Integer.parseInt(p[0].trim())));
            }
            String sHora = Consola.lerStringOpcional("Nova hora de inicio (HH:MM)");
            if (!sHora.isEmpty()) {
                String[] p = sHora.replace("h", ":").replace("H", ":").split(":");
                int min = (p.length > 1 && !p[1].trim().isEmpty()) ? Integer.parseInt(p[1].trim()) : 0;
                LocalTime nova = LocalTime.of(Integer.parseInt(p[0].trim()), min);
                aula.setHoraInicio(nova);
                aula.setHoraFim(nova.plusHours(aula.getBloco()));
            }
            String sBloco = Consola.lerStringOpcional("Novo bloco em horas (1 ou 2)");
            if (!sBloco.isEmpty()) {
                int b = Integer.parseInt(sBloco.trim());
                if (b == 1 || b == 2) {
                    aula.setBloco(b);
                    aula.setHoraFim(aula.getHoraInicio().plusHours(b));
                } else {
                    Consola.imprimirErro("Bloco deve ser 1 ou 2. Mantido o anterior.");
                }
            }
            horarioBll().atualizarAula(aula);
            Consola.imprimirSucesso("Aula atualizada com sucesso.");
        } catch (CancelamentoException e) {
            Consola.imprimirInfo("Operacao cancelada.");
        } catch (EstadoInvalidoException e) {
            Consola.imprimirErro(e.getMessage());
        } catch (Exception e) {
            Consola.imprimirErro("Erro ao editar aula: " + e.getMessage());
        }
        Consola.pausar();
    }

    /** Le uma data no formato DD-MM-AAAA (aceita tambem DD/MM/AAAA). */
    private LocalDate lerData(String prompt) {
        while (true) {
            String s = Consola.lerString(prompt);
            try {
                String[] partes = s.split("[-/]");
                return LocalDate.of(Integer.parseInt(partes[2].trim()), Integer.parseInt(partes[1].trim()), Integer.parseInt(partes[0].trim()));
            } catch (Exception e) {
                Consola.imprimirErro("Data invalida. Use o formato DD-MM-AAAA.");
            }
        }
    }

    /** Le uma hora no formato HH:MM (aceita tambem HHhMM). */
    private LocalTime lerHora(String prompt) {
        while (true) {
            String s = Consola.lerString(prompt).replace("h", ":").replace("H", ":");
            try {
                String[] partes = s.split(":");
                int min = (partes.length > 1 && !partes[1].trim().isEmpty()) ? Integer.parseInt(partes[1].trim()) : 0;
                return LocalTime.of(Integer.parseInt(partes[0].trim()), min);
            } catch (Exception e) {
                Consola.imprimirErro("Hora invalida. Use o formato HH:MM (ex: 18:00).");
            }
        }
    }


    // ---------- TIPOS DE JUSTIFICAÇÃO ----------

    /** Submenu de gestão dos tipos de justificação. */
    private void menuGerirTiposJustificacao() {
        boolean voltar = false;
        while (!voltar) {
            int opcao = view.mostrarSubMenuTiposJustificacao();
            switch (opcao) {
                case 1: listarTiposJustificacao(); break;
                case 2: criarTipoJustificacao(); break;
                case 3: removerTipoJustificacao(); break;
                case 0: voltar = true; break;
                default: view.mostrarOpcaoInvalida();
            }
        }
    }

    private void listarTiposJustificacao() {
        Consola.imprimirTitulo("Tipos de Justificacao");
        List<TipoJustificacao> tipos = justificacaoBll().listarTiposJustificacao();
        if (tipos.isEmpty()) Consola.imprimirInfo("Sem tipos definidos.");
        else for (TipoJustificacao t : tipos) Consola.imprimirInfo(t.getId() + " - " + t.getNome() + " (" + t.getDescricao() + ")");
        Consola.pausar();
    }

    private void criarTipoJustificacao() {
        try {
            String nome = Consola.lerString("Nome do tipo de justificacao");
            String desc = Consola.lerString("Descricao");
            justificacaoBll().adicionarTipo(new TipoJustificacao(0, nome, desc));
            Consola.imprimirSucesso("Tipo de justificacao criado.");
        } catch (CancelamentoException e) { Consola.imprimirInfo("Operacao cancelada."); }
        catch (EstadoInvalidoException e) { Consola.imprimirErro(e.getMessage()); }
        Consola.pausar();
    }

    private void removerTipoJustificacao() {
        try {
            listarTiposJustificacao();
            int id = Consola.lerInt("Id do tipo a remover (0 cancelar)");
            if (id == 0) return;
            justificacaoBll().removerTipo(id);
            Consola.imprimirSucesso("Tipo removido.");
        } catch (CancelamentoException e) { Consola.imprimirInfo("Operacao cancelada."); }
        catch (EstadoInvalidoException e) { Consola.imprimirErro(e.getMessage()); }
        Consola.pausar();
    }

    // ---------- APROVAÇÃO DE JUSTIFICAÇÕES ----------

    /** Lista as justificações pendentes com detalhe (aluno, UC, data, tipo) e processa uma. */
    private void aprovarJustificacoes() {
        try {
            Consola.imprimirTitulo("Aprovar Justificacoes");
            List<Justificacao> pendentes = justificacaoBll().listarPendentes();
            if (pendentes.isEmpty()) { Consola.imprimirInfo("Nao ha justificacoes pendentes."); Consola.pausar(); return; }

            List<TipoJustificacao> tipos = justificacaoBll().listarTiposJustificacao();
            System.out.printf("  %-6s | %-22s | %-12s | %-15s%n", "ID", "Aluno", "Data", "Tipo");
            for (Justificacao j : pendentes) {
                Estudante e = estudanteBll.procurarPorNumMec(j.getNumMec());
                Aula a = horarioBll().buscarPorId(j.getIdAula());
                String nome = (e != null) ? e.getNome() : ("#" + j.getNumMec());
                String data = (a != null) ? a.getData().toString() : "—";
                String tipoNome = nomeTipo(tipos, j.getIdTipoJustificacao());
                System.out.printf("  %-6d | %-22s | %-12s | %-15s%n", j.getId(), nome, data, tipoNome);
            }

            int id = Consola.lerInt("Id da justificacao a processar (0 cancelar)");
            if (id == 0) return;
            Justificacao escolhida = null;
            for (Justificacao j : pendentes) if (j.getId() == id) { escolhida = j; break; }
            if (escolhida == null) { Consola.imprimirErro("Id invalido."); Consola.pausar(); return; }

            Estudante e = estudanteBll.procurarPorNumMec(escolhida.getNumMec());
            Aula a = horarioBll().buscarPorId(escolhida.getIdAula());
            Consola.imprimirInfo("Detalhe da justificacao:");
            Consola.imprimirInfo("  Aluno: " + (e != null ? e.getNome() + " (" + e.getNumeroMecanografico() + ")" : "#" + escolhida.getNumMec()));
            if (a != null) {
                Consola.imprimirInfo("  UC: " + a.getSiglaUC());
                Consola.imprimirInfo("  Data: " + a.getData() + " | Hora: " + a.getHoraInicio() + "-" + a.getHoraFim());
            }
            Consola.imprimirInfo("  Tipo: " + nomeTipo(tipos, escolhida.getIdTipoJustificacao()));

            boolean aceite = Consola.lerSimNao("Aprovar esta justificacao?");
            String obs = Consola.lerStringOpcional("Observacao (Enter para nenhuma)");
            justificacaoBll().processarJustificacao(id, aceite, obs);
            Consola.imprimirSucesso(aceite ? "Justificacao aprovada." : "Justificacao rejeitada.");
        } catch (CancelamentoException e) { Consola.imprimirInfo("Operacao cancelada."); }
        catch (EstadoInvalidoException e) { Consola.imprimirErro(e.getMessage()); }
        Consola.pausar();
    }

    /** Nome de um tipo de justificação a partir do seu id (ou "N/A"). */
    private String nomeTipo(List<TipoJustificacao> tipos, int idTipo) {
        for (TipoJustificacao t : tipos) if (t.getId() == idTipo) return t.getNome();
        return "N/A";
    }

    // ---------- ESTATUTOS DE ESTUDANTE ----------

    /** Submenu de gestão dos estatutos de estudante. */
    private void menuGerirEstatutos() {
        boolean voltar = false;
        while (!voltar) {
            int opcao = view.mostrarSubMenuEstatutos();
            switch (opcao) {
                case 1: listarEstatutos(); break;
                case 2: criarEstatuto(); break;
                case 3: removerEstatuto(); break;
                case 4: atribuirEstatuto(); break;
                case 5: verEstatutosDeEstudante(); break;
                case 0: voltar = true; break;
                default: view.mostrarOpcaoInvalida();
            }
        }
    }

    private void listarEstatutos() {
        Consola.imprimirTitulo("Estatutos");
        List<EstatutoEstudante> ests = justificacaoBll().listarEstatutosDisponiveis();
        if (ests.isEmpty()) Consola.imprimirInfo("Sem estatutos definidos.");
        else for (EstatutoEstudante es : ests) Consola.imprimirInfo(es.getId() + " - " + es.getNome() + " (" + es.getDescricao() + ")");
        Consola.pausar();
    }

    private void criarEstatuto() {
        try {
            String nome = Consola.lerString("Nome do estatuto");
            String desc = Consola.lerString("Descricao");
            justificacaoBll().adicionarEstatuto(new EstatutoEstudante(0, nome, desc));
            Consola.imprimirSucesso("Estatuto criado.");
        } catch (CancelamentoException e) { Consola.imprimirInfo("Operacao cancelada."); }
        catch (EstadoInvalidoException e) { Consola.imprimirErro(e.getMessage()); }
        Consola.pausar();
    }

    private void removerEstatuto() {
        try {
            listarEstatutos();
            int id = Consola.lerInt("Id do estatuto a remover (0 cancelar)");
            if (id == 0) return;
            justificacaoBll().removerEstatuto(id);
            Consola.imprimirSucesso("Estatuto removido.");
        } catch (CancelamentoException e) { Consola.imprimirInfo("Operacao cancelada."); }
        catch (EstadoInvalidoException e) { Consola.imprimirErro(e.getMessage()); }
        Consola.pausar();
    }

    private void atribuirEstatuto() {
        try {
            Consola.imprimirTitulo("Estatutos disponiveis");
            for (EstatutoEstudante es : justificacaoBll().listarEstatutosDisponiveis())
                Consola.imprimirInfo(es.getId() + " - " + es.getNome());
            if (Consola.lerSimNao("Deseja ver a lista de estudantes?")) {
                for (Estudante est : estudanteBll.listarAtivos()) view.mostrarEstudante(est);
            }
            int numMec = Consola.lerInt("Numero mecanografico do estudante");
            if (estudanteBll.obterPorNumMec(numMec) == null) { view.mostrarErroNaoEncontrado("Estudante"); Consola.pausar(); return; }
            int idEstatuto = Consola.lerInt("Id do estatuto a atribuir");
            justificacaoBll().atribuirEstatuto(numMec, idEstatuto);
            Consola.imprimirSucesso("Estatuto atribuido ao estudante " + numMec + ".");
        } catch (CancelamentoException e) { Consola.imprimirInfo("Operacao cancelada."); }
        Consola.pausar();
    }

    /** Mostra os estatutos atribuídos a um estudante. */
    private void verEstatutosDeEstudante() {
        try {
            int numMec = Consola.lerInt("Numero mecanografico do estudante (0 cancelar)");
            if (numMec == 0) return;
            List<EstatutoEstudante> ests = justificacaoBll().listarEstatutosDoEstudante(numMec);
            if (ests.isEmpty()) { Consola.imprimirInfo("Este estudante nao tem estatutos atribuidos."); Consola.pausar(); return; }
            Consola.imprimirTitulo("Estatutos do Estudante " + numMec);
            for (EstatutoEstudante es : ests) Consola.imprimirInfo(es.getId() + " - " + es.getNome() + " (" + es.getDescricao() + ")");
            Consola.pausar();
        } catch (CancelamentoException e) { Consola.imprimirInfo("Operacao cancelada."); }
    }

    /** Oferece ver uma lista antes de pedir um identificador dela (consistência de UX). */
    private void oferecerLista(String pergunta, String[] itens) {
        if (Consola.lerSimNao(pergunta)) {
            if (itens == null || itens.length == 0) { Consola.imprimirInfo("(lista vazia)"); return; }
            for (String it : itens) Consola.imprimirInfo(it);
        }
    }


    private void listarEstadosCursos() {
        Consola.imprimirTitulo("Estados dos Cursos");
        List<Curso> cursos = cursoBll.listarTodos();
        if (cursos.isEmpty()) { Consola.imprimirInfo("Sem cursos."); Consola.pausar(); return; }
        for (Curso c : cursos) {
            cursoBll.atualizarEstadoCurricular(c.getSigla()); // recalcula automaticamente
            Curso atual = cursoBll.obterPorSigla(c.getSigla());
            String motivo = cursoBll.motivoNaoApto(c.getSigla());
            String extra = motivo.isEmpty() ? "" : "  (" + motivo + ")";
            Consola.imprimirInfo(c.getSigla() + " - " + c.getNome() + " | Estado: " + atual.getEstado() + extra);
        }
        Consola.pausar();
    }

    private void ativarCurso() {
        try {
            oferecerLista("Deseja ver a lista de cursos?", gestorBll.obterListaCursos());
            String sigla = Consola.lerString("Sigla do curso a ativar").toUpperCase();
            if (gestorBll.alterarEstadoCurso(sigla, EstadoCurricular.ATIVO))
                Consola.imprimirSucesso("Curso " + sigla + " ativado.");
            else
                Consola.imprimirErro("Nao reune condicoes: " + cursoBll.motivoNaoApto(sigla));
        } catch (CancelamentoException e) { Consola.imprimirInfo("Operacao cancelada."); }
        Consola.pausar();
    }

    private void desativarCurso() {
        try {
            oferecerLista("Deseja ver a lista de cursos?", gestorBll.obterListaCursos());
            String sigla = Consola.lerString("Sigla do curso a desativar").toUpperCase();
            if (gestorBll.alterarEstadoCurso(sigla, EstadoCurricular.INATIVO))
                Consola.imprimirSucesso("Curso " + sigla + " desativado (INATIVO).");
            else
                Consola.imprimirErro("Curso nao encontrado.");
        } catch (CancelamentoException e) { Consola.imprimirInfo("Operacao cancelada."); }
        Consola.pausar();
    }

    private void ativarUC() {
        try {
            oferecerLista("Deseja ver a lista de UCs?", ucBll.obterListaUcs());
            String sigla = Consola.lerString("Sigla da UC a ativar").toUpperCase();
            if (gestorBll.alterarEstadoUC(sigla, EstadoCurricular.ATIVO))
                Consola.imprimirSucesso("UC " + sigla + " ativada.");
            else
                Consola.imprimirErro("UC nao reune condicoes (precisa de docente responsavel e momentos definidos) ou nao existe.");
        } catch (CancelamentoException e) { Consola.imprimirInfo("Operacao cancelada."); }
        Consola.pausar();
    }

    private void desativarUC() {
        try {
            oferecerLista("Deseja ver a lista de UCs?", ucBll.obterListaUcs());
            String sigla = Consola.lerString("Sigla da UC a desativar").toUpperCase();
            if (gestorBll.alterarEstadoUC(sigla, EstadoCurricular.INATIVO))
                Consola.imprimirSucesso("UC " + sigla + " desativada (INATIVO).");
            else
                Consola.imprimirErro("UC nao encontrada.");
        } catch (CancelamentoException e) { Consola.imprimirInfo("Operacao cancelada."); }
        Consola.pausar();
    }

    private void menuGerirEstudante() {
        boolean voltar = false;
        while (!voltar) {
            int opcao = view.mostrarSubMenuEstudante();
            switch (opcao) {
                case 1: executarCriarEstudante(); break;
                case 2: executarListarEstudantes(); break;
                case 3: executarEditarEstudante(); break;
                case 4: executarApagarEstudante(); break;
                case 0: voltar = true; break;
                default: view.mostrarOpcaoInvalida();
            }
        }
    }

    private void executarCriarEstudante() {
        executarRegistoEstudante();  // reutiliza o método existente
    }

    private void executarListarEstudantes() {
        // A2: a listagem de estudantes do gestor mostra apenas os ativos
        // (alunos que concluíram o curso saem das listas ativas).
        List<Estudante> estudantes = estudanteBll.listarAtivos();
        if (estudantes.isEmpty()) {
            view.mostrarErroNaoEncontrado("Estudantes");
            return;
        }
        view.mostrarTitulo("Lista de Estudantes");
        for (Estudante e : estudantes) {
            view.mostrarEstudante(e);
        }
        Consola.pausar();
    }

    private void executarEditarEstudante() {
        try {
            if (Consola.lerSimNao("Deseja ver a lista de estudantes?")) {
                for (Estudante est : estudanteBll.listarAtivos()) view.mostrarEstudante(est);
            }
            int numMec = view.pedirNumeroEstudante();
            Estudante e = estudanteBll.obterPorNumMec(numMec);
            if (e == null) {
                view.mostrarErroNaoEncontrado("Estudante");
                return;
            }

            view.mostrarEstudante(e);
            view.mostrarMensagemModoEdicao();

            // Editar Nome
            String novoNome = view.pedirNovoNomeEstudante();
            if (!novoNome.isEmpty()) {
                if (Validador.isNomeValido(novoNome)) {
                    e.setNome(novoNome);
                } else {
                    view.mostrarErroNomeInvalido();
                }
            }

            // Editar NIF
            String novoNif = view.pedirNovoNifEstudante();
            if (!novoNif.isEmpty()) {
                if (Validador.validarNif(novoNif) && !gestorBll.isNifDuplicado(novoNif)) {
                    e.setNif(novoNif);
                } else {
                    view.mostrarErroNifInvalidoOuDuplicado();
                }
            }

            // Editar Data de Nascimento
            String novaData = view.pedirNovaDataNascimentoEstudante();
            if (!novaData.isEmpty()) {
                int resultado = Validador.validarDataNascimentoDetalhado(novaData);
                switch (resultado) {
                    case 0:
                        e.setDataNascimento(novaData);
                        break;
                    case 1:
                        view.mostrarErroDataInexistente();
                        break;
                    case 2:
                        view.mostrarErroDataFutura();
                        break;
                    case 3:
                        view.mostrarErroIdadeForaLimites();
                        break;
                }
            }

            // Editar Morada
            String novaMorada = view.pedirNovaMoradaEstudante();
            if (!novaMorada.isEmpty()) {
                e.setMorada(novaMorada);
            }

            estudanteBll.atualizarEstudante(e);
            view.mostrarSucessoAtualizacao("Estudante");

        } catch (CancelamentoException ex) {
            view.mostrarOperacaoCancelada();
        }
    }

    private void executarApagarEstudante() {
        try {
            if (Consola.lerSimNao("Deseja ver a lista de estudantes?")) {
                for (Estudante est : estudanteBll.listarAtivos()) view.mostrarEstudante(est);
            }
            int numMec = view.pedirNumeroEstudante();
            Estudante e = estudanteBll.obterPorNumMec(numMec);
            if (e == null) {
                view.mostrarErroNaoEncontrado("Estudante");
                return;
            }
            view.mostrarEstudante(e);
            if (view.confirmarRemocaoBoolean("estudante " + e.getNome())) {
                if (estudanteBll.removerEstudante(numMec)) {
                    view.mostrarSucessoRemocao("Estudante");
                } else {
                    view.mostrarErroRemocao("Estudante");
                }
            }
        } catch (CancelamentoException ex) {
            view.mostrarOperacaoCancelada();
        }
    }

    private void consultarHistoricoAno() {
        try {
            view.mostrarMensagem("--- Consulta de Histórico ---");
            int ano = utils.Consola.lerInt("Introduza o Ano Letivo a pesquisar");

            java.util.List<String> registos = gestorBll.obterHistoricoPorAno(ano);

            if (registos.isEmpty()) {
                view.mostrarMensagem("Nenhum registo encontrado para o ano " + ano);
                return;
            }

            view.mostrarMensagem("Registos do Ano " + ano + " (Ano;NumMec;UC;Notas;Estado):");
            for (String r : registos) {
                view.mostrarMensagem("  " + r);
            }
            utils.Consola.pausar();
        } catch (Exception e) {
            view.mostrarOperacaoCancelada();
        }
    }



    // ------------------------------------------------------------
    // MENU GERIR DOCENTE
    // ------------------------------------------------------------
    private void menuGerirDocente() {
        boolean voltar = false;
        while (!voltar) {
            int opcao = view.mostrarSubMenuDocente();
            switch (opcao) {
                case 1: executarCriarDocente(); break;
                case 2: executarListarDocentes(); break;
                case 3: executarEditarDocente(); break;
                case 4: executarApagarDocente(); break;
                case 0: voltar = true; break;
                default: view.mostrarOpcaoInvalida();
            }
        }
    }

    private void executarCriarDocente() {
        // Reutiliza o método de registo existente (executarRegistoDocente)
        executarRegistoDocente();
    }

    private void executarListarDocentes() {
        List<Docente> docentes = docenteBll.listarTodos();
        if (docentes.isEmpty()) {
            view.mostrarErroNaoEncontrado("Docentes");
            return;
        }
        view.mostrarListaDocentes(docentes);
        Consola.pausar();
    }

    private void executarEditarDocente() {
        try {
            oferecerLista("Deseja ver a lista de docentes?", gestorBll.obterListaDocentes());
            String sigla = view.pedirSiglaDocenteParaGestao();
            Docente d = docenteBll.obterPorSigla(sigla);
            if (d == null) {
                view.mostrarErroNaoEncontrado("Docente");
                return;
            }

            view.mostrarDocente(d);
            view.mostrarMensagemModoEdicao();

            // Editar Nome
            String novoNome = view.pedirNovoNomeDocente();
            if (!novoNome.isEmpty()) {
                if (Validador.isNomeValido(novoNome)) {
                    d.setNome(novoNome);
                } else {
                    view.mostrarErroNomeInvalido();
                }
            }

            // Editar NIF
            String novoNif = view.pedirNovoNifDocente();
            if (!novoNif.isEmpty()) {
                if (Validador.validarNif(novoNif) && !gestorBll.isNifDuplicado(novoNif)) {
                    d.setNif(novoNif);
                } else {
                    view.mostrarErroNifInvalidoOuDuplicado();
                }
            }

            // Editar Morada
            String novaMorada = view.pedirNovaMoradaDocente();
            if (!novaMorada.isEmpty()) {
                d.setMorada(novaMorada);
            }

            // Editar Data Nascimento
            String novaData = view.pedirNovaDataNascimentoDocente();
            if (!novaData.isEmpty()) {
                int resultado = Validador.validarDataNascimentoDetalhado(novaData);
                switch (resultado) {
                    case 0:
                        d.setDataNascimento(novaData);
                        break;
                    case 1:
                        view.mostrarErroDataInexistente();
                        break;
                    case 2:
                        view.mostrarErroDataFutura();
                        break;
                    case 3:
                        view.mostrarErroIdadeForaLimites();
                        break;
                }
            }

            docenteBll.atualizarDocente(d);
            view.mostrarSucessoAtualizacao("Docente");

        } catch (CancelamentoException e) {
            view.mostrarOperacaoCancelada();
        }
    }

    private void executarApagarDocente() {
        try {
            oferecerLista("Deseja ver a lista de docentes?", gestorBll.obterListaDocentes());
            String sigla = view.pedirSiglaDocenteParaGestao();
            Docente d = docenteBll.obterPorSigla(sigla);
            if (d == null) {
                view.mostrarErroNaoEncontrado("Docente");
                return;
            }
            view.mostrarDocente(d);
            if (!view.confirmarRemocaoBoolean("docente " + d.getNome())) {
                return;
            }

            if (docenteBll.temUcAtribuida(sigla)) {
                view.mostrarErroDocenteComUcs();
                return;
            }

            if (docenteBll.removerDocente(sigla)) {
                view.mostrarSucessoRemocao("Docente");
            } else {
                view.mostrarErroRemocao("Docente");
            }
        } catch (CancelamentoException e) {
            view.mostrarOperacaoCancelada();
        }
    }

    // --- Métodos de Registo ---

    /**
     * Coordena o registo de um novo docente.
     * Validação de NIF delegada à GestorBLL (que consulta as DALs).
     */
    private void executarRegistoDocente() {
        try {
            view.mostrarTituloRegistoDocente();

            String nome;
            do {
                nome = view.pedirNome();
                if (!Validador.isNomeValido(nome)) view.mostrarErroNomeInvalido();
            } while (!Validador.isNomeValido(nome));

            String nif;
            boolean nifInvalido, nifDuplicado;
            do {
                nif          = view.pedirNif();
                nifInvalido  = !Validador.validarNif(nif);
                nifDuplicado = !nifInvalido && gestorBll.isNifDuplicado(nif);
                if (nifInvalido)       view.mostrarErroNifInvalido();
                else if (nifDuplicado) view.mostrarErroNifDuplicado();
            } while (nifInvalido || nifDuplicado);

            String morada   = view.pedirMorada();
            String dataNasc;
            boolean dataValida = false;
            do {
                dataNasc = view.pedirDataNascimento();
                int resultado = Validador.validarDataNascimentoDetalhado(dataNasc);
                switch (resultado) {
                    case 0:
                        dataValida = true;
                        break;
                    case 1:
                        view.mostrarErroDataInexistente();
                        break;
                    case 2:
                        view.mostrarErroDataFutura();
                        break;
                    case 3:
                        view.mostrarErroIdadeForaLimites();
                        break;
                }
            } while (!dataValida);

            String[] resultado = gestorBll.registarDocente(nome, nif, morada, dataNasc);
            view.mostrarResumoRegistoDocente(resultado[0], resultado[1]);

        } catch (CancelamentoException e) {
            view.mostrarOperacaoCancelada();
        }
    }

    /**
     * Fluxo de registo de um novo Departamento.
     * Valida sigla não duplicada e nome não vazio.
     */
    private void executarRegistoDepartamento() {
        try {
            view.mostrarTituloRegistoDepartamento();

            String sigla;
            do {
                sigla = view.pedirSiglaDepartamento().toUpperCase().trim();
                if (sigla.isEmpty()) {
                    view.mostrarMensagem("ERRO: Sigla não pode estar vazia.");
                } else if (gestorBll.isDepartamentoDuplicado(sigla)) {
                    view.mostrarErroDepartamentoDuplicado();
                    sigla = "";
                }
            } while (sigla.isEmpty());

            String nome;
            do {
                nome = view.pedirNomeDepartamento().trim();
                if (nome.isEmpty()) view.mostrarMensagem("ERRO: Nome não pode estar vazio.");
            } while (nome.isEmpty());

            gestorBll.registarDepartamento(sigla, nome);
            view.mostrarResumoRegistoDepartamento(sigla, nome);

        } catch (CancelamentoException e) {
            view.mostrarOperacaoCancelada();
        }
    }


    /**
     * Coordena o registo de um novo estudante.
     * Número mecanográfico gerado automaticamente via EstudanteBLL.
     */
    private void executarRegistoEstudante() {
        try {
            view.mostrarTituloRegistoEstudante();
            int anoInscricao = repo.getAnoAtual();

            String nome;
            do {
                nome = view.pedirNome();
                if (!utils.Validador.isNomeValido(nome)) view.mostrarErroNomeInvalido();
            } while (!utils.Validador.isNomeValido(nome));

            String nif;
            boolean nifInvalido, nifDuplicado;
            do {
                nif          = view.pedirNif();
                nifInvalido  = !utils.Validador.validarNif(nif);
                nifDuplicado = !nifInvalido && gestorBll.isNifDuplicado(nif);
                if (nifInvalido)       view.mostrarErroNifInvalido();
                else if (nifDuplicado) view.mostrarErroNifDuplicado();
            } while (nifInvalido || nifDuplicado);

            String morada = view.pedirMorada();

            String dataNasc;
            boolean dataValida = false;
            do {
                dataNasc = view.pedirDataNascimento();
                int resultado = Validador.validarDataNascimentoDetalhado(dataNasc);
                switch (resultado) {
                    case 0:
                        dataValida = true;
                        break;
                    case 1:
                        view.mostrarErroDataInexistente();
                        break;
                    case 2:
                        view.mostrarErroDataFutura();
                        break;
                    case 3:
                        view.mostrarErroIdadeForaLimites();
                        break;
                }
            } while (!dataValida);

            // Com o ano letivo iniciado mostram-se os cursos inativos; caso contrario, os cursos com plano completo.
            bll.AnoLetivoBLL anoLetivoBll = new bll.AnoLetivoBLL();
            EstadoAnoLetivo estadoAno = anoLetivoBll.getEstadoAnoAtual();
            boolean anoIniciado = (estadoAno != null && estadoAno != EstadoAnoLetivo.PLANEAMENTO);

            String[] todosCursos = gestorBll.obterListaCursos();
            java.util.List<String> cursosAptos = new java.util.ArrayList<>();

            for (String cursoStr : todosCursos) {
                String sigla = cursoStr.split(" - ")[0];

                if (anoIniciado) {
                    if (cursoBll.avaliarEstado(sigla) != EstadoCurricular.ATIVO) {
                        cursosAptos.add(cursoStr);
                    }
                } else {
                    boolean temAno1 = ucDAL.contarUcsPorCursoEAno(sigla, 1, ConfigApp.PASTA_BD) > 0;
                    boolean temAno2 = ucDAL.contarUcsPorCursoEAno(sigla, 2, ConfigApp.PASTA_BD) > 0;
                    boolean temAno3 = ucDAL.contarUcsPorCursoEAno(sigla, 3, ConfigApp.PASTA_BD) > 0;
                    if (temAno1 && temAno2 && temAno3) {
                        cursosAptos.add(cursoStr);
                    }
                }
            }

            if (cursosAptos.isEmpty()) {
                view.mostrarMensagem(anoIniciado
                        ? "Erro: nao existem cursos inativos disponiveis para inscrever com o ano letivo iniciado."
                        : "Erro: Não existem cursos com UCs configuradas em todos os anos (1, 2 e 3).");
                return;
            }

            if (anoIniciado) {
                view.mostrarMensagem("Ano letivo INICIADO: a mostrar cursos INATIVOS (o aluno sera inscrito num curso que abrira quando reunir condicoes).");
            }

            String[] listaParaExibir = cursosAptos.toArray(new String[0]);
            view.mostrarListaCursos(listaParaExibir); 

            int escolha = view.pedirOpcaoCurso(listaParaExibir.length); //
            if (escolha == -1) { view.mostrarOperacaoCancelada(); return; }

            String siglaCurso = listaParaExibir[escolha - 1].split(" - ")[0];

            String email = gestorBll.registarEstudante(nome, nif, morada, dataNasc, siglaCurso, anoInscricao);

            if (email != null && !email.isEmpty()) {
                view.mostrarResumoRegistoEstudante(email);
            } else {
                view.mostrarMensagem("Erro ao processar o registo do estudante.");
            }

        } catch (utils.CancelamentoException e) {
            view.mostrarOperacaoCancelada();
        }
    }

    // ------------------------------------------------------------
    // MENU GERIR CURSO
    // ------------------------------------------------------------

    private final CursoBLL cursoBll = new CursoBLL();

    private void menuGerirCursos() {
        boolean voltar = false;
        while (!voltar) {
            int opcao = view.mostrarSubMenuCurso();
            switch (opcao) {
                case 1: executarCriarCurso(); break;
                case 2: executarListarCursos(); break;
                case 3: executarEditarCurso(); break;
                case 4: executarApagarCurso(); break;
                case 0: voltar = true; break;
                default: view.mostrarOpcaoInvalida();
            }
        }
    }

    private void executarCriarCurso() {
        try {
            view.mostrarTitulo("Criar Novo Curso");
            String sigla = view.pedirSiglaCurso();
            // Verifica se já existe
            if (cursoBll.obterPorSigla(sigla) != null) {
                view.mostrarErroCursoExistente();
                return;
            }
            String nome = view.pedirNomeCurso();

            // Listar departamentos para escolha
            String[] depts = gestorBll.obterListaDepartamentos();
            if (depts.length == 0) {
                view.mostrarErroSemDepartamentos();
                return;
            }
            view.mostrarListaCursos(depts); // reutiliza,
            String siglaDep;
            do {
                siglaDep = view.pedirDepartamento().toUpperCase();
                if (!gestorBll.isDepartamentoDuplicado(siglaDep)) {
                    view.mostrarErroDepartamentoNaoEncontrado();
                }
            } while (!gestorBll.isDepartamentoDuplicado(siglaDep));

            double propina = view.pedirPropinaCurso();
            if (propina < 0) {
                view.mostrarErroPropinaNegativa();
                return;
            }

            if (cursoBll.adicionarCurso(sigla, nome, siglaDep, propina)) {
                view.mostrarSucessoCriacao("Curso");
            } else {
                view.mostrarErroCriacaoCurso();
            }
        } catch (CancelamentoException e) {
            view.mostrarOperacaoCancelada();
        }
    }

    private void executarListarCursos() {
        List<Curso> cursos = cursoBll.listarTodos();
        if (cursos.isEmpty()) {
            view.mostrarErroNaoEncontrado("Cursos");
            return;
        }
        view.mostrarListaCursos(cursos);
        Consola.pausar();
    }

    private void executarEditarCurso() {
        try {
            oferecerLista("Deseja ver a lista de cursos?", gestorBll.obterListaCursos());
            String sigla = view.pedirSiglaCurso();
            Curso curso = cursoBll.obterPorSigla(sigla);
            if (curso == null) {
                view.mostrarErroNaoEncontrado("Curso");
                return;
            }
            if (!cursoBll.isAlteravel(sigla)) {
                view.mostrarErroCursoComAlocacoes();
                return;
            }
            view.mostrarCurso(curso);
            view.mostrarMensagemModoEdicao();

            String novoNome = view.pedirNovoNomeCurso();
            if (novoNome.isEmpty()) novoNome = null;

            String novaSiglaDep = view.pedirNovoSiglaDepartamentoCurso();
            if (novaSiglaDep.isEmpty()) novaSiglaDep = null;

            Double novaPropina = view.pedirNovaPropinaCurso();
            if (novaPropina != null) {
                double prop = novaPropina;
                if (prop < 0) {
                    view.mostrarErroPropinaNegativaMantida();
                } else {
                    // verificar 2 casas decimais
                    if (Math.round(prop * 100.0) / 100.0 != prop) {
                        view.mostrarErroPropinaDuasCasas();
                    } else {
                        novaPropina = prop;
                    }
                }
            }

            if (cursoBll.atualizarCurso(sigla, novoNome, novaSiglaDep, novaPropina)) {
                view.mostrarSucessoAtualizacao("Curso");
            } else {
                view.mostrarErroAtualizacaoCurso();
            }
        } catch (CancelamentoException e) {
            view.mostrarOperacaoCancelada();
        }
    }

    private void executarApagarCurso() {
        try {
            oferecerLista("Deseja ver a lista de cursos?", gestorBll.obterListaCursos());
            String sigla = view.pedirSiglaCurso();
            Curso curso = cursoBll.obterPorSigla(sigla);
            if (curso == null) {
                view.mostrarErroNaoEncontrado("Curso");
                return;
            }
            view.mostrarCurso(curso);
            if (!cursoBll.isAlteravel(sigla)) {
                view.mostrarErroCursoComAlocacoes();
                return;
            }
            if (view.confirmarRemocaoBoolean("curso " + curso.getNome())) {
                if (cursoBll.removerCurso(sigla)) {
                    view.mostrarSucessoRemocao("Curso");
                } else {
                    view.mostrarErroRemocao("Curso");
                }
            }
        } catch (CancelamentoException e) {
            view.mostrarOperacaoCancelada();
        }
    }

    // ------------------------------------------------------------
    // MENU GERIR DEPARTAMENTO
    // ------------------------------------------------------------

    private final DepartamentoBLL departamentoBll = new DepartamentoBLL();

    private void menuGerirDepartamento() {
        boolean voltar = false;
        while (!voltar) {
            int opcao = view.mostrarSubMenuDepartamento();
            switch (opcao) {
                case 1: executarCriarDepartamento(); break;
                case 2: executarListarDepartamentos(); break;
                case 3: executarEditarDepartamento(); break;
                case 4: executarApagarDepartamento(); break;
                case 0: voltar = true; break;
                default: view.mostrarOpcaoInvalida();
            }
        }
    }

    private void executarCriarDepartamento() {
        try {
            view.mostrarTitulo("Criar Departamento");
            String sigla = view.pedirSiglaDepartamento().toUpperCase();
            if (departamentoBll.obterPorSigla(sigla) != null) {
                view.mostrarErroDepartamentoDuplicado();
                return;
            }
            String nome = view.pedirNomeDepartamento();
            if (departamentoBll.adicionarDepartamento(sigla, nome)) {
                view.mostrarSucessoCriacao("Departamento");
            } else {
                view.mostrarErroCriarDepartamento();
            }
        } catch (CancelamentoException e) {
            view.mostrarOperacaoCancelada();
        }
    }

    private void executarListarDepartamentos() {
        List<Departamento> depts = departamentoBll.listarTodos();
        if (depts.isEmpty()) {
            view.mostrarErroNaoEncontrado("Departamentos");
            return;
        }
        view.mostrarListaDepartamentos(depts);
        Consola.pausar();
    }

    private void executarEditarDepartamento() {
        try {
            oferecerLista("Deseja ver a lista de departamentos?", gestorBll.obterListaDepartamentos());
            String sigla = view.pedirSiglaDepartamento().toUpperCase();
            Departamento dept = departamentoBll.obterPorSigla(sigla);
            if (dept == null) {
                view.mostrarErroNaoEncontrado("Departamento");
                return;
            }
            view.mostrarDepartamento(dept);
            view.mostrarMensagemModoEdicao();

            String novaSigla = view.pedirNovoSiglaDepartamento();
            if (novaSigla.isEmpty()) novaSigla = null;
            else novaSigla = novaSigla.toUpperCase();

            String novoNome = view.pedirNovoNomeDepartamento();
            if (novoNome.isEmpty()) novoNome = null;

            if (departamentoBll.atualizarDepartamento(sigla, novaSigla, novoNome)) {
                view.mostrarSucessoAtualizacao("Departamento");
            } else {
                view.mostrarErroAtualizarDepartamento();
            }
        } catch (CancelamentoException e) {
            view.mostrarOperacaoCancelada();
        }
    }

    private void executarApagarDepartamento() {
        try {
            oferecerLista("Deseja ver a lista de departamentos?", gestorBll.obterListaDepartamentos());
            String sigla = view.pedirSiglaDepartamento().toUpperCase();
            Departamento dept = departamentoBll.obterPorSigla(sigla);
            if (dept == null) {
                view.mostrarErroNaoEncontrado("Departamento");
                return;
            }
            view.mostrarDepartamento(dept);
            if (!view.confirmarRemocaoBoolean("departamento " + dept.getNome())) {
                return;
            }
            if (departamentoBll.removerDepartamento(sigla)) {
                view.mostrarSucessoRemocao("Departamento");
            } else {
                view.mostrarErroRemoverDepartamentoComCursos();
            }
        } catch (CancelamentoException e) {
            view.mostrarOperacaoCancelada();
        }
    }

    // --- Métodos de Estatísticas e Listagens ---

    /**
     * Solicita à BLL os dados estatísticos globais e apresenta a média
     * institucional através da View.
     */
    private void mostrarMediaGlobal() {
        view.mostrarCabecalhoMediaGlobal();
        double[] stats = gestorBll.calcularEstatisticasGlobais();
        if (stats == null)          { view.mostrarErroCarregarDados("Estudantes"); return; }
        if (stats[1] == 0)          { view.mostrarSemNotasRegistadas();           return; }
        view.mostrarMediaGlobal(stats[0] / stats[1], (int) stats[1]);
    }

    /**
     * Obtém o estudante com melhor desempenho académico através da BLL
     * e exibe os seus detalhes.
     */
    private void mostrarMelhorAluno() {
        view.mostrarCabecalhoMelhorAluno();
        Object[] resultado = gestorBll.obterMelhorAluno();
        if (resultado != null) {
            Estudante melhor = (Estudante) resultado[0];
            double media     = (double) resultado[1];
            view.mostrarInfoMelhorAluno(melhor.getNome(), melhor.getNumeroMecanografico(), media);
        } else {
            view.mostrarSemAlunosAvaliados();
        }
    }

    /**
     * Lista todos os estudantes que possuem saldo devedor (propinas em atraso).
     */
    private void listarDevedores() {
        view.mostrarCabecalhoDevedores();
        List<Estudante> devedores = gestorBll.obterListaDevedores();
        if (devedores.isEmpty()) { view.mostrarSemDevedores(); return; }
        for (Estudante e : devedores)
            view.mostrarEstudanteDevedor(
                    e.getNumeroMecanografico(), e.getNome(), e.getSaldoDevedor());
    }

    // --- Gestão de UCs ---

    /**
     * Recolhe dados da View para criar uma nova Unidade Curricular e
     * valida o limite máximo de UCs por ano via BLL.
     * A operação pode ser cancelada durante a introdução dos dados.
     */
    private void adicionarUc() {
        try {
            String[] cursos = gestorBll.obterListaCursos();
            String siglaCurso = null;
            int anoUc = 1;  // valor por defeito

            if (cursos.length == 0) {
                view.mostrarMensagem("Aviso: Não existem cursos registados. A UC será criada sem associação a curso e no 1º ano.");
            } else {
                view.mostrarListaCursos(cursos);
                view.mostrarOpcaoNaoAssociarCurso();

                int escolha = -1;
                while (escolha < 0 || escolha > cursos.length) {
                    try {
                        escolha = Consola.lerInt("Número do Curso (0-" + cursos.length + ")");
                        if (escolha < 0 || escolha > cursos.length) {
                            Consola.imprimirErro("Opção inválida. Escolha entre 0 e " + cursos.length + ".");
                        }
                    } catch (CancelamentoException e) {
                        throw e;
                    } catch (Exception e) {
                        Consola.imprimirErro("Número inválido.");
                    }
                }

                if (escolha == 0) {
                    // Não associar a curso - ano será 1 por defeito
                    siglaCurso = null;
                    view.mostrarMensagem("UC não associada a nenhum curso. Ano curricular definido por defeito como 1º.");
                } else {
                    siglaCurso = cursos[escolha - 1].split(" - ")[0];
                    // Se associou a curso, pedir o ano
                    try {
                        anoUc = Integer.parseInt(view.pedirAnoCurricular());
                        if (anoUc < 1 || anoUc > 3) {
                            view.mostrarMensagem("ERRO: Ano curricular deve ser 1, 2 ou 3. A operação foi cancelada.");
                            return;
                        }
                    } catch (NumberFormatException ex) {
                        view.mostrarMensagem("ERRO: Ano curricular inválido. A operação foi cancelada.");
                        return;
                    }
                }
            }

            // Se não existiam cursos, siglaCurso já é null, anoUc já é 1

            String siglaUc = view.pedirSiglaUc();
            String nomeUc  = view.pedirNomeUc();

            if (view.perguntarVerListagem("Docentes")) {
                view.mostrarResultadosListagem(gestorBll.obterListaDocentes());
            }

            String docente;
            do {
                docente = view.pedirSiglaDocente();
                if (!gestorBll.existeDocente(docente)) {
                    view.mostrarMensagem("ERRO: Docente não encontrado. Introduza uma sigla de um docente existente.");
                }
            } while (!gestorBll.existeDocente(docente));

            // Chamar a BLL (que deverá aceitar siglaCurso = null e ignorar o limite de 5 UCs por ano)
            if (gestorBll.adicionarUc(siglaCurso, anoUc, siglaUc, nomeUc, docente))
                view.mostrarSucessoCriacao("UC");
            else
                view.mostrarErroLimiteUcs(anoUc);
        } catch (CancelamentoException e) {
            view.mostrarOperacaoCancelada();
        }
    }

    /**
     * Permite a edição de uma UC existente, substituindo os dados antigos
     * pelos novos introduzidos pelo Gestor, garantindo docentes válidos.
     */
    private void editarUc() {
        try {
            String[] ucs = ucBll.obterListaUcs();
            if (ucs.length == 0) {
                view.mostrarErroNaoEncontrado("UCs");
                return;
            }
            view.mostrarListaUcs(ucs);
            int escolha = view.pedirOpcaoUc(ucs.length);
            if (escolha == -1) return;
            String siglaAntiga = ucs[escolha - 1].split(" - ")[0];

            UnidadeCurricular ucOriginal = ucBll.procurarUCCompleta(siglaAntiga);
            if (ucOriginal == null) {
                view.mostrarErroUcNaoEncontrada();
                return;
            }

            int anoOriginal = ucOriginal.getAnoCurricular();
            String cursoOriginal = "N/A";
            Curso[] cursos = ucOriginal.getCursos();
            if (cursos.length > 0 && cursos[0] != null) {
                cursoOriginal = cursos[0].getSigla();
            }

            view.mostrarMensagemModoEdicao();

            String novaSigla = view.pedirNovaSiglaUc();
            if (novaSigla.isEmpty()) novaSigla = siglaAntiga;

            String novoNome = view.pedirNovoNomeUc();
            if (novoNome.isEmpty()) novoNome = ucOriginal.getNome();

            if (view.perguntarVerListagem("Docentes")) {
                view.mostrarResultadosListagem(gestorBll.obterListaDocentes());
            }

            String novaSiglaDocente = view.pedirNovaSiglaDocenteUc();
            if (novaSiglaDocente.isEmpty()) {
                novaSiglaDocente = ucOriginal.getDocenteResponsavel().getSigla();
            } else {
                while (!gestorBll.existeDocente(novaSiglaDocente)) {
                    view.mostrarMensagem("ERRO: Docente não encontrado.");
                    novaSiglaDocente = view.pedirNovaSiglaDocenteUc();
                    if (novaSiglaDocente.isEmpty()) {
                        novaSiglaDocente = ucOriginal.getDocenteResponsavel().getSigla();
                        break;
                    }
                }
            }

            boolean sucesso = gestorBll.editarUc(
                    siglaAntiga,
                    novaSigla,
                    novoNome,
                    String.valueOf(anoOriginal),
                    novaSiglaDocente,
                    cursoOriginal);

            if (sucesso) {
                view.mostrarSucessoAtualizacao("UC");
            } else {
                view.mostrarErroEditarUc();
            }

        } catch (CancelamentoException e) {
            view.mostrarOperacaoCancelada();
        }
    }

    private void removerUc() {
        String[] ucs = ucBll.obterListaUcs();
        if (ucs.length == 0) {
            view.mostrarErroNaoEncontrado("UCs");
            return;
        }

        view.mostrarListaUcs(ucs);
        int escolha = view.pedirOpcaoUc(ucs.length);
        if (escolha == -1) {
            view.mostrarOperacaoCancelada();
            return;
        }
        String siglaUc = ucs[escolha - 1].split(" - ")[0];

        // Obter cursos associados antes de tentar remover
        List<String> cursosAssociados = ucDAL.obterCursosPorUc(siglaUc, ConfigApp.PASTA_BD);

        if (view.confirmarRemocaoBoolean(siglaUc)) {
            if (gestorBll.removerUc(siglaUc)) {
                view.mostrarSucessoRemocao("UC");
            } else {
                view.mostrarErroRemocaoUcComCursos(siglaUc, cursosAssociados);
            }
        }
    }

    private void removerAssociacaoUcCurso() {
        try {
            String[] ucs = ucBll.obterListaUcs();
            if (ucs.length == 0) {
                view.mostrarErroNaoEncontrado("UCs");
                return;
            }
            view.mostrarListaUcs(ucs);
            int escolhaUc = view.pedirOpcaoUc(ucs.length);
            if (escolhaUc == -1) return;
            String siglaUc = ucs[escolhaUc - 1].split(" - ")[0];

            // Obter cursos associados a esta UC
            List<String> cursosAssociados = ucDAL.obterCursosPorUc(siglaUc, ConfigApp.PASTA_BD);
            if (cursosAssociados.isEmpty()) {
                view.mostrarMensagem("Esta UC não está associada a nenhum curso.");
                return;
            }

            view.mostrarMensagem("Cursos associados a " + siglaUc + ":");
            for (int i = 0; i < cursosAssociados.size(); i++) {
                view.mostrarMensagem("  [" + (i+1) + "] " + cursosAssociados.get(i));
            }
            view.mostrarMensagem("  [0] Cancelar");
            int escolhaCurso = view.pedirOpcaoCurso(cursosAssociados.size());
            if (escolhaCurso == -1 || escolhaCurso == 0) return;
            String siglaCurso = cursosAssociados.get(escolhaCurso - 1);

            if (ucDAL.removerAssociacaoUcCurso(siglaUc, siglaCurso, ConfigApp.PASTA_BD)) {
                view.mostrarSucessoAssociacaoRemovida();
            } else {
                view.mostrarErroAssociacaoRemovida();
            }
        } catch (CancelamentoException e) {
            view.mostrarOperacaoCancelada();
        }
    }

    /**
     * Gere o sub-menu dedicado a consultas estatísticas.
     */
    private void menuEstatisticas() {
        boolean correr = true;
        while (correr) {
            int opcao = view.mostrarMenuEstatisticas();
            switch (opcao) {
                case 1: mostrarMediaGlobal(); break;
                case 2: mostrarMelhorAluno(); break;
                case 0: correr = false;       break;
                default: view.mostrarOpcaoInvalida();
            }
        }
    }

    /**
     * Gere o sub-menu para operações CRUD (Criar, Ler, Atualizar, Remover) em UCs.
     */
    private void menuGerirUcs() {
        boolean correr = true;
        while (correr) {
            int opcao = view.mostrarMenuCRUD("Unidades Curriculares");
            switch (opcao) {
                case 1: adicionarUc();                                          break;
                case 2: view.mostrarResultadosListagem(new String[] { gestorBll.listarTodasUcs() });break;
                case 3: editarUc();                                             break;
                case 4: removerUc();break;
                case 5: associarUcExistente(); break;
                case 6: removerAssociacaoUcCurso(); break;
                case 7: ativarUC(); break;
                case 8: desativarUC(); break;
                case 0: correr = false;                                         break;
                default: view.mostrarOpcaoInvalida();
            }
        }
    }

    /**
     * Gere o sub-menu para operações CRUD em Cursos.
     */
    private void menuGerirCurso() {
        boolean correr = true;
        while (correr) {
            int opcao = view.mostrarMenuCRUD("Cursos");
            switch (opcao) {
                case 1: executarCriarCurso(); break;
                case 2: view.mostrarResultadosListagem(new String[] { gestorBll.obterPainelCursos() }); break;
                case 3: executarEditarCurso(); break;
                case 4: executarApagarCurso(); break;
                case 5: listarUcsCurso(); break;
                case 6: listarEstadosCursos(); break;
                case 7: ativarCurso(); break;
                case 8: desativarCurso(); break;
                case 0: correr = false;   break;
                default: view.mostrarOpcaoInvalida();
            }
        }
    }

    /**
     * Permite a criação de um novo curso, validando o departamento
     * e garantindo que a propina é válida (máx 2 casas decimais e positiva).
     */
    private void adicionarCurso() {
        try {
            String siglaCurso = view.pedirSiglaCurso();
            String nomeCurso = view.pedirNomeCurso();

            if (view.perguntarVerListagem("Departamentos")) {
                view.mostrarResultadosListagem(gestorBll.obterListaDepartamentos());
            }

            String siglaDep;
            do {
                siglaDep = view.pedirDepartamento();
                if (!gestorBll.isDepartamentoDuplicado(siglaDep)) {
                    view.mostrarMensagem("ERRO: Departamento não encontrado. Introduza uma sigla existente.");
                }
            } while (!gestorBll.isDepartamentoDuplicado(siglaDep));

            double propina;
            boolean propinaValida = false;
            do {
                propina = view.pedirValorDouble("Propina anual (€)");

                if (propina != Math.round(propina * 100.0) / 100.0) {
                    view.mostrarMensagem("ERRO: A propina só pode ter até 2 casas decimais (cêntimos).");
                } else if (propina < 0) {
                    view.mostrarMensagem("ERRO: A propina não pode ter um valor negativo.");
                } else {
                    propinaValida = true;
                }
            } while (!propinaValida);

            gestorBll.adicionarCurso(siglaCurso, nomeCurso, siglaDep, propina);
            view.mostrarSucessoCriacao("Curso");

        } catch (CancelamentoException e) {
            view.mostrarOperacaoCancelada();
        }
    }

    /** Permite editar o nome, departamento e propina de um curso sem alocações. */
    private void editarCurso() {
        try {
            String[] cursos = gestorBll.listarTodosCursos();
            if (cursos.length == 0) { view.mostrarErroNaoEncontrado("Cursos"); return; }
            view.mostrarListaCursos(cursos);
            int escolha = view.pedirOpcaoCurso(cursos.length);
            if (escolha == -1) { view.mostrarOperacaoCancelada(); return; }
            String sigla = cursos[escolha - 1].split(" - ")[0];

            if (!gestorBll.isCursoAlteravel(sigla)) {
                view.mostrarErroCursoComAlocacoes(); return;
            }
            view.mostrarMensagemModoEdicao();

            String novoNome = view.pedirNomeCurso();

            String siglaDep;
            do {
                siglaDep = view.pedirDepartamento();
                if (!gestorBll.isDepartamentoDuplicado(siglaDep)) {
                    view.mostrarMensagem("ERRO: Departamento não encontrado. Introduza uma sigla existente.");
                }
            } while (!gestorBll.isDepartamentoDuplicado(siglaDep));

            double novaPropina;
            boolean propinaValida = false;
            do {
                novaPropina = view.pedirValorDouble("Nova Propina anual (€)");
                if (novaPropina != Math.round(novaPropina * 100.0) / 100.0) {
                    view.mostrarMensagem("ERRO: A propina só pode ter até 2 casas decimais (cêntimos).");
                } else if (novaPropina < 0) {
                    view.mostrarMensagem("ERRO: A propina não pode ter um valor negativo.");
                } else {
                    propinaValida = true;
                }
            } while (!propinaValida);

            boolean sucesso = gestorBll.editarCurso(sigla, novoNome, siglaDep, novaPropina);
            if (sucesso) view.mostrarSucessoAtualizacao("Curso");
        } catch (CancelamentoException e) { view.mostrarOperacaoCancelada(); }
    }

    /** Remove um curso que não tenha estudantes nem docentes alocados. */
    private void removerCurso() {
        String[] cursos = gestorBll.listarTodosCursos();
        if (cursos.length == 0) { view.mostrarErroNaoEncontrado("Cursos"); return; }
        view.mostrarListaCursos(cursos);
        int escolha = view.pedirOpcaoCurso(cursos.length);
        if (escolha == -1) { view.mostrarOperacaoCancelada(); return; }
        String sigla = cursos[escolha - 1].split(" - ")[0];

        if (!gestorBll.isCursoAlteravel(sigla)) {
            view.mostrarErroCursoComAlocacoes(); return;
        }
        if (view.confirmarRemocaoBoolean(sigla)) {
            if (gestorBll.removerCurso(sigla)) view.mostrarSucessoRemocao("Curso");
            else                               view.mostrarErroRemocao("Curso");
        }
    }

    /** Lista as UCs de um curso agrupadas por ano curricular. */
    private void listarUcsCurso() {
        try {
            String siglaCurso = obterSiglaCursoPelaView(false);
            if (siglaCurso == null || siglaCurso.isEmpty()) return;
            view.mostrarMensagem(gestorBll.listarUcsPorCurso(siglaCurso));
            utils.Consola.pausar();
        } catch (CancelamentoException e) { view.mostrarOperacaoCancelada(); }
    }

    /**
     * Fluxo para associar uma UC existente a um curso, com listagem opcional.
     */
    private void associarUcExistente() {
        try {
            String[] ucs = ucBll.obterListaUcs();
            if (ucs.length == 0) { view.mostrarErroNaoEncontrado("UCs"); return; }
            view.mostrarListaUcs(ucs);
            int escolhaUc = view.pedirOpcaoUc(ucs.length);
            if (escolhaUc == -1) return;
            String siglaUc = ucs[escolhaUc - 1].split(" - ")[0];
            String siglaCurso = obterSiglaCursoPelaView(false);

            int ano = Integer.parseInt(view.pedirAnoCurricular());
            if (ano < 1 || ano > 3) {
                view.mostrarMensagem("ERRO: Ano deve ser 1, 2 ou 3.");
                return;
            }

            if (gestorBll.associarUcExistente(siglaUc, siglaCurso, ano)) {
                view.mostrarSucessoAtualizacao("UC associada ao curso " + siglaCurso);
            } else {
                view.mostrarErroLimiteUcs(ano);
            }

        } catch (CancelamentoException | NumberFormatException e) {
            view.mostrarOperacaoCancelada();
        }
    }

    /**
     * Mostra a lista de cursos e devolve a sigla escolhida.
     * @param apenasComUcs Se true, obriga a escolher um curso que já tenha UCs (para Estudantes).
     * Se false, permite escolher qualquer curso (para Gestão de UCs).
     */
    private String obterSiglaCursoPelaView(boolean apenasComUcs) {
        String[] cursos = gestorBll.obterListaCursos();
        if (cursos.length == 0) return view.pedirSiglaCurso();

        bll.CursoBLL cursoBLL = new bll.CursoBLL();
        view.mostrarListaCursos(cursos);

        while (true) {
            int escolha = view.pedirOpcaoCurso(cursos.length);
            if (escolha == -1) throw new CancelamentoException();

            String sigla = cursos[escolha - 1].split(" - ")[0];

           if (apenasComUcs && !cursoBLL.verificarCursoTemUcs(sigla)) {
                view.mostrarMensagem("ERRO: Este curso ainda não tem UCs configuradas para o 1º ano. Escolha outro.");
                continue;
            }
            return sigla;
        }
    }

    private void menuAnoLetivo() {
        new AnoLetivoController(repo).iniciar(view);
    }

    private void alterarPassword() {
        view.mostrarCabecalhoAlterarPassword();
        String novaPass = view.pedirNovaPassword();
        if (!novaPass.trim().isEmpty()) {
            gestorBll.alterarPasswordGestor(gestor, novaPass);
            view.mostrarSucessoAlteracaoPassword();
        } else {
            view.mostrarCancelamentoPassword();
        }
    }
}