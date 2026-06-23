package model;

import java.time.LocalDateTime;

public class Presenca {
    private int id;
    private int idAula;
    private int numMec;
    private String estado;          // "PRESENTE", "FALTA", "JUSTIFICADA"
    private boolean docenteMarcou;
    private String statusDocente;   // "ABERTO" ou "FECHADO" (apenas para registo do docente)
    private LocalDateTime dataHoraRegisto;

    // Construtores, getters e setters
    public Presenca() {}

    public Presenca(int idAula, int numMec, String estado, boolean docenteMarcou,
                    String statusDocente, LocalDateTime dataHoraRegisto) {
        this.idAula = idAula;
        this.numMec = numMec;
        this.estado = estado;
        this.docenteMarcou = docenteMarcou;
        this.statusDocente = statusDocente;
        this.dataHoraRegisto = dataHoraRegisto;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdAula() { return idAula; }
    public void setIdAula(int idAula) { this.idAula = idAula; }

    public int getNumMec() { return numMec; }
    public void setNumMec(int numMec) { this.numMec = numMec; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public boolean isDocenteMarcou() { return docenteMarcou; }
    public void setDocenteMarcou(boolean docenteMarcou) { this.docenteMarcou = docenteMarcou; }

    public String getStatusDocente() { return statusDocente; }
    public void setStatusDocente(String statusDocente) { this.statusDocente = statusDocente; }

    public LocalDateTime getDataHoraRegisto() { return dataHoraRegisto; }
    public void setDataHoraRegisto(LocalDateTime dataHoraRegisto) { this.dataHoraRegisto = dataHoraRegisto; }
}