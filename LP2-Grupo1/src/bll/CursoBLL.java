package bll;

import model.Curso;
import model.Departamento;
import dal.CursoDAL;
import dal.DepartamentoDAL;

/**
 * Lógica de negócio para a gestão de Cursos.
 * Orquestra a hidratação do objeto Curso com o seu Departamento.
 */
public class CursoBLL {

    private static final String PASTA_BD = "bd";

    /**
     * Constrói e devolve o objeto Curso completo com base na sua sigla.
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
}