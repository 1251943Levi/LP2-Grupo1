package modules.login;

import common.ConfigApp;
import dal.DALUtil;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação de {@link LoginDAL} sobre o ficheiro logins.csv na pasta de dados.
 * Reutiliza {@link DALUtil} para todo o I/O, mantendo o estilo das restantes DAL
 * de ficheiros do projeto.
 *
 * Formato CSV (delimitador ';'):
 *   id;email;passwordHash;passwordSalt;tipoUtilizador;ativo;createdAt;updatedAt
 */
public class LoginDALFile implements LoginDAL {

    private static final String NOME_FICHEIRO = "logins.csv";
    private static final String CABECALHO =
            "id;email;passwordHash;passwordSalt;tipoUtilizador;ativo;createdAt;updatedAt";

    private String caminho() {
        return ConfigApp.PASTA_BD + File.separator + NOME_FICHEIRO;
    }

    @Override
    public void inicializar() {
        DALUtil.garantirFicheiroECabecalho(caminho(), CABECALHO);
        if (contar() == 0) {
            if (!importarDeCredenciaisCSV()) {
                criar(LoginDAL.adminPorOmissao());
            }
        }
    }

    /**
     * Migra os registos do antigo credenciais.csv para logins.csv.
     * O hash PBKDF2 é guardado em passwordHash com passwordSalt vazio (marcador legacy).
     * Devolve true se importou pelo menos um registo.
     */
    private boolean importarDeCredenciaisCSV() {
        String caminhoCreds = ConfigApp.PASTA_BD + File.separator + "credenciais.csv";
        List<String> linhas = DALUtil.lerFicheiro(caminhoCreds);
        if (linhas.isEmpty()) return false;
        boolean importou = false;
        for (String linha : linhas) {
            if (linha.toLowerCase().startsWith("email")) continue;
            String[] d = linha.split(";", -1);
            if (d.length < 3) continue;
            String email = d[0].trim();
            String hashPbkdf2 = d[1].trim(); // formato salt:hash PBKDF2
            String tipo = d[2].trim().toUpperCase();
            if (!existe(email)) {
                criar(new LoginModel(email, hashPbkdf2, "", tipo, true));
                importou = true;
            }
        }
        if (importou) System.out.println(">> Migração: credenciais.csv importado para logins.csv.");
        return importou;
    }

    @Override
    public LoginModel procurarPorEmail(String email) {
        if (email == null) return null;
        for (LoginModel m : listarTodos()) {
            if (m.getEmail().equalsIgnoreCase(email)) return m;
        }
        return null;
    }

    @Override
    public List<LoginModel> listarTodos() {
        List<LoginModel> registos = new ArrayList<>();
        for (String linha : DALUtil.lerFicheiro(caminho())) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            LoginModel m = parse(linha);
            if (m != null) registos.add(m);
        }
        return registos;
    }

    @Override
    public LoginModel criar(LoginModel novo) {
        DALUtil.garantirFicheiroECabecalho(caminho(), CABECALHO);
        novo.setId(proximoId());
        String agora = LocalDateTime.now().toString();
        novo.setCreatedAt(agora);
        novo.setUpdatedAt(agora);
        DALUtil.adicionarLinhaCSV(caminho(), serializar(novo));
        return novo;
    }

    @Override
    public boolean atualizar(LoginModel login) {
        List<String> linhas = DALUtil.lerFicheiro(caminho());
        if (linhas.isEmpty()) return false;

        List<String> novas = new ArrayList<>();
        boolean atualizou = false;
        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) {
                novas.add(linha);
                continue;
            }
            LoginModel m = parse(linha);
            if (m != null && m.getEmail().equalsIgnoreCase(login.getEmail())) {
                login.setId(m.getId());
                login.setCreatedAt(m.getCreatedAt());
                login.setUpdatedAt(LocalDateTime.now().toString());
                novas.add(serializar(login));
                atualizou = true;
            } else {
                novas.add(linha);
            }
        }
        if (atualizou) DALUtil.reescreverFicheiro(caminho(), novas);
        return atualizou;
    }

    @Override
    public boolean eliminar(String email) {
        List<String> linhas = DALUtil.lerFicheiro(caminho());
        if (linhas.isEmpty()) return false;

        List<String> novas = new ArrayList<>();
        boolean removeu = false;
        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) {
                novas.add(linha);
                continue;
            }
            LoginModel m = parse(linha);
            if (m != null && m.getEmail().equalsIgnoreCase(email)) {
                removeu = true;
            } else {
                novas.add(linha);
            }
        }
        if (removeu) DALUtil.reescreverFicheiro(caminho(), novas);
        return removeu;
    }

    @Override
    public boolean existe(String email) {
        return procurarPorEmail(email) != null;
    }

    @Override
    public int contar() {
        return listarTodos().size();
    }

    // ------------------------------------------------------------------

    private int proximoId() {
        int max = 0;
        for (LoginModel m : listarTodos()) {
            if (m.getId() > max) max = m.getId();
        }
        return max + 1;
    }

    private static String serializar(LoginModel m) {
        return m.getId() + ";" + m.getEmail() + ";" + m.getPasswordHash() + ";"
                + m.getPasswordSalt() + ";" + m.getTipoUtilizador() + ";" + m.isAtivo()
                + ";" + nz(m.getCreatedAt()) + ";" + nz(m.getUpdatedAt());
    }

    private static LoginModel parse(String linha) {
        String[] d = linha.split(";", -1);
        if (d.length < 6) return null;
        LoginModel m = new LoginModel(
                d[1].trim(), d[2].trim(), d[3].trim(), d[4].trim(),
                Boolean.parseBoolean(d[5].trim()));
        try {
            m.setId(Integer.parseInt(d[0].trim()));
        } catch (NumberFormatException e) {
            m.setId(0);
        }
        if (d.length >= 7) m.setCreatedAt(d[6].trim());
        if (d.length >= 8) m.setUpdatedAt(d[7].trim());
        return m;
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }
}
