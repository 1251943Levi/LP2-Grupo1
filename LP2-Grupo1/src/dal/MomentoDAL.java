package dal;

import model.Momento;
import java.util.List;

/**
 * Contrato de acesso aos momentos de avaliação (Card 3).
 * Implementado por {@link MomentoDALFile} (CSV) e {@link MomentoDALSql} (SQL Server).
 */
public interface MomentoDAL {

    /** Garante o suporte de dados (ficheiro/tabela + lookup tipo_momento). */
    void inicializar();

    /** Persiste um momento e devolve o id atribuído. */
    int adicionar(Momento momento);

    /** Momentos de uma UC. */
    List<Momento> listarPorUc(String siglaUC);

    /** Momento por id, ou null. */
    Momento procurarPorId(int id);

    /** Soma dos pesos (%) dos momentos de uma UC. */
    double somaPesos(String siglaUC);

    /** Remove um momento pelo id. @return true se removeu. */
    boolean remover(int id);
}
