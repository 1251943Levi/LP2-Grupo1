package dal;

import common.ConfigApp;
import model.Presenca;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PresencaDALFile implements PresencaDAL {

    private static final String NOME_FICHEIRO = "presencas.csv";
    private static final String CABECALHO = "id;idAula;numMec;estado;docenteMarcou;statusDocente;dataHoraRegisto";
    private final String pastaBase;

    public PresencaDALFile() {
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
        List<Presenca> todas = listarTodasInterno();
        int max = 0;
        for (Presenca p : todas) {
            if (p.getId() > max) max = p.getId();
        }
        return max + 1;
    }

    private List<Presenca> listarTodasInterno() {
        List<Presenca> resultado = new ArrayList<>();
        for (String linha : DALUtil.lerFicheiro(caminho())) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            Presenca p = deserializar(linha);
            if (p != null) resultado.add(p);
        }
        return resultado;
    }

    @Override
    public void adicionar(Presenca presenca) {
        if (presenca == null) return;
        DALUtil.garantirFicheiroECabecalho(caminho(), CABECALHO);
        int id = proximoId();
        presenca.setId(id);
        DALUtil.adicionarLinhaCSV(caminho(), serializar(presenca));
    }

    @Override
    public void atualizar(Presenca presenca) {
        if (presenca == null) return;
        List<String> linhas = DALUtil.lerFicheiro(caminho());
        List<String> novas = new ArrayList<>();
        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) {
                novas.add(linha);
                continue;
            }
            Presenca existente = deserializar(linha);
            if (existente != null && existente.getId() == presenca.getId()) {
                novas.add(serializar(presenca));
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
            Presenca p = deserializar(linha);
            if (p != null && p.getId() == id) {
                removido = true;
            } else {
                novas.add(linha);
            }
        }
        if (removido) DALUtil.reescreverFicheiro(caminho(), novas);
        return removido;
    }

    @Override
    public Presenca buscarPorId(int id) {
        for (String linha : DALUtil.lerFicheiro(caminho())) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            Presenca p = deserializar(linha);
            if (p != null && p.getId() == id) return p;
        }
        return null;
    }

    @Override
    public List<Presenca> listarPorAula(int idAula) {
        List<Presenca> resultado = new ArrayList<>();
        for (String linha : DALUtil.lerFicheiro(caminho())) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            Presenca p = deserializar(linha);
            if (p != null && p.getIdAula() == idAula) resultado.add(p);
        }
        return resultado;
    }

    @Override
    public List<Presenca> listarPorAluno(int numMec) {
        List<Presenca> resultado = new ArrayList<>();
        for (String linha : DALUtil.lerFicheiro(caminho())) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            Presenca p = deserializar(linha);
            if (p != null && p.getNumMec() == numMec) resultado.add(p);
        }
        return resultado;
    }

    @Override
    public List<Presenca> listarPorAlunoEAula(int numMec, int idAula) {
        List<Presenca> resultado = new ArrayList<>();
        for (String linha : DALUtil.lerFicheiro(caminho())) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            Presenca p = deserializar(linha);
            if (p != null && p.getNumMec() == numMec && p.getIdAula() == idAula) resultado.add(p);
        }
        return resultado;
    }

    @Override
    public boolean existePresencaAluno(int numMec, int idAula) {
        return !listarPorAlunoEAula(numMec, idAula).isEmpty();
    }

    @Override
    public boolean docenteJaMarcou(int idAula) {
        for (String linha : DALUtil.lerFicheiro(caminho())) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            Presenca p = deserializar(linha);
            if (p != null && p.getIdAula() == idAula && p.getNumMec() == -1) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void marcarDocente(int idAula) {
        // Usar o novo método com status "ABERTO"
        // A lógica agora está na BLL, mas este método mantém-se para compatibilidade
        // Vamos delegar para a lógica de abertura na BLL ou ignorar.
        // Melhor: não usar este método, usar abrirPresencas na BLL.
        // Mantido apenas para não quebrar compilação, mas não faz nada.
    }

    @Override
    public void removerPorAulaEDocente(int idAula) {
        List<String> linhas = DALUtil.lerFicheiro(caminho());
        List<String> novas = new ArrayList<>();
        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) {
                novas.add(linha);
                continue;
            }
            Presenca p = deserializar(linha);
            if (p != null && p.getIdAula() == idAula && p.getNumMec() == -1) {
                continue; // remove
            }
            novas.add(linha);
        }
        DALUtil.reescreverFicheiro(caminho(), novas);
    }

    @Override
    public void removerPorAula(int idAula) {
        List<String> linhas = DALUtil.lerFicheiro(caminho());
        List<String> novas = new ArrayList<>();
        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) {
                novas.add(linha);
                continue;
            }
            Presenca p = deserializar(linha);
            if (p != null && p.getIdAula() == idAula) continue;
            novas.add(linha);
        }
        DALUtil.reescreverFicheiro(caminho(), novas);
    }

    // ========== Serialização / Deserialização ==========

    private String serializar(Presenca p) {
        return p.getId() + ";" + p.getIdAula() + ";" + p.getNumMec() + ";" + p.getEstado() + ";"
                + p.isDocenteMarcou() + ";" + p.getStatusDocente() + ";" + p.getDataHoraRegisto();
    }

    private Presenca deserializar(String linha) {
        String[] dados = linha.split(";", -1);
        if (dados.length < 7) return null;
        try {
            Presenca p = new Presenca();
            p.setId(Integer.parseInt(dados[0].trim()));
            p.setIdAula(Integer.parseInt(dados[1].trim()));
            p.setNumMec(Integer.parseInt(dados[2].trim()));
            p.setEstado(dados[3].trim());
            p.setDocenteMarcou(Boolean.parseBoolean(dados[4].trim()));
            p.setStatusDocente(dados[5].trim());
            p.setDataHoraRegisto(LocalDateTime.parse(dados[6].trim()));
            return p;
        } catch (Exception e) {
            System.err.println(">> Erro ao deserializar linha: " + linha + " - " + e.getMessage());
            return null;
        }
    }
}