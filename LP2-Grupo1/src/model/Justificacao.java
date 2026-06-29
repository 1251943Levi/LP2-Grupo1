package model;

import java.time.LocalDateTime;

public class Justificacao {
    private int id;
    private int numMec;
    private int idAula;
    private int idTipoJustificacao;
    private String estado; // PENDENTE, APROVADA, REJEITADA
    private LocalDateTime dataCriacao;
    private LocalDateTime dataResposta;
    private String observacao;

    public Justificacao() {}

    public Justificacao(int id, int numMec, int idAula, int idTipoJustificacao,
                        String estado, LocalDateTime dataCriacao,
                        LocalDateTime dataResposta, String observacao) {
        this.id = id;
        this.numMec = numMec;
        this.idAula = idAula;
        this.idTipoJustificacao = idTipoJustificacao;
        this.estado = estado;
        this.dataCriacao = dataCriacao;
        this.dataResposta = dataResposta;
        this.observacao = observacao;
    }

    // Getters e setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getNumMec() { return numMec; }
    public void setNumMec(int numMec) { this.numMec = numMec; }
    public int getIdAula() { return idAula; }
    public void setIdAula(int idAula) { this.idAula = idAula; }
    public int getIdTipoJustificacao() { return idTipoJustificacao; }
    public void setIdTipoJustificacao(int idTipoJustificacao) { this.idTipoJustificacao = idTipoJustificacao; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
    public LocalDateTime getDataResposta() { return dataResposta; }
    public void setDataResposta(LocalDateTime dataResposta) { this.dataResposta = dataResposta; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
}