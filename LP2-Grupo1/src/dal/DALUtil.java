package dal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilitário de acesso ao sistema de ficheiros CSV.
 * Centraliza todas as operações de I/O para que nenhuma outra
 * camada precise de importar java.io diretamente.
 */
public class DALUtil {
    private DALUtil() {}

    /**
     * Garante que o ficheiro CSV existe com o cabeçalho correto.
     * Se a pasta ou o ficheiro não existirem, são criados.
     * @param caminhoCompleto Caminho para o ficheiro.
     * @param cabecalho       Primeira linha a escrever caso o ficheiro seja criado.
     */
    public static void garantirFicheiroECabecalho(String caminhoCompleto, String cabecalho) {
        File ficheiro = new File(caminhoCompleto);
        File pasta = ficheiro.getParentFile();

        if (pasta != null && !pasta.exists() && !pasta.mkdirs()) {
            System.err.println(">> AVISO: Não foi possível criar a pasta " + pasta.getPath());
        }

        if (!ficheiro.exists()) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(ficheiro))) {
                pw.println(cabecalho);
            } catch (IOException e) {
                System.err.println(">> ERRO CRÍTICO ao criar ficheiro inicial (" + caminhoCompleto + "): " + e.getMessage());
            }
        }
    }

    /**
     * Lê todas as linhas não vazias de um ficheiro CSV.
     * @param caminhoCompleto Caminho para o ficheiro a ler.
     * @return Lista de linhas; lista vazia se o ficheiro não existir.
     */
    public static List<String> lerFicheiro(String caminhoCompleto) {
        List<String> linhas = new ArrayList<>();
        File ficheiro = new File(caminhoCompleto);

        if (!ficheiro.exists()) {
            return linhas;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(ficheiro))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                if (!linha.trim().isEmpty()) {
                    linhas.add(linha);
                }
            }
        } catch (IOException e) {
            System.err.println(">> ERRO ao ler ficheiro (" + caminhoCompleto + "): " + e.getMessage());
        }
        return linhas;
    }

    /**
     * Acrescenta uma linha ao final de um ficheiro CSV.
     * Atualizado para garantir que não cola o registo na mesma linha.
     * @param caminhoCompleto Caminho para o ficheiro destino.
     * @param linha           Linha a adicionar.
     */
    public static void adicionarLinhaCSV(String caminhoCompleto, String linha) {
        File f = new File(caminhoCompleto);
        File pastaParente = f.getParentFile();
        if (pastaParente != null && !pastaParente.exists() && !pastaParente.mkdirs()) {
            System.err.println(">> AVISO: Não foi possível criar a pasta " + pastaParente.getPath());
        }

        List<String> linhas = lerFicheiro(caminhoCompleto);

        linhas.add(linha);

        reescreverFicheiro(caminhoCompleto, linhas);
    }

    /**
     * Reescreve completamente um ficheiro CSV com o novo conteúdo fornecido.
     * Usado para operações de atualização e remoção de registos.
     * @param caminhoCompleto Caminho para o ficheiro a reescrever.
     * @param linhas          Conteúdo completo que substituirá o ficheiro atual.
     */
    public static void reescreverFicheiro(String caminhoCompleto, List<String> linhas) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(caminhoCompleto, false))) {
            for (String l : linhas) {
                pw.println(l);
            }
        } catch (IOException e) {
            System.err.println(">> ERRO: Falha ao reescrever ficheiro (" + caminhoCompleto + "): " + e.getMessage());
        }
    }
}
