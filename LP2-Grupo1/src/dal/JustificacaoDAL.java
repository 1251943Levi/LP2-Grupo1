package dal;

import model.Justificacao;
import java.util.List;

public interface JustificacaoDAL {
    void inicializar();
    void adicionar(Justificacao justificacao);
    void atualizar(Justificacao justificacao);
    Justificacao buscarPorId(int id);
    List<Justificacao> listarPorAluno(int numMec);
    List<Justificacao> listarPorAula(int idAula);
    List<Justificacao> listarPendentes();
    List<Justificacao> listarTodas();
    void removerPorAula(int idAula);
}