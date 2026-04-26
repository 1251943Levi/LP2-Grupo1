package bll;

import dal.AvaliacaoDAL;
import dal.CredencialDAL;
import dal.EstudanteDAL;
import dal.UcDAL;
import model.Avaliacao;
import model.Docente;
import model.Estudante;
import model.UnidadeCurricular;
import utils.SegurancaPasswords;

import java.util.ArrayList;
import java.util.List;

/**
 * Lógica de negócio para operações do Docente.
 * Gere a filtragem de alunos por UC, o lançamento de avaliações e a segurança.
 */
public class DocenteBLL {

    private static final String PASTA_BD = "bd";

    /**
     * Verifica se uma determinada sigla de UC pertence ao plano de lecionação do docente.
     */
    public boolean lecionaEstaUC(Docente docente, String siglaUc) {
        if (siglaUc == null) return false;
        for (int i = 0; i < docente.getTotalUcsLecionadas(); i++) {
            if (docente.getUcsLecionadas()[i].getSigla().equalsIgnoreCase(siglaUc)) return true;
        }
        return false;
    }

    /**
     * Regista uma nova avaliação para um aluno, aplicando todas as validações:
     *  1. O aluno existe no sistema.
     *  2. A UC pertence às UCs lecionadas pelo docente.
     *  3. Não existe ainda uma avaliação para este aluno/UC/ano (evita duplicados).
     * @return true se o lançamento foi bem-sucedido; false com mensagem de erro.
     */
    public String lancarNota(int numMec, String siglaUc, int ano,
                             double n1, double n2, double n3, Docente d) {
        Estudante aluno = EstudanteDAL.procurarPorNumMec(numMec, PASTA_BD);
        if (aluno == null)
            return "ERRO: Aluno com nº " + numMec + " não encontrado.";

        if (!lecionaEstaUC(d, siglaUc))
            return "ERRO: A UC '" + siglaUc + "' não pertence às suas unidades curriculares.";

        if (AvaliacaoDAL.existeAvaliacao(numMec, siglaUc, ano, PASTA_BD))
            return "ERRO: Já existe uma avaliação registada para o aluno " + numMec
                    + " na UC '" + siglaUc + "' no ano " + ano + ".";

        UnidadeCurricular uc = UcDAL.procurarUC(siglaUc, PASTA_BD);
        if (uc == null)
            return "ERRO: A UC '" + siglaUc + "' não foi encontrada no sistema.";

        Avaliacao aval = new Avaliacao(uc, ano);
        if (n1 >= 0) aval.adicionarResultado(n1);
        if (n2 >= 0) aval.adicionarResultado(n2);
        if (n3 >= 0) aval.adicionarResultado(n3);

        AvaliacaoDAL.adicionarAvaliacao(aval, numMec, PASTA_BD);
        return null;
    }


    /**
     * Altera a password do docente com hashing e persistência centralizada.
     */
    public void alterarPassword(Docente docente, String novaPass) {
        String passSegura = SegurancaPasswords.gerarCredencialMista(novaPass);
        docente.setPassword(passSegura);
        CredencialDAL.atualizarPassword(docente.getEmail(), passSegura, PASTA_BD);
    }

    /**
     * Devolve uma lista de pares [Estudante, media] para os alunos do docente.
     * Cada elemento Object[] contém: [0] = Estudante, [1] = Double (média).
     * A lógica usa o histórico de avaliações (Avaliacao.getUc()) para cruzar
     * com as UCs lecionadas pelo docente.
     */
    public List<Object[]> obterAlunosDoDocenteComMedia(Docente docente) {
        List<Estudante> todos = new EstudanteBLL().carregarTodosCompleto();
        List<Object[]> resultado = new ArrayList<>();

        if (todos == null) return resultado;

        for (Estudante e : todos) {
            if (e == null || e.getPercurso() == null) continue;

            boolean alunoDoDocente = false;
            double somaMedias = 0;
            int totalAvaliacoes = 0;

            for (int i = 0; i < e.getPercurso().getTotalAvaliacoes(); i++) {
                Avaliacao av = e.getPercurso().getHistoricoAvaliacoes()[i];
                if (av == null || av.getUc() == null) continue;

                if (lecionaEstaUC(docente, av.getUc().getSigla())) {
                    alunoDoDocente = true;
                    somaMedias += av.calcularMedia();
                    totalAvaliacoes++;
                }
            }

            if (alunoDoDocente) {
                double media = totalAvaliacoes > 0 ? somaMedias / totalAvaliacoes : 0.0;
                resultado.add(new Object[]{e, media});
            }
        }
        return resultado;
    }
}