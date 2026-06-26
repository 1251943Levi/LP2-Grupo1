package dal;

import model.EstatutoEstudante;
import java.util.List;

/**
 * Acesso a dados dos estatutos de estudante.
 * Cobre o catálogo de estatutos (CRUD) e a associação
 * estudante &harr; estatuto (atribuir / listar por estudante).
 */
public interface EstatutoDAL {
    void inicializar();

    // ---- Catálogo de estatutos ----
    List<EstatutoEstudante> listarTodos();
    EstatutoEstudante buscarPorId(int id);
    boolean adicionar(EstatutoEstudante estatuto);
    boolean atualizar(EstatutoEstudante estatuto);
    boolean remover(int id);

    // ---- Associação a estudante ----
    boolean atribuir(int numMec, int idEstatuto);
    boolean removerAtribuicao(int numMec, int idEstatuto);
    List<EstatutoEstudante> listarPorEstudante(int numMec);
}
