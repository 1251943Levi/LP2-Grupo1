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
                    return new Estudante(numMec, email, hashGuardado, dados[2].trim(), dados[3].trim(), dados[4].trim(), dados[5].trim(), anoInscricao);
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
                    return new Docente(dados[0].trim(), email, hashGuardado, dados[2].trim(), dados[3].trim(), dados[4].trim(), dados[5].trim());
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
                    return new Estudante(ficheiroNum, dados[1].trim(), "", dados[2].trim(), dados[3].trim(), dados[4].trim(), dados[5].trim(), anoInscricao);
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

    /**
     * Lê o ficheiro de estudantes e calcula o próximo número mecanográfico disponível
     * com base no ano letivo atual (Prefixo = Ano, Sufixo = 4 dígitos sequenciais).
     */
    public static int obterProximoNumeroMecanografico(String pastaBase, int anoAtual) {
        String caminho = pastaBase + File.separator + "estudantes.csv";
        int maxSufixo = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(caminho))) {
            br.readLine(); // Ignorar o cabeçalho
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.trim().isEmpty()) continue;
                String[] dados = linha.split(";", -1);

                try {
                    int numAtual = Integer.parseInt(dados[0].trim());

                    // Verifica se o número do estudante pertence ao ano atual (ex: 2026)
                    // Fazemos isto dividindo por 10000 (ex: 20260004 / 10000 = 2026)
                    if (numAtual / 10000 == anoAtual) {
                        // Extrai apenas os últimos 4 dígitos (ex: 20260004 % 10000 = 4)
                        int sufixo = numAtual % 10000;
                        if (sufixo > maxSufixo) {
                            maxSufixo = sufixo;
                        }
                    }
                } catch (NumberFormatException e) {
                    // Ignora se a primeira coluna não for número
                }
            }
        } catch (IOException e) {
            // Se o ficheiro não existir, o maxSufixo continuará a 0
        }

        // Constrói o novo número: (Ano * 10000) + Próximo Sufixo
        // Exemplo: (2026 * 10000) + 5 = 20260005
        return (anoAtual * 10000) + (maxSufixo + 1);
    }
    /**
     * Lê o ficheiro de cursos e devolve um array de strings com o formato "SIGLA - Nome do Curso".
     */
    public static String[] obterListaCursos(String pastaBase) {
        String caminho = pastaBase + java.io.File.separator + "cursos.csv";
        int count = 0;

        // Primeira passagem para contar o número de cursos (para criar o array com o tamanho certo)
        try (BufferedReader br = new BufferedReader(new FileReader(caminho))) {
            br.readLine(); // Ignorar o cabeçalho
            while (br.readLine() != null) count++;
        } catch (IOException e) {
            return new String[0]; // Retorna array vazio se não houver ficheiro
        }

        String[] cursos = new String[count];

        // Segunda passagem para extrair a sigla e o nome
        try (BufferedReader br = new BufferedReader(new FileReader(caminho))) {
            br.readLine();
            String linha;
            int i = 0;
            while ((linha = br.readLine()) != null) {
                if (linha.trim().isEmpty()) continue;
                String[] dados = linha.split(";", -1);

                // dados[0] é a Sigla, dados[1] é o Nome
                cursos[i] = dados[0].trim() + " - " + dados[1].trim();
                i++;
            }
        } catch (IOException e) {
            System.out.println(">> AVISO: Não foi possível ler o ficheiro de cursos.");
        }

        return cursos;
    }

    /**
     * Conta quantas UCs existem num curso e ano específicos.
     */
    public static int contarUcsPorCursoEAno(String siglaCurso, int anoCurricular, String pastaBase) {
        String caminho = pastaBase + java.io.File.separator + "ucs.csv";
        int contagem = 0;

        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(caminho))) {
            br.readLine();
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.trim().isEmpty()) continue;
                String[] dados = linha.split(";", -1);

                if (dados.length >= 5) {
                    try {
                        int anoUc = Integer.parseInt(dados[2].trim());
                        String cursoAssociado = dados[4].trim();

                        if (anoUc == anoCurricular && cursoAssociado.equalsIgnoreCase(siglaCurso)) {
                            contagem++;
                        }
                    } catch (NumberFormatException ex) {}
                }
            }
        } catch (java.io.IOException e) {}
        return contagem;
    }

    /**
     * Verifica se existe algum estudante associado à sigla do curso.
     */
    public static boolean existeEstudanteNoCurso(String siglaCurso, String pastaBase) {
        String caminho = pastaBase + java.io.File.separator + "estudantes.csv";
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(caminho))) {
            br.readLine();
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.trim().isEmpty()) continue;
                String[] dados = linha.split(";", -1);

                if (dados.length >= 8) {
                    if (dados[7].trim().equalsIgnoreCase(siglaCurso)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {}
        return false;
    }

// Listagens para os CRUDs

    public static String listarTodasUcs(String pastaBase) {
        String caminho = pastaBase + java.io.File.separator + "ucs.csv";
        StringBuilder sb = new StringBuilder("\n--- LISTA DE UNIDADES CURRICULARES ---\n");
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(caminho))) {
            br.readLine(); // Ignorar cabeçalho
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.trim().isEmpty()) continue;
                String[] dados = linha.split(";", -1);
                sb.append("Sigla: ").append(dados[0]).append(" | Nome: ").append(dados[1])
                        .append(" | Ano: ").append(dados[2]).append(" | Docente: ").append(dados[3])
                        .append(" | Curso: ").append(dados[4]).append("\n");
            }
        } catch (Exception e) { return ">> Erro ao ler ficheiro de UCs."; }
        return sb.toString();
    }

    public static String listarTodosCursos(String pastaBase) {
        String caminho = pastaBase + java.io.File.separator + "cursos.csv";
        StringBuilder sb = new StringBuilder("\n--- LISTA DE CURSOS ---\n");
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(caminho))) {
            br.readLine();
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.trim().isEmpty()) continue;
                String[] dados = linha.split(";", -1);
                sb.append("Sigla: ").append(dados[0]).append(" | Nome: ").append(dados[1])
                        .append(" | Departamento: ").append(dados[2]).append("\n");
            }
        } catch (Exception e) { return ">> Erro ao ler ficheiro de Cursos."; }
        return sb.toString();
    }
// ==========================================
    // MÉTODOS PARA ESTATÍSTICAS
    // ==========================================

    /**
     * Carrega todos os estudantes da base de dados e os seus respetivos históricos de avaliação.
     * Utilizado para o cálculo de Estatísticas.
     */
    public static Estudante[] carregarTodosEstudantes(String pastaBase) {
        String caminho = pastaBase + java.io.File.separator + "estudantes.csv";
        int count = 0;

        // 1. Contar quantos estudantes existem para inicializar o array
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(caminho))) {
            br.readLine();
            while (br.readLine() != null) count++;
        } catch (java.io.IOException e) {
            return new Estudante[0];
        }

        Estudante[] estudantes = new Estudante[count];

        // 2. Carregar os estudantes
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(caminho))) {
            br.readLine();
            String linha;
            int i = 0;
            while ((linha = br.readLine()) != null) {
                if (linha.trim().isEmpty()) continue;
                String[] dados = linha.split(";", -1);

                int numMec = Integer.parseInt(dados[0].trim());
                int anoInsc = Integer.parseInt(dados[6].trim());
                Estudante e = new Estudante(numMec, dados[1].trim(), "", dados[2].trim(), dados[3].trim(), dados[4].trim(), dados[5].trim(), anoInsc);

                // 3. Carregar as notas deste estudante
                carregarAvaliacoesDoEstudante(e, pastaBase);

                estudantes[i] = e;
                i++;
            }
        } catch (Exception e) {
            System.out.println(">> ERRO: Problema ao ler os estudantes para as estatísticas.");
        }

        return estudantes;
    }

    /**
     * Vai ao ficheiro avaliacoes.csv e carrega as notas para o percurso do estudante para que a média seja calculada.
     */
    private static void carregarAvaliacoesDoEstudante(Estudante e, String pastaBase) {
        String caminho = pastaBase + java.io.File.separator + "avaliacoes.csv";
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(caminho))) {
            br.readLine();
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.trim().isEmpty()) continue;
                String[] dados = linha.split(";", -1);

                // Se a nota pertencer a este aluno, adicionamos ao percurso dele
                if (Integer.parseInt(dados[0].trim()) == e.getNumeroMecanografico()) {
                    UnidadeCurricular uc = procurarUC(dados[1].trim(), pastaBase);
                    if (uc != null) {
                        int anoLetivo = Integer.parseInt(dados[2].trim());
                        Avaliacao av = new Avaliacao(uc, anoLetivo);

                        double n1 = Double.parseDouble(dados[3].trim());
                        double n2 = Double.parseDouble(dados[4].trim());
                        double n3 = Double.parseDouble(dados[5].trim());

                        // Só regista se a nota for válida (>= 0)
                        if (n1 >= 0) av.adicionarResultado(n1);
                        if (n2 >= 0) av.adicionarResultado(n2);
                        if (n3 >= 0) av.adicionarResultado(n3);

                        e.getPercurso().registarAvaliacao(av);
                    }
                }
            }
        } catch (Exception ex) {}
    }

}
