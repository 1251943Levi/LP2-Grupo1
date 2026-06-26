package dal;

import common.ConfigApp;
import model.EstatutoEstudante;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação em ficheiros CSV do {@link EstatutoDAL}.
 * <ul>
 *   <li>{@code estatutos.csv} &mdash; catálogo (id;nome;descricao)</li>
 *   <li>{@code estudante_estatuto.csv} &mdash; atribuições (numMec;idEstatuto)</li>
 * </ul>
 */
public class EstatutoDALFile implements EstatutoDAL {

    private static final String FICH_CATALOGO = "estatutos.csv";
    private static final String CAB_CATALOGO = "id;nome;descricao";

    private static final String FICH_ATRIBUICOES = "estudante_estatuto.csv";
    private static final String CAB_ATRIBUICOES = "numMec;idEstatuto";

    private final String pastaBase;

    public EstatutoDALFile() {
        this.pastaBase = ConfigApp.PASTA_BD;
    }

    private String caminhoCatalogo() {
        return pastaBase + File.separator + FICH_CATALOGO;
    }

    private String caminhoAtribuicoes() {
        return pastaBase + File.separator + FICH_ATRIBUICOES;
    }

    @Override
    public void inicializar() {
        DALUtil.garantirFicheiroECabecalho(caminhoCatalogo(), CAB_CATALOGO);
        DALUtil.garantirFicheiroECabecalho(caminhoAtribuicoes(), CAB_ATRIBUICOES);
        if (listarTodos().isEmpty()) {
            adicionar(new EstatutoEstudante(0, "Estudante atleta", "Desporto de alta competição"));
            adicionar(new EstatutoEstudante(0, "Estudante trabalhador", "Trabalhador-estudante"));
            adicionar(new EstatutoEstudante(0, "Estudante pai", "Estudante com filhos"));
            adicionar(new EstatutoEstudante(0, "Matrimónio", "Casamento"));
            adicionar(new EstatutoEstudante(0, "Baixa médica", "Doença/lesão com atestado médico"));
        }
    }

    private int proximoId() {
        return listarTodos().stream().mapToInt(EstatutoEstudante::getId).max().orElse(0) + 1;
    }

    // ---------------------------- Catálogo ----------------------------

    @Override
    public List<EstatutoEstudante> listarTodos() {
        List<EstatutoEstudante> lista = new ArrayList<>();
        for (String linha : DALUtil.lerFicheiro(caminhoCatalogo())) {
            if (linha.equalsIgnoreCase(CAB_CATALOGO)) continue;
            String[] d = linha.split(";", -1);
            if (d.length >= 3) {
                try {
                    lista.add(new EstatutoEstudante(
                            Integer.parseInt(d[0].trim()),
                            d[1].trim(),
                            d[2].trim()
                    ));
                } catch (NumberFormatException ignored) {}
            }
        }
        return lista;
    }

    @Override
    public EstatutoEstudante buscarPorId(int id) {
        for (EstatutoEstudante e : listarTodos()) {
            if (e.getId() == id) return e;
        }
        return null;
    }

    @Override
    public boolean adicionar(EstatutoEstudante estatuto) {
        if (estatuto == null) return false;
        DALUtil.garantirFicheiroECabecalho(caminhoCatalogo(), CAB_CATALOGO);
        int id = proximoId();
        estatuto.setId(id);
        DALUtil.adicionarLinhaCSV(caminhoCatalogo(),
                id + ";" + estatuto.getNome() + ";" + estatuto.getDescricao());
        return true;
    }

    @Override
    public boolean atualizar(EstatutoEstudante estatuto) {
        List<String> novas = new ArrayList<>();
        boolean encontrou = false;
        for (String linha : DALUtil.lerFicheiro(caminhoCatalogo())) {
            if (linha.equalsIgnoreCase(CAB_CATALOGO)) { novas.add(linha); continue; }
            String[] d = linha.split(";", -1);
            if (d.length >= 1 && parseInt(d[0]) == estatuto.getId()) {
                novas.add(estatuto.getId() + ";" + estatuto.getNome() + ";" + estatuto.getDescricao());
                encontrou = true;
            } else {
                novas.add(linha);
            }
        }
        if (encontrou) DALUtil.reescreverFicheiro(caminhoCatalogo(), novas);
        return encontrou;
    }

    @Override
    public boolean remover(int id) {
        List<String> novas = new ArrayList<>();
        boolean removido = false;
        for (String linha : DALUtil.lerFicheiro(caminhoCatalogo())) {
            if (linha.equalsIgnoreCase(CAB_CATALOGO)) { novas.add(linha); continue; }
            String[] d = linha.split(";", -1);
            if (d.length >= 1 && parseInt(d[0]) == id) {
                removido = true;
            } else {
                novas.add(linha);
            }
        }
        if (removido) {
            DALUtil.reescreverFicheiro(caminhoCatalogo(), novas);
            // Remove também todas as atribuições deste estatuto.
            removerAtribuicoesPorEstatuto(id);
        }
        return removido;
    }

    // -------------------------- Atribuições ---------------------------

    @Override
    public boolean atribuir(int numMec, int idEstatuto) {
        DALUtil.garantirFicheiroECabecalho(caminhoAtribuicoes(), CAB_ATRIBUICOES);
        // Evita duplicados.
        for (int[] par : lerAtribuicoes()) {
            if (par[0] == numMec && par[1] == idEstatuto) return false;
        }
        DALUtil.adicionarLinhaCSV(caminhoAtribuicoes(), numMec + ";" + idEstatuto);
        return true;
    }

    @Override
    public boolean removerAtribuicao(int numMec, int idEstatuto) {
        List<String> novas = new ArrayList<>();
        boolean removido = false;
        for (String linha : DALUtil.lerFicheiro(caminhoAtribuicoes())) {
            if (linha.equalsIgnoreCase(CAB_ATRIBUICOES)) { novas.add(linha); continue; }
            String[] d = linha.split(";", -1);
            if (d.length >= 2 && parseInt(d[0]) == numMec && parseInt(d[1]) == idEstatuto) {
                removido = true;
            } else {
                novas.add(linha);
            }
        }
        if (removido) DALUtil.reescreverFicheiro(caminhoAtribuicoes(), novas);
        return removido;
    }

    @Override
    public List<EstatutoEstudante> listarPorEstudante(int numMec) {
        List<EstatutoEstudante> resultado = new ArrayList<>();
        for (int[] par : lerAtribuicoes()) {
            if (par[0] == numMec) {
                EstatutoEstudante e = buscarPorId(par[1]);
                if (e != null) resultado.add(e);
            }
        }
        return resultado;
    }

    // ----------------------------- Apoio ------------------------------

    private void removerAtribuicoesPorEstatuto(int idEstatuto) {
        List<String> novas = new ArrayList<>();
        for (String linha : DALUtil.lerFicheiro(caminhoAtribuicoes())) {
            if (linha.equalsIgnoreCase(CAB_ATRIBUICOES)) { novas.add(linha); continue; }
            String[] d = linha.split(";", -1);
            if (!(d.length >= 2 && parseInt(d[1]) == idEstatuto)) {
                novas.add(linha);
            }
        }
        DALUtil.reescreverFicheiro(caminhoAtribuicoes(), novas);
    }

    private List<int[]> lerAtribuicoes() {
        List<int[]> pares = new ArrayList<>();
        for (String linha : DALUtil.lerFicheiro(caminhoAtribuicoes())) {
            if (linha.equalsIgnoreCase(CAB_ATRIBUICOES)) continue;
            String[] d = linha.split(";", -1);
            if (d.length >= 2) {
                try {
                    pares.add(new int[]{Integer.parseInt(d[0].trim()), Integer.parseInt(d[1].trim())});
                } catch (NumberFormatException ignored) {}
            }
        }
        return pares;
    }

    private static int parseInt(String s) {
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return -1; }
    }
}
