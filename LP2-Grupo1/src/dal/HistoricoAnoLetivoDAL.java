package dal;

import model.AnoLetivo;
import model.EstadoAnoLetivo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Acesso ao histórico de anos letivos fechados em anos_letivos_historico.csv.
 * Formato das colunas: ano;estado;dataArquivo
 *
 * Tarefa 6 do card Sprint 6:
 *   - Quando um ano é fechado, é arquivado aqui.
 *   - Este ficheiro é consultável via menu "Histórico de Anos Fechados".
 *   - O ficheiro anos_letivos.csv mantém os registos activos (PLANEAMENTO/INICIADO/FECHADO).
 */
public class HistoricoAnoLetivoDAL {

    private static final String NOME_FICHEIRO = "anos_letivos_historico.csv";
    private static final String CABECALHO     = "ano;estado;dataArquivo";

    /**
     * Arquiva um ano letivo fechado no ficheiro de histórico.
     * Não duplica: se já existir uma entrada para o mesmo ano, não adiciona.
     *
     * @param anoLetivo Ano letivo a arquivar (deve ter estado FECHADO).
     * @param pastaBase Caminho da pasta de dados.
     */
    public static void arquivar(AnoLetivo anoLetivo, String pastaBase) {
        if (anoLetivo == null) return;

        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        DALUtil.garantirFicheiroECabecalho(caminho, CABECALHO);

        // Verificar duplicado
        if (jaExiste(anoLetivo.getAno(), caminho)) return;

        String dataHoje = java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"));

        String linha = anoLetivo.getAno() + ";" + anoLetivo.getEstado() + ";" + dataHoje;
        DALUtil.adicionarLinhaCSV(caminho, linha);
    }

    /**
     * Lista todos os anos letivos arquivados (formato legível para a View).
     * Cada entrada: "ANO | ESTADO | Data de arquivo: DD-MM-YYYY"
     *
     * @param pastaBase Caminho da pasta de dados.
     * @return Lista de linhas formatadas para apresentação.
     */
    public static List<String> listar(String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        List<String> resultado = new ArrayList<>();

        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 3) {
                resultado.add(String.format("Ano %-6s | Estado: %-10s | Arquivado em: %s",
                        dados[0].trim(), dados[1].trim(), dados[2].trim()));
            } else if (dados.length == 2) {
                resultado.add(String.format("Ano %-6s | Estado: %-10s",
                        dados[0].trim(), dados[1].trim()));
            }
        }
        return resultado;
    }

    // ---------- Privado ----------

    private static boolean jaExiste(int ano, String caminho) {
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 1) {
                try {
                    if (Integer.parseInt(dados[0].trim()) == ano) return true;
                } catch (NumberFormatException ignored) {}
            }
        }
        return false;
    }
}
