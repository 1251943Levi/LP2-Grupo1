package utils;

import model.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

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

        try (BufferedReader br = new BufferedReader(new FileReader(caminhoCredenciais))) {
            br.readLine();
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.trim().isEmpty()) continue;
                String[] dados = linha.split(";", -1);

                if (dados[0].trim().equalsIgnoreCase(email)) {
                    hashGuardado = dados[1].trim();
                    tipoUtilizador = dados[2].trim().toUpperCase();
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println(">> Aviso: Ficheiro credenciais.csv não encontrado.");
            return null;
        }

        if (hashGuardado == null || !SegurancaPasswords.verificarPassword(passwordIntroduzida, hashGuardado)) {
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

                if (dados[1].trim().equalsIgnoreCase(email)) {
                    int numMec = Integer.parseInt(dados[0].trim());
                    int anoInscricao = Integer.parseInt(dados[6].trim());
                    Estudante e = new Estudante(numMec, email, hashGuardado, dados[2].trim(), dados[3].trim(), dados[4].trim(), dados[5].trim(), anoInscricao);
                    if (dados.length > 8 && !dados[8].trim().isEmpty()) {
                        e.setSaldoDevedor(Double.parseDouble(dados[8].trim()));
                    }
                    carregarDadosAcademicos(e, pastaBase); // Garante que carrega notas e inscrições
                    return e;
                }
            }
        } catch (IOException e) {}
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

                if (dados[1].trim().equalsIgnoreCase(email)) {
                    Docente d = new Docente(dados[0].trim(), email, hashGuardado, dados[2].trim(), dados[3].trim(), dados[4].trim(), dados[5].trim());
                    carregarUcsDoDocente(d, pastaBase); // Garante que carrega as UCs que ele leciona
                    return d;
                }
            }
        } catch (IOException e) {}
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

                if (dados[0].trim().equalsIgnoreCase(email)) {
                    return new Gestor(email, hashGuardado, dados[1].trim(), dados[2].trim(), dados[3].trim(), dados[4].trim());
                }
            }
        } catch (IOException e) {}
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
                if (dados[0].trim().equalsIgnoreCase(sigla)) {
                    return new Departamento(dados[0].trim(), dados[1].trim());
                }
            }
        } catch (IOException e) {}
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
                if (dados[0].trim().equalsIgnoreCase(sigla)) {
                    Departamento dep = procurarDepartamento(dados[2].trim(), pastaBase);
                    return new Curso(dados[0].trim(), dados[1].trim(), dep);
                }
            }
        } catch (IOException e) {}
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
                if (dados[0].trim().equalsIgnoreCase(sigla)) {
                    return new Docente(dados[0].trim(), dados[1].trim(), "", dados[2].trim(), dados[3].trim(), dados[4].trim(), dados[5].trim());
                }
            }
        } catch (IOException e) {}
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

                int ficheiroNum = Integer.parseInt(dados[0].trim());
                if (ficheiroNum == numMec) {
                    int anoInscricao = Integer.parseInt(dados[6].trim());
                    Estudante e = new Estudante(ficheiroNum, dados[1].trim(), "", dados[2].trim(), dados[3].trim(), dados[4].trim(), dados[5].trim(), anoInscricao);
                    if (dados.length > 8 && !dados[8].trim().isEmpty()) {
                        e.setSaldoDevedor(Double.parseDouble(dados[8].trim()));
                    }
                    carregarDadosAcademicos(e, pastaBase);
                    return e;
                }
            }
        } catch (IOException | NumberFormatException e) {}
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

                if (dados[0].trim().equalsIgnoreCase(sigla)) {
                    if (ucEncontrada == null) {
                        int ano = Integer.parseInt(dados[2].trim());
                        Docente doc = procurarDocentePorSigla(dados[3].trim(), pastaBase);
                        ucEncontrada = new UnidadeCurricular(dados[0].trim(), dados[1].trim(), ano, doc);
                    }
                    if (dados.length > 4 && !dados[4].trim().equalsIgnoreCase("N/A")) {
                        Curso c = procurarCurso(dados[4].trim(), pastaBase);
                        if (c != null) ucEncontrada.adicionarCurso(c);
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {}

        return ucEncontrada;
    }


    // NOVOS MÉTODOS (PARA SUPORTAR ESTATÍSTICAS E CRUZAMENTO DE DADOS)


    /**
     * Carrega todos os estudantes para um array (útil para médias globais).
     */
    public static Estudante[] carregarTodosEstudantes(String pastaBase) {
        Estudante[] lista = new Estudante[500]; // Buffer para até 500 alunos
        int contador = 0;
        String caminho = pastaBase + File.separator + "estudantes.csv";

        try (BufferedReader br = new BufferedReader(new FileReader(caminho))) {
            br.readLine();
            String linha;
            while ((linha = br.readLine()) != null && contador < lista.length) {
                if (linha.trim().isEmpty()) continue;
                String[] dados = linha.split(";", -1);

                int numMec = Integer.parseInt(dados[0].trim());
                int anoInsc = Integer.parseInt(dados[6].trim());

                Estudante e = new Estudante(numMec, dados[1].trim(), "", dados[2].trim(),
                        dados[3].trim(), dados[4].trim(), dados[5].trim(), anoInsc);

                if (dados.length > 8 && !dados[8].trim().isEmpty()) {
                    e.setSaldoDevedor(Double.parseDouble(dados[8].trim()));
                }

                carregarDadosAcademicos(e, pastaBase);
                lista[contador++] = e;
            }
        } catch (Exception e) {}
        return lista;
    }

    /**
     * Preenche as UCs que o docente leciona.
     */
    public static void carregarUcsDoDocente(Docente d, String pastaBase) {
        String caminho = pastaBase + File.separator + "ucs.csv";
        try (BufferedReader br = new BufferedReader(new FileReader(caminho))) {
            br.readLine();
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] dados = linha.split(";", -1);
                if (dados[3].trim().equalsIgnoreCase(d.getSigla())) {
                    UnidadeCurricular uc = new UnidadeCurricular(dados[0].trim(), dados[1].trim(), Integer.parseInt(dados[2].trim()), d);
                    d.adicionarUcLecionada(uc);
                }
            }
        } catch (Exception e) {}
    }

    /**
     * Carrega inscrições e avaliações do aluno.
     */
    private static void carregarDadosAcademicos(Estudante e, String pastaBase) {
        // Carregar Inscrições
        String caminhoInsc = pastaBase + File.separator + "inscricoes.csv";
        if (new File(caminhoInsc).exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(caminhoInsc))) {
                br.readLine();
                String linha;
                while ((linha = br.readLine()) != null) {
                    String[] dados = linha.split(";", -1);
                    if (Integer.parseInt(dados[0].trim()) == e.getNumeroMecanografico()) {
                        UnidadeCurricular uc = procurarUC(dados[1].trim(), pastaBase);
                        if (uc != null) e.getPercurso().inscreverEmUc(uc);
                    }
                }
            } catch (Exception ex) {}
        }

        // Carregar Avaliações (Notas)
        String caminhoNotas = pastaBase + File.separator + "avaliacoes.csv";
        if (new File(caminhoNotas).exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(caminhoNotas))) {
                br.readLine();
                String linha;
                while ((linha = br.readLine()) != null) {
                    String[] dados = linha.split(";", -1);
                    if (Integer.parseInt(dados[0].trim()) == e.getNumeroMecanografico()) {
                        UnidadeCurricular uc = procurarUC(dados[1].trim(), pastaBase);
                        Avaliacao av = new Avaliacao(uc, 2026);
                        av.adicionarResultado(Double.parseDouble(dados[2].trim())); // Nota Normal
                        if (dados.length > 3) av.adicionarResultado(Double.parseDouble(dados[3].trim())); // Recurso
                        e.getPercurso().registarAvaliacao(av);
                    }
                }
            } catch (Exception ex) {}
        }
    }
}