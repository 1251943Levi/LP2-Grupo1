package dal;

import model.Curso;
import model.Departamento;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Acesso aos dados de cursos armazenados em cursos.csv.
 * Formato das colunas: sigla; nome; siglaDepartamento; propina; estado.
 */
public class CursoDAL {
    private static final String NOME_FICHEIRO = "cursos.csv";
    private static final String CABECALHO = "sigla;nome;siglaDepartamento;propina;estado";


    // --- ESCRITA ---

    /**
     * Persiste um novo curso no ficheiro CSV.
     * @param curso     Curso a adicionar.
     * @param pastaBase Caminho da pasta de dados.
     */
    public static void adicionarCurso(Curso curso, String pastaBase) {
        if (curso == null) return;
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        DALUtil.garantirFicheiroECabecalho(caminho, CABECALHO);

        String siglaDep = (curso.getDepartamento() != null)
                ? curso.getDepartamento().getSigla() : "N/A";
        String linha = curso.getSigla() + ";" + curso.getNome() + ";" + siglaDep + ";"
                + curso.getValorPropinaAnual() + ";" + curso.getEstado();

        DALUtil.adicionarLinhaCSV(caminho, linha);
    }


    /**
     * Atualiza o registo de um curso existente.
     * A sigla é usada como chave de pesquisa.
     * @param cursoAtualizado Curso com os dados novos.
     * @param pastaBase       Caminho da pasta de dados.
     */
    public static void atualizarCurso(Curso cursoAtualizado, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhasAntigas = DALUtil.lerFicheiro(caminho);
        if (linhasAntigas.isEmpty()) return;

        List<String> linhasAtualizadas = new ArrayList<>();
        boolean atualizado = false;

        for (String linha : linhasAntigas) {
            if (linha.equalsIgnoreCase(CABECALHO)) { linhasAtualizadas.add(linha); continue; }
            String[] dados = linha.split(";", -1);
            if (dados.length > 0 && dados[0].trim().equalsIgnoreCase(cursoAtualizado.getSigla())) {
                String siglaDep = (cursoAtualizado.getDepartamento() != null)
                        ? cursoAtualizado.getDepartamento().getSigla() : "N/A";
                linhasAtualizadas.add(cursoAtualizado.getSigla() + ";" + cursoAtualizado.getNome() + ";"
                        + siglaDep + ";" + cursoAtualizado.getValorPropinaAnual() + ";"
                        + cursoAtualizado.getEstado());
                atualizado = true;
            } else {
                linhasAtualizadas.add(linha);
            }
        }
        if (atualizado) DALUtil.reescreverFicheiro(caminho, linhasAtualizadas);
    }

    /**
     * Remove um curso pelo sua sigla.
     * @param sigla     Sigla do curso a eliminar.
     * @param pastaBase Caminho da pasta de dados.
     * @return true se o curso foi encontrado e removido.
     */
    public static boolean removerCurso(String sigla, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhasAntigas = DALUtil.lerFicheiro(caminho);
        if (linhasAntigas.isEmpty()) return false;

        List<String> linhasAtualizadas = new ArrayList<>();
        boolean encontrou = false;

        for (String linha : linhasAntigas) {
            if (linha.equalsIgnoreCase(CABECALHO)) { linhasAtualizadas.add(linha); continue; }
            String[] dados = linha.split(";", -1);
            if (dados.length > 0 && dados[0].trim().equalsIgnoreCase(sigla)) {
                encontrou = true;
            } else {
                linhasAtualizadas.add(linha);
            }
        }
        if (encontrou) DALUtil.reescreverFicheiro(caminho, linhasAtualizadas);
        return encontrou;
    }

    /**
     * Devolve os campos em bruto de um curso para construção na BLL.
     * @param sigla     Sigla do curso a pesquisar.
     * @param pastaBase Caminho da pasta de dados.
     * @return Array de campos CSV, ou null se não existir.
     */
    public static String[] obterDadosBrutosCurso(String sigla, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);

        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 3 && dados[0].trim().equalsIgnoreCase(sigla)) {
                return dados;
            }
        }
        return null;
    }

    /**
     * Constrói e devolve um objeto Curso com o departamento associado.
     * @param sigla     Sigla do curso.
     * @param pastaBase Caminho da pasta de dados.
     * @return O Curso completo, ou null se não existir.
     */
    public static Curso procurarCurso(String sigla, String pastaBase) {
        String[] dados = obterDadosBrutosCurso(sigla, pastaBase);
        if (dados == null) return null;

        double propina = 0.0;
        if (dados.length >= 4) {
            try { propina = Double.parseDouble(dados[3].trim()); }
            catch (NumberFormatException ignored) {}
        }

        Departamento dep = DepartamentoDAL.procurarDepartamento(
                dados.length >= 3 ? dados[2].trim() : "N/A", pastaBase);

        Curso curso = new Curso(dados[0].trim(), dados[1].trim(), dep, propina);

        if (dados.length >= 5 && !dados[4].trim().isEmpty())
            curso.setEstado(dados[4].trim());

        return curso;
    }

    /**
     * Devolve um array "SIGLA - Nome" de todos os cursos para menus de seleção.
     * @param pastaBase Caminho da pasta de dados.
     * @return Array de strings.
     */
    public static String[] obterListaCursos(String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        List<String> listaCursos = new ArrayList<>();

        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 2) {
                listaCursos.add(dados[0].trim() + " - " + dados[1].trim());
            }
        }
        return listaCursos.toArray(new String[0]);
    }

    /**
     * Devolve uma listagem formatada de todos os cursos para exibição em consola.
     * @param pastaBase Caminho da pasta de dados.
     * @return String com todos os cursos.
     */
    public static String listarTodosCursos(String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        StringBuilder sb = new StringBuilder("\n--- LISTA DE CURSOS ---\n");

        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 3) {
                sb.append("Sigla: ").append(dados[0].trim())
                        .append(" | Nome: ").append(dados[1].trim())
                        .append(" | Departamento: ").append(dados[2].trim());
                if (dados.length >= 5) sb.append(" | Estado: ").append(dados[4].trim());
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}