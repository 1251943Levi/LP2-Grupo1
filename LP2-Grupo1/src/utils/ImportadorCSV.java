package utils;

import model.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Classe responsável pela importação de dados a partir de ficheiros CSV individuais por entidade.
 * Cada entidade tem o seu próprio ficheiro, garantindo organização e integridade estrutural.
 */
public class ImportadorCSV {

    private ImportadorCSV(){}

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

    private static List<String[]> lerLinhasCSV(String caminho){
        List<String[]> linhasLidas = new ArrayList<>();
        try(BufferedReader leitura = new BufferedReader(new FileReader(caminho))){
            leitura.readLine();
            String linha;
            while ((linha = leitura.readLine()) != null){
                if(!linha.trim().isEmpty()){
                    linhasLidas.add(linha.split(";", -1));
                }
            }
        } catch (IOException e){
            System.out.println(">> ERRO: Não foi possível ler: " + caminho);
        }
        return linhasLidas;
    }


    //  -------- METODOS DE IMPORTAÇÃO INDIVIDUAIS --------

   public static void importarDepartamentos(String caminho, RepositorioDados repo){
        for(String[] departamento : lerLinhasCSV(caminho)){
            if(departamento.length< 2) continue;
            repo.adicionarDepartamento(new Departamento(departamento[0].trim(), departamento[1].trim()));
        }
        System.out.println(">> Departamentos importados.");
   }

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

    private static Departamento procurarDepartamento(String sigla, RepositorioDados repo) {
        for (int i = 0; i < repo.getTotalDepartamentos(); i++) {
            Departamento departamento = repo.getDepartamentos()[i];
            if (departamento != null && departamento.getSigla().equalsIgnoreCase(sigla)) return departamento;
        }
        return null;
    }

    private static Curso procurarCurso(String sigla, RepositorioDados repo) {
        for (int i = 0; i < repo.getTotalCursos(); i++) {
            Curso curso = repo.getCursos()[i];
            if (curso != null && curso.getSigla().equalsIgnoreCase(sigla)) return curso;
        }
        return null;
    }

    private static Docente procurarDocente(String sigla, RepositorioDados repo) {
        for (int i = 0; i < repo.getTotalDocentes(); i++) {
            Docente docente = repo.getDocentes()[i];
            if (docente != null && docente.getSigla().equalsIgnoreCase(sigla)) return docente;
        }
        return null;
    }

    private static Estudante procurarEstudante(int numMec, RepositorioDados repo) {
        for (int i = 0; i < repo.getTotalEstudantes(); i++) {
            Estudante estudante = repo.getEstudantes()[i];
            if (estudante != null && estudante.getNumeroMecanografico() == numMec) return estudante;
        }
        return null;
    }

    private static UnidadeCurricular procurarUC(String sigla, RepositorioDados repo) {
        for (int i = 0; i < repo.getTotalUcs(); i++) {
            UnidadeCurricular uc = repo.getUcs()[i];
            if (uc != null && uc.getSigla().equalsIgnoreCase(sigla)) return uc;
        }
        return null;
    }
}