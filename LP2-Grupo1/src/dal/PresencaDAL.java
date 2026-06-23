package dal;

import model.Presenca;
import java.util.List;

public interface PresencaDAL {
    void inicializar();
    void adicionar(Presenca presenca);
    void atualizar(Presenca presenca);
    boolean remover(int id);
    Presenca buscarPorId(int id);
    List<Presenca> listarPorAula(int idAula);
    List<Presenca> listarPorAluno(int numMec);
    List<Presenca> listarPorAlunoEAula(int numMec, int idAula);
    boolean existePresencaAluno(int numMec, int idAula);
    boolean docenteJaMarcou(int idAula);
    void marcarDocente(int idAula);
    void removerPorAulaEDocente(int idAula);
    void removerPorAula(int idAula);
}