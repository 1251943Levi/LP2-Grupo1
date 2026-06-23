package model;

/**
 * Tipos possíveis de um momento de avaliação (F4).
 * Permite distinguir, por exemplo, um teste de um trabalho.
 *
 * A integração completa (associar um tipo a cada momento de uma UC) está
 * descrita em SPEC_F3_UC_CURSO.md e deve ser feita em conjunto com a
 * normalização da UC (F3), por ambas viverem na entidade UC.
 */
public enum TipoMomento {
    TESTE("Teste"),
    TRABALHO("Trabalho"),
    EXAME("Exame"),
    PROJETO("Projeto");

    private final String etiqueta;

    TipoMomento(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    /** Forma canónica usada na persistência (CSV/SQL) e na apresentação. */
    public String etiqueta() {
        return etiqueta;
    }

    /** Converte uma string (legado ou canónica) para o enum; por omissão TESTE. */
    public static TipoMomento fromString(String s) {
        if (s == null) return TESTE;
        switch (s.trim().toUpperCase()) {
            case "TRABALHO": return TRABALHO;
            case "EXAME":    return EXAME;
            case "PROJETO":  return PROJETO;
            case "TESTE":    return TESTE;
            default:          return TESTE;
        }
    }
}
