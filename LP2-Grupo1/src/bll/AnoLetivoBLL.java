package bll;

import dal.AnoLetivoDAL;
import dal.AvaliacaoDAL;
import dal.CursoDAL;
import dal.EstudanteDAL;
import dal.InscricaoDAL;
import dal.UcDAL;
import model.AnoLetivo;
import model.Avaliacao;
import model.EstadoAnoLetivo;
import model.Estudante;
import model.RepositorioDados;
import view.GestorView;

import java.util.ArrayList;
import java.util.List;

/**
 * Lógica de negócio do módulo Ano Letivo.
 * Coordena o ciclo de vida: Criar → Iniciar → Fechar → Avançar.
 * Em caso de violação de pré-condição é lançada EstadoInvalidoException.
 */
public class AnoLetivoBLL {

    private static final String PASTA_BD = "bd";

    // ============================================================
    // CRUD básico
    // ============================================================

    /**
     * Cria um novo ano letivo em estado PLANEAMENTO.
     * @throws EstadoInvalidoException se já existir um ano com esse número.
     */
    public void criar(int ano) {
        if (AnoLetivoDAL.procurarPorAno(ano, PASTA_BD) != null) {
            throw new EstadoInvalidoException(
                    "Já existe um ano letivo registado com o ano " + ano + ".");
        }
        AnoLetivoDAL.adicionar(new AnoLetivo(ano), PASTA_BD);
    }

    /**
     * Permite alterar o número de um ano enquanto este estiver em PLANEAMENTO.
     * Como o ano é a chave (final no modelo), o "editar" é remover+adicionar.
     */
    public void editar(int anoAntigo, int anoNovo) {
        AnoLetivo alvo = AnoLetivoDAL.procurarPorAno(anoAntigo, PASTA_BD);
        if (alvo == null) {
            throw new EstadoInvalidoException("Ano " + anoAntigo + " não existe.");
        }
        if (alvo.getEstado() != EstadoAnoLetivo.PLANEAMENTO) {
            throw new EstadoInvalidoException(
                    "Só anos em PLANEAMENTO podem ser editados. Estado atual: " + alvo.getEstado() + ".");
        }
        if (AnoLetivoDAL.procurarPorAno(anoNovo, PASTA_BD) != null) {
            throw new EstadoInvalidoException(
                    "Já existe um ano letivo registado com o ano " + anoNovo + ".");
        }
        AnoLetivoDAL.remover(anoAntigo, PASTA_BD);
        AnoLetivoDAL.adicionar(new AnoLetivo(anoNovo), PASTA_BD);
    }

    /** @return Lista de todos os anos letivos (passados, atual e em planeamento). */
    public List<AnoLetivo> listar() {
        return AnoLetivoDAL.listarTodos(PASTA_BD);
    }

    // ============================================================
    // Ciclo de vida — Iniciar / Fechar / Avançar
    // ============================================================

    /**
     * Inicia um ano letivo (PLANEAMENTO → INICIADO).
     * Valida quórum por curso e momentos de avaliação das UCs.
     */
    public void iniciar(int ano) {
        AnoLetivo alvo = AnoLetivoDAL.procurarPorAno(ano, PASTA_BD);
        if (alvo == null) {
            throw new EstadoInvalidoException("Ano " + ano + " não existe.");
        }
        if (alvo.getEstado() != EstadoAnoLetivo.PLANEAMENTO) {
            throw new EstadoInvalidoException(
                    "Só anos em PLANEAMENTO podem ser iniciados. Estado atual: " + alvo.getEstado() + ".");
        }

        List<String> erros = new ArrayList<>();
        erros.addAll(validarQuorumCursos());
        erros.addAll(validarMomentosUcs());

        if (!erros.isEmpty()) {
            StringBuilder msg = new StringBuilder("Bloqueado para iniciar o ano letivo:");
            for (String e : erros) msg.append("\n  - ").append(e);
            throw new EstadoInvalidoException(msg.toString());
        }

        alvo.setEstado(EstadoAnoLetivo.INICIADO);
        AnoLetivoDAL.atualizar(alvo, PASTA_BD);
    }

    /**
     * Fecha um ano letivo (INICIADO → FECHADO).
     * Bloqueia se houver alguma nota por lançar.
     */
    public void fechar(int ano) {
        AnoLetivo alvo = AnoLetivoDAL.procurarPorAno(ano, PASTA_BD);
        if (alvo == null) {
            throw new EstadoInvalidoException("Ano " + ano + " não existe.");
        }
        if (alvo.getEstado() != EstadoAnoLetivo.INICIADO) {
            throw new EstadoInvalidoException(
                    "Só anos INICIADOS podem ser fechados. Estado atual: " + alvo.getEstado() + ".");
        }

        List<String> pendentes = validarNotasPendentes(ano);
        if (!pendentes.isEmpty()) {
            StringBuilder msg = new StringBuilder("Bloqueado para fechar — notas em falta:");
            for (String p : pendentes) msg.append("\n  - ").append(p);
            throw new EstadoInvalidoException(msg.toString());
        }

        alvo.setEstado(EstadoAnoLetivo.FECHADO);
        AnoLetivoDAL.atualizar(alvo, PASTA_BD);

        // TODO HOOK [Card Histórico]: chamar HistoricoBLL.arquivar(ano) aqui quando estiver disponível.
    }

    /**
     * Avança o ano letivo (transita alunos e cria o próximo).
     * Pré-condição: o ano atual tem de estar FECHADO.
     * Delega a transição de alunos para a lógica existente da Sprint 4 (GestorBLL.avancarAnoLetivo).
     */
    public void avancar(RepositorioDados repo, GestorView view) {
        AnoLetivo atual = AnoLetivoDAL.obterAnoAtivo(PASTA_BD);
        if (atual == null) {
            throw new EstadoInvalidoException("Não existe ano letivo registado no sistema.");
        }
        if (atual.getEstado() != EstadoAnoLetivo.FECHADO) {
            throw new EstadoInvalidoException(
                    "Só anos FECHADOS podem avançar. Estado atual: " + atual.getEstado() + ".");
        }

        // Delega transição de alunos para a lógica existente
        new GestorBLL().avancarAnoLetivo(repo, view);

        // Cria o próximo ano em PLANEAMENTO
        int proximo = atual.getAno() + 1;
        if (AnoLetivoDAL.procurarPorAno(proximo, PASTA_BD) == null) {
            AnoLetivoDAL.adicionar(new AnoLetivo(proximo), PASTA_BD);
        }

        // TODO HOOK [Card Histórico]: arquivar inscrições/avaliações de "atual" aqui.
    }

    // ============================================================
    // Estado — read-only, idempotente (NÃO altera ficheiros)
    // ============================================================

    /**
     * Devolve um sumário do estado atual sem alterar nada nos ficheiros.
     * Mostra o ano atual e as pendências aplicáveis ao seu estado.
     */
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
                if (errosInicio.isEmpty()) {
                    linhas.add("[INICIAR] Pronto para iniciar.");
                } else {
                    for (String e : errosInicio) linhas.add("[INICIAR - BLOQUEIO] " + e);
                }
                break;

            case INICIADO:
                List<String> pendentes = validarNotasPendentes(atual.getAno());
                if (pendentes.isEmpty()) {
                    linhas.add("[FECHAR] Pronto para fechar — todas as notas lançadas.");
                } else {
                    for (String p : pendentes) linhas.add("[FECHAR - BLOQUEIO] " + p);
                }
                break;

            case FECHADO:
                linhas.add("[AVANÇAR] Pronto para avançar para o ano " + (atual.getAno() + 1) + ".");
                break;
        }
        return linhas;
    }

    // ============================================================
    // Validadores privados
    // ============================================================

    /**
     * Regra do enunciado v1.1:
     *   - 1.º ano: mínimo 5 alunos inscritos
     *   - 2.º e 3.º anos: mínimo 1 aluno (já satisfeito se existirem alunos)
     * Cursos sem alunos no ano em causa são ignorados (não bloqueiam).
     */
    private List<String> validarQuorumCursos() {
        List<String> erros = new ArrayList<>();
        String[] cursos = CursoDAL.obterListaCursos(PASTA_BD);

        for (String c : cursos) {
            String sigla = c.split(" - ")[0];
            int a1 = EstudanteDAL.contarEstudantesPorCursoEAno(sigla, 1, PASTA_BD);
            if (a1 > 0 && a1 < 5) {
                erros.add("Curso " + sigla + " — 1.º ano tem " + a1 + " aluno(s); mínimo exigido é 5.");
            }
            // 2.º e 3.º anos: mínimo 1 é automaticamente cumprido se houver pelo menos 1 inscrito
        }
        return erros;
    }

    /** Verifica se cada UC tem momentos de avaliação definidos (via mock por agora). */
    private List<String> validarMomentosUcs() {
        List<String> erros = new ArrayList<>();
        String[] ucs = UcDAL.obterListaUcs(PASTA_BD);

        for (String linha : ucs) {
            String sigla = linha.split(" - ")[0];
            if (!mockTemMomentosDefinidos(sigla)) {
                erros.add("UC " + sigla + " — momentos de avaliação não definidos.");
            }
        }
        return erros;
    }

    /** Para cada inscrição ativa, verifica se existe avaliação com pelo menos uma nota. */
    private List<String> validarNotasPendentes(int anoLetivo) {
        List<String> pendentes = new ArrayList<>();
        List<Estudante> estudantes = EstudanteDAL.carregarTodos(PASTA_BD);

        for (Estudante e : estudantes) {
            if (e == null || e.getAnoCurricular() > 3) continue;

            List<String> siglasInscritas =
                    InscricaoDAL.obterSiglasUcsPorAluno(e.getNumeroMecanografico(), PASTA_BD);

            for (String siglaUc : siglasInscritas) {
                Avaliacao av = AvaliacaoDAL.obterAvaliacao(
                        e.getNumeroMecanografico(), siglaUc, anoLetivo, PASTA_BD);

                if (av == null || av.getTotalAvaliacoesLancadas() == 0) {
                    pendentes.add(String.format(
                            "Aluno %d (%s) — UC %s sem nota lançada.",
                            e.getNumeroMecanografico(), e.getNome(), siglaUc));
                }
            }
        }
        return pendentes;
    }

    // ============================================================
    // MOCK — Momentos de Avaliação
    // ============================================================

    /**
     * Indica se uma UC tem os momentos de avaliação definidos.
     *
     * Implementação temporária enquanto o card dedicado aos Momentos de Avaliação
     * não está disponível. Devolve sempre true, EXCETO para a sigla "TESTE99"
     * — usada para demonstrar o bloqueio de Iniciar na Sprint Review.
     *
     * @return true se tiver momentos definidos; false caso contrário.
     */
    private boolean mockTemMomentosDefinidos(String siglaUc) {
        // TODO: substituir pela chamada real à BLL de Momentos de Avaliação
        if (siglaUc == null) return true;
        if (siglaUc.equalsIgnoreCase("TESTE99")) {
            return false;  // força erro para demonstração
        }
        return true;
    }
}