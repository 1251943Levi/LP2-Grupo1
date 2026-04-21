package model;

public abstract class RepositorioSessao {
    private static Utilizador utilizadorLogado = null;

    public static void login(Utilizador u) {
        utilizadorLogado = u;
    }

    public static void logout() {
        utilizadorLogado = null;
    }

    public static Utilizador getUtilizadorLogado() {
        return utilizadorLogado;
    }

    public static boolean estaAutenticado() {
        return utilizadorLogado != null;
    }
}