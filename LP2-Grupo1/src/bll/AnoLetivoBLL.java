package bll;

import dal.AnoLetivoDAL;
import dal.AvaliacaoDAL;
import dal.CursoDAL;
import dal.EstudanteDAL;
import dal.InscricaoDAL;
import dal.UcDAL;
import model.AnoLetivo;
import model.Avaliacao;
import model.Curso;
import model.EstadoAnoLetivo;
import model.Estudante;
import model.RepositorioDados;
import utils.Config;
import view.AnoLetivoView;   // FIX: necessário para as novas assinaturas iniciar/fechar
import view.GestorView;

import java.util.ArrayList;
import java.util.List;

/**
 * Lógica de negócio do módulo Ano Letivo — Sprint 6.
 * Ciclo de vida: Criar → Iniciar → Fechar → Avançar.
 * Em caso de violação de pré-condição é lançada EstadoInvalidoException.
 */
public class AnoLetivoBLL {

    private static final String PASTA_BD = "bd";
    private final EstudanteDAL estudanteDAL = new EstudanteDAL(PASTA_BD);

    // ============================================================
    // CRUD básico
    // ============================================================

    public void criar(int ano) {
        if (AnoLetivoDAL.procurarPorAno(ano, PASTA_BD) != null) {
            throw new EstadoInvalidoException(
                    "Já existe um ano letivo registado com o ano " + ano + ".");
        }
        AnoLetivoDAL.adicionar(new AnoLetivo(ano), PASTA_BD);
    }

    public void editar(int anoAntigo, int anoNovo) {
        AnoLetivo alvo = AnoLetivoDAL.procurarPorAno(anoAntigo, PASTA_BD);
        if (alvo == null)
            throw new EstadoInvalidoException("Ano " + anoAntigo + " não existe.");
        if (alvo.getEstado() != EstadoAnoLetivo.PLANEAMENTO)
            throw new EstadoInvalidoException(
                    "Só anos em PLANEAMENTO podem ser editados. Estado atual: " + alvo.getEstado() + ".");
        if (AnoLetivoDAL.procurarPorAno(anoNovo, PASTA_BD) != null)
            throw new EstadoInvalidoException(
                    "Já existe um ano letivo registado com o ano " + anoNovo + ".");
        AnoLetivoDAL.remover(anoAntigo, PASTA_BD);
        AnoLetivoDAL.adicionar(new AnoLetivo(anoNovo), PASTA_BD);
    }

    public List<AnoLetivo> listar() {
        return AnoLetivoDAL.listarTodos(PASTA_BD);
    }

    // ============================================================
    // Ciclo de vida — Iniciar / Fechar / Avançar (Sprint 6)
    // ============================================================

    /**
     * Inicia um ano letivo (PLANEAMENTO → INICIADO) com relatório de aptidão.
     *
     * FIX: assinatura corrigida para (int, AnoLetivoView) devolvendo List<String>,
     * conforme o AnoLetivoController da Sprint 6 (linha 94):
     *     List&lt;String&gt; cursosInativados = bll.iniciar(ano, view);
     *
     * Regras:
     *   - Bloqueia (lança exceção) se o ano não estiver em PLANEAMENTO.
     *   - Bloqueia se alguma UC não tiver momentos de avaliação definidos.
     *   - Cursos com 1.º ano sem quórum (1–4 alunos) são automaticamente inativados.
     *
     * @param ano  Ano letivo a iniciar.
     * @param view Vista do módulo (reservada para feedback interativo).
     * @return Lista de siglas de cursos inativados por falta de quórum (vazia se todos aptos).
     */
    public List<String> iniciar(int ano, AnoLetivoView view) {
        AnoLetivo alvo = AnoLetivoDAL.procurarPorAno(ano, PASTA_BD);
        if (alvo == null)
            throw new EstadoInvalidoException("Ano " + ano + " não existe.");
        if (alvo.getEstado() != EstadoAnoLetivo.PLANEAMENTO)
            throw new EstadoInvalidoException(
                    "Só anos em PLANEAMENTO podem ser iniciados. Estado atual: " + alvo.getEstado() + ".");

        // Momentos de avaliação em falta → bloqueia o arranque
        List<String> errosMomentos = validarMomentosUcs();
        if (!errosMomentos.isEmpty()) {
            StringBuilder msg = new StringBuilder("Bloqueado para iniciar — momentos de avaliação em falta:");
            for (String e : errosMomentos) msg.append("\n  - ").append(e);
            throw new EstadoInvalidoException(msg.toString());
        }

        // Quórum: cursos do 1.º ano com 1–4 alunos são inativados (não bloqueiam o arranque)
        List<String> cursosInativados = new ArrayList<>();
        CursoBLL cursoBll = new CursoBLL();
        String[] cursos = CursoDAL.obterListaCursos(PASTA_BD);
        for (String c : cursos) {
            String sigla = c.split(" - ")[0];
            int a1 = estudanteDAL.contarEstudantesPorCursoEAno(sigla, 1);
            if (a1 > 0 && a1 < 5) {
                Curso curso = cursoBll.procurarCursoCompleto(sigla);
                if (curso != null) {
                    curso.setEstado("Inativo");
                    CursoDAL.atualizarCurso(curso, PASTA_BD);
                    cursosInativados.add(sigla);
                }
            }
        }

        alvo.setEstado(EstadoAnoLetivo.INICIADO);
        AnoLetivoDAL.atualizar(alvo, PASTA_BD);
        return cursosInativados;
    }

    /**
     * Fecha um ano letivo (INICIADO → FECHADO) com verificação de notas e propinas.
     *
     * FIX: assinatura corrigida para (int, AnoLetivoView), conforme o
     * AnoLetivoController da Sprint 6 (linha 116): bll.fechar(ano, view);
     *
     * Se existirem pendências (notas por lançar ou propinas em dívida),
     * são listadas por categoria na mensagem da exceção e o fecho é bloqueado.
     *
     * @param ano  Ano letivo a fechar.
     * @param view Vista do módulo (reservada para feedback adicional).
     */
    public void fechar(int ano, AnoLetivoView view) {
        AnoLetivo alvo = AnoLetivoDAL.procurarPorAno(ano, PASTA_BD);
        if (alvo == null)
            throw new EstadoInvalidoException("Ano " + ano + " não existe.");
        if (alvo.getEstado() != EstadoAnoLetivo.INICIADO)
            throw new EstadoInvalidoException(
                    "Só anos INICIADOS podem ser fechados. Estado atual: " + alvo.getEstado() + ".");

        List<String> pendentesNotas    = validarNotasPendentes(ano);
        List<String> pendentesPropinas = validarPropinasPendentes();

        if (!pendentesNotas.isEmpty() || !pendentesPropinas.isEmpty()) {
            StringBuilder msg = new StringBuilder("Fecho bloqueado — existem pendências:");
            if (!pendentesNotas.isEmpty()) {
                msg.append("\n  [NOTAS EM FALTA]");
                for (String p : pendentesNotas) msg.append("\n    - ").append(p);
            }
            if (!pendentesPropinas.isEmpty()) {
                msg.append("\n  [PROPINAS EM DÍVIDA]");
                for (String p : pendentesPropinas) msg.append("\n    - ").append(p);
            }
            throw new EstadoInvalidoException(msg.toString());
        }

        alvo.setEstado(EstadoAnoLetivo.FECHADO);
        AnoLetivoDAL.atualizar(alvo, PASTA_BD);

        System.out.println("A calcular o aproveitamento e a guardar o histórico académico do ano " + ano + "...");

        List<Estudante> listaAlunos = new EstudanteBLL().carregarTodosCompleto();
        for (Estudante e : listaAlunos) {
            if (e == null) continue;
            for (int i = 0; i < e.getPercurso().getTotalAvaliacoes(); i++) {
                model.Avaliacao av = e.getPercurso().getHistoricoAvaliacoes()[i];
                if (av != null && av.getAnoLetivo() == ano) {
                    String notas = "";
                    for (int n = 0; n < av.getTotalAvaliacoesLancadas(); n++) {
                        notas += av.getResultados()[n] + " ";
                    }
                    String estado = av.isAprovado() ? "APROVADO" : "REPROVADO";
                    dal.HistoricoDAL.guardarRegistoHistorico(
                            ano, e.getNumeroMecanografico(),
                            av.getUc().getSigla(), notas.trim(), estado, PASTA_BD);
                }
            }
        }
        System.out.println("Histórico académico fechado com sucesso!");
    }

    public void avancar(RepositorioDados repo, GestorView view) {
        AnoLetivo atual = AnoLetivoDAL.obterAnoAtivo(Config.PASTA_BD);
        if (atual == null)
            throw new EstadoInvalidoException("Não existe ano letivo registado no sistema.");
        if (atual.getEstado() != EstadoAnoLetivo.FECHADO)
            throw new EstadoInvalidoException(
                    "Só anos FECHADOS podem avançar. Estado atual: " + atual.getEstado() + ".");

        new GestorBLL().avancarAnoLetivo(repo, view);

        int proximo = atual.getAno() + 1;
        if (AnoLetivoDAL.procurarPorAno(proximo, Config.PASTA_BD) == null)
            AnoLetivoDAL.adicionar(new AnoLetivo(proximo), Config.PASTA_BD);
    }

    // ============================================================
    // Estado — read-only
    // ============================================================

    public List<String> obterEstadoResumo() {
        List<String> linhas = new ArrayList<>();
        AnoLetivo atual = AnoLetivoDAL.obterAnoAtivo(PASTA_BD);

        if (atual == null) {
            linhas.add("Não existe nenhum ano letivo registado.");
            return linhas;
        }

        linhas.add("Ano atual: " + atual.getAno() + " (" + atual.getEstado() + ")");

        switch (atual.getEstado()) {
            case PLANEAMENTO:
                List<String> errosInicio = new ArrayList<>();
                errosInicio.addAll(validarQuorumCursos());
                errosInicio.addAll(validarMomentosUcs());
                if (errosInicio.isEmpty()) linhas.add("[INICIAR] Pronto para iniciar.");
                else for (String e : errosInicio) linhas.add("[INICIAR - BLOQUEIO] " + e);
                break;

            case INICIADO:
                List<String> pendentes = validarNotasPendentes(atual.getAno());
                if (pendentes.isEmpty()) linhas.add("[FECHAR] Pronto para fechar — todas as notas lançadas.");
                else for (String p : pendentes) linhas.add("[FECHAR - BLOQUEIO] " + p);
                break;

            case FECHADO:
                linhas.add("[AVANÇAR] Pronto para avançar para o ano " + (atual.getAno() + 1) + ".");
                break;
        }
        return linhas;
    }

    // ============================================================
    // Histórico e Momentos de Avaliação (Sprint 6)
    // ============================================================

    /**
     * Devolve a lista de anos letivos já fechados (arquivados no histórico).
     * Usado pelo AnoLetivoController.verHistorico() (linha 151) — devolve List<String>.
     */
    public List<String> obterHistoricoAnos() {
        List<String> anos = new ArrayList<>();
        List<AnoLetivo> todos = AnoLetivoDAL.listarTodos(PASTA_BD);
        for (AnoLetivo a : todos) {
            if (a.getEstado() == EstadoAnoLetivo.FECHADO) {
                anos.add(a.getAno() + " (FECHADO)");
            }
        }
        return anos;
    }

    /**
     * Devolve o número de momentos de avaliação configurados para uma UC.
     * Usado pelo AnoLetivoController.alterarMomentosUc() (linha 164).
     *
     * Nota: usa valor padrão enquanto a tabela de momentos não estiver implementada.
     *
     * @param siglaUc Sigla da UC.
     * @return Número de momentos (padrão 3).
     */
    public int obterMomentosUc(String siglaUc) {
        // TODO: substituir por leitura real da tabela de momentos quando disponível
        return 3;
    }

    /**
     * Altera o número de momentos de avaliação de uma UC.
     * Usado pelo AnoLetivoController.alterarMomentosUc() (linha 168).
     *
     * @param siglaUc  Sigla da UC.
     * @param momentos Novo número de momentos (1–3).
     * @throws EstadoInvalidoException se a sigla for inválida ou o nº de momentos estiver fora de 1–3.
     */
    public void alterarMomentosUc(String siglaUc, int momentos) {
        if (siglaUc == null || siglaUc.isEmpty())
            throw new EstadoInvalidoException("Sigla de UC inválida.");
        if (momentos < 1 || momentos > 3)
            throw new EstadoInvalidoException("O número de momentos deve estar entre 1 e 3.");
        // TODO: persistir quando a tabela de momentos estiver implementada
    }

    // ============================================================
    // Validadores privados
    // ============================================================

    private List<String> validarQuorumCursos() {
        List<String> erros = new ArrayList<>();
        String[] cursos = CursoDAL.obterListaCursos(PASTA_BD);
        for (String c : cursos) {
            String sigla = c.split(" - ")[0];
            int a1 = estudanteDAL.contarEstudantesPorCursoEAno(sigla, 1);
            if (a1 > 0 && a1 < 5)
                erros.add("Curso " + sigla + " — 1.º ano tem " + a1 + " aluno(s); mínimo exigido é 5.");
        }
        return erros;
    }

    private List<String> validarMomentosUcs() {
        List<String> erros = new ArrayList<>();
        String[] ucs = UcDAL.obterListaUcs(PASTA_BD);
        for (String linha : ucs) {
            String sigla = linha.split(" - ")[0];
            if (!mockTemMomentosDefinidos(sigla))
                erros.add("UC " + sigla + " — momentos de avaliação não definidos.");
        }
        return erros;
    }

    private List<String> validarNotasPendentes(int anoLetivo) {
        List<String> pendentes = new ArrayList<>();
        List<Estudante> estudantes = estudanteDAL.carregarTodos();
        for (Estudante e : estudantes) {
            if (e == null || e.getAnoCurricular() > 3) continue;
            List<String> siglasInscritas = InscricaoDAL.obterSiglasUcsPorAluno(
                    e.getNumeroMecanografico(), anoLetivo, PASTA_BD);
            for (String siglaUc : siglasInscritas) {
                Avaliacao av = AvaliacaoDAL.obterAvaliacao(
                        e.getNumeroMecanografico(), siglaUc, anoLetivo, PASTA_BD);
                if (av == null || av.getTotalAvaliacoesLancadas() == 0)
                    pendentes.add(String.format("Aluno %d (%s) — UC %s sem nota lançada.",
                            e.getNumeroMecanografico(), e.getNome(), siglaUc));
            }
        }
        return pendentes;
    }

    /** Devolve os alunos com saldo devedor positivo (propina em dívida). */
    private List<String> validarPropinasPendentes() {
        List<String> pendentes = new ArrayList<>();
        for (Estudante e : estudanteDAL.carregarTodos()) {
            if (e != null && e.getSaldoDevedor() > 0) {
                pendentes.add(String.format("Aluno %d (%s) — dívida de %.2f€.",
                        e.getNumeroMecanografico(), e.getNome(), e.getSaldoDevedor()));
            }
        }
        return pendentes;
    }

    // ============================================================
    // MOCK — Momentos de Avaliação
    // ============================================================

    private boolean mockTemMomentosDefinidos(String siglaUc) {
        if (siglaUc == null) return true;
        if (siglaUc.equalsIgnoreCase("TESTE99")) return false;
        return true;
    }
}