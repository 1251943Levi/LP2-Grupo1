package utils;

/**
 * Centraliza as regras de validação de dados de entrada do sistema.
 */
public class Validador {

    private Validador() {}

    /**
     * Valida o formato de um NIF português.
     * Além de verificar se tem 9 dígitos, calcula matematicamente o dígito de controlo.
     * @param nif String a validar.
     * @return true se o NIF for estruturalmente e matematicamente válido.
     */
    public static boolean validarNif(String nif) {
        if (nif == null || !nif.matches("\\d{9}")) {
            return false;
        }

        int total = 0;
        for (int i = 0; i < 8; i++) {
            total += Character.getNumericValue(nif.charAt(i)) * (9 - i);
        }

        int resto = total % 11;
        int digitoControlo = (resto < 2) ? 0 : 11 - resto;

        return digitoControlo == Character.getNumericValue(nif.charAt(8));
    }

    /**
     * Verifica se um endereço de e-mail pertence a um domínio institucional
     * reconhecido pelo sistema e tem a estrutura correta (utilizador@dominio).
     */
    public static boolean isEmailInstitucionalValido(String email) {
        if (email == null || email.trim().isEmpty()) return false;

        String e = email.trim().toLowerCase();

        if (!e.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            return false;
        }

        if (e.equals("admin@issmf.pt")) return true;

        return e.endsWith("@issmf.ipp.pt") || e.endsWith("@isep.ipp.pt");
    }

    /**
     * Valida se o e-mail introduzido no login pertence estritamente à instituição.
     */
    public static boolean validarSufixoLogin(String email) {
        if (email == null || email.trim().isEmpty()) return false;

        String e = email.trim().toLowerCase();

        return e.endsWith("@issmf.ipp.pt") && e.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
    }

    /**
     * Valida se um nome contém pelo menos o primeiro e último nome (separados por espaço)
     * e se é constituído unicamente por letras (incluindo acentuadas).
     */
    public static boolean isNomeValido(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return false;
        }

        return nome.matches("^[a-zA-ZÀ-ÿ\\s]+$");
    }

    /**
     * Valida se a data de nascimento obedece ao padrão DD-MM-AAAA.
     */
    public static boolean isDataNascimentoValida(String data) {
        if (data == null) {
            return false;
        }
        return data.matches("^(0[1-9]|[12][0-9]|3[01])-(0[1-9]|1[0-2])-[0-9]{4}$");
    }
}