package utils;
import java.util.Random;

public class PasswordGenerator {
    public static String gerarPassword() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < 8; i++) {
            password.append(caracteres.charAt(rnd.nextInt(caracteres.length())));
        }
        return password.toString();
    }
}
