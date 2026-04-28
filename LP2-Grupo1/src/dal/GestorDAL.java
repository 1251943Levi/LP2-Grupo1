package dal;

import model.Gestor;
import java.io.File;
import java.util.List;

/**
 * Acesso aos dados de gestores armazenados em gestores.csv.
 * Formato das colunas: email; nome; nif; morada; dataNascimento.
 */
public class GestorDAL {
    private static final String NOME_FICHEIRO = "gestores.csv";
    private static final String CABECALHO = "email;nome;nif;morada;dataNascimento";

    /**
     * Carrega o perfil de um gestor a partir do seu email.
     * @param email     Email do gestor.
     * @param hash      Hash PBKDF2 da palavra-chave.
     * @param pastaBase Caminho da pasta de dados.
     * @return O Gestor encontrado, ou null se não existir.
     */
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