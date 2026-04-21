package dal;

import model.UnidadeCurricular;
import model.Docente;
import model.Curso;
import java.io.File;
import java.util.List;

/**
 * Responsável pelas operações de acesso a dados das Unidades Curriculares.
 */
public class UcDAL {
    private static final String NOME_FICHEIRO = "ucs.csv";

    public static UnidadeCurricular procurarUC(String sigla, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        UnidadeCurricular ucEncontrada = null;

        for (String linha : linhas) {
            String[] dados = linha.split(";", -1);

            if (dados.length >= 4 && dados[0].trim().equalsIgnoreCase(sigla)) {

                if (ucEncontrada == null) {
                    try {
                        int ano = Integer.parseInt(dados[2].trim());

                        String siglaDocente = dados[3].trim();
                        Docente doc = dal.DocenteDAL.procurarPorSigla(siglaDocente, pastaBase);

                        ucEncontrada = new UnidadeCurricular(dados[0].trim(), dados[1].trim(), ano, doc);
                    } catch (NumberFormatException e) {
                        continue;
                    }
                }
                    if (dados.length >= 5 && !dados[4].trim().equalsIgnoreCase("N/A")) {
                    String siglaCurso = dados[4].trim();
                    Curso curso = dal.CursoDAL.procurarCurso(siglaCurso, pastaBase);

                    if (curso != null) {
                        ucEncontrada.adicionarCurso(curso);
                    }
                }
            }
        }
        return ucEncontrada;
    }
}