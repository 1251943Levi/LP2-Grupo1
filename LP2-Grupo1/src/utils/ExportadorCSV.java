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
 * Aplica uma abordagem relacional (separando credenciais de perfis) e utiliza
 * operações de Append (adicionar) e Update (atualizar linha) para poupar memória.
 */
public class ExportadorCSV {

    private ExportadorCSV() {}

    /**
     * Verifica se a pasta e o ficheiro existem. Se não existirem, cria-os e insere o cabeçalho.
     * @param caminho Caminho completo do ficheiro CSV.
     * @param cabecalho String com o nome das colunas separadas por ponto e vírgula.
     */
    private static void garantirFicheiroECabecalho(String caminho, String cabecalho) {
        File ficheiro = new File(caminho);
        File pasta = ficheiro.getParentFile();
        if (pasta != null && !pasta.exists()) pasta.mkdirs();

        if (!ficheiro.exists()) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(ficheiro))) {
                pw.println(cabecalho);
            } catch (IOException e) {
                System.out.println(">> ERRO ao criar ficheiro inicial: " + caminho);
            }
        }
    }

    /**
     * Adiciona uma nova linha de texto ao final de um ficheiro existente (modo Append).
     * @param caminho Caminho do ficheiro de destino.
     * @param linha Dados formatados em CSV a inserir.
     */
    private static void adicionarLinhaCSV(String caminho, String linha) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(caminho, true))) {
            pw.println(linha);
        } catch (IOException e) {
            System.err.println(">> ERRO: Falha ao guardar dados em: " + caminho);
        }
    }


    /**
     * Regista as credenciais de acesso de um utilizador no ficheiro central.
     * @param email Email do utilizador (funciona como Chave Estrangeira).
     * @param passwordSegura Password já com o Hash aplicado.
     * @param tipo Tipo de utilizador (ESTUDANTE, DOCENTE ou GESTOR).
     * @param pastaBase Caminho da diretoria de dados.
     */
    private static void adicionarCredencial(String email, String passwordSegura, String tipo, String pastaBase) {
        String caminho = pastaBase + File.separator + "credenciais.csv";
        garantirFicheiroECabecalho(caminho, "email;password_hash;tipo");
        adicionarLinhaCSV(caminho, email + ";" + passwordSegura + ";" + tipo);
    }

    /**
     * Atualiza a password de um utilizador específico no ficheiro de credenciais.
     * @param email Email do utilizador a atualizar.
     * @param novaPasswordSegura Nova credencial encriptada.
     * @param pastaBase Caminho da diretoria de dados.
     */
    public static void atualizarPasswordCentralizada(String email, String novaPasswordSegura, String pastaBase) {
        String caminho = pastaBase + File.separator + "credenciais.csv";
        List<String> linhas = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(caminho))) {
            String cabecalho = br.readLine();
            if (cabecalho != null) linhas.add(cabecalho);

            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.trim().isEmpty()) continue;
                String[] dados = linha.split(";", -1);

                if (dados[0].trim().equalsIgnoreCase(email)) {
                    linhas.add(dados[0] + ";" + novaPasswordSegura + ";" + dados[2]);
                } else {
                    linhas.add(linha);
                }
            }
            reescreverFicheiro(caminho, linhas);
        } catch (IOException e) {
            System.out.println(">> ERRO ao atualizar password nas credenciais.");
        }
    }


    /**
     * Adiciona um novo estudante ao sistema, criando as credenciais e o perfil.
     * @param estudante Objeto com os dados do estudante.
     * @param pastaBase Caminho da diretoria de dados.
     * @param siglaCurso Sigla do curso onde o estudante foi inscrito.
     */
    public static void adicionarEstudante(Estudante estudante, String pastaBase, String siglaCurso) {
        adicionarCredencial(estudante.getEmail(), estudante.getPassword(), "ESTUDANTE", pastaBase);

        String caminho = pastaBase + File.separator + "estudantes.csv";
        garantirFicheiroECabecalho(caminho, "numMec;email;nome;nif;morada;dataNascimento;anoInscricao;siglaCurso;saldoDevedor");

        String linha = estudante.getNumeroMecanografico() + ";" + estudante.getEmail() + ";" +
                estudante.getNome() + ";" + estudante.getNif() + ";" + estudante.getMorada() + ";" +
                estudante.getDataNascimento() + ";" + estudante.getAnoPrimeiraInscricao() + ";" + siglaCurso + ";" + estudante.getSaldoDevedor();

        adicionarLinhaCSV(caminho, linha);
    }

    /**
     * Adiciona um novo docente ao sistema, registando credenciais e perfil.
     * @param docente Objeto com os dados do docente.
     * @param pastaBase Caminho da diretoria de dados.
     */
    public static void adicionarDocente(Docente docente, String pastaBase) {
        adicionarCredencial(docente.getEmail(), docente.getPassword(), "DOCENTE", pastaBase);

        String caminho = pastaBase + File.separator + "docentes.csv";
        garantirFicheiroECabecalho(caminho, "sigla;email;nome;nif;morada;dataNascimento");

        String linha = docente.getSigla() + ";" + docente.getEmail() + ";" + docente.getNome() + ";" +
                docente.getNif() + ";" + docente.getMorada() + ";" + docente.getDataNascimento();

        adicionarLinhaCSV(caminho, linha);
    }

    /**
     * Adiciona um novo gestor (backoffice) ao sistema.
     * @param gestor Objeto com os dados do gestor.
     * @param pastaBase Caminho da diretoria de dados.
     */
    public static void adicionarGestor(Gestor gestor, String pastaBase) {
        adicionarCredencial(gestor.getEmail(), gestor.getPassword(), "GESTOR", pastaBase);

        String caminho = pastaBase + File.separator + "gestores.csv";
        garantirFicheiroECabecalho(caminho, "email;nome;nif;morada;dataNascimento");

        String linha = gestor.getEmail() + ";" + gestor.getNome() + ";" + gestor.getNif() + ";" +
                gestor.getMorada() + ";" + gestor.getDataNascimento();

        adicionarLinhaCSV(caminho, linha);
    }

    /**
     * Regista as notas de um momento de avaliação para um estudante.
     * @param avaliacao Objeto com as notas e a UC associada.
     * @param numMec Número mecanográfico do estudante.
     * @param pastaBase Caminho da diretoria de dados.
     */
    public static void adicionarAvaliacao(Avaliacao avaliacao, int numMec, String pastaBase) {
        String caminho = pastaBase + File.separator + "avaliacoes.csv";
        garantirFicheiroECabecalho(caminho, "numMec;siglaUC;anoLetivo;nota1;nota2;nota3");

        double[] notas = avaliacao.getResultados();
        String nota1 = (notas.length > 0) ? String.valueOf(notas[0]) : "-1.0";
        String nota2 = (notas.length > 1) ? String.valueOf(notas[1]) : "-1.0";
        String nota3 = (notas.length > 2) ? String.valueOf(notas[2]) : "-1.0";

        String linha = numMec + ";" + avaliacao.getUc().getSigla() + ";" + avaliacao.getAnoLetivo() + ";" + nota1 + ";" + nota2 + ";" + nota3;
        adicionarLinhaCSV(caminho, linha);
    }

    /**
     * Adiciona um novo departamento à base de dados.
     * @param departamento Objeto departamento.
     * @param pastaBase Caminho da diretoria de dados.
     */
    public static void adicionarDepartamento(Departamento departamento, String pastaBase) {
        String caminho = pastaBase + File.separator + "departamentos.csv";
        garantirFicheiroECabecalho(caminho, "sigla;nome");

        String linha = departamento.getSigla() + ";" + departamento.getNome();
        adicionarLinhaCSV(caminho, linha);
    }

    /**
     * Adiciona um novo curso e vincula-o ao respetivo departamento.
     * @param curso Objeto curso.
     * @param pastaBase Caminho da diretoria de dados.
     */
    public static void adicionarCurso(Curso curso, String pastaBase) {
        String caminho = pastaBase + File.separator + "cursos.csv";
        garantirFicheiroECabecalho(caminho, "sigla;nome;siglaDepartamento");

        String siglaDep = (curso.getDepartamento() != null) ? curso.getDepartamento().getSigla() : "N/A";
        String linha = curso.getSigla() + ";" + curso.getNome() + ";" + siglaDep;

        adicionarLinhaCSV(caminho, linha);
    }

    /**
     * Adiciona uma nova Unidade Curricular. Se a UC pertencer a vários cursos,
     * gera uma linha de registo por cada curso associado.
     * @param uc Objeto Unidade Curricular.
     * @param pastaBase Caminho da diretoria de dados.
     */
    public static void adicionarUnidadeCurricular(UnidadeCurricular uc, String pastaBase) {
        String caminho = pastaBase + File.separator + "ucs.csv";
        garantirFicheiroECabecalho(caminho, "sigla;nome;anoCurricular;siglaDocente;siglaCurso");

        String siglaDoc = (uc.getDocenteResponsavel() != null) ? uc.getDocenteResponsavel().getSigla() : "N/A";

        if (uc.getTotalCursos() == 0) {
            String linha = uc.getSigla() + ";" + uc.getNome() + ";" + uc.getAnoCurricular() + ";" + siglaDoc + ";N/A";
            adicionarLinhaCSV(caminho, linha);
        } else {
            for (int i = 0; i < uc.getTotalCursos(); i++) {
                Curso c = uc.getCursos()[i];
                if (c != null) {
                    String linha = uc.getSigla() + ";" + uc.getNome() + ";" + uc.getAnoCurricular() + ";" + siglaDoc + ";" + c.getSigla();
                    adicionarLinhaCSV(caminho, linha);
                }
            }
        }
    }


    /**
     * Atualiza os dados de perfil de um estudante existente (ex: nova morada).
     * Lê o ficheiro, modifica a linha do estudante e reescreve o ficheiro atualizado.
     * @param estudanteAtualizado O objeto com os dados mais recentes.
     * @param pastaBase Caminho da diretoria de dados.
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
                    String siglaCurso = dados.length > 7 ? dados[7] : "N/A";
                    String novaLinha = estudanteAtualizado.getNumeroMecanografico() + ";" + estudanteAtualizado.getEmail() + ";" +
                            estudanteAtualizado.getNome() + ";" + estudanteAtualizado.getNif() + ";" +
                            estudanteAtualizado.getMorada() + ";" + estudanteAtualizado.getDataNascimento() + ";" +
                            estudanteAtualizado.getAnoPrimeiraInscricao() + ";" + siglaCurso + ";" + estudanteAtualizado.getSaldoDevedor();
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

    /**
     * Reescreve totalmente um ficheiro CSV substituindo o conteúdo existente.
     * Utilizado após uma operação de Update (atualização de uma linha no meio do ficheiro).
     * @param caminho Caminho do ficheiro a reescrever.
     * @param linhas Lista em memória contendo as novas linhas a guardar.
     */
    private static void reescreverFicheiro(String caminho, List<String> linhas) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(caminho, false))) {
            for (String l : linhas) pw.println(l);
        } catch (IOException e) {
            System.err.println(">> ERRO: Falha ao reescrever ficheiro após atualização.");
        }
    }
}