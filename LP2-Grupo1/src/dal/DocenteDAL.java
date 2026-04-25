package dal;

import model.Docente;
import model.UnidadeCurricular;
import java.io.File;
import java.util.List;

public class DocenteDAL {
    private static final String NOME_FICHEIRO = "docentes.csv";
    private static final String CABECALHO = "sigla;email;nome;nif;morada;dataNascimento";

    public static void adicionarDocente(Docente docente, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        DALUtil.garantirFicheiroECabecalho(caminho, CABECALHO);
        String linha = docente.getSigla() + ";" + docente.getEmail() + ";"
                + docente.getNome() + ";" + docente.getNif() + ";"
                + docente.getMorada() + ";" + docente.getDataNascimento();
        DALUtil.adicionarLinhaCSV(caminho, linha);
    }

    /**
     * Usado no Login. Carrega o perfil completo do docente, incluindo as suas UCs.
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
     * Procura um docente pela sua sigla (Usado ao carregar uma Unidade Curricular).
     * Nota: Não carrega as UCs novamente para evitar loops infinitos (StackOverflow).
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
     * Método auxiliar privado para ler ucs.csv e associar as disciplinas a este docente.
     */
    private static void carregarUcsDoDocente(Docente d, String pastaBase) {
        String caminho = pastaBase + File.separator + "ucs.csv";
        List<String> linhas = DALUtil.lerFicheiro(caminho);

        for (String linha : linhas) {
            if (linha.toLowerCase().startsWith("sigla")) continue;

            String[] dados = linha.split(";", -1);
            if (dados.length >= 4 && dados[3].trim().equalsIgnoreCase(d.getSigla())) {
                try {
                    UnidadeCurricular uc = new UnidadeCurricular(
                            dados[0].trim(), dados[1].trim(),
                            Integer.parseInt(dados[2].trim()), d);
                    d.adicionarUcLecionada(uc);
                } catch (NumberFormatException ignored) { }
            }
        }
    }

    /**
     * Verifica se já existe um docente com o NIF indicado.
     * Usado pela GestorBLL.isNifDuplicado() antes de registar um novo utilizador.
     */
    public static boolean existeNif(String nif, String pastaBase) {
        if (nif == null || nif.trim().isEmpty()) return false;
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] dados = linha.split(";", -1);
            // col 3 = nif em docentes.csv (sigla;email;nome;nif;morada;dataNasc)
            if (dados.length >= 4 && dados[3].trim().equals(nif.trim())) return true;
        }
        return false;
    }
}