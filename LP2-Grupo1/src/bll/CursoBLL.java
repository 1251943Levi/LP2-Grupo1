package bll;

import dal.EstudanteDAL;
import model.Curso;
import model.Departamento;
import dal.CursoDAL;
import dal.DepartamentoDAL;

/**
 * Lógica de negócio para a entidade Curso.
 * Responsável por construir o objeto Curso com o departamento associado,
 * evitando que os controllers acedam diretamente à DAL.
 */
public class CursoBLL {

    private static final String PASTA_BD = "bd";

    /**
     * Constrói e devolve um objeto Curso completamente preenchido.
     * @param sigla Sigla do curso a pesquisar.
     * @return O Curso com departamento associado, ou null se não existir.
     */
    public Curso procurarCursoCompleto(String sigla) {
        String[] dados = CursoDAL.obterDadosBrutosCurso(sigla, PASTA_BD);

        if (dados == null) return null;

        String siglaCurso = dados[0].trim();
        String nomeCurso = dados[1].trim();
        String siglaDepartamento = dados[2].trim();

        double propina = 0.0;
        if (dados.length >= 4) {
            try {
                propina = Double.parseDouble(dados[3].trim());
            } catch (NumberFormatException ignored) {}
        }

        Departamento dep = DepartamentoDAL.procurarDepartamento(siglaDepartamento, PASTA_BD);

        Curso curso = new Curso(siglaCurso, nomeCurso, dep, propina);

        if (dados.length >= 5 && !dados[4].trim().isEmpty()) {
            curso.setEstado(dados[4].trim());
        }

        return curso;
    }

    /**
     * Valida se um curso está pronto para receber matrículas,
     * verificando se tem pelo menos 1 UC configurada no 1º ano.
     * @param siglaCurso Sigla do curso a verificar.
     * @return true se tiver pelo menos 1 UC no 1º ano.
     */
    public boolean verificarCursoTemUcs(String siglaCurso) {
        return dal.UcDAL.contarUcsPorCursoEAno(siglaCurso, 1, PASTA_BD) > 0;
    }

    /**
     * Valida se o primeiro ano de um curso pode ser iniciado.
     * @param siglaCurso Sigla do curso a verificar.
     * @return true se tiver 5 ou mais alunos
     */
    public boolean verificarQuorumPrimeiroAno(String siglaCurso) {
        int total = dal.EstudanteDAL.contarEstudantesPorCursoEAno(siglaCurso, 1, "bd");
        return total >= 5;
    }
}