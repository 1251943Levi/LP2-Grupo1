package utils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * Utilitário responsável pelo hashing e verificação segura de credenciais.
 * Utiliza o algoritmo PBKDF2 com HMAC-SHA256.
 */
public class SegurancaPasswords {

    private static final int ITERACOES = 65536;
    private static final int TAMANHO_CHAVE = 256;
    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";

    private SegurancaPasswords() {}

    /**
     * Gera um "Salt" criptograficamente seguro e aleatório.
     * Este valor deve ser guardado na base de dados junto ao utilizador.
     * @return O Salt codificado em formato Base64.
     */
    public static String gerarSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Gera um hash irreversível a partir da palavra-passe em texto limpo e do respetivo Salt.
     * @param password A palavra-passe original introduzida.
     * @param saltBase64 O salt único previamente gerado para o utilizador.
     * @return O hash resultante codificado em formato Base64.
     */
    public static String gerarHash(String password, String saltBase64) {
        try {
            byte[] salt = Base64.getDecoder().decode(saltBase64);

            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERACOES, TAMANHO_CHAVE);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITMO);

            byte[] hash = factory.generateSecret(spec).getEncoded();

            return Base64.getEncoder().encodeToString(hash);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Erro crítico: Algoritmo de hashing ou chave inválidos.", e);
        }
    }

    /**
     * Cria a credencial final segura pronta a guardar no ficheiro.
     * Junta o Salt e o Hash separados por dois pontos (:).
     */
    public static String gerarCredencialMista(String passwordLimpa) {
        String salt = gerarSalt();
        String hash = gerarHash(passwordLimpa, salt);
        return salt + ":" + hash;
    }

    /**
     * Verifica se a password introduzida no login corresponde à guardada no sistema.
     */
    public static boolean verificarPassword(String passwordIntroduzida, String credencialMista) {
        try {
            String[] partes = credencialMista.split(":");
            if (partes.length != 2) return false;

            String saltGuardado = partes[0];
            String hashGuardado = partes[1];

            String novoHash = gerarHash(passwordIntroduzida, saltGuardado);

            return novoHash.equals(hashGuardado);
        } catch (Exception e) {
            return false;
        }
    }
}