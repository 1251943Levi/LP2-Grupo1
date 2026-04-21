package dal;

import model.Departamento;
import java.io.File;
import java.util.List;

public class DepartamentoDAL {
    private static final String NOME_FICHEIRO = "departamentos.csv";
    private static final String CABECALHO = "sigla;nome";

    public static void adicionarDepartamento(Departamento departamento, String pastaBase) {
        if (departamento == null) return;
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        DALUtil.garantirFicheiroECabecalho(caminho, CABECALHO);

        String linha = departamento.getSigla() + ";" + departamento.getNome();
        DALUtil.adicionarLinhaCSV(caminho, linha);
    }

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
}