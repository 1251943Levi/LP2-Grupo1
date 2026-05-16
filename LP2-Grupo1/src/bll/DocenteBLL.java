package bll;

import dal.AvaliacaoDAL;
import dal.CredencialDAL;
import dal.EstudanteDAL;
import dal.UcDAL;
import dal.InscricaoDAL;
import model.Avaliacao;
import model.Docente;
import model.Estudante;
import model.UnidadeCurricular;
import utils.SegurancaPasswords;

import java.util.ArrayList;
import java.util.List;


/**
 * Lógica de negócio para o perfil Docente.
 * Gere o lançamento de avaliações com todas as validações necessárias,
 * a obtenção dos alunos associados e a alteração segura de credenciais.
 */
public class DocenteBLL {

    private static final String PASTA_BD = "bd";


    /**
     * Verifica se uma UC pertence ao plano de lecionação do docente.
     *
     * @param docente Docente autenticado.
     * @param siglaUc Sigla da UC a verificar.
     * @return true se o docente lecionar a UC indicada.
     */
    public boolean lecionaEstaUC(Docente docente, String siglaUc) {
        if (siglaUc == null) return false;
        for (int i = 0; i < docente.getTotalUcsLecionadas(); i++) {
            if (docente.getUcsLecionadas()[i].getSigla().equalsIgnoreCase(siglaUc)) return true;
        }
        return false;
    }

    /**
     * Lança uma nota de forma faseada. Se já existir avaliação, anexa a nova nota (até ao limite de 3).
     * Se não existir, cria a primeira.
     */
    public String lancarNota(int numMec, String siglaUc, int ano, double notaMomento, Docente d) {
        Estudante aluno = EstudanteDAL.procurarPorNumMec(numMec, PASTA_BD);
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

            AvaliacaoDAL.adicionarAvaliacao(novaAvaliacao, numMec, PASTA_BD);
            return null;
        }
    }

    /**
     * Altera a password do docente com hashing e persistência.
     *
     * @param docente  Docente autenticado.
     * @param novaPass Nova password em texto limpo.
     */
    public void alterarPassword(Docente docente, String novaPass) {
        String passSegura = SegurancaPasswords.gerarCredencialMista(novaPass);
        docente.setPassword(passSegura);
        CredencialDAL.atualizarPassword(docente.getEmail(), passSegura, PASTA_BD);
    }

    /**
     * Devolve os alunos associados ao docente com a respetiva média global.
     * Cada elemento da lista é um array com [Estudante, Double (média)].
     *
     * @param docente Docente autenticado.
     * @return Lista de pares [Estudante, média].
     */
    public List<Object[]> obterAlunosDoDocenteComMedia(Docente docente) {
        List<Object[]> resultado = new ArrayList<>();
        List<Integer> alunosAdicionados = new ArrayList<>();  // controlo de duplicados

        for (int i = 0; i < docente.getTotalUcsLecionadas(); i++) {
            UnidadeCurricular uc = docente.getUcsLecionadas()[i];
            if (uc == null) continue;

            // obter todos os alunos inscritos nesta UC
            List<Integer> numsMec = InscricaoDAL.obterAlunosPorUc(uc.getSigla(), PASTA_BD);

            for (int numMec : numsMec) {
                if (contemAluno(alunosAdicionados, numMec)) continue;

                Estudante aluno = EstudanteDAL.procurarPorNumMec(numMec, PASTA_BD);
                if (aluno == null) continue;

                // garantir que as avaliações do aluno estão carregadas
                carregarAvaliacoesSeNecessario(aluno);

                // calcular a média do aluno na UC atual
                double media = calcularMediaAlunoNaUc(aluno, uc.getSigla());

                resultado.add(new Object[]{aluno, media});
                alunosAdicionados.add(numMec);
            }
        }
        return resultado;
    }

    /**
     * Verifica se um número mecanográfico já está na lista.
     */
    private boolean contemAluno(List<Integer> lista, int numMec) {
        for (int m : lista) {
            if (m == numMec) return true;
        }
        return false;
    }

    /**
     * Calcula a média do aluno numa determinada UC.
     * Retorna 0.0 se não existirem avaliações.
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
     */
    private void carregarAvaliacoesSeNecessario(Estudante aluno) {
        if (aluno.getPercurso().getTotalAvaliacoes() == 0) {
            List<Avaliacao> avaliacoes = AvaliacaoDAL.obterAvaliacoesPorAluno(aluno.getNumeroMecanografico(), PASTA_BD);
            for (Avaliacao av : avaliacoes) {
                aluno.getPercurso().registarAvaliacao(av);
            }
        }
    }

    /**
     * Lança notas para todos os alunos inscritos numa UC, pedindo uma nota para cada um.
     *
     * @return String com o relatório detalhado das operações.
     */
    public String lancarNotasEmLote(String siglaUc, int anoLetivo, Docente docente, java.util.function.Function<Integer, Double> obterNota) {
        if (!lecionaEstaUC(docente, siglaUc)) {
            return "ERRO: Não lecciona a UC " + siglaUc;
        }
        UnidadeCurricular uc = new UcBLL().procurarUCCompleta(siglaUc);
        if (uc == null) return "ERRO: UC não encontrada.";

        List<Integer> alunosInscritos = InscricaoDAL.obterAlunosPorUc(siglaUc, PASTA_BD);
        StringBuilder relatorio = new StringBuilder();
        int sucessos = 0, erros = 0;

        for (int numMec : alunosInscritos) {
            Estudante aluno = EstudanteDAL.procurarPorNumMec(numMec, PASTA_BD);
            String nome = (aluno != null) ? aluno.getNome() : "Desconhecido";

            // Obter a nota para este aluno (função fornecida pelo controller)
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
        relatorio.insert(0, String.format("Resumo: %d sucessos, %d falhas, %d saltos.\n", sucessos, erros, alunosInscritos.size() - sucessos - erros));
        return relatorio.toString();
    }

    /**
     * Retorna uma lista de strings "numMec - nome" para os alunos inscritos numa UC.
     */
    public List<String> obterAlunosInscritosNaUc(String siglaUc) {
        List<Integer> nums = InscricaoDAL.obterAlunosPorUc(siglaUc, PASTA_BD);
        List<String> alunosFormatados = new ArrayList<>();
        for (int num : nums) {
            Estudante e = EstudanteDAL.procurarPorNumMec(num, PASTA_BD);
            String nome = (e != null) ? e.getNome() : "Desconhecido";
            alunosFormatados.add(num + " - " + nome);
        }
        return alunosFormatados;
    }
}