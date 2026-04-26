package bll;

import dal.CredencialDAL;
import utils.SegurancaPasswords;
import utils.PasswordGenerator;
import utils.EmailService;

/**
 * Camada de Lógica de Negócio dedicada à gestão de passwords.
 */
public class PasswordBLL {

    private static final String PASTA_BD = "bd";

    /**
     * Processa a recuperação de password: gera uma nova, encripta e persiste via DAL.
     * @param email E-mail do utilizador.
     */
    public boolean recuperarPassword(String email) {
        String[] creds = CredencialDAL.obterCredenciais(email, PASTA_BD);
        if (creds == null) return false;

        String novaPassLimpa  = PasswordGenerator.gerarPasswordSegura();
        String novaPassSegura = SegurancaPasswords.gerarCredencialMista(novaPassLimpa);
        CredencialDAL.atualizarPassword(email, novaPassSegura, PASTA_BD);
        EmailService.enviarRecuperacaoPassword("Utilizador", email, novaPassLimpa);
        return true;
    }
}