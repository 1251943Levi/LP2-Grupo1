package modules.login;

import common.ConfigApp;
import common.PasswordHasher;
import model.LoginModel;

import java.util.List;

/**
 * Contrato de persistência do módulo Login. Tem duas implementações
 * intermutáveis — {@link LoginDALSql} e {@link LoginDALFile} — escolhidas em
 * tempo de execução pela {@link LoginDALFactory} consoante config.properties.
 *
 * Ambas garantem paridade total: autenticar (via procurarPorEmail), criar,
 * atualizar, listar e eliminar comportam-se de forma idêntica.
 */
public interface LoginDAL {

    /**
     * Prepara o armazenamento: cria a tabela/ficheiro se necessário e,
     * se estiver vazio, popula com o administrador de arranque.
     */
    void inicializar();

    /** Devolve o registo do email indicado, ou null se não existir. */
    LoginModel procurarPorEmail(String email);

    /** Devolve todos os registos de login. */
    List<LoginModel> listarTodos();

    /** Persiste um novo registo e devolve-o já com o id atribuído. */
    LoginModel criar(LoginModel novo);

    /** Atualiza o registo com o email de {@code login}. Devolve true se afetou alguma linha. */
    boolean atualizar(LoginModel login);

    /** Remove o registo do email indicado. Devolve true se removeu. */
    boolean eliminar(String email);

    /** Indica se já existe um registo com o email indicado. */
    boolean existe(String email);

    /** Número total de registos. */
    int contar();

    /**
     * Constrói o registo do administrador de arranque a partir de config.properties.
     * Partilhado pelas duas implementações para garantir o mesmo seed.
     *
     * admin.password.hash pode estar no formato salt:hash; se estiver vazio,
     * gera-se uma credencial para a password por omissão "admin123" e avisa-se.
     */
    static LoginModel adminPorOmissao() {
        String combinado = ConfigApp.ADMIN_PASSWORD_HASH;
        String salt;
        String hash;
        String[] partes = combinado.split(":", 2);
        if (partes.length == 2 && !partes[0].isBlank() && !partes[1].isBlank()) {
            salt = partes[0];
            hash = partes[1];
        } else {
            System.err.println(">> AVISO: admin.password.hash em falta/ inválido no config.properties. "
                    + "A usar password por omissão \"admin123\".");
            PasswordHasher.Credencial c = PasswordHasher.criar("admin123");
            salt = c.salt();
            hash = c.hash();
        }
        return new LoginModel(ConfigApp.ADMIN_EMAIL, hash, salt, "GESTOR", true);
    }
}
