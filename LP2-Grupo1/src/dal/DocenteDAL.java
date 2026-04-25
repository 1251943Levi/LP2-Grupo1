package dal;

import model.Docente;
import java.io.File;
import java.util.List;

public class DocenteDAL {
    private static final String NOME_FICHEIRO = "docentes.csv";
    private static final String CABECALHO = "sigla;email;nome;nif;morada;dataNascimento";

    public static void adicionarDocente(Docente docente, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        DALUtil.garantirFicheiroECabecalho(caminho, CABECALHO);

        String linha = docente.getSigla() + ";" + docente.getEmail() + ";" +
                docente.getNome() + ";" + docente.getNif() + ";" +
                docente.getMorada() + ";" + docente.getDataNascimento();

        DALUtil.adicionarLinhaCSV(caminho, linha);
    }

    public static Docente procurarPorEmail(String email, String hash, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);

        for (String linha : linhas) {
            String[] dados = linha.split(";", -1);
            if (dados.length >= 6 && dados[1].trim().equalsIgnoreCase(email)) {
                return new Docente(dados[0].trim(), email, hash,
                        dados[2].trim(), dados[3].trim(), dados[4].trim(), dados[5].trim());
            }
        }
        return null;
    }

    /**
     * Procura um docente pela sua sigla.
     * Útil para mapear o docente responsável ao carregar uma Unidade Curricular.
     */
    public static Docente procurarPorSigla(String sigla, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);

        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;

            String[] dados = linha.split(";", -1);
            if (dados.length >= 6 && dados[0].trim().equalsIgnoreCase(sigla)) {
                return new Docente(dados[0].trim(), dados[1].trim(), "",
                        dados[2].trim(), dados[3].trim(), dados[4].trim(), dados[5].trim());
            }
        }
        return null;
    }
}