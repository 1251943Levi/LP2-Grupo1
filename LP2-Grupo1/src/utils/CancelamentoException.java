package utils;

/**
 * Exceção disparada sempre que o utilizador introduz "0"
 * para cancelar a meio de um fluxo de inserção de dados.
 */
public class CancelamentoException extends RuntimeException {
    public CancelamentoException() {
        super("Operação cancelada pelo utilizador. A regressar ao menu...");
    }
}