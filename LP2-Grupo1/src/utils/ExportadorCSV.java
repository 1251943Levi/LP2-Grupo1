package utils;

import model.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilitário responsável pela escrita e atualização "On-Demand" de ficheiros CSV.
 */
public class ExportadorCSV {

    private ExportadorCSV() {}

    /**
     * Verifica se a pasta e o ficheiro existem. Se não existirem, cria-os e insere o cabeçalho.
     */
    private static void garantirFicheiroECabecalho(String caminho, String cabecalho) {
        File ficheiro = new File(caminho);
        File pasta = ficheiro.getParentFile();
        if (pasta != null && !pasta.exists()) {
            pasta.mkdirs();
        }
        if (!ficheiro.exists()) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(ficheiro))) {
                pw.println(cabecalho);
            } catch (IOException e) {
                System.err.println(">> ERRO CRÍTICO ao criar ficheiro inicial (" + caminho + "): " + e.getMessage());
            }
        }
    }

    /**
     * Adiciona uma nova linha de texto ao final de um ficheiro (modo Append) — via caminho completo.
     */
    private static void adicionarLinhaCSV(String caminho, String linha) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(caminho, true))) {
            pw.println(linha);
        } catch (IOException e) {
            System.err.println(">> ERRO: Falha ao guardar dados em (" + caminho + "): " + e.getMessage());
        }
    }

    /**
     * Adiciona uma nova linha ao final de um ficheiro CSV (operação pública de Create num CRUD).
     *
     * CORRIGIDO: garante que a diretoria existe antes de tentar escrever, evitando
     * falhas silenciosas quando o ficheiro ainda não foi criado por outro método.
     *
     * @param nomeFicheiro Nome do ficheiro (ex: "ucs.csv").
     * @param novaLinha    Dados já formatados em CSV.
     * @param pastaBase    Caminho da pasta base (ex: "bd").
     */
    public static void adicionarLinhaCSV(String nomeFicheiro, String novaLinha, String pastaBase) {
        String caminho = pastaBase + File.separator + nomeFicheiro;

        // Garante que a diretoria existe (CORRIGIDO)
        File f = new File(caminho);
        if (f.getParentFile() != null) f.getParentFile().mkdirs();

        try (FileWriter fw = new FileWriter(caminho, true);
             java.io.BufferedWriter bw = new java.io.BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(novaLinha);
        } catch (IOException e) {
            System.out.println(">> Erro ao guardar em " + nomeFicheiro);
        }
    }

    private static void adicionarCredencial(String email, String passwordSegura, String tipo, String pastaBase) {
        if (email == null || passwordSegura == null) return;
        String caminho = pastaBase + File.separator + "credenciais.csv";
        garantirFicheiroECabecalho(caminho, "email;password_hash;tipo");
        adicionarLinhaCSV(caminho, email + ";" + passwordSegura + ";" + tipo);
    }

    private static void reescreverFicheiro(String caminho, List<String> linhas) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(caminho, false))) {
            for (String l : linhas) pw.println(l);
        } catch (IOException e) {
            System.err.println(">> ERRO: Falha ao reescrever ficheiro após atualização.");
        }
    }

    public static void atualizarPasswordCentralizada(String email, String novaPasswordSegura, String pastaBase) {
        if (email == null || novaPasswordSegura == null) return;

        String caminho = pastaBase + File.separator + "credenciais.csv";
        List<String> linhas = new ArrayList<>();
        boolean atualizado = false;

        File f = new File(caminho);
        if (!f.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(caminho))) {
            String cabecalho = br.readLine();
            if (cabecalho != null) linhas.add(cabecalho);

            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.trim().isEmpty()) continue;
                String[] dados = linha.split(";", -1);
                if (dados.length >= 3 && dados[0].trim().equalsIgnoreCase(email)) {
                    linhas.add(dados[0] + ";" + novaPasswordSegura + ";" + dados[2]);
                    atualizado = true;
                } else {
                    linhas.add(linha);
                }
            }
            if (atualizado) reescreverFicheiro(caminho, linhas);
        } catch (IOException e) {
            System.err.println(">> ERRO ao atualizar password nas credenciais: " + e.getMessage());
        }
    }

    public static void adicionarEstudante(Estudante estudante, String pastaBase, String siglaCurso) {
        adicionarCredencial(estudante.getEmail(), estudante.getPassword(), "ESTUDANTE", pastaBase);

        String caminho = pastaBase + File.separator + "estudantes.csv";
        garantirFicheiroECabecalho(caminho,
                "numMec;email;nome;nif;morada;dataNascimento;anoInscricao;siglaCurso;saldoDevedor;anoCurricular");

        String linha = estudante.getNumeroMecanografico() + ";" + estudante.getEmail() + ";"
                + estudante.getNome() + ";" + estudante.getNif() + ";" + estudante.getMorada() + ";"
                + estudante.getDataNascimento() + ";" + estudante.getAnoPrimeiraInscricao() + ";"
                + siglaCurso + ";" + estudante.getSaldoDevedor() + ";" + estudante.getAnoCurricular();

        adicionarLinhaCSV(caminho, linha);
    }

    public static void adicionarDocente(Docente docente, String pastaBase) {
        if (docente == null) return;
        adicionarCredencial(docente.getEmail(), docente.getPassword(), "DOCENTE", pastaBase);

        String caminho = pastaBase + File.separator + "docentes.csv";
        garantirFicheiroECabecalho(caminho, "sigla;email;nome;nif;morada;dataNascimento");

        String linha = docente.getSigla() + ";" +
                docente.getEmail() + ";" +
                docente.getNome() + ";" +
                docente.getNif() + ";" +
                docente.getMorada() + ";" +
                docente.getDataNascimento();

        adicionarLinhaCSV(caminho, linha);
    }

    public static void adicionarGestor(Gestor gestor, String pastaBase) {
        if (gestor == null) return;
        adicionarCredencial(gestor.getEmail(), gestor.getPassword(), "GESTOR", pastaBase);

        String caminho = pastaBase + File.separator + "gestores.csv";
        garantirFicheiroECabecalho(caminho, "email;nome;nif;morada;dataNascimento");

        String linha = gestor.getEmail() + ";" +
                gestor.getNome() + ";" +
                gestor.getNif() + ";" +
                gestor.getMorada() + ";" +
                gestor.getDataNascimento();

        adicionarLinhaCSV(caminho, linha);
    }

    public static void adicionarAvaliacao(Avaliacao avaliacao, int numMec, String pastaBase) {
        if (avaliacao == null || avaliacao.getUc() == null) return;

        String caminho = pastaBase + File.separator + "avaliacoes.csv";
        garantirFicheiroECabecalho(caminho, "numMec;siglaUC;anoLetivo;nota1;nota2;nota3");

        double[] notas = avaliacao.getResultados();
        String nota1 = (notas.length > 0) ? String.valueOf(notas[0]) : "";
        String nota2 = (notas.length > 1) ? String.valueOf(notas[1]) : "";
        String nota3 = (notas.length > 2) ? String.valueOf(notas[2]) : "";

        String linha = numMec + ";" +
                avaliacao.getUc().getSigla() + ";" +
                avaliacao.getAnoLetivo() + ";" +
                nota1 + ";" + nota2 + ";" + nota3;

        adicionarLinhaCSV(caminho, linha);
    }

    public static void adicionarDepartamento(Departamento departamento, String pastaBase) {
        if (departamento == null) return;
        String caminho = pastaBase + File.separator + "departamentos.csv";
        garantirFicheiroECabecalho(caminho, "sigla;nome");
        String linha = departamento.getSigla() + ";" + departamento.getNome();
        adicionarLinhaCSV(caminho, linha);    }

    public static void adicionarCurso(Curso curso, String pastaBase) {
        if (curso == null) return;
        String caminho = pastaBase + File.separator + "cursos.csv";
        garantirFicheiroECabecalho(caminho, "sigla;nome;siglaDepartamento;propina;estado");
        String siglaDep = (curso.getDepartamento() != null) ? curso.getDepartamento().getSigla() : "N/A";
        String linha = curso.getSigla() + ";" + curso.getNome() + ";" + siglaDep + ";"
                + curso.getValorPropinaAnual() + ";" + curso.getEstado();
        adicionarLinhaCSV(caminho, linha);
    }

    public static void adicionarUnidadeCurricular(UnidadeCurricular uc, String pastaBase) {
        if (uc == null) return;
        String caminho = pastaBase + File.separator + "ucs.csv";
        garantirFicheiroECabecalho(caminho, "sigla;nome;anoCurricular;siglaDocente;siglaCurso");

        String siglaDoc = (uc.getDocenteResponsavel() != null) ? uc.getDocenteResponsavel().getSigla() : "N/A";

        if (uc.getTotalCursos() == 0) {
            adicionarLinhaCSV(caminho,
                    uc.getSigla() + ";" + uc.getNome() + ";" + uc.getAnoCurricular() + ";" + siglaDoc + ";N/A");
        } else {
            for (int i = 0; i < uc.getTotalCursos(); i++) {
                Curso c = uc.getCursos()[i];
                if (c != null)
                    adicionarLinhaCSV(caminho,
                            uc.getSigla() + ";" + uc.getNome() + ";" + uc.getAnoCurricular()
                                    + ";" + siglaDoc + ";" + c.getSigla());
            }
        }
    }

    /**
     * Atualiza os dados de perfil de um estudante existente.
     *
     * CORRIGIDO: usa getSiglaCurso() para preservar a sigla a partir do próprio objeto.
     * Fallback para o valor do ficheiro caso a sigla não esteja populada no objeto.
     */
    public static void atualizarEstudante(Estudante estudanteAtualizado, String pastaBase) {
        String caminho = pastaBase + File.separator + "estudantes.csv";
        List<String> linhas = new ArrayList<>();
        boolean atualizado = false;

        try (BufferedReader br = new BufferedReader(new FileReader(caminho))) {
            String cabecalho = br.readLine();
            if (cabecalho != null) linhas.add(cabecalho);

            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.trim().isEmpty()) continue;
                String[] dados = linha.split(";", -1);

                if (Integer.parseInt(dados[0].trim()) == estudanteAtualizado.getNumeroMecanografico()) {
                    String siglaCurso = (estudanteAtualizado.getSiglaCurso() != null
                            && !estudanteAtualizado.getSiglaCurso().isEmpty())
                            ? estudanteAtualizado.getSiglaCurso()
                            : (dados.length > 7 ? dados[7] : "N/A");

                    String novaLinha = estudanteAtualizado.getNumeroMecanografico() + ";"
                            + estudanteAtualizado.getEmail() + ";" + estudanteAtualizado.getNome() + ";"
                            + estudanteAtualizado.getNif() + ";" + estudanteAtualizado.getMorada() + ";"
                            + estudanteAtualizado.getDataNascimento() + ";"
                            + estudanteAtualizado.getAnoPrimeiraInscricao() + ";" + siglaCurso + ";"
                            + estudanteAtualizado.getSaldoDevedor() + ";"
                            + estudanteAtualizado.getAnoCurricular();


                    linhas.add(novaLinha);
                    atualizado = true;
                } else {
                    linhas.add(linha);
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println(">> ERRO ao ler ficheiro para atualização.");
            return;
        }

        if (atualizado) reescreverFicheiro(caminho, linhas);
    }

    public static void atualizarCurso(Curso cursoAtualizado, String pastaBase) {
        String caminho = pastaBase + File.separator + "cursos.csv";
        List<String> linhas = new ArrayList<>();
        boolean atualizado = false;

        try (BufferedReader br = new BufferedReader(new FileReader(caminho))) {
            String cabecalho = br.readLine();
            if (cabecalho != null) linhas.add(cabecalho);

            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.trim().isEmpty()) continue;
                String[] dados = linha.split(";", -1);

                if (dados[0].trim().equalsIgnoreCase(cursoAtualizado.getSigla())) {
                    String siglaDep = (cursoAtualizado.getDepartamento() != null)
                            ? cursoAtualizado.getDepartamento().getSigla() : "N/A";
                    linhas.add(cursoAtualizado.getSigla() + ";" + cursoAtualizado.getNome() + ";"
                            + siglaDep + ";" + cursoAtualizado.getValorPropinaAnual() + ";"
                            + cursoAtualizado.getEstado());
                    atualizado = true;
                } else {
                    linhas.add(linha);
                }
            }
        } catch (IOException e) { return; }

        if (atualizado) reescreverFicheiro(caminho, linhas);
    }

    public static boolean removerLinhaCSV(String nomeFicheiro, String idAProcurar, String pastaBase) {
        String caminho = pastaBase + File.separator + nomeFicheiro;
        File ficheiroOriginal = new File(caminho);
        File ficheiroTemporario = new File(pastaBase + File.separator + "temp.csv");
        boolean encontrou = false;

        try (BufferedReader br = new BufferedReader(new FileReader(ficheiroOriginal));
             PrintWriter pw = new PrintWriter(new FileWriter(ficheiroTemporario))) {

            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.trim().isEmpty()) continue;
                String[] dados = linha.split(";", -1);
                if (dados[0].equalsIgnoreCase(idAProcurar)) {
                    encontrou = true;
                    continue;
                }
                pw.println(linha);
            }
        } catch (Exception e) { return false; }

        if (encontrou) {
            ficheiroOriginal.delete();
            ficheiroTemporario.renameTo(ficheiroOriginal);
        } else {
            ficheiroTemporario.delete();
        }
        return encontrou;
    }

    public static int contarEstudantesPorCursoEAno(String siglaCurso, int anoCurricular, String pastaBase) {
        String caminho = pastaBase + File.separator + "estudantes.csv";
        int contagem = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(caminho))) {
            br.readLine();
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.trim().isEmpty()) continue;
                String[] dados = linha.split(";", -1);
                if (dados.length > 7 && dados[7].trim().equalsIgnoreCase(siglaCurso)) {
                    int anoAluno = (dados.length > 9 && !dados[9].trim().isEmpty())
                            ? Integer.parseInt(dados[9].trim()) : 1;
                    if (anoAluno == anoCurricular) contagem++;
                }
            }
        } catch (Exception e) {}
        return contagem;
    }
}
