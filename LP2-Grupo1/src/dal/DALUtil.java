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
 * Classe utilitária para centralizar as operações de Input/Output (I/O).
 * Nenhuma outra camada fora do package 'dal' deve importar java.io.*.
 */
public class DALUtil {
    private DALUtil() {}

    /**
     * Garante que a pasta e o ficheiro existem. Se não, cria-os e insere o cabeçalho.
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
     * Lê todas as linhas de um ficheiro CSV, ignorando linhas vazias.
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
     * Adiciona uma única linha ao final de um ficheiro CSV (Append).
     */
    public static void adicionarLinhaCSV(String caminhoCompleto, String linha) {
        File f = new File(caminhoCompleto);
        File pastaParente = f.getParentFile();
        if (pastaParente != null && !pastaParente.exists() && !pastaParente.mkdirs()) {
            System.err.println(">> AVISO: Não foi possível criar a pasta " + pastaParente.getPath());
        }

        try (FileWriter fw = new FileWriter(caminhoCompleto, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(linha);
        } catch (IOException e) {
            System.err.println(">> ERRO: Falha ao adicionar dados em (" + caminhoCompleto + "): " + e.getMessage());
        }
    }

    /**
     * Reescreve todo o conteúdo de um ficheiro (útil para Updates e Deletes).
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
