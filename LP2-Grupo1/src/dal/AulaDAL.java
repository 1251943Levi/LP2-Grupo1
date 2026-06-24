package dal;

import model.Aula;
import java.time.LocalDate;
import java.util.List;

public interface AulaDAL {
    void inicializar();

    void adicionar(Aula aula);
    void atualizar(Aula aula);
    boolean remover(int id);

    Aula buscarPorId(int id);

    List<Aula> listarPorAnoLetivo(int anoLetivo);
    List<Aula> listarPorUC(String siglaUC, int anoLetivo);
    List<Aula> listarPorDocente(String siglaDocente, int anoLetivo);
    List<Aula> listarPorDataEDocente(LocalDate data, String siglaDocente);

    List<Aula> listarTodas();
}