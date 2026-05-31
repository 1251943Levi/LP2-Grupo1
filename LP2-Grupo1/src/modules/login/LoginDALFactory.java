package modules.login;

import common.ConfigApp;

/**
 * Decide, em tempo de execução, qual implementação de {@link LoginDAL} usar,
 * lendo login.persistence.mode do config.properties (via {@link ConfigApp}).
 *
 * O {@link LoginController} recebe a interface e nunca sabe se está a falar
 * com SQL Server ou com ficheiros — é a essência do padrão dual.
 */
public final class LoginDALFactory {

    private LoginDALFactory() {}

    /** Devolve a implementação correspondente ao modo configurado ("sql" ou "file"). */
    public static LoginDAL create() {
        return ConfigApp.isModoSql() ? new LoginDALSql() : new LoginDALFile();
    }
}
