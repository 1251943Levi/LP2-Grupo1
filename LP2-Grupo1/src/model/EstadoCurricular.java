package model;

/**
 * Estado curricular partilhado por Curso e UC (Card 1).
 * 3 estados: ATIVO, INATIVO, SEM_CONDICOES.
 *
 *  - ATIVO         : reúne todas as condições e está em funcionamento.
 *  - INATIVO       : desativado manualmente pelo gestor.
 *  - SEM_CONDICOES : criado mas não reúne as condições mínimas para abrir
 *                    (inscrições/quórum, docentes, momentos, propina).
 *
 * O valor canónico persistido (CSV/SQL) é o próprio nome do estado.
 * A tabela SQL correspondente é [EstadoCurricular] (nome + descrição).
 */
public enum EstadoCurricular {
    ATIVO("Reúne todas as condições e está em funcionamento."),
    INATIVO("Desativado manualmente pelo gestor."),
    SEM_CONDICOES("Criado mas não reúne as condições mínimas para abrir.");

    private final String descricao;

    EstadoCurricular(String descricao) {
        this.descricao = descricao;
    }

    /** Descrição legível do estado (também guardada na tabela [EstadoCurricular]). */
    public String descricao() {
        return descricao;
    }

    /** Valor canónico persistido (= nome do estado: ATIVO/INATIVO/SEM_CONDICOES). */
    public String etiqueta() {
        return name();
    }

    /** Converte uma string (canónica ou legado) para o enum; por omissão SEM_CONDICOES. */
    public static EstadoCurricular fromString(String s) {
        if (s == null) return SEM_CONDICOES;
        switch (s.trim().toUpperCase().replace(" ", "_")) {
            case "ATIVO":
            case "ACTIVO":
                return ATIVO;
            case "INATIVO":
            case "INACTIVO":
                return INATIVO;
            case "SEM_CONDICOES":
            case "PENDENTE":   // legado
                return SEM_CONDICOES;
            default:
                return SEM_CONDICOES;
        }
    }
}
