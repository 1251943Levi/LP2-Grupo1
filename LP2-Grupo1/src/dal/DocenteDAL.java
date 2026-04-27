package dal;

import model.Docente;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Acesso aos dados de docentes armazenados em docentes.csv.
 * Formato das colunas: sigla; email; nome; nif; morada; dataNascimento.
 */
public class DocenteDAL {
    private static final String NOME_FICHEIRO = "docentes.csv";
    private static final String CABECALHO = "sigla;email;nome;nif;morada;dataNascimento";


    /**
     * Persiste um novo docente no ficheiro CSV.
     * @param docente   Docente a adicionar.
     * @param pastaBase Caminho da pasta de dados.
     */
    public static void adicionarDocente(Docente docente, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        DALUtil.garantirFicheiroECabecalho(caminho, CABECALHO);
        String linha = docente.getSigla() + ";" + docente.getEmail() + ";"
                + docente.getNome() + ";" + docente.getNif() + ";"
                + docente.getMorada() + ";" + docente.getDataNascimento();
        DALUtil.adicionarLinhaCSV(caminho, linha);
    }

    /**
     * Carrega o perfil de um docente a partir do seu email.
     * @param email     Email do docente.
     * @param hash      Hash PBKDF2 da palavra-chave.
     * @param pastaBase Caminho da pasta de dados.
     * @return O Docente encontrado, ou null se não existir.
     */
    public static Docente procurarPorEmail(String email, String hash, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 6 && dados[1].trim().equalsIgnoreCase(email)) {
                return new Docente(dados[0].trim(), email, hash,
                        dados[2].trim(), dados[3].trim(), dados[4].trim(), dados[5].trim());
            }
        }
        return null;
    }


    /**
     * Carrega o perfil básico de um docente a partir da sua sigla.
     * Não carrega as UCs para evitar dependência circular com UcDAL.
     * @param sigla     Sigla do docente.
     * @param pastaBase Caminho da pasta de dados.
     * @return O Docente encontrado, ou null se não existir.
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

    /**
     * Verifica se já existe um docente com a sigla indicada.
     * @param sigla     Sigla a verificar.
     * @param pastaBase Caminho da pasta de dados.
     * @return true se a sigla já estiver em uso.
     */
    public static boolean existeSigla(String sigla, String pastaBase) {
        if (sigla == null || sigla.trim().isEmpty()) return false;
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] dados = linha.split(";", -1);
            // col 0 = sigla em docentes.csv (sigla;email;nome;nif;morada;dataNasc)
            if (dados.length >= 1 && dados[0].trim().equalsIgnoreCase(sigla.trim())) return true;
        }
        return false;
    }

    /**
     * Verifica se já existe um docente com o NIF indicado.
     * @param nif       NIF a verificar.
     * @param pastaBase Caminho da pasta de dados.
     * @return true se o NIF já estiver registado.
     */
    public static boolean existeNif(String nif, String pastaBase) {
        if (nif == null || nif.trim().isEmpty()) return false;
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 4 && dados[3].trim().equals(nif.trim())) return true;
        }
        return false;
    }

    /**
     * Devolve um array "SIGLA - Nome" de todos os docentes.
     */
    public static String[] obterListaDocentes(String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        List<String> lista = new ArrayList<>();

        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 3) {
                lista.add(dados[0].trim() + " - " + dados[2].trim());
            }
        }
        return lista.toArray(new String[0]);
    }
}