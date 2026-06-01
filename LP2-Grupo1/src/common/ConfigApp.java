package common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuração centralizada da aplicação, lida a partir de config.properties
 * (na raiz de execução do projeto) usando java.util.Properties + FileInputStream.
 *
 * Substitui as constantes "bd" espalhadas pelo código: toda a configuração de
 * pastas, base de dados SQL Server e modo de persistência do Login passa por aqui.
 */
public final class ConfigApp {

    /** Nome do ficheiro de configuração procurado na pasta de execução. */
    private static final String FICHEIRO_CONFIG = "config.properties";

    private static final Properties PROPS = carregar();

    /** Pasta onde estão guardados os ficheiros CSV (sem separador final). */
    public static final String PASTA_BD = normalizarPasta(get("pasta.bd", "bd"));

    // --- SQL Server -------------------------------------------------------
    public static final String DB_SERVER   = get("db.server",   "ctespbd.dei.isep.ipp.pt");
    public static final String DB_DATABASE = get("db.database", "2026_LP2_G1_FEIRA");
    public static final String DB_USER     = get("db.user",     "2026_LP2_G1_FEIRA");
    public static final String DB_PASSWORD = get("db.password", "");

    // --- Login ------------------------------------------------------------
    /** Modo de persistência do Login: "sql" ou "file". */
    public static final String LOGIN_PERSISTENCE_MODE = get("login.persistence.mode", "file").trim().toLowerCase();

    public static final String ADMIN_EMAIL         = get("admin.email", "admin@issmf.pt");
    /** Credencial do admin no formato salt:hash (ver common.PasswordHasher). */
    public static final String ADMIN_PASSWORD_HASH = get("admin.password.hash", "");

    private ConfigApp() {}

    /**
     * URL JDBC para o SQL Server do ISEP, construído a partir das propriedades db.*.
     * encrypt=true + trustServerCertificate=true são exigidos pelo driver mssql-jdbc recente.
     */
    public static String jdbcUrl() {
        return "jdbc:jtds:sqlserver://" + DB_SERVER + ":1433/" + DB_DATABASE;
    }

    /** true se o Login deve usar SQL Server; false para modo ficheiros. */
    public static boolean isModoSql() {
        return "sql".equals(LOGIN_PERSISTENCE_MODE);
    }

    /** Lê uma propriedade arbitrária com valor por omissão. */
    public static String get(String chave, String omissao) {
        String v = PROPS.getProperty(chave);
        return (v == null || v.isBlank()) ? omissao : v.trim();
    }

    private static Properties carregar() {
        Properties p = new Properties();
        File f = localizarFicheiro();
        if (f != null) {
            try (InputStream in = new FileInputStream(f)) {
                p.load(in);
            } catch (IOException e) {
                System.err.println(">> AVISO: falha ao ler " + f.getPath() + ": " + e.getMessage());
            }
        } else {
            System.err.println(">> AVISO: " + FICHEIRO_CONFIG + " não encontrado; a usar valores por omissão.");
        }
        return p;
    }

    /**
     * Procura config.properties na pasta de execução e em locais habituais,
     * para funcionar quer se corra a partir da raiz do módulo quer de uma subpasta.
     */
    private static File localizarFicheiro() {
        String[] candidatos = {
                FICHEIRO_CONFIG,
                "LP2-Grupo1" + File.separator + FICHEIRO_CONFIG,
                ".." + File.separator + FICHEIRO_CONFIG
        };
        for (String c : candidatos) {
            File f = new File(c);
            if (f.exists() && f.isFile()) return f;
        }
        return null;
    }

    private static String normalizarPasta(String valor) {
        String v = valor.trim();
        while (v.endsWith("/") || v.endsWith("\\")) {
            v = v.substring(0, v.length() - 1);
        }
        return v.isEmpty() ? "bd" : v;
    }
}
