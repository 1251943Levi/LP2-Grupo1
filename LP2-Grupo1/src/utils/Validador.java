package utils;

/**
 * Centraliza as regras de validação de dados de entrada do sistema ISSMF.
 */
public class Validador {

    private Validador() {}

    /**
     * Valida o formato de um NIF português (exatamente 9 dígitos numéricos).
     * @param nif String a validar.
     * @return true se o NIF tiver 9 dígitos.
     */
    public static boolean validarNif(String nif) {
        return nif != null && nif.matches("\\d{9}");
    }

    /**
     * Verifica se um endereço de e-mail pertence a um domínio institucional
     * reconhecido pelo sistema.
     * Domínios aceites:
     *   @issmf.ipp.pt  — utilizadores normais (estudantes, docentes, gestores)
     *   @isep.ipp.pt   — backups e staff ISEP
     *   admin@issmf.pt — conta de sistema estática
     * @param email E-mail a validar (pode ter espaços à volta — são ignorados).
     * @return true se o e-mail for considerado institucional válido.
     */
    public static boolean isEmailInstitucionalValido(String email) {
        if (email == null || email.trim().isEmpty()) return false;

        String e = email.trim().toLowerCase();

        if (e.equals("admin@issmf.pt")) return true;

        return e.endsWith("@issmf.ipp.pt")
                || e.endsWith("@isep.ipp.pt");
    }
    /**
     * Valida se o e-mail introduzido no login pertence à instituição.
     */
    public static boolean validarSufixoLogin(String email) {
        return email != null && email.toLowerCase().endsWith("@issmf.ipp.pt");
    }
}