package utils;

import java.security.SecureRandom;

public class PasswordGenerator {

    private static final String CARACTERES_PERMITIDOS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
    private static final int TAMANHO_MINIMO = 12;

    private PasswordGenerator() {}

    /**
     * Gera uma palavra-passe forte de forma criptograficamente segura.
     * @return Uma String contendo a password gerada.
     */
    public static String gerarPasswordSegura() {
        SecureRandom geradorSeguro = new SecureRandom();
        StringBuilder password = new StringBuilder(TAMANHO_MINIMO);

        for (int i = 0; i < TAMANHO_MINIMO; i++) {
            int index = geradorSeguro.nextInt(CARACTERES_PERMITIDOS.length());
            password.append(CARACTERES_PERMITIDOS.charAt(index));
        }

        return password.toString();
    }
}
