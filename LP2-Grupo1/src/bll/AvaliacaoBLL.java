package bll;

import common.ConfigApp;
import dal.AvaliacaoDAL;
import dal.AvaliacaoDALFile;
import dal.AvaliacaoDALSql;
import dal.MomentoDAL;
import dal.MomentoDALFile;
import dal.MomentoDALSql;
import dal.NotaDAL;
import dal.NotaDALFile;
import dal.NotaDALSql;
import dal.UcDAL;
import dal.UcDALFile;
import dal.UcDALSql;
import model.Avaliacao;
import model.Momento;
import model.Nota;

import java.util.List;

/**
 * Fonte de verdade ÚNICA da avaliação de um aluno numa UC (unificação dos
 * Cards 3/5 com o sistema antigo).
 *
 * Regra: se a UC tiver momentos de avaliação definidos, usa a NOTA FINAL
 * PONDERADA (Cards 3/5); caso contrário, recorre ao sistema antigo
 * ({@link Avaliacao}). Isto permite migrar os fluxos sem partir dados/UCs
 * que ainda não usam momentos.
 */
public class AvaliacaoBLL {

    private static final double NOTA_MINIMA = 9.5;

    private final MomentoDAL momentoDAL =
            ConfigApp.isModoSql() ? new MomentoDALSql() : new MomentoDALFile();
    private final NotaDAL notaDAL =
            ConfigApp.isModoSql() ? new NotaDALSql() : new NotaDALFile();
    private final AvaliacaoDAL avaliacaoDAL =
            ConfigApp.isModoSql() ? new AvaliacaoDALSql() : new AvaliacaoDALFile();
    private final UcDAL ucDAL =
            ConfigApp.isModoSql() ? new UcDALSql() : new UcDALFile();

    /** true se a UC usa o sistema de momentos tipificados (Card 3). */
    public boolean ucUsaMomentos(String siglaUC) {
        return !momentoDAL.listarPorUc(siglaUC).isEmpty();
    }

    /** Nota final de um aluno numa UC (ponderada se houver momentos; senão, média do sistema antigo). */
    public double notaFinal(int numMec, String siglaUC, int anoLetivo) {
        List<Momento> momentos = momentoDAL.listarPorUc(siglaUC);
        if (!momentos.isEmpty()) {
            double somaPesos = 0, somaPond = 0;
            for (Momento m : momentos) {
                Nota n = notaDAL.procurar(numMec, m.getId());
                if (n != null) {
                    somaPond += n.getValor() * m.getPeso();
                    somaPesos += m.getPeso();
                }
            }
            return somaPesos == 0 ? 0.0 : somaPond / somaPesos;
        }
        Avaliacao av = avaliacaoDAL.obterAvaliacao(numMec, siglaUC, anoLetivo);
        return av == null ? 0.0 : av.calcularMedia();
    }

    /** Aprovado numa UC (≥ 9,5): nota final ponderada se houver momentos; senão, regra do sistema antigo. */
    public boolean aprovadoNaUc(int numMec, String siglaUC, int anoLetivo) {
        if (ucUsaMomentos(siglaUC)) {
            return notaFinal(numMec, siglaUC, anoLetivo) >= NOTA_MINIMA;
        }
        Avaliacao av = avaliacaoDAL.obterAvaliacao(numMec, siglaUC, anoLetivo);
        return av != null && av.isAprovado();
    }

    /**
     * Indica se faltam notas/momentos por lançar a um aluno numa UC.
     * Com momentos: pendente se algum momento não tiver nota.
     * Sem momentos: pendente se faltarem notas face ao nº de momentos (sistema antigo).
     */
    public boolean temNotasPendentes(int numMec, String siglaUC, int anoLetivo) {
        List<Momento> momentos = momentoDAL.listarPorUc(siglaUC);
        if (!momentos.isEmpty()) {
            for (Momento m : momentos) {
                if (notaDAL.procurar(numMec, m.getId()) == null) return true;
            }
            return false;
        }
        int numMomentos = ucDAL.obterMomentos(siglaUC, ConfigApp.PASTA_BD);
        if (numMomentos <= 0) numMomentos = 1;
        Avaliacao av = avaliacaoDAL.obterAvaliacao(numMec, siglaUC, anoLetivo);
        int lancadas = (av == null) ? 0 : av.getTotalAvaliacoesLancadas();
        return lancadas < numMomentos;
    }
}
