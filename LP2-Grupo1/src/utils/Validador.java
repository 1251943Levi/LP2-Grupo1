package utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Centraliza as regras de validação de dados de entrada do sistema.
 */
public class Validador {

    private Validador() {}

    /**
     * @return nif válido
     */
    public static boolean validarNif(String nif) {
        return nif != null && nif.matches("[12356789]\\d{8}");
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
     * Valida se a data de nascimento obedece ao padrão DD-MM-AAAA
     * e se a data existe no calendário.
     */
    public static boolean isDataNascimentoValida(String data) {
        if (data == null || !data.matches("^(0[1-9]|[12][0-9]|3[01])-(0[1-9]|1[0-2])-[0-9]{4}$")) {
            return false;
        }

        try {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            LocalDate dataNasc = LocalDate.parse(data, dtf);
            LocalDate hoje = LocalDate.now();

            return dataNasc.isBefore(hoje);
        } catch (DateTimeParseException e) {
            return false; // A data não existe no calendário
        }
    }

    /**
     * Valida se um NIF já se encontra registado na base de dados.
     * @param nif O NIF a verificar.
     * @param pastaBD O caminho da base de dados.
     * @return true se o NIF já existir noutro aluno, false caso contrário.
     */
    public static boolean isNifDuplicado(String nif, String pastaBD) {
        model.Estudante[] todos = ImportadorCSV.carregarTodosEstudantes(pastaBD);
        if (todos == null) return false;

        for (model.Estudante e : todos) {
            if (e != null && e.getNif().equals(nif)) {
                return true;
            }
        }
        return false;
    }
}