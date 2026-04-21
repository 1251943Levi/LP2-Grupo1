package bll;

import model.*;
import utils.*;

/**
 * Lógica de negócio para operações do Docente.
 * Gere a filtragem de alunos por UC e o lançamento de avaliações.
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
     * Regista uma nova avaliação para um aluno, validando a existência do mesmo.
     * @return true se o lançamento foi bem sucedido.
     */
    public boolean lancarNota(int numMec, String siglaUc, int ano, double n1, double n2, double n3, Docente d) {
        Estudante aluno = ImportadorCSV.procurarEstudantePorNumMec(numMec, PASTA_BD);
        if (aluno == null) return false;

        UnidadeCurricular uc = new UnidadeCurricular(siglaUc, "UC Lançada", 1, d);
        Avaliacao aval = new Avaliacao(uc, ano);
        aval.adicionarResultado(n1);
        aval.adicionarResultado(n2);
        aval.adicionarResultado(n3);

        ExportadorCSV.adicionarAvaliacao(aval, numMec, PASTA_BD);
        return true;
    }

    /**
     * Altera a password do docente com hashing e persistência centralizada.
     */
    public void alterarPassword(Docente docente, String novaPass) {
        String passSegura = SegurancaPasswords.gerarCredencialMista(novaPass);
        docente.setPassword(passSegura);
        ExportadorCSV.atualizarPasswordCentralizada(docente.getEmail(), passSegura, PASTA_BD);
    }
}