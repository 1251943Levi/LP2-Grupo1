package dal;

import model.Estudante;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Responsável pelas operações de CRUD exclusivas do ficheiro estudantes.csv.
 */
public class EstudanteDAL {
    private static final String NOME_FICHEIRO = "estudantes.csv";
    private static final String CABECALHO = "numMec;email;nome;nif;morada;dataNascimento;anoInscricao;siglaCurso;saldoDevedor;anoCurricular";

    public static void adicionarEstudante(Estudante estudante, String siglaCurso, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        DALUtil.garantirFicheiroECabecalho(caminho, CABECALHO);

        String linha = estudante.getNumeroMecanografico() + ";" + estudante.getEmail() + ";"
                + estudante.getNome() + ";" + estudante.getNif() + ";" + estudante.getMorada() + ";"
                + estudante.getDataNascimento() + ";" + estudante.getAnoPrimeiraInscricao() + ";"
                + siglaCurso + ";" + estudante.getSaldoDevedor() + ";" + estudante.getAnoCurricular();

        DALUtil.adicionarLinhaCSV(caminho, linha);
    }

    public static void atualizarEstudante(Estudante estudante, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhasAntigas = DALUtil.lerFicheiro(caminho);
        if (linhasAntigas.isEmpty()) return;

        List<String> linhasAtualizadas = new ArrayList<>();
        boolean atualizado = false;

        for (String linha : linhasAntigas) {
            if (linha.equalsIgnoreCase(CABECALHO)) { linhasAtualizadas.add(linha); continue; }
            String[] dados = linha.split(";", -1);
            if (dados.length >= 10) {
                try {
                    if (Integer.parseInt(dados[0].trim()) == estudante.getNumeroMecanografico()) {
                        String siglaCurso = (estudante.getSiglaCurso() != null && !estudante.getSiglaCurso().isEmpty())
                                ? estudante.getSiglaCurso() : dados[7].trim();
                        linhasAtualizadas.add(estudante.getNumeroMecanografico() + ";"
                                + estudante.getEmail() + ";" + estudante.getNome() + ";"
                                + estudante.getNif() + ";" + estudante.getMorada() + ";"
                                + estudante.getDataNascimento() + ";"
                                + estudante.getAnoPrimeiraInscricao() + ";" + siglaCurso + ";"
                                + estudante.getSaldoDevedor() + ";" + estudante.getAnoCurricular());
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
     * Substitui o carregarPerfilEstudante do ImportadorCSV.
     */
    public static Estudante carregarPerfil(String email, String hash, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);

        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 7 && dados[1].trim().equalsIgnoreCase(email)) {
                try {
                    int numMec  = Integer.parseInt(dados[0].trim());
                    int anoInsc = Integer.parseInt(dados[6].trim());
                    Estudante e = new Estudante(numMec, email, hash,
                            dados[2].trim(), dados[3].trim(), dados[4].trim(), dados[5].trim(), anoInsc);
                    if (dados.length > 7) e.setSiglaCurso(dados[7].trim());
                    if (dados.length > 8 && !dados[8].isEmpty())
                        e.setSaldoDevedor(Double.parseDouble(dados[8].trim()));
                    if (dados.length > 9 && !dados[9].isEmpty())
                        e.setAnoCurricular(Integer.parseInt(dados[9].trim()));
                    return e;
                } catch (NumberFormatException ex) { }
            }
        }
        return null;
    }

    public static Estudante procurarPorNumMec(int numMec, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);

        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 7) {
                try {
                    if (Integer.parseInt(dados[0].trim()) == numMec) {
                        int anoInsc = Integer.parseInt(dados[6].trim());
                        Estudante e = new Estudante(numMec, dados[1].trim(), "",
                                dados[2].trim(), dados[3].trim(), dados[4].trim(), dados[5].trim(), anoInsc);
                        if (dados.length > 7 && !dados[7].trim().isEmpty())
                            e.setSiglaCurso(dados[7].trim());
                        if (dados.length > 8 && !dados[8].trim().isEmpty())
                            e.setSaldoDevedor(Double.parseDouble(dados[8].trim()));
                        if (dados.length > 9 && !dados[9].trim().isEmpty())
                            e.setAnoCurricular(Integer.parseInt(dados[9].trim()));
                        return e;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return null;
    }

    /**
     * Carrega todos os estudantes com os dados básicos do CSV (sem percurso académico).
     * Usado pela GestorBLL para estatísticas, devedores e avanço de ano letivo.
     */
    public static List<Estudante> carregarTodos(String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        List<Estudante> lista = new ArrayList<>();

        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 7) {
                try {
                    int numMec  = Integer.parseInt(dados[0].trim());
                    int anoInsc = Integer.parseInt(dados[6].trim());
                    Estudante e = new Estudante(numMec, dados[1].trim(), "",
                            dados[2].trim(), dados[3].trim(), dados[4].trim(), dados[5].trim(), anoInsc);
                    if (dados.length > 7 && !dados[7].trim().isEmpty())
                        e.setSiglaCurso(dados[7].trim());
                    if (dados.length > 8 && !dados[8].trim().isEmpty()) {
                        try { e.setSaldoDevedor(Double.parseDouble(dados[8].trim())); }
                        catch (NumberFormatException ignored) {}
                    }
                    if (dados.length > 9 && !dados[9].trim().isEmpty()) {
                        try { e.setAnoCurricular(Integer.parseInt(dados[9].trim())); }
                        catch (NumberFormatException ignored) {}
                    }
                    lista.add(e);
                } catch (NumberFormatException ignored) {}
            }
        }
        return lista;
    }


    public static int contarEstudantesPorCursoEAno(String siglaCurso, int anoCurricular, String pastaBase) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        int contagem = 0;
        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length > 7 && dados[7].trim().equalsIgnoreCase(siglaCurso)) {
                int anoAluno = (dados.length > 9 && !dados[9].trim().isEmpty())
                        ? Integer.parseInt(dados[9].trim()) : 1;
                if (anoAluno == anoCurricular) contagem++;
            }
        }
        return contagem;
    }

    public static int obterProximoNumeroMecanografico(String pastaBase, int anoAtual) {
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        int maxSufixo = 0;
        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length > 0 && !dados[0].isEmpty()) {
                try {
                    int numAtual = Integer.parseInt(dados[0].trim());
                    if (numAtual / 10000 == anoAtual) {
                        int sufixo = numAtual % 10000;
                        if (sufixo > maxSufixo) maxSufixo = sufixo;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return (anoAtual * 10000) + (maxSufixo + 1);
    }

    /**
     * Verifica se já existe um estudante com o NIF indicado.
     * Usado pela GestorBLL.isNifDuplicado() antes de registar um novo utilizador.
     */
    public static boolean existeNif(String nif, String pastaBase) {
        if (nif == null || nif.trim().isEmpty()) return false;
        String caminho = pastaBase + File.separator + NOME_FICHEIRO;
        List<String> linhas = DALUtil.lerFicheiro(caminho);
        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] dados = linha.split(";", -1);
            if (dados.length >= 4 && dados[3].trim().equals(nif.trim())) return true;
        }
        return false;
    }
}
