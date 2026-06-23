// model/Aula.java
package model;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class Aula {
    private int id;
    private int anoLetivo;
    private String siglaUC;
    private String siglaCurso;
    private String siglaDocente;
    private DayOfWeek diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private int bloco;

    // Construtor vazio (necessário para alguns frameworks)
    public Aula() {}

    // Construtor com todos os campos (exceto id)
    public Aula(String siglaUC, String siglaDocente, DayOfWeek diaSemana,
                LocalTime horaInicio, LocalTime horaFim, int bloco, int anoLetivo) {
        this.siglaUC = siglaUC;
        this.siglaDocente = siglaDocente;
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
        this.bloco = bloco;
        this.anoLetivo = anoLetivo;
    }

    // Construtor completo com id
    public Aula(int id, String siglaUC, String siglaCurso, String siglaDocente,
                DayOfWeek diaSemana, LocalTime horaInicio, LocalTime horaFim,
                int bloco, int anoLetivo) {
        this.id = id;
        this.siglaUC = siglaUC;
        this.siglaCurso = siglaCurso;
        this.siglaDocente = siglaDocente;
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
        this.bloco = bloco;
        this.anoLetivo = anoLetivo;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getAnoLetivo() { return anoLetivo; }
    public void setAnoLetivo(int anoLetivo) { this.anoLetivo = anoLetivo; }

    public String getSiglaUC() { return siglaUC; }
    public void setSiglaUC(String siglaUC) { this.siglaUC = siglaUC; }

    public String getSiglaCurso() { return siglaCurso; }
    public void setSiglaCurso(String siglaCurso) { this.siglaCurso = siglaCurso; }

    public String getSiglaDocente() { return siglaDocente; }
    public void setSiglaDocente(String siglaDocente) { this.siglaDocente = siglaDocente; }

    public DayOfWeek getDiaSemana() { return diaSemana; }
    public void setDiaSemana(DayOfWeek diaSemana) { this.diaSemana = diaSemana; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFim() { return horaFim; }
    public void setHoraFim(LocalTime horaFim) { this.horaFim = horaFim; }

    public int getBloco() { return bloco; }
    public void setBloco(int bloco) { this.bloco = bloco; }

    @Override
    public String toString() {
        return String.format("Aula{id=%d, uc=%s, curso=%s, docente=%s, %s %s-%s, bloco=%dh, ano=%d}",
                id, siglaUC, siglaCurso, siglaDocente, diaSemana, horaInicio, horaFim, bloco, anoLetivo);
    }
}