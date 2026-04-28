package bll;

import dal.CredencialDAL;
import utils.SegurancaPasswords;
import utils.PasswordGenerator;
import utils.EmailService;

/**
 * Lógica de negócio para a gestão de palavras-passe.
 * Gera uma nova password segura, aplica hashing PBKDF2,
 * persiste a credencial e envia a nova password por email ao utilizador.
 */
public class PasswordBLL {

    private static final String PASTA_BD = "bd";

    /**
     * Recupera a password de um utilizador substituindo-a por uma nova.
     * @param email Email do utilizador que pediu a recuperação.
     * @return true se o email existir e a operação for bem-sucedida.
     */
    public void recuperarPassword(String email) {
        String novaPassLimpa = PasswordGenerator.gerarPasswordSegura();
        String novaPassHash = SegurancaPasswords.gerarCredencialMista(novaPassLimpa);

        CredencialDAL.atualizarPassword(email, novaPassHash, PASTA_BD);

        String nome = "Utilizador";
        EmailService.enviarRecuperacaoPassword(nome, email, novaPassLimpa);
    }
}