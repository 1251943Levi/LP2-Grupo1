package dal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Acesso aos dados de autenticação armazenados em credenciais.csv.
 * Cada linha contém o email, o hash PBKDF2 e o tipo de utilizador
 * (ESTUDANTE, DOCENTE ou GESTOR).
 */
public class CredencialDAL {
    private static final String NOME_FICHEIRO = "credenciais.csv";
    private static final String CABECALHO = "email;password_hash;tipo";

    /**
     * Obtém as credenciais de um utilizador pelo email.
     * @param email     Email a pesquisar.
     * @param pastaBase Caminho da pasta de dados.
     * @return Array [hash, tipo] se encontrado; null caso contrário.
     */
    public static String[] obterCredenciais(String email, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);

        for (String linha : linhas) {
            String[] dados = linha.split(";", -1);
            if (dados.length >= 3 && dados[0].trim().equalsIgnoreCase(email)) {
                return new String[]{dados[1].trim(), dados[2].trim().toUpperCase()};
            }
        }
        return null;
    }

    /**
     * Regista uma nova credencial no ficheiro.
     * @param email        Email institucional.
     * @param passwordHash Hash PBKDF2 da palavra-chave.
     * @param tipo         Tipo de utilizador (ESTUDANTE, DOCENTE ou GESTOR).
     * @param pastaBase    Caminho da pasta de dados.
     */
    public static void adicionarCredencial(String email, String passwordHash, String tipo, String pastaBase) {
        if (email == null || passwordHash == null) return;

        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        DALUtil.garantirFicheiroECabecalho(caminho, CABECALHO);

        String novaLinha = email + ";" + passwordHash + ";" + tipo;
        DALUtil.adicionarLinhaCSV(caminho, novaLinha);
    }

    /**
     * Atualiza o hash da palavra-chave de um utilizador existente.
     * @param email            Email do utilizador a atualizar.
     * @param novaPasswordHash Novo hash PBKDF2.
     * @param pastaBase        Caminho da pasta de dados.
     */
    public static void atualizarPassword(String email, String novaPasswordHash, String pastaBase) {
        if (email == null || novaPasswordHash == null) return;

        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhasAntigas = DALUtil.lerFicheiro(caminho);

        if (linhasAntigas.isEmpty()) return;

        List<String> linhasAtualizadas = new ArrayList<>();
        boolean atualizado = false;

        for (String linha : linhasAntigas) {
            String[] dados = linha.split(";", -1);

            if (linha.equalsIgnoreCase(CABECALHO)) {
                linhasAtualizadas.add(linha);
                continue;
            }

            if (dados.length >= 3 && dados[0].trim().equalsIgnoreCase(email)) {
                linhasAtualizadas.add(dados[0] + ";" + novaPasswordHash + ";" + dados[2]);
                atualizado = true;
            } else {
                linhasAtualizadas.add(linha);
            }
        }

        if (atualizado) {
            DALUtil.reescreverFicheiro(caminho, linhasAtualizadas);
        }
    }
}
