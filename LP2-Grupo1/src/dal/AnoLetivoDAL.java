package dal;

import model.AnoLetivo;
import model.EstadoAnoLetivo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Acesso aos dados de anos letivos armazenados em anos_letivos.csv.
 * Formato das colunas: ano;estado
 * Cada linha representa um ano letivo do histórico ou o ano corrente.
 */
public class AnoLetivoDAL {

    private static final String NOME_FICHEIRO = "anos_letivos.csv";
    private static final String CABECALHO = "ano;estado";

    /**
     * Persiste um novo ano letivo no ficheiro CSV.
     * Não faz validação de duplicados — essa responsabilidade é da BLL.
     * @param novoAno   Ano letivo a adicionar.
     * @param pastaBase Caminho da pasta de dados.
     */
    public static void adicionar(AnoLetivo novoAno, String pastaBase) {
        if (novoAno == null) return;
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        DALUtil.garantirFicheiroECabecalho(caminho, CABECALHO);
        DALUtil.adicionarLinhaCSV(caminho, novoAno.toCSV());
    }

    /**
     * Atualiza o registo de um ano letivo existente (tipicamente para mudar o estado).
     * O ano é usado como chave de pesquisa.
     * @param anoAtualizado Ano letivo com os dados novos.
     * @param pastaBase     Caminho da pasta de dados.
     */
    public static void atualizar(AnoLetivo anoAtualizado, String pastaBase) {
        if (anoAtualizado == null) return;
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhasAntigas = DALUtil.lerFicheiro(caminho);
        if (linhasAntigas.isEmpty()) return;

        List<String> linhasAtualizadas = new ArrayList<>();
        boolean atualizado = false;

        for (String linha : linhasAntigas) {
            if (linha.equalsIgnoreCase(CABECALHO)) {
                linhasAtualizadas.add(linha);
                continue;
            }
            String[] dados = linha.split(";", -1);
            if (dados.length >= 1) {
                try {
                    int anoLido = Integer.parseInt(dados[0].trim());
                    if (anoLido == anoAtualizado.getAno()) {
                        linhasAtualizadas.add(anoAtualizado.toCSV());
                        atualizado = true;
                        continue;
                    }
                } catch (NumberFormatException ignored) {}
            }
            linhasAtualizadas.add(linha);
        }
        if (atualizado) DALUtil.reescreverFicheiro(caminho, linhasAtualizadas);
    }

    /**
     * Remove um ano letivo pelo seu número.
     * Usado pelo "Editar" da BLL para substituir o número de um ano em PLANEAMENTO.
     * @param ano       Ano a remover.
     * @param pastaBase Caminho da pasta de dados.
     * @return true se foi encontrado e removido.
     */
    public static boolean remover(int ano, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhasAntigas = DALUtil.lerFicheiro(caminho);
        if (linhasAntigas.isEmpty()) return false;

        List<String> linhasAtualizadas = new ArrayList<>();
        boolean encontrou = false;

        for (String linha : linhasAntigas) {
            if (linha.equalsIgnoreCase(CABECALHO)) { linhasAtualizadas.add(linha); continue; }
            String[] dados = linha.split(";", -1);
            if (dados.length >= 1) {
                try {
                    if (Integer.parseInt(dados[0].trim()) == ano) {
                        encontrou = true;
                        continue;
                    }
                } catch (NumberFormatException ignored) {}
            }
            linhasAtualizadas.add(linha);
        }
        if (encontrou) DALUtil.reescreverFicheiro(caminho, linhasAtualizadas);
        return encontrou;
    }

    /**
     * Procura um ano letivo específico pelo seu ano.
     * @param ano       Ano a procurar (ex: 2026).
     * @param pastaBase Caminho da pasta de dados.
     * @return O AnoLetivo se existir, null caso contrário.
     */
    public static AnoLetivo procurarPorAno(int ano, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);

        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 2) {
                try {
                    int anoLido = Integer.parseInt(dados[0].trim());
                    if (anoLido == ano) {
                        EstadoAnoLetivo estado = EstadoAnoLetivo.valueOf(dados[1].trim());
                        return new AnoLetivo(anoLido, estado);
                    }
                } catch (NumberFormatException | IllegalArgumentException ignored) {}
            }
        }
        return null;
    }

    /**
     * Devolve todos os anos letivos registados.
     * @param pastaBase Caminho da pasta de dados.
     * @return Lista de AnoLetivo; vazia se não houver registos.
     */
    public static List<AnoLetivo> listarTodos(String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        List<AnoLetivo> anos = new ArrayList<>();

        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 2) {
                try {
                    int ano = Integer.parseInt(dados[0].trim());
                    EstadoAnoLetivo estado = EstadoAnoLetivo.valueOf(dados[1].trim());
                    anos.add(new AnoLetivo(ano, estado));
                } catch (NumberFormatException | IllegalArgumentException ignored) {}
            }
        }
        return anos;
    }

    /**
     * Devolve o ano letivo atualmente ativo, com a seguinte regra de prioridade:
     *   1º — Se existir um ano em estado INICIADO, devolve esse.
     *   2º — Se não houver INICIADO mas existir um em PLANEAMENTO, devolve esse.
     *   3º — Caso contrário (só fechados ou lista vazia), devolve null.
     *
     * @param pastaBase Caminho da pasta de dados.
     * @return O AnoLetivo ativo, ou null se não houver nenhum.
     */
    public static AnoLetivo obterAnoAtivo(String pastaBase) {
        List<AnoLetivo> todos = listarTodos(pastaBase);

        // Prioridade 1: INICIADO
        for (AnoLetivo a : todos) {
            if (a.getEstado() == EstadoAnoLetivo.INICIADO) return a;
        }
        // Prioridade 2: PLANEAMENTO
        for (AnoLetivo a : todos) {
            if (a.getEstado() == EstadoAnoLetivo.PLANEAMENTO) return a;
        }
        return null;
    }
}