package utils;

import model.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ImportadorCSV {

    // ---------- CONSTRUTOR ----------
    private ImportadorCSV() {}

    // ---------- MÉTODOS DE LÓGICA E AÇÃO ----------

    public static void importarDados(String caminho, RepositorioDados repositorio) {
        try (BufferedReader br = new BufferedReader(new FileReader(caminho))) {
            String linha;
            br.readLine();

            while ((linha = br.readLine()) != null) {
                if (linha.trim().isEmpty()) continue;

                String[] dados = linha.split(";");
                String tipo = dados[0].toUpperCase();

                switch (tipo) {

                    case "DEPARTAMENTO":
                        repositorio.adicionarDepartamento(new Departamento(dados[1], dados[2]));
                        break;

                    case "DOCENTE":
                        repositorio.adicionarDocente(new Docente(dados[1], dados[2], dados[3], dados[4], dados[5], dados[6], dados[7]));
                        break;

                    case "CURSO":
                        Departamento dep = procurarDepartamento(dados[3], repositorio);
                        if (dep != null) {
                            Curso novoCurso = new Curso(dados[1], dados[2], dep);
                            if (repositorio.adicionarCurso(novoCurso)) {
                                dep.adicionarCurso(novoCurso);
                            }
                        }
                        break;

                    case "UC":
                        Docente doc = procurarDocente(dados[4], repositorio);
                        Curso cursoUC = procurarCurso(dados[5], repositorio);
                        if (doc != null && cursoUC != null) {
                            UnidadeCurricular novaUc = new UnidadeCurricular(dados[1], dados[2], Integer.parseInt(dados[3]), doc);
                            if (repositorio.adicionarUnidadeCurricular(novaUc)) {
                                cursoUC.adicionarUnidadeCurricular(novaUc);
                                novaUc.adicionarCurso(cursoUC);
                                doc.adicionarUcResponsavel(novaUc);
                                doc.adicionarUcLecionada(novaUc);
                            }
                        }
                        break;

                    case "ESTUDANTE":
                        Curso cursoEst = null;
                        if (dados.length > 9) {
                            cursoEst = procurarCurso(dados[9], repositorio);
                        }

                        Estudante est = new Estudante(
                                Integer.parseInt(dados[1]), dados[2], dados[3], dados[4],
                                dados[5], dados[6], dados[7], Integer.parseInt(dados[8])
                        );

                        if (cursoEst != null && est.getPercurso() != null) {
                            for (int i = 0; i < cursoEst.getTotalUCs(); i++) {
                                UnidadeCurricular uc = cursoEst.getUnidadesCurriculares()[i];
                                if (uc != null && uc.getAnoCurricular() == est.getAnoFrequencia()) {
                                    est.getPercurso().inscreverEmUc(uc);
                                }
                            }
                        }
                        repositorio.adicionarEstudante(est);
                        break;

                    case "AVALIACAO":
                    case "HISTORICO":
                        int numMecHist = Integer.parseInt(dados[1]);
                        String siglaUCHist = dados[2];
                        int anoAvaliacao = Integer.parseInt(dados[3]);

                        Estudante estHist = procurarEstudante(numMecHist, repositorio);
                        UnidadeCurricular ucHist = procurarUC(siglaUCHist, repositorio);

                        if (estHist != null && ucHist != null) {
                            Avaliacao avaliacao = new Avaliacao(ucHist, anoAvaliacao);

                            for (int i = 4; i <= 6 && i < dados.length; i++) {
                                double valorNota = Double.parseDouble(dados[i]);
                                if (valorNota > 0) {
                                    avaliacao.adicionarResultado(valorNota);
                                }
                            }
                            estHist.getPercurso().registarAvaliacao(avaliacao);
                        }
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao ler o ficheiro: " + e.getMessage());
        }
    }

    // ---------- MÉTODOS AUXILIARES PRIVADOS ----------

    private static Departamento procurarDepartamento(String sigla, RepositorioDados repo) {
        for (int i = 0; i < repo.getTotalDepartamentos(); i++) {
            if (repo.getDepartamentos()[i] != null && repo.getDepartamentos()[i].getSigla().equalsIgnoreCase(sigla)) {
                return repo.getDepartamentos()[i];
            }
        }
        return null;
    }

    private static Curso procurarCurso(String sigla, RepositorioDados repo) {
        for (int i = 0; i < repo.getTotalCursos(); i++) {
            if (repo.getCursos()[i] != null && repo.getCursos()[i].getSigla().equalsIgnoreCase(sigla)) {
                return repo.getCursos()[i];
            }
        }
        return null;
    }

    private static Docente procurarDocente(String sigla, RepositorioDados repo) {
        for (int i = 0; i < repo.getTotalDocentes(); i++) {
            if (repo.getDocentes()[i] != null && repo.getDocentes()[i].getSigla().equalsIgnoreCase(sigla)) {
                return repo.getDocentes()[i];
            }
        }
        return null;
    }

    private static Estudante procurarEstudante(int numMec, RepositorioDados repo) {
        for (int i = 0; i < repo.getTotalEstudantes(); i++) {
            if (repo.getEstudantes()[i] != null && repo.getEstudantes()[i].getNumeroMecanografico() == numMec) {
                return repo.getEstudantes()[i];
            }
        }
        return null;
    }

    private static UnidadeCurricular procurarUC(String sigla, RepositorioDados repo) {
        for (int i = 0; i < repo.getTotalUcs(); i++) {
            if (repo.getUcs()[i] != null && repo.getUcs()[i].getSigla().equalsIgnoreCase(sigla)) {
                return repo.getUcs()[i];
            }
        }
        return null;
    }
}