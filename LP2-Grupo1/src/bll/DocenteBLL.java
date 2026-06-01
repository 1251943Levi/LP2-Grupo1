package bll;

import dal.AvaliacaoDAL;
import dal.EstudanteDAL;
import dal.UcDAL;
import dal.InscricaoDAL;
import model.Avaliacao;
import model.Docente;
import model.Estudante;
import model.UnidadeCurricular;
import controller.LoginController;
import utils.Config;
import java.util.ArrayList;
import java.util.List;
import dal.DocenteDAL;


/**
 * Lógica de negócio para o perfil Docente.
 * Gere o lançamento de avaliações com todas as validações necessárias,
 * a obtenção dos alunos associados e a alteração segura de credenciais.
 */
public class DocenteBLL {

    private static final String PASTA_BD = "bd";
    private final LoginController loginController = new LoginController();
    private final EstudanteDAL estudanteDAL = new EstudanteDAL(PASTA_BD);

    /**
     * Verifica se uma UC pertence ao plano de lecionação do docente.
     */
    public boolean lecionaEstaUC(Docente docente, String siglaUc) {
        if (siglaUc == null) return false;
        for (int i = 0; i < docente.getTotalUcsLecionadas(); i++) {
            if (docente.getUcsLecionadas()[i].getSigla().equalsIgnoreCase(siglaUc)) return true;
        }
        return false;
    }

    /**
     * Lança uma nota de forma faseada.
     * Se já existir avaliação, anexa a nova nota (até ao limite de 3).
     * Se não existir, cria a primeira.
     *
     * FIX (Bug DocenteBLL):
     *   - Linha ~27: "carregarAvaliacoesPorAluno" → "obterAvaliacoesPorAluno"  (nome errado na DAL)
     *   - Linha ~275: "adicionarAvaliacao(avaliacao)" → "adicionarAvaliacao(avaliacao, numMec, PASTA_BD)" (args em falta)
     */
    public String lancarNota(int numMec, String siglaUc, int ano, double notaMomento, Docente d) {
        Estudante aluno = estudanteDAL.procurarPorNumMec(numMec);

        if (aluno == null)
            return "ERRO: Aluno com nº " + numMec + " não encontrado.";

        if (!lecionaEstaUC(d, siglaUc))
            return "ERRO: A UC '" + siglaUc + "' não pertence às suas unidades curriculares.";

        UnidadeCurricular uc = new UcBLL().procurarUCCompleta(siglaUc);
        if (uc == null)
            return "ERRO: A UC '" + siglaUc + "' não foi encontrada no sistema.";

        Avaliacao avaliacaoExistente = AvaliacaoDAL.obterAvaliacao(numMec, siglaUc, ano, PASTA_BD);

        if (avaliacaoExistente != null) {
            if (avaliacaoExistente.getTotalAvaliacoesLancadas() >= 3) {
                return "ERRO: O aluno já tem as 3 notas máximas lançadas para esta UC.";
            }
            avaliacaoExistente.adicionarResultado(notaMomento);
            AvaliacaoDAL.atualizarAvaliacao(avaliacaoExistente, numMec, PASTA_BD);
            return null;

        } else {
            Avaliacao novaAvaliacao = new Avaliacao(uc, ano);
            novaAvaliacao.adicionarResultado(notaMomento);
            // FIX: era "adicionarAvaliacao(novaAvaliacao)" — faltavam numMec e PASTA_BD
            AvaliacaoDAL.adicionarAvaliacao(novaAvaliacao, numMec, PASTA_BD);
            return null;
        }
    }

    /**
     * Altera a password do docente com hashing e persistência.
     */
    public void alterarPassword(Docente docente, String novaPass) {
        loginController.atualizarPassword(docente.getEmail(), novaPass);
    }

    /**
     * Devolve os alunos associados ao docente (das suas UCs) com a média e a lista de UCs.
     * Cada elemento da lista é um Object[] de 3 posições: [Estudante, Double média, String ucs].
     *
     * FIX (compile): método estava em falta — causava "cannot find symbol method
     * obterAlunosDoDocenteComMedia(model.Docente)" no DocenteController (linhas 67 e 163).
     *
     * FIX (runtime): a versão original devolvia apenas 2 elementos [Estudante, média],
     * mas o DocenteController acede a par[2] (string de UCs) em listarMeusAlunos() e
     * listarAlunosPorUC() — o que provocaria ArrayIndexOutOfBoundsException.
     * Agora devolve sempre 3 posições.
     *
     * @param docente Docente autenticado.
     * @return Lista de [Estudante, média (Double), UCs inscritas (String)].
     */
    public List<Object[]> obterAlunosDoDocenteComMedia(Docente docente) {
        List<Object[]> resultado = new ArrayList<>();
        List<Integer> alunosAdicionados = new ArrayList<>(); // evita duplicados
        int anoAtual = Config.getAnoAtual();

        for (int i = 0; i < docente.getTotalUcsLecionadas(); i++) {
            UnidadeCurricular uc = docente.getUcsLecionadas()[i];
            if (uc == null) continue;

            List<Integer> numsMec = InscricaoDAL.obterAlunosPorUc(uc.getSigla(), anoAtual, PASTA_BD);
            for (int numMec : numsMec) {
                if (contemAluno(alunosAdicionados, numMec)) continue;

                Estudante aluno = estudanteDAL.procurarPorNumMec(numMec);                if (aluno == null) continue;

                carregarAvaliacoesSeNecessario(aluno);
                double media = calcularMediaAlunoNaUc(aluno, uc.getSigla());

                // par[2]: string das UCs em que o aluno está inscrito (usada em .contains())
                List<String> siglasInscritas =
                        InscricaoDAL.obterSiglasUcsPorAluno(numMec, anoAtual, PASTA_BD);
                String ucs = String.join(", ", siglasInscritas);

                resultado.add(new Object[]{aluno, media, ucs});
                alunosAdicionados.add(numMec);
            }
        }
        return resultado;
    }

    /** Verifica se um número mecanográfico já está na lista (controlo de duplicados). */
    private boolean contemAluno(List<Integer> lista, int numMec) {
        for (int m : lista) {
            if (m == numMec) return true;
        }
        return false;
    }

    /**
     * Devolve a lista de alunos inscritos numa UC no ano corrente.
     */
    public List<String> obterAlunosInscritosNaUc(String siglaUc) {
        List<Integer> nums = InscricaoDAL.obterAlunosPorUc(siglaUc, Config.getAnoAtual(), PASTA_BD);
        List<String> alunosFormatados = new ArrayList<>();
        for (int num : nums) {
            Estudante e = estudanteDAL.procurarPorNumMec(num);            String nome = (e != null) ? e.getNome() : "Desconhecido";
            alunosFormatados.add(num + " - " + nome);
        }
        return alunosFormatados;
    }

    /**
     * Lista todos os docentes (dados básicos, sem UCs carregadas).
     */
    public List<Docente> listarTodos() {
        return DocenteDAL.carregarTodos(PASTA_BD);
    }

    /**
     * Obtém um docente pela sua sigla (com dados básicos).
     */
    public Docente obterPorSigla(String sigla) {
        return DocenteDAL.procurarPorSigla(sigla, PASTA_BD);
    }

    /**
     * Actualiza os dados de um docente (nome, morada, dataNascimento, NIF).
     */
    public boolean atualizarDocente(Docente docente) {
        if (docente == null) return false;
        DocenteDAL.atualizarDocente(docente, PASTA_BD);
        return true;
    }

    /**
     * Verifica se um docente tem UCs atribuídas.
     */
    public boolean temUcAtribuida(String sigla) {
        return DocenteDAL.temUcAtribuida(sigla, PASTA_BD);
    }

    /**
     * Remove um docente (apenas se não tiver UCs atribuídas).
     */
    public boolean removerDocente(String sigla) {
        if (temUcAtribuida(sigla)) return false;
        return DocenteDAL.removerDocente(sigla, PASTA_BD);
    }

    /**
     * Lança notas para todos os alunos inscritos numa UC, pedindo uma nota para cada um.
     * @return String com o relatório detalhado das operações.
     */
    public String lancarNotasEmLote(String siglaUc, int anoLetivo, Docente docente,
                                    java.util.function.Function<Integer, Double> obterNota) {
        if (!lecionaEstaUC(docente, siglaUc)) {
            return "ERRO: Não lecciona a UC " + siglaUc;
        }
        UnidadeCurricular uc = new UcBLL().procurarUCCompleta(siglaUc);
        if (uc == null) return "ERRO: UC não encontrada.";

        List<Integer> alunosInscritos = InscricaoDAL.obterAlunosPorUc(siglaUc, anoLetivo, PASTA_BD);
        StringBuilder relatorio = new StringBuilder();
        int sucessos = 0, erros = 0;

        for (int numMec : alunosInscritos) {
            Estudante aluno = estudanteDAL.procurarPorNumMec(numMec);
            String nome = (aluno != null) ? aluno.getNome() : "Desconhecido";

            Double nota = obterNota.apply(numMec);
            if (nota == null) {
                relatorio.append(String.format(" %d - %s → Saltado pelo docente\n", numMec, nome));
                continue;
            }

            String resultado = lancarNota(numMec, siglaUc, anoLetivo, nota, docente);
            if (resultado == null) {
                sucessos++;
                relatorio.append(String.format("  %d - %s → Nota %.1f registada\n", numMec, nome, nota));
            } else {
                erros++;
                relatorio.append(String.format("  %d - %s → %s\n", numMec, nome, resultado));
            }
        }
        relatorio.insert(0, String.format("Resumo: %d sucessos, %d falhas, %d saltos.\n",
                sucessos, erros, alunosInscritos.size() - sucessos - erros));
        return relatorio.toString();
    }

    /**
     * Retorna uma lista de strings "numMec - nome" para os alunos inscritos numa UC.
     */
    public List<String> obterAlunosFormatados(String siglaUc, int anoLetivo) {
        List<Integer> nums = InscricaoDAL.obterAlunosPorUc(siglaUc, anoLetivo, PASTA_BD);
        List<String> formatados = new ArrayList<>();
        for (int num : nums) {
            Estudante e = estudanteDAL.procurarPorNumMec(num);
            String nome = (e != null) ? e.getNome() : "Desconhecido";
            formatados.add(num + " - " + nome);
        }
        return formatados;
    }

    // ── Métodos privados ──────────────────────────────────────────────

    /**
     * Calcula a média de um aluno numa UC específica.
     * FIX: usava "carregarAvaliacoesPorAluno" → correto é "obterAvaliacoesPorAluno".
     */
    private double calcularMediaAlunoNaUc(Estudante aluno, String siglaUc) {
        for (int i = 0; i < aluno.getPercurso().getTotalAvaliacoes(); i++) {
            Avaliacao av = aluno.getPercurso().getHistoricoAvaliacoes()[i];
            if (av != null && av.getUc() != null && av.getUc().getSigla().equalsIgnoreCase(siglaUc)) {
                return av.calcularMedia();
            }
        }
        return 0.0;
    }

    /**
     * Carrega as avaliações de um estudante se ainda não estiverem carregadas.
     * FIX: era "AvaliacaoDAL.carregarAvaliacoesPorAluno" → correto: "obterAvaliacoesPorAluno".
     */
    private void carregarAvaliacoesSeNecessario(Estudante aluno) {
        if (aluno.getPercurso().getTotalAvaliacoes() == 0) {
            // FIX: nome correto do método na DAL é obterAvaliacoesPorAluno
            List<Avaliacao> avaliacoes = AvaliacaoDAL.obterAvaliacoesPorAluno(
                    aluno.getNumeroMecanografico(), PASTA_BD);
            for (Avaliacao av : avaliacoes) {
                aluno.getPercurso().registarAvaliacao(av);
            }
        }
    }
}