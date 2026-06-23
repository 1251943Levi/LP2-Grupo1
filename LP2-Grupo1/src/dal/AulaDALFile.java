// dal/AulaDALFile.java
package dal;

import common.ConfigApp;
import model.Aula;
import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class AulaDALFile implements AulaDAL {
    private static final String NOME_FICHEIRO = "aulas.csv";
    private static final String CABECALHO = "id;anoLetivo;siglaUC;siglaCurso;siglaDocente;diaSemana;horaInicio;horaFim;bloco";
    private final String pastaBase;

    public AulaDALFile() {
        this.pastaBase = ConfigApp.PASTA_BD;
    }

    private String caminho() {
        return pastaBase + File.separator + NOME_FICHEIRO;
    }

    @Override
    public void inicializar() {
        DALUtil.garantirFicheiroECabecalho(caminho(), CABECALHO);
    }

    private int proximoId() {
        List<Aula> todas = listarTodas();
        int max = 0;
        for (Aula a : todas) {
            if (a.getId() > max) max = a.getId();
        }
        return max + 1;
    }

    @Override
    public void adicionar(Aula aula) {
        if (aula == null) return;
        DALUtil.garantirFicheiroECabecalho(caminho(), CABECALHO);
        int id = proximoId();
        aula.setId(id);
        String linha = serializar(aula);
        DALUtil.adicionarLinhaCSV(caminho(), linha);
    }

    @Override
    public void atualizar(Aula aula) {
        if (aula == null) return;
        List<String> linhas = DALUtil.lerFicheiro(caminho());
        List<String> novas = new ArrayList<>();
        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) {
                novas.add(linha);
                continue;
            }
            Aula existente = deserializar(linha);
            if (existente != null && existente.getId() == aula.getId()) {
                novas.add(serializar(aula));
            } else {
                novas.add(linha);
            }
        }
        DALUtil.reescreverFicheiro(caminho(), novas);
    }

    @Override
    public boolean remover(int id) {
        List<String> linhas = DALUtil.lerFicheiro(caminho());
        List<String> novas = new ArrayList<>();
        boolean removido = false;
        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) {
                novas.add(linha);
                continue;
            }
            Aula a = deserializar(linha);
            if (a != null && a.getId() == id) {
                removido = true;
            } else {
                novas.add(linha);
            }
        }
        if (removido) DALUtil.reescreverFicheiro(caminho(), novas);
        return removido;
    }

    @Override
    public Aula buscarPorId(int id) {
        for (String linha : DALUtil.lerFicheiro(caminho())) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            Aula a = deserializar(linha);
            if (a != null && a.getId() == id) return a;
        }
        return null;
    }

    @Override
    public List<Aula> listarPorAnoLetivo(int anoLetivo) {
        List<Aula> resultado = new ArrayList<>();
        for (String linha : DALUtil.lerFicheiro(caminho())) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            Aula a = deserializar(linha);
            if (a != null && a.getAnoLetivo() == anoLetivo) resultado.add(a);
        }
        return resultado;
    }

    @Override
    public List<Aula> listarPorUC(String siglaUC, int anoLetivo) {
        List<Aula> resultado = new ArrayList<>();
        for (String linha : DALUtil.lerFicheiro(caminho())) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            Aula a = deserializar(linha);
            if (a != null && a.getSiglaUC().equalsIgnoreCase(siglaUC) && a.getAnoLetivo() == anoLetivo)
                resultado.add(a);
        }
        return resultado;
    }

    @Override
    public List<Aula> listarPorDocente(String siglaDocente, int anoLetivo) {
        List<Aula> resultado = new ArrayList<>();
        for (String linha : DALUtil.lerFicheiro(caminho())) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            Aula a = deserializar(linha);
            if (a != null && a.getSiglaDocente().equalsIgnoreCase(siglaDocente) && a.getAnoLetivo() == anoLetivo)
                resultado.add(a);
        }
        return resultado;
    }

    @Override
    public List<Aula> listarPorDocenteEDia(String siglaDocente, DayOfWeek dia, int anoLetivo) {
        List<Aula> resultado = new ArrayList<>();
        for (String linha : DALUtil.lerFicheiro(caminho())) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            Aula a = deserializar(linha);
            if (a != null && a.getSiglaDocente().equalsIgnoreCase(siglaDocente)
                    && a.getDiaSemana() == dia && a.getAnoLetivo() == anoLetivo)
                resultado.add(a);
        }
        return resultado;
    }

    @Override
    public List<Aula> listarTodas() {
        List<Aula> resultado = new ArrayList<>();
        for (String linha : DALUtil.lerFicheiro(caminho())) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            Aula a = deserializar(linha);
            if (a != null) resultado.add(a);
        }
        return resultado;
    }

    // ------ Serialização / Deserialização ------
    private String serializar(Aula a) {
        return a.getId() + ";" + a.getAnoLetivo() + ";" + a.getSiglaUC() + ";" + a.getSiglaCurso()
                + ";" + a.getSiglaDocente() + ";" + a.getDiaSemana().name() + ";" + a.getHoraInicio()
                + ";" + a.getHoraFim() + ";" + a.getBloco();
    }

    private Aula deserializar(String linha) {
        String[] dados = linha.split(";", -1);
        if (dados.length < 9) return null;
        try {
            int id = Integer.parseInt(dados[0].trim());
            int ano = Integer.parseInt(dados[1].trim());
            String siglaUC = dados[2].trim();
            String siglaCurso = dados[3].trim();
            String siglaDoc = dados[4].trim();
            DayOfWeek dia = DayOfWeek.valueOf(dados[5].trim().toUpperCase());
            LocalTime inicio = LocalTime.parse(dados[6].trim());
            LocalTime fim = LocalTime.parse(dados[7].trim());
            int bloco = Integer.parseInt(dados[8].trim());
            Aula a = new Aula(siglaUC, siglaDoc, dia, inicio, fim, bloco, ano);
            a.setId(id);
            a.setSiglaCurso(siglaCurso);
            return a;
        } catch (Exception e) {
            System.err.println(">> Erro ao deserializar linha: " + linha + " - " + e.getMessage());
            return null;
        }
    }
}