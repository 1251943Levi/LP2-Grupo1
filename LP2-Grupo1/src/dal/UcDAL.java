package dal;

import model.UnidadeCurricular;
import model.Docente;
import model.Curso;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Acesso aos dados de unidades curriculares em ucs.csv.
 * Formato das colunas: sigla; nome; anoCurricular; siglaDocente; siglaCurso; ects.
 * A mesma UC pode aparecer em múltiplas linhas quando pertence a vários cursos.
 * A coluna ects é retrocompatível: registos sem essa coluna usam ECTS_PADRAO.
 */
public class UcDAL {
    private static final String NOME_FICHEIRO = "ucs.csv";
    private static final String CABECALHO = "sigla;nome;anoCurricular;siglaDocenteResponsavel;siglaCurso;ects";


    /**
     * Devolve os campos em bruto da primeira ocorrência de uma UC pela sigla.
     * @param sigla     Sigla da UC.
     * @param pastaBase Caminho da pasta de dados.
     * @return Array de campos CSV, ou null se não existir.
     */
    public static String[] obterDadosBrutosUC(String sigla, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);

        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 4 && dados[0].trim().equalsIgnoreCase(sigla)) {
                return dados;
            }
        }
        return null;
    }

    /**
     * Constrói um objeto UC com todas as dependências associadas.
     * Agrega todas as linhas da mesma sigla para preencher os cursos associados.
     * @param sigla     Sigla da UC a pesquisar.
     * @param pastaBase Caminho da pasta de dados.
     * @return A UC completa, ou null se não existir.
     */
    public static UnidadeCurricular procurarUC(String sigla, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        UnidadeCurricular ucEncontrada = null;

        for (String linha : linhas) {
            String[] dados = linha.split(";", -1);
            if (dados.length >= 4 && dados[0].trim().equalsIgnoreCase(sigla)) {
                if (ucEncontrada == null) {
                    try {
                        int ano  = Integer.parseInt(dados[2].trim());
                        int ects = (dados.length >= 6 && !dados[5].trim().isEmpty())
                                ? Integer.parseInt(dados[5].trim())
                                : model.UnidadeCurricular.ECTS_PADRAO;
                        Docente doc = DocenteDAL.procurarPorSigla(dados[3].trim(), pastaBase);
                        ucEncontrada = new UnidadeCurricular(dados[0].trim(), dados[1].trim(), ano, doc, ects);
                    } catch (NumberFormatException e) { continue; }
                }
                if (dados.length >= 5 && !dados[4].trim().equalsIgnoreCase("N/A")) {
                    Curso curso = CursoDAL.procurarCurso(dados[4].trim(), pastaBase);
                    if (curso != null) ucEncontrada.adicionarCurso(curso);
                }
            }
        }
        return ucEncontrada;
    }

    /**
     * Persiste uma nova UC no ficheiro CSV.
     * @param uc         UC a adicionar.
     * @param siglaCurso Sigla do curso ao qual a UC pertence.
     * @param pastaBase  Caminho da pasta de dados.
     */
    public static void adicionarUC(UnidadeCurricular uc, String siglaCurso, String pastaBase) {
        if (uc == null) return;
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        DALUtil.garantirFicheiroECabecalho(caminho, CABECALHO);

        String siglaDocente = (uc.getDocenteResponsavel() != null)
                ? uc.getDocenteResponsavel().getSigla() : "N/A";
        String cursoStr = (siglaCurso != null && !siglaCurso.isEmpty()) ? siglaCurso : "N/A";

        DALUtil.adicionarLinhaCSV(caminho,
                uc.getSigla() + ";" + uc.getNome() + ";"
                        + uc.getAnoCurricular() + ";" + siglaDocente + ";" + cursoStr
                        + ";" + uc.getEcts());
    }

    /**
     * Remove todas as linhas de uma UC pela sua sigla.
     * @param siglaUc   Sigla da UC a eliminar.
     * @param pastaBase Caminho da pasta de dados.
     * @return true se foi encontrada e removida.
     */
    public static boolean removerUC(String siglaUc, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhasAntigas = DALUtil.lerFicheiro(caminho);
        if (linhasAntigas.isEmpty()) return false;

        List<String> linhasAtualizadas = new ArrayList<>();
        boolean encontrou = false;

        for (String linha : linhasAntigas) {
            if (linha.equalsIgnoreCase(CABECALHO)) { linhasAtualizadas.add(linha); continue; }
            String[] dados = linha.split(";", -1);
            if (dados.length > 0 && dados[0].trim().equalsIgnoreCase(siglaUc)) {
                encontrou = true;
            } else {
                linhasAtualizadas.add(linha);
            }
        }

        if (encontrou) DALUtil.reescreverFicheiro(caminho, linhasAtualizadas);
        return encontrou;
    }


    /**
     * Devolve as siglas das UCs de um curso num determinado ano curricular.
     * @param siglaCurso Sigla do curso.
     * @param ano        Ano curricular (1, 2 ou 3).
     * @param pastaBase  Caminho da pasta de dados.
     * @return Lista de siglas sem duplicados.
     */
    public static List<String> obterSiglasUcsPorCursoEAno(String siglaCurso, int ano, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        List<String> siglas = new ArrayList<>();

        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 5) {
                try {
                    int anoCurricular = Integer.parseInt(dados[2].trim());
                    if (anoCurricular == ano && dados[4].trim().equalsIgnoreCase(siglaCurso)) {
                        String sigla = dados[0].trim();
                        if (!siglas.contains(sigla)) siglas.add(sigla);
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return siglas;
    }

    /**
     * Conta o número de UCs de um curso num dado ano.
     * @param siglaCurso Sigla do curso.
     * @param ano        Ano curricular.
     * @param pastaBase  Caminho da pasta de dados.
     * @return Contagem de UCs.
     */
    public static int contarUcsPorCursoEAno(String siglaCurso, int ano, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        int contagem = 0;

        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;

            String[] dados = linha.split(";", -1);
            if (dados.length >= 5) {
                try {
                    int anoCurricular = Integer.parseInt(dados[2].trim());
                    if (anoCurricular == ano && dados[4].trim().equalsIgnoreCase(siglaCurso)) {
                        contagem++;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return contagem;
    }

    /**
     * Devolve um array "SIGLA - Nome" de todas as UCs para menus de seleção.
     * @param pastaBase Caminho da pasta de dados.
     * @return Array de strings.
     */
    public static String[] obterListaUcs(String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        List<String> lista = new ArrayList<>();

        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 2) {
                String entrada = dados[0].trim() + " - " + dados[1].trim();
                if (!lista.contains(entrada)) lista.add(entrada);
            }
        }
        return lista.toArray(new String[0]);
    }

    /**
     * Devolve uma listagem formatada de todas as UCs para exibição em consola.
     * @param pastaBase Caminho da pasta de dados.
     * @return String com todas as UCs.
     */
    public static String listarTodasUcs(String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        StringBuilder sb = new StringBuilder("\n--- LISTA DE UNIDADES CURRICULARES ---\n");

        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 5) {
                sb.append("Sigla: ").append(dados[0].trim())
                        .append(" | Nome: ").append(dados[1].trim())
                        .append(" | Ano: ").append(dados[2].trim())
                        .append(" | Docente: ").append(dados[3].trim())
                        .append(" | Curso: ").append(dados[4].trim());
                if (dados.length >= 6 && !dados[5].trim().isEmpty())
                    sb.append(" | ECTS: ").append(dados[5].trim());
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Devolve o plano de estudos de um curso agrupado por ano curricular.
     * @param siglaCurso Sigla do curso.
     * @param pastaBase  Caminho da pasta de dados.
     * @return Plano de estudos formatado.
     */
    public static String listarUcsPorCurso(String siglaCurso, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        Map<Integer, List<String>> ucsPorAno = new TreeMap<>();

        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 5 && dados[4].trim().equalsIgnoreCase(siglaCurso)) {
                try {
                    int ano = Integer.parseInt(dados[2].trim());
                    ucsPorAno.putIfAbsent(ano, new ArrayList<>());
                    ucsPorAno.get(ano).add("[" + dados[0].trim() + "] "
                            + dados[1].trim()
                            + " (Doc. Resp: " + dados[3].trim()
                            + " | ECTS: " + (dados.length >= 6 && !dados[5].trim().isEmpty() ? dados[5].trim() : model.UnidadeCurricular.ECTS_PADRAO) + ")");
                } catch (NumberFormatException ignored) {}
            }
        }

        if (ucsPorAno.isEmpty())
            return ">> Não existem UCs associadas ao curso " + siglaCurso + ".";

        StringBuilder sb = new StringBuilder("\n--- PLANO DE ESTUDOS: " + siglaCurso + " ---\n");
        for (Map.Entry<Integer, List<String>> entry : ucsPorAno.entrySet()) {
            sb.append(">> Ano ").append(entry.getKey()).append(":\n");
            for (String ucStr : entry.getValue())
                sb.append("   - ").append(ucStr).append("\n");
        }
        return sb.toString();
    }

    /**
     * Devolve as siglas das UCs associadas a um docente.
     * @param siglaDocente Sigla do docente.
     * @param pastaBase    Caminho da pasta de dados.
     * @return Lista de siglas.
     */
    public static List<String> obterSiglasUcsPorDocente(String siglaDocente, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        List<String> siglas = new ArrayList<>();

        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 4 && dados[3].trim().equalsIgnoreCase(siglaDocente)) {
                siglas.add(dados[0].trim());
            }
        }
        return siglas;
    }

    /**
     * Constrói objetos UC para todas as UCs de um docente.
     * Usado para carregar as UCs do docente após o login.
     * @param docente   Docente cujas UCs se pretendem carregar.
     * @param pastaBase Caminho da pasta de dados.
     * @return Lista de UCs do docente.
     */
    public static List<UnidadeCurricular> obterUcsPorDocente(Docente docente, String pastaBase) {
        List<String> siglas = obterSiglasUcsPorDocente(docente.getSigla(), pastaBase);
        List<UnidadeCurricular> ucs = new ArrayList<>();

        for (String sigla : siglas) {
            try {
                String[] dados = obterDadosBrutosUC(sigla, pastaBase);
                if (dados != null && dados.length >= 3) {
                    int ano  = Integer.parseInt(dados[2].trim());
                    int ects = (dados.length >= 6 && !dados[5].trim().isEmpty())
                            ? Integer.parseInt(dados[5].trim())
                            : model.UnidadeCurricular.ECTS_PADRAO;
                    ucs.add(new UnidadeCurricular(dados[0].trim(), dados[1].trim(), ano, docente, ects));
                }
            } catch (NumberFormatException ignored) {}
        }
        return ucs;
    }
}