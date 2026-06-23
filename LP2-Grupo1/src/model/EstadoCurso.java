package model;

/**
 * Estados possíveis de um Curso (F1/F2).
 * Substitui as strings soltas "Ativo"/"Inativo" e acrescenta um 3.º estado.
 *
 *  - ATIVO    : reúne todas as condições e está a decorrer.
 *  - INATIVO  : desativado manualmente pelo gestor.
 *  - PENDENTE : criado mas ainda não reúne condições para abrir
 *               (sem UCs, sem quórum, sem propina definida ou sem momentos).
 *
 * O estado é calculado a partir de TODAS as restrições de curso e UC
 * (ver {@code bll.CursoBLL.avaliarEstado}), não apenas dos estudantes.
 */
public enum EstadoCurso {
    ATIVO("Ativo"),
    INATIVO("Inativo"),
    PENDENTE("Pendente");

    private final String etiqueta;

    EstadoCurso(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    /** Forma canónica usada na persistência (CSV/SQL) e na apresentação. */
    public String etiqueta() {
        return etiqueta;
    }

    /** Converte uma string (legado ou canónica) para o enum; por omissão PENDENTE. */
    public static EstadoCurso fromString(String s) {
        if (s == null) return PENDENTE;
        switch (s.trim().toUpperCase()) {
            case "ATIVO":
            case "ACTIVO":
                return ATIVO;
            case "INATIVO":
            case "INACTIVO":
                return INATIVO;
            case "PENDENTE":
                return PENDENTE;
            default:
                return PENDENTE;
        }
    }
}
