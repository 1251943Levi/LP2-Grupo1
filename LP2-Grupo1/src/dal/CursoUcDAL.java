package dal;

import java.util.List;

/**
 * Contrato da associação muitos-para-muitos entre Cursos e UCs (Card 2).
 * Tabela/ficheiro [curso_uc] com PK composta (siglaCurso, siglaUC, anoLetivo).
 * Implementado por {@link CursoUcDALFile} (CSV) e {@link CursoUcDALSql} (SQL Server).
 */
public interface CursoUcDAL {

    /** Garante que o suporte de dados existe (cria ficheiro/tabela). */
    void inicializar();

    /** Indica se já existe a associação (siglaCurso, siglaUC, anoLetivo). */
    boolean existeAssociacao(String siglaCurso, String siglaUC, int anoLetivo);

    /**
     * Associa uma UC a um curso num ano letivo.
     * @return true se associou; false se já existia (rejeita duplicado).
     */
    boolean associar(String siglaCurso, String siglaUC, int anoCurricular, int anoLetivo);

    /** Remove a associação (a UC continua a existir). @return true se removeu. */
    boolean removerAssociacao(String siglaCurso, String siglaUC, int anoLetivo);

    /** Cursos a que uma UC está associada num ano letivo. */
    List<String> obterCursosPorUc(String siglaUC, int anoLetivo);

    /** Siglas das UCs de um curso, por ano curricular e ano letivo. */
    List<String> obterSiglasUcsPorCursoEAno(String siglaCurso, int anoCurricular, int anoLetivo);

    /** Conta as UCs de um curso, por ano curricular e ano letivo. */
    int contarUcsPorCursoEAno(String siglaCurso, int anoCurricular, int anoLetivo);
}
