package utils;

import model.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Utilitário responsável pela leitura e processamento de ficheiros CSV.
 * Esta classe converte dados brutos de texto em objetos (Estudantes, Docentes, ...),
 * garantindo que as dependências entre entidades sejam respeitadas durante a importação.
 */
public class ImportadorCSV {

    /**
     * Construtor privado para impedir a instanciação de uma classe utilitária.
     */
    private ImportadorCSV(){}

    /**
     * Coordena a importação global de todas as entidades do sistema.
     * A ordem de chamada dos métodos respeita a hierarquia de dados (ex: departamentos antes de cursos).
     * @param pastaBase Caminho da pasta que contém os ficheiros CSV.
     * @param repo Repositório de dados onde os objetos criados serão armazenados.
     */
    public static void importarTodos(String pastaBase, RepositorioDados repo){
        String pasta = pastaBase.endsWith(File.separator) ? pastaBase : pastaBase + File.separator;

        importarDepartamentos(pasta + "departamentos.csv", repo);
        importarDocentes(pasta + "docentes.csv", repo);
        importarCursos(pasta + "cursos.csv", repo);
        importarUCs(pasta + "ucs.csv", repo);
        importarEstudantes(pasta + "estudantes.csv", repo);
        importarAvaliacoes(pasta + "avaliacoes.csv", repo);

        System.out.println(">> Importação completa a partir de: " + pasta);
    }

    // --- METODO AUXILIAR DE LEITURA ---

    /**
     * Método auxiliar que centraliza a lógica de leitura de ficheiros.
     * Lê o ficheiro linha a linha, ignora o cabeçalho e divide cada linha em campos usando o separador ';'.
     * @param caminho O caminho completo do ficheiro a ser lido.
     * @return List de arrays de String, onde cada array representa os campos de uma linha.
     */
    private static List<String[]> lerLinhasCSV(String caminho){
        List<String[]> linhasLidas = new ArrayList<>();
        try(BufferedReader leitura = new BufferedReader(new FileReader(caminho))){
            leitura.readLine();
            String linha;
            while ((linha = leitura.readLine()) != null){
                if(!linha.trim().isEmpty()){
                    // O limite -1 preserva campos vazios no final da linha
                    linhasLidas.add(linha.split(";", -1));
                }
            }
        } catch (IOException e){
            System.out.println(">> ERRO: Não foi possível ler: " + caminho);
        }
        return linhasLidas;
    }


    //  -------- METODOS DE IMPORTAÇÃO INDIVIDUAIS --------

    /**
     * Importa departamentos e adiciona-os ao repositório.
     * @param caminho Caminho do ficheiro departamentos.csv.
     * @param repo Destino dos dados.
     */
   public static void importarDepartamentos(String caminho, RepositorioDados repo){
        for(String[] departamento : lerLinhasCSV(caminho)){
            if(departamento.length< 2) continue;
            repo.adicionarDepartamento(new Departamento(departamento[0].trim(), departamento[1].trim()));
        }
        System.out.println(">> Departamentos importados.");
   }

    /**
     * Importa docentes do ficheiro CSV para o repositório.
     * Formato esperado: sigla;email;password;nome;nif;morada;dataNascimento
     * @param caminho Caminho do ficheiro docentes.csv.
     * @param repo Repositório de destino.
     */
   public static void importarDocentes(String caminho, RepositorioDados repo){
        for(String[] docente : lerLinhasCSV(caminho)){
            if(docente.length < 7) continue;
            repo.adicionarDocente(new Docente(
                    docente[0].trim(), docente[1].trim(), docente[2].trim(),
                    docente[3].trim(), docente[4].trim(), docente[5].trim(), docente[6].trim()
            ));
        }
        System.out.println(">> Docentes importados.");
   }

    /**
     * Importa cursos, associando-os ao departamento correspondente já existente no repositório.
     * @param caminho Caminho do ficheiro cursos.csv.
     * @param repo Repositório de destino.
     */
    public static void importarCursos(String caminho, RepositorioDados repo) {
        for (String[] curso : lerLinhasCSV(caminho)) {
            if (curso.length < 3) continue;

            Departamento dep = procurarDepartamento(curso[2].trim(), repo);
            if (dep == null) {
                System.out.println(">> ERRO: Departamento não encontrado para o curso: " + curso[0].trim());
                continue;
            }

            Curso novoCurso = new Curso(curso[0].trim(), curso[1].trim(), dep);
            if (repo.adicionarCurso(novoCurso)) {
                dep.adicionarCurso(novoCurso);
            }
        }
        System.out.println(">> Cursos importados.");
    }

    /**
     * Importa Unidades Curriculares (UCs), vinculando o docente responsável e o curso associado[cite: 19].
     * @param caminho Caminho do ficheiro ucs.csv.
     * @param repo Repositório de destino.
     */
    public static void importarUCs(String caminho, RepositorioDados repo) {
        for (String[] ucs : lerLinhasCSV(caminho)) {
            if (ucs.length < 5) continue;

            Docente doc = procurarDocente(ucs[3].trim(), repo);
            Curso curso = procurarCurso(ucs[4].trim(), repo);

            if (doc == null || curso == null) continue;

            try {
                int anoCurricular = Integer.parseInt(ucs[2].trim());
                UnidadeCurricular novaUc = new UnidadeCurricular(ucs[0].trim(), ucs[1].trim(), anoCurricular, doc);

                if (repo.adicionarUnidadeCurricular(novaUc)) {
                    curso.adicionarUnidadeCurricular(novaUc);
                    novaUc.adicionarCurso(curso);
                    doc.adicionarUcResponsavel(novaUc);
                    doc.adicionarUcLecionada(novaUc);
                }
            } catch (NumberFormatException e) {
                System.out.println(">> ERRO:  Ano curricular inválido na UC: " + ucs[0].trim());
            }
        }
        System.out.println(">> UCs importadas.");
    }

    /**
     * Importa estudantes e inscreve-os automaticamente nas UCs do seu respetivo curso e ano[cite: 20].
     * @param caminho Caminho do ficheiro estudantes.csv.
     * @param repo Repositório de destino.
     */
    public static void importarEstudantes(String caminho, RepositorioDados repo) {
        for (String[] estudante : lerLinhasCSV(caminho)) {
            if (estudante.length < 8) continue;

            try {
                int numMec = Integer.parseInt(estudante[0].trim());
                int anoInscricao = Integer.parseInt(estudante[7].trim());

                Estudante est = new Estudante(
                        numMec, estudante[1].trim(), estudante[2].trim(),
                        estudante[3].trim(), estudante[4].trim(), estudante[5].trim(), estudante[6].trim(),
                        anoInscricao
                );

                if (estudante.length >= 9 && !estudante[8].trim().isEmpty() && !estudante[8].trim().equalsIgnoreCase("N/A")) {
                    Curso cursoEst = procurarCurso(estudante[8].trim(), repo);
                    if (cursoEst != null && est.getPercurso() != null) {
                        for (int i = 0; i < cursoEst.getTotalUCs(); i++) {
                            UnidadeCurricular uc = cursoEst.getUnidadesCurriculares()[i];
                            if (uc != null && uc.getAnoCurricular() == est.getAnoFrequencia()) {
                                est.getPercurso().inscreverEmUc(uc);
                            }
                        }
                    }
                }
                repo.adicionarEstudante(est);

            } catch (NumberFormatException e) {
                System.out.println(">> ERRO: Formato numérico inválido no estudante: " + estudante[3].trim());
            }
        }
        System.out.println(">> Estudantes importados.");
    }

    /**
     * Importa o histórico de avaliações e associa-o ao percurso académico do estudante[cite: 15, 16].
     * @param caminho Caminho do ficheiro avaliacoes.csv.
     * @param repo Repositório de destino.
     */
    public static void importarAvaliacoes(String caminho, RepositorioDados repo) {
        for (String[] avaliacoes : lerLinhasCSV(caminho)) {
            if (avaliacoes.length < 4) continue;

            try {
                int numMec = Integer.parseInt(avaliacoes[0].trim());
                int anoLetivo = Integer.parseInt(avaliacoes[2].trim());

                Estudante est = procurarEstudante(numMec, repo);
                UnidadeCurricular uc = procurarUC(avaliacoes[1].trim(), repo);

                if (est == null || uc == null) continue;

                Avaliacao avaliacao = new Avaliacao(uc, anoLetivo);
                for (int i = 3; i <= 5 && i < avaliacoes.length; i++) {
                    try {
                        double nota = Double.parseDouble(avaliacoes[i].trim());
                        if (nota >= 0) avaliacao.adicionarResultado(nota);
                    } catch (NumberFormatException ignored) {}
                }
                est.getPercurso().registarAvaliacao(avaliacao);

            } catch (NumberFormatException e) {
                System.out.println(">> ERRO: Erro ao ler número mecanográfico ou ano letivo na avaliação.");
            }
        }
        System.out.println(">> Avaliações importadas.");
    }

    // --- MÉTODOS AUXILIARES DE PESQUISA

    /**
     * Procura um Departamento no repositório através da sua sigla.
     * Este método é essencial para validar a existência de um departamento antes de
     * associá-lo a um novo curso
     * @param sigla A sigla identificadora do departamento (ex: "DEI").
     * @param repo  O repositório central onde os dados estão carregados.
     * @return O objeto Departamento correspondente, ou null se não for encontrado.
     */
    private static Departamento procurarDepartamento(String sigla, RepositorioDados repo) {
        for (int i = 0; i < repo.getTotalDepartamentos(); i++) {
            Departamento departamento = repo.getDepartamentos()[i];
            if (departamento != null && departamento.getSigla().equalsIgnoreCase(sigla)) return departamento;
        }
        return null;
    }
    /**
     * Procura um Curso no repositório através da sua sigla.
     * Utilizado durante a importação de Unidades Curriculares e Estudantes para garantir
     * que estes são vinculados a cursos válidos.
     * * @param sigla A sigla identificadora do curso (ex: "LEI").
     * @param repo  O repositório central onde os dados estão carregados.
     * @return O objeto curso correspondente, ou null se não for encontrado.
     */
    private static Curso procurarCurso(String sigla, RepositorioDados repo) {
        for (int i = 0; i < repo.getTotalCursos(); i++) {
            Curso curso = repo.getCursos()[i];
            if (curso != null && curso.getSigla().equalsIgnoreCase(sigla)) return curso;
        }
        return null;
    }

    /**
     * Procura um Docente no repositório através da sua sigla.
     * Garante que uma Unidade Curricular só é importada se tiver um docente
     * responsável previamente registado no sistema.
     * * @param sigla A sigla de 3 letras do docente (atribuída no registo)[cite: 21].
     * @param repo  O repositório central onde os dados estão carregados.
     * @return O objeto Docente correspondente, ou null se não for encontrado.
     */
    private static Docente procurarDocente(String sigla, RepositorioDados repo) {
        for (int i = 0; i < repo.getTotalDocentes(); i++) {
            Docente docente = repo.getDocentes()[i];
            if (docente != null && docente.getSigla().equalsIgnoreCase(sigla)) return docente;
        }
        return null;
    }

    /**
     * Procura um Estudante no repositório através do seu número mecanográfico.
     * Este método é utilizado principalmente na importação de avaliações para identificar
     * o aluno a quem as notas pertencem.
     * * @param numMec O número mecanográfico único do estudante.
     * @param repo   O repositório central onde os dados estão carregados.
     * @return O objeto Estudante correspondente, ou null se não for encontrado.
     */
    private static Estudante procurarEstudante(int numMec, RepositorioDados repo) {
        for (int i = 0; i < repo.getTotalEstudantes(); i++) {
            Estudante estudante = repo.getEstudantes()[i];
            if (estudante != null && estudante.getNumeroMecanografico() == numMec) return estudante;
        }
        return null;
    }

    /**
     * Procura uma Unidade Curricular (UC) no repositório através da sua sigla.
     * Fundamental para o registo de momentos de avaliação, permitindo associar
     * as notas à disciplina correta.
     * * @param sigla A sigla identificadora da UC (ex: "LP2").
     * @param repo  O repositório central onde os dados estão carregados.
     * @return O objeto UnidadeCurricular correspondente, ou nuill se não for encontrado.
     */
    private static UnidadeCurricular procurarUC(String sigla, RepositorioDados repo) {
        for (int i = 0; i < repo.getTotalUcs(); i++) {
            UnidadeCurricular uc = repo.getUcs()[i];
            if (uc != null && uc.getSigla().equalsIgnoreCase(sigla)) return uc;
        }
        return null;
    }
}