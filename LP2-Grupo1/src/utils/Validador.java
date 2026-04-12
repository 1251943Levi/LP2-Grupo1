package utils;

public class Validador {

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
     * Valida se o NIF contém exatamente 9 dígitos numéricos.
     */
    public static boolean isNifValido(String nif) {
        if (nif == null) {
            return false;
        }
        return nif.matches("^[0-9]{9}$");
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
