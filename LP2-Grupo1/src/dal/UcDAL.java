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
 * Responsável pelas operações de acesso a dados das Unidades Curriculares.
 * Esta classe SÓ lê/escreve texto CSV. A construção de objetos ricos
 * com dependências hidratadas é responsabilidade da UcBLL.
 */
public class UcDAL {
    private static final String NOME_FICHEIRO = "ucs.csv";
    private static final String CABECALHO = "sigla;nome;anoCurricular;siglaDocenteResponsavel;siglaCurso;ects";


    /**
     * Devolve os dados crus de uma UC (primeira ocorrência encontrada pela sigla).
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
     * Constrói o objeto UnidadeCurricular com todas as dependências hidratadas.
     * Pode ter várias linhas no CSV para a mesma sigla (uma por curso associado).
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
     * Devolve as siglas de todas as UCs de um determinado curso e ano curricular.
     * Usado ao inscrever um estudante nas UCs do seu primeiro ano.
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
     * Conta quantas UCs existem num determinado curso e ano.
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
     * Devolve um array de strings com o formato "SIGLA - Nome" para todas as UCs.
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
     * Devolve uma listagem formatada de todas as UCs (para exibição no ecrã).
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
     * Devolve a listagem das UCs de um curso, agrupadas por ano curricular.
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
     * Devolve uma lista de siglas de UCs associadas a um docente específico.
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
     * Devolve uma lista de objetos UnidadeCurricular do docente (para associar no login).
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
}