package utils;

import model.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class ExportadorCSV {

    // ---------- CONSTRUTOR ----------
    private ExportadorCSV() {}

    // ---------- MÉTODOS DE LÓGICA E AÇÃO ----------
    public static void exportarDados(String caminho, RepositorioDados repo) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(caminho))) {
            pw.println("Tipo;Coluna1;Coluna2;Coluna3;Coluna4;Coluna5;Coluna6;Coluna7;Coluna8;Coluna9");

            for (int i = 0; i < repo.getTotalDepartamentos(); i++) {
                Departamento d = repo.getDepartamentos()[i];
                if (d != null) {
                    pw.println("DEPARTAMENTO;" + d.getSigla() + ";" + d.getNome());
                }
            }

            for (int i = 0; i < repo.getTotalDocentes(); i++) {
                Docente d = repo.getDocentes()[i];
                if (d != null) {
                    pw.println("DOCENTE;" + d.getSigla() + ";" + d.getEmail() + ";" + d.getPassword() + ";" +
                            d.getNome() + ";" + d.getNif() + ";" + d.getMorada() + ";" + d.getDataNascimento());
                }
            }

            for (int i = 0; i < repo.getTotalCursos(); i++) {
                Curso c = repo.getCursos()[i];
                if (c != null) {
                    String siglaDep = (c.getDepartamento() != null) ? c.getDepartamento().getSigla() : "N/A";
                    pw.println("CURSO;" + c.getSigla() + ";" + c.getNome() + ";" + siglaDep);
                }
            }

            for (int i = 0; i < repo.getTotalUcs(); i++) {
                UnidadeCurricular uc = repo.getUcs()[i];
                if (uc != null) {
                    String siglaDocente = (uc.getDocenteResponsavel() != null) ? uc.getDocenteResponsavel().getSigla() : "N/A";
                    String siglaCursoUC = (uc.getTotalCursos() > 0 && uc.getCursos()[0] != null) ? uc.getCursos()[0].getSigla() : "N/A";

                    pw.println("UC;" + uc.getSigla() + ";" + uc.getNome() + ";" + uc.getAnoCurricular() + ";" +
                            siglaDocente + ";" + siglaCursoUC);
                }
            }

            for (int i = 0; i < repo.getTotalEstudantes(); i++) {
                Estudante e = repo.getEstudantes()[i];
                if (e != null) {
                    String siglaCurso = "N/A";

                    pw.println("ESTUDANTE;" + e.getNumeroMecanografico() + ";" + e.getEmail() + ";" + e.getPassword() + ";" +
                            e.getNome() + ";" + e.getNif() + ";" + e.getMorada() + ";" + e.getDataNascimento() + ";" +
                            e.getAnoPrimeiraInscricao() + ";" + siglaCurso);
                }
            }

            for (int i = 0; i < repo.getTotalEstudantes(); i++) {
                Estudante e = repo.getEstudantes()[i];
                if (e != null && e.getPercurso() != null) {
                    PercursoAcademico pa = e.getPercurso();
                    for (int j = 0; j < pa.getTotalAvaliacoes(); j++) {
                        Avaliacao av = pa.getHistoricoAvaliacoes()[j];
                        if (av != null && av.getUc() != null) {
                            double[] notas = av.getResultados();

                            pw.println("AVALIACAO;" + e.getNumeroMecanografico() + ";" + av.getUc().getSigla() + ";" +
                                    av.getAnoLetivo() + ";" + notas[0] + ";" + notas[1] + ";" + notas[2]);
                        }
                    }
                }
            }

            System.out.println(">> Dados exportados com sucesso para: " + caminho);
        } catch (IOException e) {
            System.err.println("Erro ao exportar dados: " + e.getMessage());
        }
    }
}