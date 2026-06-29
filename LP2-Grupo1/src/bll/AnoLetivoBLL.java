package bll;

import common.ConfigApp;
import dal.CursoDALFile;
import dal.CursoDALSql;
import dal.UcDALFile;
import dal.UcDALSql;

import dal.AnoLetivoDAL;
import dal.AnoLetivoDALFile;
import dal.AnoLetivoDALSql;
import dal.AvaliacaoDAL;
import dal.AvaliacaoDALFile;
import dal.AvaliacaoDALSql;
import dal.CursoDAL;
import dal.EstudanteDAL;
import dal.EstudanteDALFile;
import dal.EstudanteDALSql;
import dal.HistoricoAnoLetivoDAL;
import dal.HistoricoAnoLetivoDALFile;
import dal.HistoricoAnoLetivoDALSql;
import dal.HistoricoDAL;
import dal.HistoricoDALFile;
import dal.HistoricoDALSql;
import dal.InscricaoDAL;
import dal.InscricaoDALFile;
import dal.InscricaoDALSql;
import dal.UcDAL;
import model.*;
import view.AnoLetivoView;
import view.GestorView;

import java.util.ArrayList;
import java.util.List;

/**
 * Lógica de negócio do módulo Ano Letivo.
 * Ciclo de vida: Criar → Iniciar → Fechar → Avançar.
 * Em caso de violação de pré-condição é lançada EstadoInvalidoException.
 */
public class AnoLetivoBLL {
    private final CursoDAL cursoDAL = ConfigApp.isModoSql() ? new CursoDALSql() : new CursoDALFile();
    private final UcDAL ucDAL = ConfigApp.isModoSql() ? new UcDALSql() : new UcDALFile();
    // Unificação: decisão única de aprovação/nota (ponderada com fallback ao sistema antigo).
    private final AvaliacaoBLL avaliacaoBll = new AvaliacaoBLL();

    private final AnoLetivoDAL dal;
    private final HistoricoAnoLetivoDAL historicoDAL;
    // A7: acesso ao módulo do estudante (lazy — evita efeitos colaterais no arranque)
    private EstudanteBLL moduloEstudante;
    private EstudanteBLL moduloEstudante() {
        if (moduloEstudante == null) moduloEstudante = new EstudanteBLL();
        return moduloEstudante;
    }
    private final InscricaoDAL inscricaoDAL;
    private final AvaliacaoDAL avaliacaoDAL;
    private final HistoricoDAL historicoAcademicoDAL;

    public AnoLetivoBLL() {
        this.dal         = ConfigApp.isModoSql() ? new AnoLetivoDALSql()         : new AnoLetivoDALFile();
        this.historicoDAL = ConfigApp.isModoSql() ? new HistoricoAnoLetivoDALSql() : new HistoricoAnoLetivoDALFile();
        this.inscricaoDAL = ConfigApp.isModoSql() ? new InscricaoDALSql() : new InscricaoDALFile();
        this.avaliacaoDAL = ConfigApp.isModoSql() ? new AvaliacaoDALSql() : new AvaliacaoDALFile();
        this.historicoAcademicoDAL = ConfigApp.isModoSql() ? new HistoricoDALSql() : new HistoricoDALFile();
        dal.inicializar();
        historicoDAL.inicializar();
        inscricaoDAL.inicializar();
        avaliacaoDAL.inicializar();
        historicoAcademicoDAL.inicializar();
    }

    // ============================================================
    // CRUD básico
    // ============================================================

    public void criar(int ano) {
        if (dal.procurarPorAno(ano) != null) {
            throw new EstadoInvalidoException(
                    "Já existe um ano letivo registado com o ano " + ano + ".");
        }
        dal.adicionar(new AnoLetivo(ano));
    }

    public void editar(int anoAntigo, int anoNovo) {
        AnoLetivo alvo = dal.procurarPorAno(anoAntigo);
        if (alvo == null)
            throw new EstadoInvalidoException("Ano " + anoAntigo + " não existe.");
        if (alvo.getEstado() != EstadoAnoLetivo.PLANEAMENTO)
            throw new EstadoInvalidoException(
                    "Só anos em PLANEAMENTO podem ser editados. Estado atual: " + alvo.getEstado() + ".");
        if (dal.procurarPorAno(anoNovo) != null)
            throw new EstadoInvalidoException(
                    "Já existe um ano letivo registado com o ano " + anoNovo + ".");
        dal.remover(anoAntigo);
        dal.adicionar(new AnoLetivo(anoNovo));
    }

    public List<AnoLetivo> listar() {
        return dal.listarTodos();
    }

    // ============================================================
    // Ciclo de vida — Iniciar / Fechar / Avançar
    // ============================================================

    /**
     * Inicia um ano letivo (PLANEAMENTO → INICIADO).
     * Bloqueia se faltarem momentos de avaliação.
     * Inativa automaticamente cursos do 1.º ano sem quórum (1–4 alunos).
     *
     * @return Lista de siglas de cursos inativados (vazia se todos aptos).
     */
    public List<String> iniciar(int ano, AnoLetivoView view) {
        AnoLetivo alvo = dal.procurarPorAno(ano);
        if (alvo == null)
            throw new EstadoInvalidoException("Ano " + ano + " não existe.");
        if (alvo.getEstado() != EstadoAnoLetivo.PLANEAMENTO)
            throw new EstadoInvalidoException(
                    "Só anos em PLANEAMENTO podem ser iniciados. Estado atual: " + alvo.getEstado() + ".");

        List<String> errosMomentos = validarMomentosUcs();
        if (!errosMomentos.isEmpty()) {
            List<String> pendentes = listarMomentosUcPendentes();
            view.mostrarPendenciasMomentosUc(pendentes);   // <-- NOVO

            StringBuilder msg = new StringBuilder("Bloqueado para iniciar — momentos de avaliação em falta:");
            for (String e : errosMomentos) msg.append("\n  - ").append(e);
            throw new EstadoInvalidoException(msg.toString());
        }

        // C2: a propina anual tem de estar definida (>0) em todos os cursos com
        // alunos antes de o ano poder iniciar (à semelhança dos momentos).
        List<String> errosPropinas = validarPropinasCursos();
        if (!errosPropinas.isEmpty()) {
            StringBuilder msgPropinas = new StringBuilder("Bloqueado para iniciar — propinas por definir:");
            for (String e : errosPropinas) msgPropinas.append("\n  - ").append(e);
            throw new EstadoInvalidoException(msgPropinas.toString());
        }

        List<String> cursosInativados = new ArrayList<>();
        CursoBLL cursoBll = new CursoBLL();
        String[] cursos = cursoDAL.obterListaCursos(ConfigApp.PASTA_BD);
        for (String c : cursos) {
            String sigla = c.split(" - ")[0];
            int a1 = moduloEstudante().contarEstudantesPorCursoEAno(sigla, 1);
            if (a1 > 0 && a1 < 5) {
                Curso curso = cursoBll.procurarCursoCompleto(sigla);
                if (curso != null) {
                    curso.setEstadoCurricular(EstadoCurricular.SEM_CONDICOES); // não reúne quórum para abrir
                    cursoDAL.atualizarCurso(curso, ConfigApp.PASTA_BD);
                    cursosInativados.add(sigla);
                }
            }
        }

        alvo.setEstado(EstadoAnoLetivo.INICIADO);
        dal.atualizar(alvo);
        return cursosInativados;
    }

    /**
     * Fecha um ano letivo (INICIADO → FECHADO).
     * Bloqueia se existirem notas por lançar ou propinas em dívida.
     * Após fechar: arquiva no histórico e guarda o histórico académico.
     */
    public void fechar(int ano, AnoLetivoView view) {
        AnoLetivo alvo = dal.procurarPorAno(ano);
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
        dal.atualizar(alvo);

        // Arquivar no histórico de anos letivos
        historicoDAL.arquivar(alvo);

        System.out.println("A calcular o aproveitamento e a guardar o histórico académico do ano " + ano + "...");

        // Unificação: o histórico é calculado por UC inscrita, usando a nota final
        // (ponderada se a UC tiver momentos; senão, média do sistema antigo) e a
        // decisão única de aprovação.
        List<Estudante> listaAlunos = moduloEstudante().carregarTodos();
        for (Estudante e : listaAlunos) {
            if (e == null) continue;
            int numMec = e.getNumeroMecanografico();
            for (String siglaUc : inscricaoDAL.obterSiglasUcsPorAluno(numMec, ano)) {
                double notaFinal = avaliacaoBll.notaFinal(numMec, siglaUc, ano);
                String estado = avaliacaoBll.aprovadoNaUc(numMec, siglaUc, ano) ? "APROVADO" : "REPROVADO";
                historicoAcademicoDAL.guardarRegistoHistorico(
                        ano, numMec, siglaUc, String.format("%.1f", notaFinal), estado);
            }
        }
        System.out.println("Histórico académico fechado com sucesso!");
    }

    public void avancar(RepositorioDados repo, GestorView view) {
        AnoLetivo atual = dal.obterAnoAtivo();
        if (atual == null)
            throw new EstadoInvalidoException("Não existe ano letivo registado no sistema.");
        if (atual.getEstado() != EstadoAnoLetivo.FECHADO)
            throw new EstadoInvalidoException(
                    "Só anos FECHADOS podem avançar. Estado atual: " + atual.getEstado() + ".");

        new GestorBLL().avancarAnoLetivo(repo, view);

        // Reset dos momentos de avaliação: cada novo ano começa sem
        // configurações herdadas. Feito após a transição dos alunos e
        // antes de criar o novo ano letivo em PLANEAMENTO.
        resetarMomentosUcs();

        int proximo = atual.getAno() + 1;
        if (dal.procurarPorAno(proximo) == null)
            dal.adicionar(new AnoLetivo(proximo));

        // A5: o ano fechado já foi arquivado no histórico em fechar(); agora é
        // removido da lista principal (anos_letivos), ficando apenas no histórico.
        dal.remover(atual.getAno());
    }

    /**
     * Repõe numMomentos = 0 em todas as UCs (não definido).
     * Chamado após avançar o ano letivo para que os docentes definam
     * os momentos para o novo ano antes de o iniciar.
     */
    private void resetarMomentosUcs() {
        String[] ucs = ucDAL.obterListaUcs(ConfigApp.PASTA_BD);
        for (String entrada : ucs) {
            String sigla = entrada.split(" - ")[0].trim();
            ucDAL.atualizarMomentos(sigla, 0, ConfigApp.PASTA_BD);
        }
        System.out.println("Momentos de avaliação resetados (0 = por definir) para todas as UCs.");
    }

    // ============================================================
    // Estado — read-only
    // ============================================================

    public List<String> obterEstadoResumo() {
        List<String> linhas = new ArrayList<>();
        AnoLetivo atual = dal.obterAnoAtivo();

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
                errosInicio.addAll(validarPropinasCursos());
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
    // Histórico e Momentos de Avaliação
    // ============================================================

    /**
     * Devolve a lista de anos letivos arquivados no histórico.
     * Em modo SQL lê da tabela [anoLetivoHistorico]; em modo file lê do CSV.
     */
    public List<String> obterHistoricoAnos() {
        return historicoDAL.listar();
    }

    public int obterMomentosUc(String siglaUc) {
        return ucDAL.obterMomentos(siglaUc, ConfigApp.PASTA_BD);
    }

    public void alterarMomentosUc(String siglaUc, int momentos) {
        if (siglaUc == null || siglaUc.isEmpty())
            throw new EstadoInvalidoException("Sigla de UC inválida.");
        if (momentos < 1 || momentos > 3)
            throw new EstadoInvalidoException("O número de momentos deve estar entre 1 e 3.");
        // A4: só é permitido alterar momentos com o ano letivo em PLANEAMENTO
        // (o caminho do docente — DocenteBLL.definirMomentosAvaliacao — já valida isto).
        EstadoAnoLetivo estado = getEstadoAnoAtual();
        if (estado != null && estado != EstadoAnoLetivo.PLANEAMENTO)
            throw new EstadoInvalidoException(
                    "Só é possível alterar os momentos com o ano letivo em PLANEAMENTO (estado atual: " + estado + ").");
        ucDAL.atualizarMomentos(siglaUc, momentos, ConfigApp.PASTA_BD);
    }

    // ============================================================
    // Validadores privados
    // ============================================================

    private List<String> validarQuorumCursos() {
        List<String> erros = new ArrayList<>();
        String[] cursos = cursoDAL.obterListaCursos(ConfigApp.PASTA_BD);
        for (String c : cursos) {
            String sigla = c.split(" - ")[0];
            int a1 = moduloEstudante().contarEstudantesPorCursoEAno(sigla, 1);
            if (a1 > 0 && a1 < 5)
                erros.add("Curso " + sigla + " — 1.º ano tem " + a1 + " aluno(s); mínimo exigido é 5.");
        }
        return erros;
    }

    private List<String> validarMomentosUcs() {
        List<String> erros = new ArrayList<>();
        String[] ucs = ucDAL.obterListaUcs(ConfigApp.PASTA_BD);
        for (String linha : ucs) {
            String sigla = linha.split(" - ")[0];
            if (ucDAL.obterMomentos(sigla, ConfigApp.PASTA_BD) == 0)
                erros.add("UC " + sigla + " — momentos de avaliação não definidos.");
        }
        return erros;
    }

    /**
     * C2: valida que todos os cursos COM alunos têm a propina anual definida (>0).
     * Pré-condição para iniciar o ano letivo, à semelhança dos momentos das UCs.
     */
    private List<String> validarPropinasCursos() {
        List<String> erros = new ArrayList<>();
        String[] cursos = cursoDAL.obterListaCursos(ConfigApp.PASTA_BD);
        for (String c : cursos) {
            String sigla = c.split(" - ")[0];
            int totalAlunos = moduloEstudante().contarEstudantesPorCursoEAno(sigla, 1)
                    + moduloEstudante().contarEstudantesPorCursoEAno(sigla, 2)
                    + moduloEstudante().contarEstudantesPorCursoEAno(sigla, 3);
            if (totalAlunos == 0) continue; // só interessam cursos com alunos
            Curso curso = cursoDAL.procurarCurso(sigla, ConfigApp.PASTA_BD);
            if (curso != null && curso.getValorPropinaAnual() <= 0)
                erros.add("Curso " + sigla + " — propina anual não definida (está a 0).");
        }
        return erros;
    }

    private List<String> validarNotasPendentes(int anoLetivo) {
        List<String> pendentes = new ArrayList<>();
        List<Estudante> estudantes = moduloEstudante().carregarTodos();
        for (Estudante e : estudantes) {
            if (e == null || e.getAnoCurricular() > 3) continue;
            List<String> siglasInscritas = inscricaoDAL.obterSiglasUcsPorAluno(
                    e.getNumeroMecanografico(), anoLetivo);
            for (String siglaUc : siglasInscritas) {
                // Unificação: pendente se faltarem notas (momentos por avaliar, ou notas
                // por lançar no sistema antigo).
                if (avaliacaoBll.temNotasPendentes(e.getNumeroMecanografico(), siglaUc, anoLetivo)) {
                    pendentes.add(String.format("Aluno %d (%s) — UC %s: notas por lançar.",
                            e.getNumeroMecanografico(), e.getNome(), siglaUc));
                }
            }
        }
        return pendentes;
    }

    private List<String> validarPropinasPendentes() {
        List<String> pendentes = new ArrayList<>();
        for (Estudante e : moduloEstudante().carregarTodos()) {
            if (e != null && e.getSaldoDevedor() > 0) {
                pendentes.add(String.format("Aluno %d (%s) — dívida de %.2f€.",
                        e.getNumeroMecanografico(), e.getNome(), e.getSaldoDevedor()));
            }
        }
        return pendentes;
    }

    /**
     * Obtém o estado do ano letivo ativo (mais recente).
     */
    public EstadoAnoLetivo getEstadoAnoAtual() {
        AnoLetivo atual = dal.obterAnoAtivo();
        return atual == null ? null : atual.getEstado();
    }

    /**
     * Lista as UCs que não têm momentos de avaliação definidos (momentos == 0),
     * incluindo o nome/sigla do docente responsável.
     */
    private List<String> listarMomentosUcPendentes() {
        List<String> pendentes = new ArrayList<>();
        String[] ucs = ucDAL.obterListaUcs(ConfigApp.PASTA_BD);
        for (String linha : ucs) {
            String sigla = linha.split(" - ")[0];
            if (ucDAL.obterMomentos(sigla, ConfigApp.PASTA_BD) == 0) {
                UnidadeCurricular uc = new UcBLL().procurarUCCompleta(sigla);
                String docenteInfo;
                if (uc != null && uc.getDocenteResponsavel() != null) {
                    docenteInfo = uc.getDocenteResponsavel().getNome() + " (" + uc.getDocenteResponsavel().getSigla() + ")";
                } else {
                    docenteInfo = "Docente não atribuído";
                }
                pendentes.add("UC " + sigla + " — Docente: " + docenteInfo);
            }
        }
        return pendentes;
    }
}
