package utils;

/**
 * Constantes de configuração globais da aplicação.
 *
 * Centraliza valores (como caminhos de pastas) que estavam dispersos como
 * constantes locais em várias BLLs, eliminando duplicação.
 */
public final class Config {

    /** Pasta base onde residem todos os ficheiros CSV de dados. */
    public static final String PASTA_BD = "bd";

    /** Impede instanciação — esta classe contém apenas constantes. */
    private Config() {}
}