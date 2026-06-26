package dal;

import common.ConfigApp;
import model.Justificacao;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class JustificacaoDALFile implements JustificacaoDAL {
    private static final String NOME_FICHEIRO = "justificacoes.csv";
    private static final String CABECALHO = "id;numMec;idAula;idTipo;estado;dataCriacao;dataResposta;observacao";
    private final String pastaBase;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public JustificacaoDALFile() {
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
        return listarTodas().stream().mapToInt(Justificacao::getId).max().orElse(0) + 1;
    }

    private String serializar(Justificacao j) {
        return j.getId() + ";" + j.getNumMec() + ";" + j.getIdAula() + ";" +
                j.getIdTipoJustificacao() + ";" + j.getEstado() + ";" +
                (j.getDataCriacao() != null ? j.getDataCriacao().format(FMT) : "") + ";" +
                (j.getDataResposta() != null ? j.getDataResposta().format(FMT) : "") + ";" +
                (j.getObservacao() != null ? j.getObservacao() : "");
    }

    private Justificacao deserializar(String linha) {
        String[] d = linha.split(";", -1);
        if (d.length < 8) return null;
        try {
            Justificacao j = new Justificacao();
            j.setId(Integer.parseInt(d[0].trim()));
            j.setNumMec(Integer.parseInt(d[1].trim()));
            j.setIdAula(Integer.parseInt(d[2].trim()));
            j.setIdTipoJustificacao(Integer.parseInt(d[3].trim()));
            j.setEstado(d[4].trim());
            if (!d[5].trim().isEmpty()) j.setDataCriacao(LocalDateTime.parse(d[5].trim(), FMT));
            if (!d[6].trim().isEmpty()) j.setDataResposta(LocalDateTime.parse(d[6].trim(), FMT));
            j.setObservacao(d[7].trim());
            return j;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void adicionar(Justificacao justificacao) {
        if (justificacao == null) return;
        DALUtil.garantirFicheiroECabecalho(caminho(), CABECALHO);
        int id = proximoId();
        justificacao.setId(id);
        DALUtil.adicionarLinhaCSV(caminho(), serializar(justificacao));
    }

    @Override
    public void atualizar(Justificacao justificacao) {
        List<String> linhas = DALUtil.lerFicheiro(caminho());
        List<String> novas = new ArrayList<>();
        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) { novas.add(linha); continue; }
            Justificacao existente = deserializar(linha);
            if (existente != null && existente.getId() == justificacao.getId()) {
                novas.add(serializar(justificacao));
            } else {
                novas.add(linha);
            }
        }
        DALUtil.reescreverFicheiro(caminho(), novas);
    }

    @Override
    public Justificacao buscarPorId(int id) {
        for (String linha : DALUtil.lerFicheiro(caminho())) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            Justificacao j = deserializar(linha);
            if (j != null && j.getId() == id) return j;
        }
        return null;
    }

    @Override
    public List<Justificacao> listarPorAluno(int numMec) {
        List<Justificacao> resultado = new ArrayList<>();
        for (String linha : DALUtil.lerFicheiro(caminho())) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            Justificacao j = deserializar(linha);
            if (j != null && j.getNumMec() == numMec) resultado.add(j);
        }
        return resultado;
    }

    @Override
    public List<Justificacao> listarPorAula(int idAula) {
        List<Justificacao> resultado = new ArrayList<>();
        for (String linha : DALUtil.lerFicheiro(caminho())) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            Justificacao j = deserializar(linha);
            if (j != null && j.getIdAula() == idAula) resultado.add(j);
        }
        return resultado;
    }

    @Override
    public List<Justificacao> listarPendentes() {
        List<Justificacao> resultado = new ArrayList<>();
        for (String linha : DALUtil.lerFicheiro(caminho())) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            Justificacao j = deserializar(linha);
            if (j != null && "PENDENTE".equalsIgnoreCase(j.getEstado())) resultado.add(j);
        }
        return resultado;
    }

    @Override
    public List<Justificacao> listarTodas() {
        List<Justificacao> resultado = new ArrayList<>();
        for (String linha : DALUtil.lerFicheiro(caminho())) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            Justificacao j = deserializar(linha);
            if (j != null) resultado.add(j);
        }
        return resultado;
    }

    @Override
    public void removerPorAula(int idAula) {
        List<String> linhas = DALUtil.lerFicheiro(caminho());
        List<String> novas = new ArrayList<>();
        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) { novas.add(linha); continue; }
            Justificacao j = deserializar(linha);
            if (j != null && j.getIdAula() == idAula) continue;
            novas.add(linha);
        }
        DALUtil.reescreverFicheiro(caminho(), novas);
    }
}