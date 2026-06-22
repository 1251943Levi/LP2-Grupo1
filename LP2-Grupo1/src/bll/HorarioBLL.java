// bll/HorarioBLL.java
package bll;

import common.ConfigApp;
import dal.AulaDAL;
import dal.AulaDALFile;
import dal.AulaDALSql;
import model.Aula;
import model.Docente;
import model.UnidadeCurricular;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class HorarioBLL {

    private final AulaDAL dal;
    private final UcBLL ucBll = new UcBLL();
    private final DocenteBLL docenteBll = new DocenteBLL();

    // Horário permitido
    private static final LocalTime INICIO_PERMITIDO = LocalTime.of(18, 0);
    private static final LocalTime FIM_PERMITIDO = LocalTime.of(23, 30);
    private static final LocalTime INICIO_JANTAR = LocalTime.of(20, 0);
    private static final LocalTime FIM_JANTAR = LocalTime.of(20, 30);
    private static final int MAX_HORAS_DIA = 5;
    private static final int MAX_HORAS_UC_SEMANA = 6;

    public HorarioBLL() {
        this.dal = ConfigApp.isModoSql() ? new AulaDALSql() : new AulaDALFile();
        dal.inicializar();

    }

    // ============================================================
    // MÉTODOS PÚBLICOS
    // ============================================================

    /**
     * Adiciona uma nova aula com validação completa.
     * @throws EstadoInvalidoException se violar alguma regra.
     */
    public void adicionarAula(Aula aula) {
        validarAula(aula);
        validarSobreposicaoDocente(aula, false);
        validarCargaDiariaDocente(aula, false);
        validarCargaSemanalUC(aula, false);
        dal.adicionar(aula);
    }

    /**
     * Atualiza uma aula existente.
     * @throws EstadoInvalidoException se violar alguma regra.
     */
    public void atualizarAula(Aula aula) {
        if (dal.buscarPorId(aula.getId()) == null) {
            throw new EstadoInvalidoException("Aula não encontrada.");
        }
        validarAula(aula);
        validarSobreposicaoDocente(aula, true);
        validarCargaDiariaDocente(aula, true);
        validarCargaSemanalUC(aula, true);
        dal.atualizar(aula);
    }

    /**
     * Remove uma aula pelo ID.
     * @return true se removida, false se não encontrada.
     */
    public boolean removerAula(int id) {
        return dal.remover(id);
    }

    /**
     * Busca uma aula pelo ID.
     * @return Aula ou null se não encontrada.
     */
    public Aula buscarPorId(int id) {
        return dal.buscarPorId(id);
    }

    /**
     * Lista todas as aulas de um ano letivo.
     */
    public List<Aula> listarPorAnoLetivo(int ano) {
        return dal.listarPorAnoLetivo(ano);
    }

    /**
     * Lista aulas de uma UC num ano.
     */
    public List<Aula> listarPorUC(String siglaUC, int ano) {
        return dal.listarPorUC(siglaUC, ano);
    }

    /**
     * Lista aulas de um docente num ano.
     */
    public List<Aula> listarPorDocente(String siglaDocente, int ano) {
        return dal.listarPorDocente(siglaDocente, ano);
    }

    // ============================================================
    // VALIDAÇÕES PRIVADAS
    // ============================================================

    /**
     * Valida as regras básicas da aula (horário, bloco, existência de UC/docente, etc.)
     */
    private void validarAula(Aula aula) {
        if (aula == null) throw new EstadoInvalidoException("Aula inválida.");
        if (aula.getAnoLetivo() <= 0) throw new EstadoInvalidoException("Ano letivo inválido.");
        if (aula.getSiglaUC() == null || aula.getSiglaUC().isEmpty())
            throw new EstadoInvalidoException("UC não especificada.");
        if (aula.getSiglaDocente() == null || aula.getSiglaDocente().isEmpty())
            throw new EstadoInvalidoException("Docente não especificado.");
        if (aula.getDiaSemana() == null)
            throw new EstadoInvalidoException("Dia da semana não especificado.");
        if (aula.getHoraInicio() == null || aula.getHoraFim() == null)
            throw new EstadoInvalidoException("Horário incompleto.");

        // Verificar se a UC existe
        UnidadeCurricular uc = ucBll.procurarUCCompleta(aula.getSiglaUC());
        if (uc == null) throw new EstadoInvalidoException("UC não encontrada.");

        // Verificar se o curso da aula corresponde ao da UC (opcional: pode ser diferente, mas é mais seguro verificar)
        // Para simplificar, não forçamos a correspondência, mas podemos verificar se o curso existe.

        // Verificar se o docente existe
        Docente doc = docenteBll.obterPorSigla(aula.getSiglaDocente());
        if (doc == null) throw new EstadoInvalidoException("Docente não encontrado.");

        // Verificar bloco
        if (aula.getBloco() != 1 && aula.getBloco() != 2)
            throw new EstadoInvalidoException("Bloco deve ser 1 ou 2 horas.");

        // Duração compatível com bloco
        long duracao = java.time.Duration.between(aula.getHoraInicio(), aula.getHoraFim()).toHours();
        if (duracao != aula.getBloco())
            throw new EstadoInvalidoException("A duração da aula (" + duracao + "h) não corresponde ao bloco (" + aula.getBloco() + "h).");

        // Horário permitido (18h-23h30)
        if (aula.getHoraInicio().isBefore(INICIO_PERMITIDO) || aula.getHoraFim().isAfter(FIM_PERMITIDO))
            throw new EstadoInvalidoException("Horário fora do permitido (18h-23h30).");

        // Pausa de jantar (20h-20h30)
        if (aula.getHoraInicio().isBefore(FIM_JANTAR) && aula.getHoraFim().isAfter(INICIO_JANTAR))
            throw new EstadoInvalidoException("Aula não pode sobrepor a pausa de jantar (20h-20h30).");
    }

    /**
     * Valida sobreposição de horário para o mesmo docente no mesmo dia.
     * @param aula Aula a validar.
     * @param isUpdate true se for atualização (exclui a própria aula da verificação).
     */
    private void validarSobreposicaoDocente(Aula aula, boolean isUpdate) {
        List<Aula> existentes = dal.listarPorDocenteEDia(
                aula.getSiglaDocente(), aula.getDiaSemana(), aula.getAnoLetivo());

        for (Aula outra : existentes) {
            if (isUpdate && outra.getId() == aula.getId()) continue;
            if (aula.getHoraInicio().isBefore(outra.getHoraFim()) &&
                    aula.getHoraFim().isAfter(outra.getHoraInicio())) {
                throw new EstadoInvalidoException(
                        "Sobreposição de horário: o docente já tem aula das "
                                + outra.getHoraInicio() + " às " + outra.getHoraFim()
                                + " neste dia.");
            }
        }
    }

    /**
     * Valida que a carga horária diária do docente não excede 5h.
     */
    private void validarCargaDiariaDocente(Aula aula, boolean isUpdate) {
        List<Aula> existentes = dal.listarPorDocenteEDia(
                aula.getSiglaDocente(), aula.getDiaSemana(), aula.getAnoLetivo());

        int totalHoras = existentes.stream()
                .filter(a -> !isUpdate || a.getId() != aula.getId())
                .mapToInt(Aula::getBloco)
                .sum() + aula.getBloco();

        if (totalHoras > MAX_HORAS_DIA)
            throw new EstadoInvalidoException(
                    "Carga horária diária excede " + MAX_HORAS_DIA + " horas (atual: " + totalHoras + "h).");
    }

    /**
     * Valida que a carga horária semanal da UC não excede 6h.
     */
    private void validarCargaSemanalUC(Aula aula, boolean isUpdate) {
        List<Aula> existentes = dal.listarPorUC(aula.getSiglaUC(), aula.getAnoLetivo());

        int totalHoras = existentes.stream()
                .filter(a -> !isUpdate || a.getId() != aula.getId())
                .mapToInt(Aula::getBloco)
                .sum() + aula.getBloco();

        if (totalHoras > MAX_HORAS_UC_SEMANA)
            throw new EstadoInvalidoException(
                    "Carga horária semanal da UC excede " + MAX_HORAS_UC_SEMANA + " horas (atual: " + totalHoras + "h).");
    }

    /**
     * Lista as aulas de um estudante com base no curso e ano curricular.
     * Filtra por siglaCurso e pelo anoCurricular da UC (obtido via UcBLL).
     * @param siglaCurso    Curso do estudante
     * @param anoCurricular Ano curricular do estudante (1, 2 ou 3)
     * @param anoLetivo     Ano letivo a consultar
     * @return Lista de aulas ordenada por dia e hora
     */
    public List<Aula> listarHorarioEstudante(String siglaCurso, int anoCurricular, int anoLetivo) {
        List<Aula> todas = dal.listarPorAnoLetivo(anoLetivo);
        List<Aula> resultado = new ArrayList<>();
        UcBLL ucBll = new UcBLL();

        for (Aula aula : todas) {
            if (!aula.getSiglaCurso().equalsIgnoreCase(siglaCurso)) continue;
            UnidadeCurricular uc = ucBll.procurarUCCompleta(aula.getSiglaUC());
            if (uc != null && uc.getAnoCurricular() == anoCurricular) {
                resultado.add(aula);
            }
        }

        ordenarPorDiaEHora(resultado);
        return resultado;
    }

    /**
     * Lista as aulas de um docente (aulas que leciona).
     * @param siglaDocente Sigla do docente
     * @param anoLetivo    Ano letivo a consultar
     * @return Lista de aulas ordenada por dia e hora
     */
    public List<Aula> listarHorarioDocente(String siglaDocente, int anoLetivo) {
        List<Aula> resultado = dal.listarPorDocente(siglaDocente, anoLetivo);
        ordenarPorDiaEHora(resultado);
        return resultado;
    }

    /**
     * Ordena a lista de aulas por dia da semana e hora de início.
     */
    private void ordenarPorDiaEHora(List<Aula> aulas) {
        aulas.sort((a1, a2) -> {
            int cmp = a1.getDiaSemana().compareTo(a2.getDiaSemana());
            if (cmp != 0) return cmp;
            return a1.getHoraInicio().compareTo(a2.getHoraInicio());
        });
    }
}