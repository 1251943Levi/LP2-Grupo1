package dal;

import model.Curso;
import model.Departamento;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CursoDAL {
    private static final String NOME_FICHEIRO = "cursos.csv";
    private static final String CABECALHO = "sigla;nome;siglaDepartamento;propina;estado";

    public static void adicionarCurso(Curso curso, String pastaBase) {
        if (curso == null) return;
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        DALUtil.garantirFicheiroECabecalho(caminho, CABECALHO);

        String siglaDep = (curso.getDepartamento() != null) ? curso.getDepartamento().getSigla() : "N/A";
        String linha = curso.getSigla() + ";" + curso.getNome() + ";" + siglaDep + ";"
                + curso.getValorPropinaAnual() + ";" + curso.getEstado();

        DALUtil.adicionarLinhaCSV(caminho, linha);
    }

    public static Curso procurarCurso(String sigla, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);

        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;

            String[] dados = linha.split(";", -1);
            if (dados.length >= 3 && dados[0].trim().equalsIgnoreCase(sigla)) {
                Departamento dep = DepartamentoDAL.procurarDepartamento(dados[2].trim(), pastaBase);
                double propina = 0.0;

                if (dados.length >= 4) {
                    try { propina = Double.parseDouble(dados[3].trim()); }
                    catch (NumberFormatException ex) { propina = 0.0; }
                }
                return new Curso(dados[0].trim(), dados[1].trim(), dep, propina);
            }
        }
        return null;
    }

    public static void atualizarCurso(Curso cursoAtualizado, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhasAntigas = DALUtil.lerFicheiro(caminho);

        if (linhasAntigas.isEmpty()) return;

        List<String> linhasAtualizadas = new ArrayList<>();
        boolean atualizado = false;

        for (String linha : linhasAntigas) {
            if (linha.equalsIgnoreCase(CABECALHO)) {
                linhasAtualizadas.add(linha);
                continue;
            }

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

        if (atualizado) {
            DALUtil.reescreverFicheiro(caminho, linhasAtualizadas);
        }
    }
}