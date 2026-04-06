package utils;

import model.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilitário responsável pela exportação e persistência de dados do sistema em formato CSV.
 * Esta classe converte os objetos presentes no RepositorioDados em linhas de texto formatadas,
 * permitindo que a informação seja guardada permanentemente no disco rígido.
 */
public class ExportadorCSV {

    /**
     * Construtor privado para garantir que a classe seja utilizada apenas de forma estática,
     * impedindo a criação de instâncias desnecessárias.
     */
    private ExportadorCSV() {}

    /**
     * Coordena a exportação total do sistema, gerando ficheiros individuais para cada entidade.
     * Verifica se a diretoria de destino existe e cria-a automaticamente se necessário.
     * @param pastaBase O caminho do diretório onde os ficheiros serão guardados (ex: "bd/").
     * @param repo O repositório central que contém os dados a exportar.
     */
    public static void exportarTodos(String pastaBase, RepositorioDados repo) {
        File pasta = new File(pastaBase);
        if (!pasta.exists()) {
            pasta.mkdirs();
        }

        String prefixo = pastaBase.endsWith(File.separator) ? pastaBase : pastaBase + File.separator;

        exportarDepartamentos(prefixo + "departamentos.csv", repo);
        exportarDocentes(prefixo + "docentes.csv", repo);
        exportarCursos(prefixo + "cursos.csv", repo);
        exportarUCs(prefixo + "ucs.csv", repo);
        exportarEstudantes(prefixo + "estudantes.csv", repo);
        exportarAvaliacoes(prefixo + "avaliacoes.csv", repo);

        System.out.println(">> Exportação global concluída para a pasta: " + prefixo);
    }

    // --- MÉTODO AUXILIAR DE ESCRITA ---

    /**
     * Método auxiliar interno que centraliza a lógica de escrita em ficheiro.
     * Abre um fluxo de escrita, grava o cabeçalho e todas as linhas de dados fornecidas.
     * @param caminho O caminho completo do ficheiro de destino.
     * @param cabecalho A primeira linha do ficheiro CSV (nomes das colunas).
     * @param linhas Uma lista de Strings, onde cada String representa uma linha de dados formatada.
     */
    private static void escreverLinhasCSV(String caminho, String cabecalho, List<String> linhas) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(caminho))) {
            pw.println(cabecalho);
            for (String linha : linhas) {
                pw.println(linha);
            }
            System.out.println(">> Exportado com sucesso: " + caminho);
        } catch (IOException e) {
            System.err.println(">> ERRO: Falha ao exportar para: " + caminho + " (" + e.getMessage() + ")");
        }
    }

    // --- MÉTODOS DE EXPORTAÇÃO INDIVIDUAIS ---

    /**
     * Exporta a lista de Departamentos para um ficheiro CSV.
     * @param caminho Destino do ficheiro departamentos.csv.
     * @param repo Fonte dos dados.
     */
    public static void exportarDepartamentos(String caminho, RepositorioDados repo) {
        List<String> linhas = new ArrayList<>();
        for (int i = 0; i < repo.getTotalDepartamentos(); i++) {
            Departamento departamento = repo.getDepartamentos()[i];
            if (departamento != null) {
                linhas.add(departamento.getSigla() + ";" + departamento.getNome());
            }
        }
        escreverLinhasCSV(caminho, "sigla;nome", linhas);
    }

    /**
     * Exporta a lista de Docentes para um ficheiro CSV.
     * @param caminho Destino do ficheiro docentes.csv.
     * @param repo Fonte dos dados.
     */
    public static void exportarDocentes(String caminho, RepositorioDados repo) {
        List<String> linhas = new ArrayList<>();
        for (int i = 0; i < repo.getTotalDocentes(); i++) {
            Docente docente = repo.getDocentes()[i];
            if (docente != null) {
                linhas.add(docente.getSigla() + ";" + docente.getEmail() + ";" + docente.getPassword() + ";" +
                        docente.getNome() + ";" + docente.getNif() + ";" + docente.getMorada() + ";" + docente.getDataNascimento());
            }
        }
        escreverLinhasCSV(caminho, "sigla;email;password;nome;nif;morada;dataNascimento", linhas);
    }

    /**
     * Exporta a lista de Cursos, incluindo a relação com o respetivo departamento.
     * @param caminho Destino do ficheiro cursos.csv.
     * @param repo Fonte dos dados.
     */
    public static void exportarCursos(String caminho, RepositorioDados repo) {
        List<String> linhas = new ArrayList<>();
        for (int i = 0; i < repo.getTotalCursos(); i++) {
            Curso curso = repo.getCursos()[i];
            if (curso != null) {
                String siglaDep = (curso.getDepartamento() != null) ? curso.getDepartamento().getSigla() : "N/A";
                linhas.add(curso.getSigla() + ";" + curso.getNome() + ";" + siglaDep);
            }
        }
        escreverLinhasCSV(caminho, "sigla;nome;siglaDepartamento", linhas);
    }

    /**
     * Exporta as Unidades Curriculares, gerando múltiplas entradas caso uma UC pertença a vários cursos.
     * @param caminho Destino do ficheiro ucs.csv.
     * @param repo Fonte dos dados.
     */
    public static void exportarUCs(String caminho, RepositorioDados repo) {
        List<String> linhas = new ArrayList<>();
        for (int i = 0; i < repo.getTotalUcs(); i++) {
            UnidadeCurricular uc = repo.getUcs()[i];
            if (uc == null) continue;

            String siglaDoc = (uc.getDocenteResponsavel() != null) ? uc.getDocenteResponsavel().getSigla() : "N/A";

            if (uc.getTotalCursos() == 0) {
                linhas.add(uc.getSigla() + ";" + uc.getNome() + ";" + uc.getAnoCurricular() + ";" + siglaDoc + ";N/A");
            } else {
                for (int j = 0; j < uc.getTotalCursos(); j++) {
                    Curso c = uc.getCursos()[j];
                    if (c != null) {
                        linhas.add(uc.getSigla() + ";" + uc.getNome() + ";" + uc.getAnoCurricular() + ";" + siglaDoc + ";" + c.getSigla());
                    }
                }
            }
        }
        escreverLinhasCSV(caminho, "sigla;nome;anoCurricular;siglaDocente;siglaCurso", linhas);
    }

    /**
     * Exporta a lista de Estudantes e identifica o curso principal onde estão integrados.
     * @param caminho Destino do ficheiro estudantes.csv.
     * @param repo Fonte dos dados.
     */
    public static void exportarEstudantes(String caminho, RepositorioDados repo) {
        List<String> linhas = new ArrayList<>();
        for (int i = 0; i < repo.getTotalEstudantes(); i++) {
            Estudante estudante = repo.getEstudantes()[i];
            if (estudante == null) continue;

            String siglaCurso = "N/A";
            if (estudante.getPercurso() != null && estudante.getPercurso().getTotalUcsInscrito() > 0) {
                UnidadeCurricular primeiraUc = estudante.getPercurso().getUcsInscrito()[0];
                if (primeiraUc != null && primeiraUc.getTotalCursos() > 0 && primeiraUc.getCursos()[0] != null) {
                    siglaCurso = primeiraUc.getCursos()[0].getSigla();
                }
            }

            linhas.add(estudante.getNumeroMecanografico() + ";" + estudante.getEmail() + ";" + estudante.getPassword() + ";" +
                    estudante.getNome() + ";" + estudante.getNif() + ";" + estudante.getMorada() + ";" +
                    estudante.getDataNascimento() + ";" + estudante.getAnoPrimeiraInscricao() + ";" + siglaCurso);
        }
        escreverLinhasCSV(caminho, "numMec;email;password;nome;nif;morada;dataNascimento;anoInscricao;siglaCurso", linhas);
    }

    /**
     * Exporta o histórico de avaliações de todos os estudantes.
     * Garante que cada linha contenha exatamente 3 campos de nota, preenchendo com "-1.0"
     * as avaliações ainda não realizadas, respeitando o limite do portal.
     * @param caminho Destino do ficheiro avaliacoes.csv.
     * @param repo Fonte dos dados.
     */
    public static void exportarAvaliacoes(String caminho, RepositorioDados repo) {
        List<String> linhas = new ArrayList<>();
        for (int i = 0; i < repo.getTotalEstudantes(); i++) {
            Estudante estudante = repo.getEstudantes()[i];
            if (estudante == null || estudante.getPercurso() == null) continue;

            PercursoAcademico pa = estudante.getPercurso();
            for (int j = 0; j < pa.getTotalAvaliacoes(); j++) {
                Avaliacao av = pa.getHistoricoAvaliacoes()[j];
                if (av == null || av.getUc() == null) continue;

                double[] notas = av.getResultados();

                // Proteção: Se a nota existir no array, guarda-a. Se não, guarda -1.0
                String nota1 = (notas.length > 0) ? String.valueOf(notas[0]) : "-1.0";
                String nota2 = (notas.length > 1) ? String.valueOf(notas[1]) : "-1.0";
                String nota3 = (notas.length > 2) ? String.valueOf(notas[2]) : "-1.0";

                linhas.add(estudante.getNumeroMecanografico() + ";" + av.getUc().getSigla() + ";" +
                        av.getAnoLetivo() + ";" + nota1 + ";" + nota2 + ";" + nota3);
            }
        }
        escreverLinhasCSV(caminho, "numMec;siglaUC;anoLetivo;nota1;nota2;nota3", linhas);
    }
}
