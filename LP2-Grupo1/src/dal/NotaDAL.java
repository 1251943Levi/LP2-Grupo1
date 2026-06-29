package dal;

import model.Nota;
import java.util.List;

/**
 * Contrato de acesso às notas por momento (Card 5).
 * Implementado por {@link NotaDALFile} (CSV) e {@link NotaDALSql} (SQL Server).
 */
public interface NotaDAL {

    void inicializar();

    /** Insere ou atualiza a nota de um momento (upsert por numMec+idMomento). */
    void guardar(Nota nota);

    /** Nota de um aluno num momento, ou null. */
    Nota procurar(int numMec, int idMomento);

    /** Notas de um aluno numa UC. */
    List<Nota> listarPorAlunoEUc(int numMec, String siglaUC);

    /** Remove a nota de um aluno num momento. */
    boolean remover(int numMec, int idMomento);
}
