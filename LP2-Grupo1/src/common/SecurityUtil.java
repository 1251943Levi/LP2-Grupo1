package common;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class SecurityUtil {

    private static final int ITERACOES = 65536;
    private static final int TAMANHO_CHAVE = 256;
    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";

    private SecurityUtil() {}

    public static String gerarSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String gerarHash(String password, String saltBase64) {
        try {
            byte[] salt = Base64.getDecoder().decode(saltBase64);
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERACOES, TAMANHO_CHAVE);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITMO);
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Erro crítico na cifragem.", e);
        }
    }

    public static String gerarCredencialMista(String passwordLimpa) {
        String salt = gerarSalt();
        String hash = gerarHash(passwordLimpa, salt);
        return salt + ":" + hash;
    }

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