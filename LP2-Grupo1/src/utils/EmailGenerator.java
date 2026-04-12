package utils;

public class EmailGenerator {
    public static String gerarEmailEstudante(int numeroMecanografico) {
        return numeroMecanografico + "@issmf.ipp.pt";
    }

    public static String gerarEmailDocente(String sigla) {
        return sigla.toLowerCase() + "@issmf.ipp.pt";
    }
}

