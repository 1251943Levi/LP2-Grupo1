package bll;

import common.ConfigApp;
import dal.CursoDALFile;
import dal.CursoDALSql;
import dal.UcDALFile;
import dal.UcDALSql;

import model.UnidadeCurricular;
import model.Docente;
import model.Curso;
import dal.UcDAL;
import dal.DocenteDALFile;
import dal.DocenteDALSql;
import dal.CursoDAL;
import dal.CursoUcDAL;
import dal.CursoUcDALFile;
import dal.CursoUcDALSql;

/**
 * Lógica de negócio para a entidade UnidadeCurricular.
 * Constrói objetos UC com docente e cursos associados
 * e fornece listagens para os menus de seleção dos controllers.
 */
public class UcBLL {

    private static final String PASTA_BD = ConfigApp.PASTA_BD;
    private final CursoDAL cursoDAL = ConfigApp.isModoSql() ? new CursoDALSql() : new CursoDALFile();
    private final UcDAL ucDAL = ConfigApp.isModoSql() ? new UcDALSql() : new UcDALFile();
    private final CursoUcDAL cursoUcDAL = ConfigApp.isModoSql() ? new CursoUcDALSql() : new CursoUcDALFile();

    // ==================================================================
    // Card 2: UC independente + relação M:N com cursos (curso_uc)
    // ==================================================================

    /**
     * Cria a UC apenas se a sigla ainda não existir (evita duplicados).
     * Se já existir, não cria nova — fica disponível para ser associada.
     * @return true se a UC existe (criada agora ou já existente).
     */
    public boolean criarUC(UnidadeCurricular uc, String siglaCursoPrimario) {
        if (uc == null || uc.getSigla() == null || uc.getSigla().trim().isEmpty()) return false;
        if (procurarUCCompleta(uc.getSigla()) != null) return true; // já existe — não duplica
        String curso = (siglaCursoPrimario == null || siglaCursoPrimario.isEmpty()) ? "N/A" : siglaCursoPrimario;
        ucDAL.adicionarUC(uc, curso, PASTA_BD);
        return true;
    }

    /**
     * Associa uma UC (existente) a um curso num ano letivo.
     * @return true se associou; false se já existia a associação (rejeita duplicado).
     */
    public boolean associarUCaCurso(String siglaUC, String siglaCurso, int anoCurricular, int anoLetivo) {
        return cursoUcDAL.associar(siglaCurso, siglaUC, anoCurricular, anoLetivo);
    }

    /** Remove a associação UC-curso num ano letivo; a UC continua a existir. */
    public boolean removerAssociacaoUcCurso(String siglaUC, String siglaCurso, int anoLetivo) {
        return cursoUcDAL.removerAssociacao(siglaCurso, siglaUC, anoLetivo);
    }

    /** Cursos a que uma UC está associada num ano letivo. */
    public java.util.List<String> obterCursosDaUc(String siglaUC, int anoLetivo) {
        return cursoUcDAL.obterCursosPorUc(siglaUC, anoLetivo);
    }

    /**
     * Constrói e devolve uma UC com docente e cursos associados.
     * @param sigla Sigla da UC a pesquisar.
     * @return A UC construída, ou null se não existir.
     */
    public UnidadeCurricular procurarUCCompleta(String sigla) {
        String[] dados = ucDAL.obterDadosBrutosUC(sigla, PASTA_BD);
        if (dados == null) return null;

        try {
            String siglaUc   = dados[0].trim();
            String nomeUc    = dados[1].trim();
            int ano          = Integer.parseInt(dados[2].trim());
            String siglaDoc  = dados[3].trim();

            Docente docResponsavel =
                    (ConfigApp.isModoSql() ? new DocenteDALSql() : new DocenteDALFile())
                            .procurarPorSigla(siglaDoc);
            UnidadeCurricular uc = new UnidadeCurricular(siglaUc, nomeUc, ano, docResponsavel);

            if (dados.length >= 5
                    && !dados[4].trim().equalsIgnoreCase("N/A")
                    && !dados[4].trim().isEmpty()) {
                Curso curso = cursoDAL.procurarCurso(dados[4].trim(), PASTA_BD);
                if (curso != null) uc.adicionarCurso(curso);
            }

            // Ler número de momentos (coluna 6, retrocompatível)
            if (dados.length >= 7 && !dados[6].trim().isEmpty()) {
                try {
                    int momentos = Integer.parseInt(dados[6].trim());
                    if (momentos > 0) uc.setNumMomentos(momentos);
                } catch (NumberFormatException ignored) {}
            }

            // Ler estado curricular (coluna 7, retrocompatível) — Card 1
            if (dados.length >= 8 && !dados[7].trim().isEmpty()) {
                uc.setEstadoCurricular(model.EstadoCurricular.fromString(dados[7].trim()));
            }

            return uc;

        } catch (NumberFormatException e) {
            System.err.println(">> Erro na BLL ao construir a UC " + sigla + ": ano inválido.");
            return null;
        }
    }

    /**
     * Devolve um array "SIGLA - Nome" de todas as UCs para menus de seleção.
     * @return Array de strings.
     */
    public String[] obterListaUcs() {
        return ucDAL.obterListaUcs(PASTA_BD);
    }
}