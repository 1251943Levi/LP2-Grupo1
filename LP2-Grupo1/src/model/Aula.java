
package model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Aula {
    private int id;
    private int anoLetivo;
    private String siglaUC;
    private String siglaCurso;
    private String siglaDocente;
    private LocalDate data;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private int bloco;

    public Aula() {}

    public Aula(String siglaUC, String siglaDocente, LocalDate data,
                LocalTime horaInicio, LocalTime horaFim, int bloco, int anoLetivo) {
        this.siglaUC = siglaUC;
        this.siglaDocente = siglaDocente;
        this.data = data;
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

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFim() { return horaFim; }
    public void setHoraFim(LocalTime horaFim) { this.horaFim = horaFim; }

    public int getBloco() { return bloco; }
    public void setBloco(int bloco) { this.bloco = bloco; }

    @Override
    public String toString() {
        return String.format("Aula{id=%d, uc=%s, curso=%s, docente=%s, data=%s, %s-%s, bloco=%dh, ano=%d}",
                id, siglaUC, siglaCurso, siglaDocente, data, horaInicio, horaFim, bloco, anoLetivo);
    }
}