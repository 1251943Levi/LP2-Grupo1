package model;

/**
 * Tipos de momento de avaliação (Card 3). Extensível.
 * Valores base: EXAME, TRABALHO, MINI_TESTE, PROJETO.
 * A tabela SQL correspondente é [tipo_momento] (nome + descrição).
 */
public enum TipoMomento {
    EXAME("Exame"),
    TRABALHO("Trabalho"),
    MINI_TESTE("Mini-teste"),
    PROJETO("Projeto");

    private final String descricao;

    TipoMomento(String descricao) {
        this.descricao = descricao;
    }

    /** Descrição legível (guardada na tabela [tipo_momento]). */
    public String descricao() {
        return descricao;
    }

    /** Valor canónico persistido (= nome do tipo). */
    public String etiqueta() {
        return name();
    }

    /** Converte uma string para o enum; devolve null se não corresponder a nenhum tipo. */
    public static TipoMomento fromString(String s) {
        if (s == null) return null;
        switch (s.trim().toUpperCase().replace(" ", "_").replace("-", "_")) {
            case "EXAME":      return EXAME;
            case "TRABALHO":   return TRABALHO;
            case "MINI_TESTE":
            case "MINITESTE":
            case "TESTE":      return MINI_TESTE;
            case "PROJETO":    return PROJETO;
            default:            return null;
        }
    }
}
