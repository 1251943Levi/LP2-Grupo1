package utils;

import model.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Year;

/**
 * Lê ficheiros de forma cirúrgica apenas quando a informação é solicitada.
 */
public class ImportadorCSV {

    private ImportadorCSV() {}

    /**
     * Autentica o utilizador de forma centralizada usando o ficheiro credenciais.csv.
     */
    public static Utilizador autenticarNoFicheiro(String email, String passwordIntroduzida, String pastaBase) {
        String caminhoCredenciais = pastaBase + File.separator + "credenciais.csv";
        String tipoUtilizador = null;
        String hashGuardado = null;

        File ficheiro = new File(caminhoCredenciais);
        if (!ficheiro.exists()) {
            System.err.println(">> Erro crítico: Ficheiro credenciais.csv não encontrado no caminho: " + caminhoCredenciais);
            return null;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(ficheiro))) {
            br.readLine(); // Ignorar cabeçalho
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.trim().isEmpty()) continue;
                String[] dados = linha.split(";", -1);

                if (dados.length >= 3 && dados[0].trim().equalsIgnoreCase(email)) {
                    hashGuardado = dados[1].trim();
                    tipoUtilizador = dados[2].trim().toUpperCase();
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println(">> Erro ao ler credenciais.csv: " + e.getMessage());
            return null;
        }

        if (hashGuardado == null || tipoUtilizador == null || !SegurancaPasswords.verificarPassword(passwordIntroduzida, hashGuardado)) {
            return null;
        }

        switch (tipoUtilizador) {
            case "ESTUDANTE":
                return carregarPerfilEstudante(email, hashGuardado, pastaBase);
            case "DOCENTE":
                return carregarPerfilDocente(email, hashGuardado, pastaBase);
            case "GESTOR":
                return carregarPerfilGestor(email, hashGuardado, pastaBase);
            default:
                System.err.println(">> Aviso: Tipo de utilizador desconhecido (" + tipoUtilizador + ").");
                return null;
        }
    }

    private static Estudante carregarPerfilEstudante(String email, String hashGuardado, String pastaBase) {
        String caminho = pastaBase + File.separator + "estudantes.csv";
        try (BufferedReader br = new BufferedReader(new FileReader(caminho))) {
            br.readLine();
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.trim().isEmpty()) continue;
                String[] dados = linha.split(";", -1);

                if (dados.length >= 7 && dados[1].trim().equalsIgnoreCase(email)) {
                    try {
                        int numMec = Integer.parseInt(dados[0].trim());
                        int anoInscricao = Integer.parseInt(dados[6].trim());
                        Estudante e = new Estudante(numMec, email, hashGuardado, dados[2].trim(), dados[3].trim(), dados[4].trim(), dados[5].trim(), anoInscricao);
                        carregarDadosAcademicos(e, pastaBase);
                        return e;
                    } catch (NumberFormatException ex) {
                        System.err.println(">> Erro na formatação de números no estudante: " + email);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println(">> Erro ao ler estudantes.csv: " + e.getMessage());
        }
        return null;
    }

    private static Docente carregarPerfilDocente(String email, String hashGuardado, String pastaBase) {
        String caminho = pastaBase + File.separator + "docentes.csv";
        try (BufferedReader br = new BufferedReader(new FileReader(caminho))) {
            br.readLine();
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.trim().isEmpty()) continue;
                String[] dados = linha.split(";", -1);

                if (dados.length >= 6 && dados[1].trim().equalsIgnoreCase(email)) {
                    Docente d = new Docente(dados[0].trim(), email, hashGuardado, dados[2].trim(), dados[3].trim(), dados[4].trim(), dados[5].trim());
                    carregarUcsDoDocente(d, pastaBase);
                    return d;
                }
            }
        } catch (IOException e) {
            System.err.println(">> Erro ao ler docentes.csv: " + e.getMessage());
        }
        return null;
    }

    private static Gestor carregarPerfilGestor(String email, String hashGuardado, String pastaBase) {
        String caminho = pastaBase + File.separator + "gestores.csv";
        try (BufferedReader br = new BufferedReader(new FileReader(caminho))) {
            br.readLine();
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.trim().isEmpty()) continue;
                String[] dados = linha.split(";", -1);

                if (dados.length >= 5 && dados[0].trim().equalsIgnoreCase(email)) {
                    return new Gestor(email, hashGuardado, dados[1].trim(), dados[2].trim(), dados[3].trim(), dados[4].trim());
                }
            }
        } catch (IOException e) {
            System.err.println(">> Erro ao ler gestores.csv: " + e.getMessage());
        }
        return null;
    }

    public static Departamento procurarDepartamento(String sigla, String pastaBase) {
        String caminho = pastaBase + File.separator + "departamentos.csv";
        try (BufferedReader br = new BufferedReader(new FileReader(caminho))) {
            br.readLine();
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.trim().isEmpty()) continue;
                String[] dados = linha.split(";", -1);
                if (dados.length >= 2 && dados[0].trim().equalsIgnoreCase(sigla)) {
                    return new Departamento(dados[0].trim(), dados[1].trim());
                }
            }
        } catch (IOException e) {
            System.err.println(">> Erro ao ler departamentos.csv: " + e.getMessage());
        }
        return null;
    }

    public static Curso procurarCurso(String sigla, String pastaBase) {
        String caminho = pastaBase + File.separator + "cursos.csv";
        try (BufferedReader br = new BufferedReader(new FileReader(caminho))) {
            br.readLine();
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.trim().isEmpty()) continue;
                String[] dados = linha.split(";", -1);
                if (dados.length >= 3 && dados[0].trim().equalsIgnoreCase(sigla)) {
                    Departamento dep = procurarDepartamento(dados[2].trim(), pastaBase);
                    return new Curso(dados[0].trim(), dados[1].trim(), dep);
                }
            }
        } catch (IOException e) {
            System.err.println(">> Erro ao ler cursos.csv: " + e.getMessage());
        }
        return null;
    }

    public static Docente procurarDocentePorSigla(String sigla, String pastaBase) {
        String caminho = pastaBase + File.separator + "docentes.csv";
        try (BufferedReader br = new BufferedReader(new FileReader(caminho))) {
            br.readLine();
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.trim().isEmpty()) continue;
                String[] dados = linha.split(";", -1);
                if (dados.length >= 6 && dados[0].trim().equalsIgnoreCase(sigla)) {
                    return new Docente(dados[0].trim(), dados[1].trim(), "", dados[2].trim(), dados[3].trim(), dados[4].trim(), dados[5].trim());
                }
            }
        } catch (IOException e) {
            System.err.println(">> Erro ao ler docentes.csv: " + e.getMessage());
        }
        return null;
    }

    public static Estudante procurarEstudantePorNumMec(int numMec, String pastaBase) {
        String caminho = pastaBase + File.separator + "estudantes.csv";
        try (BufferedReader br = new BufferedReader(new FileReader(caminho))) {
            br.readLine();
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.trim().isEmpty()) continue;
                String[] dados = linha.split(";", -1);

                if (dados.length >= 7) {
                    try {
                        int ficheiroNum = Integer.parseInt(dados[0].trim());
                        if (ficheiroNum == numMec) {
                            int anoInscricao = Integer.parseInt(dados[6].trim());
                            Estudante e = new Estudante(ficheiroNum, dados[1].trim(), "", dados[2].trim(), dados[3].trim(), dados[4].trim(), dados[5].trim(), anoInscricao);
                            carregarDadosAcademicos(e, pastaBase);
                            return e;
                        }
                    } catch (NumberFormatException ex) {
                        // Linha mal formatada, ignorar silenciosamente e tentar a próxima
                    }
                }
            }
        } catch (IOException e) {
            System.err.println(">> Erro ao ler estudantes.csv: " + e.getMessage());
        }
        return null;
    }

    public static UnidadeCurricular procurarUC(String sigla, String pastaBase) {
        String caminho = pastaBase + File.separator + "ucs.csv";
        UnidadeCurricular ucEncontrada = null;

        try (BufferedReader br = new BufferedReader(new FileReader(caminho))) {
            br.readLine();
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.trim().isEmpty()) continue;
                String[] dados = linha.split(";", -1);

                if (dados.length >= 4 && dados[0].trim().equalsIgnoreCase(sigla)) {
                    if (ucEncontrada == null) {
                        try {
                            int ano = Integer.parseInt(dados[2].trim());
                            Docente doc = procurarDocentePorSigla(dados[3].trim(), pastaBase);
                            ucEncontrada = new UnidadeCurricular(dados[0].trim(), dados[1].trim(), ano, doc);
                        } catch (NumberFormatException ex) {
                            System.err.println(">> Erro ao converter ano na UC: " + sigla);
                            continue;
                        }
                    }
                    if (dados.length >= 5 && !dados[4].trim().equalsIgnoreCase("N/A")) {
                        Curso c = procurarCurso(dados[4].trim(), pastaBase);
                        if (c != null) ucEncontrada.adicionarCurso(c);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println(">> Erro ao ler ucs.csv: " + e.getMessage());
        }

        return ucEncontrada;
    }

    public static Estudante[] carregarTodosEstudantes(String pastaBase) {
        Estudante[] lista = new Estudante[500];
        int contador = 0;
        String caminho = pastaBase + File.separator + "estudantes.csv";

        try (BufferedReader br = new BufferedReader(new FileReader(caminho))) {
            br.readLine();
            String linha;
            while ((linha = br.readLine()) != null && contador < lista.length) {
                if (linha.trim().isEmpty()) continue;
                String[] dados = linha.split(";", -1);

                if (dados.length >= 7) {
                    try {
                        int numMec = Integer.parseInt(dados[0].trim());
                        int anoInsc = Integer.parseInt(dados[6].trim());

                        Estudante e = new Estudante(numMec, dados[1].trim(), "", dados[2].trim(),
                                dados[3].trim(), dados[4].trim(), dados[5].trim(), anoInsc);

                        carregarDadosAcademicos(e, pastaBase);
                        lista[contador++] = e;
                    } catch (NumberFormatException ex) {
                        // Ignorar linha mal formatada
                    }
                }
            }
        } catch (IOException e) {
            System.err.println(">> Erro ao ler lista global de estudantes.csv: " + e.getMessage());
        }
        return lista;
    }

    public static void carregarUcsDoDocente(Docente d, String pastaBase) {
        String caminho = pastaBase + File.separator + "ucs.csv";
        try (BufferedReader br = new BufferedReader(new FileReader(caminho))) {
            br.readLine();
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.trim().isEmpty()) continue;
                String[] dados = linha.split(";", -1);

                if (dados.length >= 4 && dados[3].trim().equalsIgnoreCase(d.getSigla())) {
                    try {
                        UnidadeCurricular uc = new UnidadeCurricular(dados[0].trim(), dados[1].trim(), Integer.parseInt(dados[2].trim()), d);
                        d.adicionarUcLecionada(uc);
                    } catch (NumberFormatException ex) {
                        System.err.println(">> Erro na formatação do ano da UC lecionada pelo docente " + d.getSigla());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println(">> Erro ao carregar UCs do docente a partir de ucs.csv: " + e.getMessage());
        }
    }

    private static void carregarDadosAcademicos(Estudante e, String pastaBase) {
        String caminhoInsc = pastaBase + File.separator + "inscricoes.csv";
        if (new File(caminhoInsc).exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(caminhoInsc))) {
                br.readLine();
                String linha;
                while ((linha = br.readLine()) != null) {
                    if (linha.trim().isEmpty()) continue;
                    String[] dados = linha.split(";", -1);
                    if (dados.length >= 2) {
                        try {
                            if (Integer.parseInt(dados[0].trim()) == e.getNumeroMecanografico()) {
                                UnidadeCurricular uc = procurarUC(dados[1].trim(), pastaBase);
                                if (uc != null) e.getPercurso().inscreverEmUc(uc);
                            }
                        } catch (NumberFormatException ex) {}
                    }
                }
            } catch (IOException ex) {
                System.err.println(">> Erro ao ler inscricoes.csv: " + ex.getMessage());
            }
        }

        String caminhoNotas = pastaBase + File.separator + "avaliacoes.csv";
        if (new File(caminhoNotas).exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(caminhoNotas))) {
                br.readLine();
                String linha;
                int anoAtual = Year.now().getValue(); // Evita usar o ano 2026 fixo

                while ((linha = br.readLine()) != null) {
                    if (linha.trim().isEmpty()) continue;
                    String[] dados = linha.split(";", -1);
                    if (dados.length >= 3) {
                        try {
                            if (Integer.parseInt(dados[0].trim()) == e.getNumeroMecanografico()) {
                                UnidadeCurricular uc = procurarUC(dados[1].trim(), pastaBase);
                                if (uc != null) {
                                    Avaliacao av = new Avaliacao(uc, anoAtual);
                                    av.adicionarResultado(Double.parseDouble(dados[2].trim()));
                                    if (dados.length >= 4 && !dados[3].trim().isEmpty()) {
                                        av.adicionarResultado(Double.parseDouble(dados[3].trim()));
                                    }
                                    e.getPercurso().registarAvaliacao(av);
                                }
                            }
                        } catch (NumberFormatException ex) {
                            System.err.println(">> Erro a ler notas do aluno " + e.getNumeroMecanografico() + " nas avaliações.");
                        }
                    }
                }
            } catch (IOException ex) {
                System.err.println(">> Erro ao ler avaliacoes.csv: " + ex.getMessage());
            }
        }
    }
}