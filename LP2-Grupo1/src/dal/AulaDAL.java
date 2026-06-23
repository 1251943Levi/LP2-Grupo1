// dal/AulaDAL.java
package dal;

import model.Aula;
import java.time.DayOfWeek;
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
    List<Aula> listarPorDocenteEDia(String siglaDocente, DayOfWeek dia, int anoLetivo);
    List<Aula> listarTodas();
}