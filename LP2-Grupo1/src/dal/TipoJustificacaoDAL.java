package dal;

import model.TipoJustificacao;
import java.util.List;

public interface TipoJustificacaoDAL {
    void inicializar();
    List<TipoJustificacao> listarTodos();
    TipoJustificacao buscarPorId(int id);
    boolean adicionar(TipoJustificacao tipo);
    boolean atualizar(TipoJustificacao tipo);
    boolean remover(int id);
}