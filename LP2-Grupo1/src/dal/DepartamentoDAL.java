package dal;

import model.Departamento;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Acesso aos dados de departamentos armazenados em departamentos.csv.
 * Formato das colunas: sigla; nome.
 */
public class DepartamentoDAL {
    private static final String NOME_FICHEIRO = "departamentos.csv";
    private static final String CABECALHO = "sigla;nome";


    /**
     * Persiste um novo departamento no ficheiro CSV.
     * @param departamento Departamento a adicionar.
     * @param pastaBase    Caminho da pasta de dados.
     */
    public static void adicionarDepartamento(Departamento departamento, String pastaBase) {
        if (departamento == null) return;
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        DALUtil.garantirFicheiroECabecalho(caminho, CABECALHO);

        String linha = departamento.getSigla() + ";" + departamento.getNome();
        DALUtil.adicionarLinhaCSV(caminho, linha);
    }


    /**
     * Procura um departamento pela sua sigla.
     * @param sigla     Sigla do departamento.
     * @param pastaBase Caminho da pasta de dados.
     * @return O Departamento encontrado, ou null se não existir.
     */
    public static Departamento procurarDepartamento(String sigla, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);

        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;

            String[] dados = linha.split(";", -1);
            if (dados.length >= 2 && dados[0].trim().equalsIgnoreCase(sigla)) {
                return new Departamento(dados[0].trim(), dados[1].trim());
            }
        }
        return null;
    }

    /**
     * Devolve um array "SIGLA - Nome" de todos os departamentos.
     */
    public static String[] obterListaDepartamentos(String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        List<String> lista = new ArrayList<>();

        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 2) {
                lista.add(dados[0].trim() + " - " + dados[1].trim());
            }
        }
        return lista.toArray(new String[0]);
    }
}