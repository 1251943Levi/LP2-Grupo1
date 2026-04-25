package bll;

import model.UnidadeCurricular;
import model.Docente;
import model.Curso;
import dal.UcDAL;
import dal.DocenteDAL;
import dal.CursoDAL;

/**
 * Camada de Lógica de Negócio para as Unidades Curriculares.
 * Orquestra a construção dos objetos e delega listagens à DAL.
 *
 * NÃO referencia ImportadorCSV nem ExportadorCSV.
 */
public class UcBLL {

    private static final String PASTA_BD = "bd";

    /**
     * Constrói o objeto UnidadeCurricular completo com as suas dependências hidratadas.
     */
    public UnidadeCurricular procurarUCCompleta(String sigla) {
        String[] dados = UcDAL.obterDadosBrutosUC(sigla, PASTA_BD);
        if (dados == null) return null;

        try {
            String siglaUc   = dados[0].trim();
            String nomeUc    = dados[1].trim();
            int ano          = Integer.parseInt(dados[2].trim());
            String siglaDoc  = dados[3].trim();

            Docente docResponsavel = DocenteDAL.procurarPorSigla(siglaDoc, PASTA_BD);
            UnidadeCurricular uc = new UnidadeCurricular(siglaUc, nomeUc, ano, docResponsavel);

            if (dados.length >= 5
                    && !dados[4].trim().equalsIgnoreCase("N/A")
                    && !dados[4].trim().isEmpty()) {
                Curso curso = CursoDAL.procurarCurso(dados[4].trim(), PASTA_BD);
                if (curso != null) uc.adicionarCurso(curso);
            }

            return uc;

        } catch (NumberFormatException e) {
            System.err.println(">> Erro na BLL ao construir a UC " + sigla + ": ano inválido.");
            return null;
        }
    }

    /**
     * Devolve um array de strings com o formato "SIGLA - Nome" para todas as UCs.
     * Usado pelo GestorController nos menus de seleção de UC.
     */
    public String[] obterListaUcs() {
        return UcDAL.obterListaUcs(PASTA_BD);
    }

    /**
     * Devolve uma listagem formatada de todas as UCs para exibição no ecrã.
     */
    public String listarTodasUcs() {
        return UcDAL.listarTodasUcs(PASTA_BD);
    }

}