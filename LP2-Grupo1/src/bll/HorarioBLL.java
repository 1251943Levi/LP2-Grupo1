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
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

public class HorarioBLL {

    private final AulaDAL dal;
    private final UcBLL ucBll = new UcBLL();
    private final DocenteBLL docenteBll = new DocenteBLL();

    // Regras para dias úteis
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
    // CRUD BÁSICO
    // ============================================================

    public void adicionarAula(Aula aula) {
        validarAula(aula);
        validarSobreposicaoDocente(aula, false);
        validarCargaDiariaDocente(aula, false);
        validarCargaSemanalUC(aula, false);
        dal.adicionar(aula);
    }

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

    public boolean removerAula(int id) {
        return dal.remover(id);
    }

    public Aula buscarPorId(int id) {
        return dal.buscarPorId(id);
    }

    public List<Aula> listarPorAnoLetivo(int ano) {
        return dal.listarPorAnoLetivo(ano);
    }

    public List<Aula> listarPorUC(String siglaUC, int anoLetivo) {
        List<Aula> resultado = dal.listarPorUC(siglaUC, anoLetivo);
        ordenarPorDataEHora(resultado);
        return resultado;
    }

    /**
     * Lista as aulas de uma UC num intervalo de datas.
     */
    public List<Aula> listarPorUC(String siglaUC, int anoLetivo, LocalDate inicio, LocalDate fim) {
        List<Aula> todas = dal.listarPorUC(siglaUC, anoLetivo);
        List<Aula> resultado = new ArrayList<>();
        for (Aula a : todas) {
            if (!a.getData().isBefore(inicio) && !a.getData().isAfter(fim)) {
                resultado.add(a);
            }
        }
        ordenarPorDataEHora(resultado);
        return resultado;
    }


    // ============================================================
    // CRIAÇÃO EM LOTE (Intervalo de Datas)
    // ============================================================

    /**
     * Cria aulas em todas as datas do intervalo que correspondem ao dia da semana escolhido.
     * @param diaSemanaNum 1-Segunda, 2-Terça, 3-Quarta, 4-Quinta, 5-Sexta
     */
    public List<Aula> criarAulasEmIntervalo(String siglaUC, String siglaCurso, String siglaDocente,
                                            LocalDate dataInicio, LocalDate dataFim,
                                            int diaSemanaNum, LocalTime horaInicio,
                                            int bloco, int anoLetivo) {
        if (diaSemanaNum < 1 || diaSemanaNum > 5) {
            throw new EstadoInvalidoException("Dia da semana inválido (1 a 5).");
        }
        DayOfWeek diaEscolhido = DayOfWeek.of(diaSemanaNum);

        List<Aula> criadas = new ArrayList<>();
        LocalDate data = dataInicio;

        while (!data.isAfter(dataFim)) {
            if (data.getDayOfWeek() == diaEscolhido) {
                Aula aula = new Aula();
                aula.setSiglaUC(siglaUC);
                aula.setSiglaCurso(siglaCurso);
                aula.setSiglaDocente(siglaDocente);
                aula.setData(data);
                aula.setHoraInicio(horaInicio);
                aula.setHoraFim(horaInicio.plusHours(bloco));
                aula.setBloco(bloco);
                aula.setAnoLetivo(anoLetivo);

                // adicionar com validações
                adicionarAula(aula);
                criadas.add(aula);
            }
            data = data.plusDays(1);
        }
        return criadas;
    }

    // ============================================================
    // HORÁRIO SEMANAL (ESTUDANTE E DOCENTE)
    // ============================================================

    public List<Aula> listarHorarioSemanalEstudante(String siglaCurso, int anoCurricular, int anoLetivo) {
        List<Aula> todas = dal.listarPorAnoLetivo(anoLetivo);
        List<Aula> resultado = new ArrayList<>();
        LocalDate hoje = LocalDate.now();
        LocalDate inicioSemana = hoje.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate fimSemana = hoje.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        for (Aula aula : todas) {
            if (!aula.getSiglaCurso().equalsIgnoreCase(siglaCurso)) continue;
            if (aula.getData().isBefore(inicioSemana) || aula.getData().isAfter(fimSemana)) continue;
            UnidadeCurricular uc = ucBll.procurarUCCompleta(aula.getSiglaUC());
            if (uc != null && uc.getAnoCurricular() == anoCurricular) {
                resultado.add(aula);
            }
        }
        ordenarPorDataEHora(resultado);
        return resultado;
    }

    public List<Aula> listarHorarioSemanalDocente(String siglaDocente, int anoLetivo) {
        List<Aula> todas = dal.listarPorDocente(siglaDocente, anoLetivo);
        List<Aula> resultado = new ArrayList<>();
        LocalDate hoje = LocalDate.now();
        LocalDate inicioSemana = hoje.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate fimSemana = hoje.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        for (Aula aula : todas) {
            if (!aula.getData().isBefore(inicioSemana) && !aula.getData().isAfter(fimSemana)) {
                resultado.add(aula);
            }
        }
        ordenarPorDataEHora(resultado);
        return resultado;
    }

    /**
     * Lista aulas de um estudante num intervalo de datas.
     */
    public List<Aula> listarHorarioEstudante(String siglaCurso, int anoCurricular,
                                             int anoLetivo, LocalDate dataInicio, LocalDate dataFim) {
        List<Aula> todas = dal.listarPorAnoLetivo(anoLetivo);
        List<Aula> resultado = new ArrayList<>();
        for (Aula aula : todas) {
            if (!aula.getSiglaCurso().equalsIgnoreCase(siglaCurso)) continue;
            if (aula.getData().isBefore(dataInicio) || aula.getData().isAfter(dataFim)) continue;
            UnidadeCurricular uc = ucBll.procurarUCCompleta(aula.getSiglaUC());
            if (uc != null && uc.getAnoCurricular() == anoCurricular) {
                resultado.add(aula);
            }
        }
        ordenarPorDataEHora(resultado);
        return resultado;
    }

    /**
     * Lista aulas de um docente num intervalo de datas.
     */
    public List<Aula> listarHorarioDocente(String siglaDocente, int anoLetivo,
                                           LocalDate dataInicio, LocalDate dataFim) {
        List<Aula> todas = dal.listarPorDocente(siglaDocente, anoLetivo);
        List<Aula> resultado = new ArrayList<>();
        for (Aula aula : todas) {
            if (!aula.getData().isBefore(dataInicio) && !aula.getData().isAfter(dataFim)) {
                resultado.add(aula);
            }
        }
        ordenarPorDataEHora(resultado);
        return resultado;
    }

    // ============================================================
    // VALIDAÇÕES PRIVADAS
    // ============================================================

    // bll/HorarioBLL.java

    /**
     * Valida todas as regras de negócio para uma aula.
     * @throws EstadoInvalidoException se alguma regra for violada.
     */
    private void validarAula(Aula aula) {
        // --- 1. Validações estruturais ---
        if (aula == null) {
            throw new EstadoInvalidoException("Aula inválida.");
        }
        if (aula.getAnoLetivo() <= 0) {
            throw new EstadoInvalidoException("Ano letivo inválido.");
        }
        if (aula.getSiglaUC() == null || aula.getSiglaUC().isEmpty()) {
            throw new EstadoInvalidoException("UC não especificada.");
        }
        if (aula.getSiglaDocente() == null || aula.getSiglaDocente().isEmpty()) {
            throw new EstadoInvalidoException("Docente não especificado.");
        }
        if (aula.getData() == null) {
            throw new EstadoInvalidoException("Data da aula não especificada.");
        }
        if (aula.getHoraInicio() == null || aula.getHoraFim() == null) {
            throw new EstadoInvalidoException("Horário incompleto.");
        }

        // --- 2. Verificar se é dia útil (Segunda a Sexta) ---
        DayOfWeek dia = aula.getData().getDayOfWeek();
        if (dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY) {
            throw new EstadoInvalidoException("Apenas são permitidas aulas em dias úteis (Segunda a Sexta).");
        }

        // --- 3. Verificar se a UC existe ---
        UnidadeCurricular uc = ucBll.procurarUCCompleta(aula.getSiglaUC());
        if (uc == null) {
            throw new EstadoInvalidoException("UC não encontrada.");
        }

        // --- 4. Verificar se o docente existe ---
        Docente doc = docenteBll.obterPorSigla(aula.getSiglaDocente());
        if (doc == null) {
            throw new EstadoInvalidoException("Docente não encontrado.");
        }

        // --- 5. Verificar bloco (1 ou 2 horas) ---
        if (aula.getBloco() != 1 && aula.getBloco() != 2) {
            throw new EstadoInvalidoException("Bloco deve ser 1 ou 2 horas.");
        }

        // --- 6. Calcular duração em horas (suporta passagem da meia-noite) ---
        long duracao;
        if (aula.getHoraFim().isBefore(aula.getHoraInicio())) {
            // Exemplo: 22:00 -> 00:00 => duração = 24 - (22:00 - 00:00) = 2h
            // Usamos Duration.between(horaFim, horaInicio) que devolve um valor negativo (ex: -22h)
            // Subtraímos esse valor a 24 para obter a duração positiva.
            duracao = 24 - Math.abs(java.time.Duration.between(aula.getHoraFim(), aula.getHoraInicio()).toHours());
        } else {
            duracao = java.time.Duration.between(aula.getHoraInicio(), aula.getHoraFim()).toHours();
        }

        if (duracao != aula.getBloco()) {
            throw new EstadoInvalidoException(
                    "Duração (" + duracao + "h) não corresponde ao bloco (" + aula.getBloco() + "h).");
        }

        // --- 7. Horário permitido (18h-23h30) ---
        if (aula.getHoraInicio().isBefore(INICIO_PERMITIDO) || aula.getHoraFim().isAfter(FIM_PERMITIDO)) {
            throw new EstadoInvalidoException("Horário fora do permitido (18h-23h30).");
        }

        // --- 8. Pausa de jantar (20h-20h30) ---
        if (aula.getHoraInicio().isBefore(FIM_JANTAR) && aula.getHoraFim().isAfter(INICIO_JANTAR)) {
            throw new EstadoInvalidoException("Aula não pode sobrepor a pausa de jantar (20h-20h30).");
        }
    }

    private void validarSobreposicaoDocente(Aula aula, boolean isUpdate) {
        List<Aula> existentes = dal.listarPorDataEDocente(aula.getData(), aula.getSiglaDocente());
        for (Aula outra : existentes) {
            if (isUpdate && outra.getId() == aula.getId()) continue;
            if (aula.getHoraInicio().isBefore(outra.getHoraFim()) &&
                    aula.getHoraFim().isAfter(outra.getHoraInicio())) {
                throw new EstadoInvalidoException(
                        "Sobreposição com outra aula: " + outra.getSiglaUC() +
                                " das " + outra.getHoraInicio() + " às " + outra.getHoraFim() +
                                " no dia " + aula.getData());
            }
        }
    }

    private void validarCargaDiariaDocente(Aula aula, boolean isUpdate) {
        List<Aula> existentes = dal.listarPorDataEDocente(aula.getData(), aula.getSiglaDocente());
        int totalHoras = existentes.stream()
                .filter(a -> !isUpdate || a.getId() != aula.getId())
                .mapToInt(Aula::getBloco)
                .sum() + aula.getBloco();
        if (totalHoras > MAX_HORAS_DIA)
            throw new EstadoInvalidoException("Carga diária excede " + MAX_HORAS_DIA + "h (total: " + totalHoras + "h).");
    }

    private void validarCargaSemanalUC(Aula aula, boolean isUpdate) {
        LocalDate inicioSemana = aula.getData().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate fimSemana = aula.getData().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        List<Aula> todasUC = dal.listarPorUC(aula.getSiglaUC(), aula.getAnoLetivo());
        int totalHoras = todasUC.stream()
                .filter(a -> !a.getData().isBefore(inicioSemana) && !a.getData().isAfter(fimSemana))
                .filter(a -> !isUpdate || a.getId() != aula.getId())
                .mapToInt(Aula::getBloco)
                .sum() + aula.getBloco();

        if (totalHoras > MAX_HORAS_UC_SEMANA)
            throw new EstadoInvalidoException("Carga semanal da UC excede " + MAX_HORAS_UC_SEMANA + "h (total: " + totalHoras + "h).");
    }

    // ============================================================
    // ORDENAÇÃO
    // ============================================================

    private void ordenarPorDataEHora(List<Aula> aulas) {
        aulas.sort((a1, a2) -> {
            int cmp = a1.getData().compareTo(a2.getData());
            if (cmp != 0) return cmp;
            return a1.getHoraInicio().compareTo(a2.getHoraInicio());
        });
    }

    public List<Aula> listarPorUCEData(String siglaUC, int anoLetivo, LocalDate data) {
        List<Aula> todas = dal.listarPorUC(siglaUC, anoLetivo);
        List<Aula> resultado = new ArrayList<>();
        for (Aula a : todas) {
            if (a.getData().equals(data)) {
                resultado.add(a);
            }
        }
        ordenarPorDataEHora(resultado);
        return resultado;
    }

    /**
     * Lista todas as aulas de um docente num ano letivo (sem filtro de datas).
     * Usado para obter todas as aulas para extrair UCs.
     */
    public List<Aula> listarHorarioDocente(String siglaDocente, int anoLetivo) {
        return dal.listarPorDocente(siglaDocente, anoLetivo);
    }

    /**
     * Lista todas as aulas de um estudante (sem filtro de datas).
     * Usado para obter todas as aulas para extrair UCs.
     */
    public List<Aula> listarHorarioEstudante(String siglaCurso, int anoCurricular, int anoLetivo) {
        List<Aula> todas = dal.listarPorAnoLetivo(anoLetivo);
        List<Aula> resultado = new ArrayList<>();
        for (Aula aula : todas) {
            if (!aula.getSiglaCurso().equalsIgnoreCase(siglaCurso)) continue;
            UnidadeCurricular uc = ucBll.procurarUCCompleta(aula.getSiglaUC());
            if (uc != null && uc.getAnoCurricular() == anoCurricular) {
                resultado.add(aula);
            }
        }
        ordenarPorDataEHora(resultado);
        return resultado;
    }
}