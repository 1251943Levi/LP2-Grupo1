package dal;

import model.Gestor;
import java.io.File;
import java.util.List;

public class GestorDAL {
    private static final String NOME_FICHEIRO = "gestores.csv";
    private static final String CABECALHO = "email;nome;nif;morada;dataNascimento";

    public static void adicionarGestor(Gestor gestor, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        DALUtil.garantirFicheiroECabecalho(caminho, CABECALHO);

        String linha = gestor.getEmail() + ";" + gestor.getNome() + ";" +
                gestor.getNif() + ";" + gestor.getMorada() + ";" +
                gestor.getDataNascimento();

        DALUtil.adicionarLinhaCSV(caminho, linha);
    }

    public static Gestor procurarPorEmail(String email, String hash, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);

        for (String linha : linhas) {
            String[] dados = linha.split(";", -1);
            if (dados.length >= 5 && dados[0].trim().equalsIgnoreCase(email)) {
                return new Gestor(email, hash,
                        dados[1].trim(), dados[2].trim(), dados[3].trim(), dados[4].trim());
            }
        }
        return null;
    }
}