package modules.login;

import common.PasswordHasher;

import java.util.List;

/**
 * Controlador do módulo Login. Chamado pela View; não tem BLL — a "lógica" é só
 * validação e hashing, centralizados em {@link PasswordHasher}.
 *
 * Delega toda a persistência na interface {@link LoginDAL} (obtida da
 * {@link LoginDALFactory}), pelo que funciona de forma idêntica em modo SQL ou
 * ficheiros. As BLLs/Controllers das outras entidades devem delegar aqui a
 * autenticação e a gestão de credenciais, em vez de falarem com logins.csv ou
 * com a tabela [logins] diretamente.
 */
public class LoginController {

    private final LoginDAL dal;

    public LoginController() {
        this(LoginDALFactory.create());
    }

    public LoginController(LoginDAL dal) {
        this.dal = dal;
    }

    /** Garante o armazenamento pronto (tabela/ficheiro + seed do admin). */
    public void inicializar() {
        dal.inicializar();
    }

    /**
     * Autentica um utilizador.
     * @return O LoginModel se as credenciais forem válidas e a conta estiver ativa; null caso contrário.
     */
    public LoginModel autenticar(String email, String passwordLimpa) {
        if (email == null || passwordLimpa == null) return null;
        LoginModel m = dal.procurarPorEmail(email);
        if (m == null || !m.isAtivo()) return null;
        boolean ok = PasswordHasher.verificar(passwordLimpa, m.getPasswordSalt(), m.getPasswordHash());
        return ok ? m : null;
    }

    /**
     * Cria uma nova credencial.
     * @return false se o email já existir ou os dados forem inválidos.
     */
    public boolean criarCredencial(String email, String passwordLimpa, String tipoUtilizador) {
        if (email == null || email.isBlank() || passwordLimpa == null || passwordLimpa.isEmpty()) return false;
        if (dal.existe(email)) return false;
        PasswordHasher.Credencial c = PasswordHasher.criar(passwordLimpa);
        dal.criar(new LoginModel(email, c.hash(), c.salt(), tipoUtilizador, true));
        return true;
    }

    /**
     * Atualiza a password de um utilizador existente (gera novo salt + hash).
     * @return false se o email não existir.
     */
    public boolean atualizarPassword(String email, String novaPasswordLimpa) {
        LoginModel m = dal.procurarPorEmail(email);
        if (m == null || novaPasswordLimpa == null || novaPasswordLimpa.isEmpty()) return false;
        PasswordHasher.Credencial c = PasswordHasher.criar(novaPasswordLimpa);
        m.setPasswordHash(c.hash());
        m.setPasswordSalt(c.salt());
        return dal.atualizar(m);
    }

    /** Ativa ou desativa uma conta sem alterar a password. */
    public boolean definirAtivo(String email, boolean ativo) {
        LoginModel m = dal.procurarPorEmail(email);
        if (m == null) return false;
        m.setAtivo(ativo);
        return dal.atualizar(m);
    }

    /** Lista todas as credenciais. */
    public List<LoginModel> listar() {
        return dal.listarTodos();
    }

    /** Remove a credencial de um email. */
    public boolean eliminar(String email) {
        return dal.eliminar(email);
    }
}
