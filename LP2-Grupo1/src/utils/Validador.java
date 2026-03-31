package utils;

public class Validador {
    public static boolean validarNif(String nif) {
        return nif != null && nif.matches("\\d{9}");
    }
}
