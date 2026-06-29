package dal;

import common.ConfigApp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação em ficheiro (CSV) de {@link CursoUcDAL}.
 * Ficheiro: curso_uc.csv — colunas: siglaCurso;siglaUC;anoCurricular;anoLetivo.
 */
public class CursoUcDALFile implements CursoUcDAL {

    private static final String NOME_FICHEIRO = "curso_uc.csv";
    private static final String CABECALHO = "siglaCurso;siglaUC;anoCurricular;anoLetivo";

    private String caminho() {
        return ConfigApp.PASTA_BD + File.separator + NOME_FICHEIRO;
    }

    @Override
    public void inicializar() {
        DALUtil.garantirFicheiroECabecalho(caminho(), CABECALHO);
    }

    @Override
    public boolean existeAssociacao(String siglaCurso, String siglaUC, int anoLetivo) {
        for (String linha : DALUtil.lerFicheiro(caminho())) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] d = linha.split(";", -1);
            if (d.length >= 4
                    && d[0].trim().equalsIgnoreCase(siglaCurso)
                    && d[1].trim().equalsIgnoreCase(siglaUC)) {
                try { if (Integer.parseInt(d[3].trim()) == anoLetivo) return true; }
                catch (NumberFormatException ignored) {}
            }
        }
        return false;
    }

    @Override
    public boolean associar(String siglaCurso, String siglaUC, int anoCurricular, int anoLetivo) {
        if (siglaCurso == null || siglaUC == null) return false;
        if (existeAssociacao(siglaCurso, siglaUC, anoLetivo)) return false; // rejeita duplicado
        DALUtil.garantirFicheiroECabecalho(caminho(), CABECALHO);
        DALUtil.adicionarLinhaCSV(caminho(),
                siglaCurso.trim() + ";" + siglaUC.trim() + ";" + anoCurricular + ";" + anoLetivo);
        return true;
    }

    @Override
    public boolean removerAssociacao(String siglaCurso, String siglaUC, int anoLetivo) {
        List<String> linhas = DALUtil.lerFicheiro(caminho());
        List<String> novas = new ArrayList<>();
        boolean removeu = false;
        for (String linha : linhas) {
            if (linha.equalsIgnoreCase(CABECALHO)) { novas.add(linha); continue; }
            String[] d = linha.split(";", -1);
            boolean alvo = d.length >= 4
                    && d[0].trim().equalsIgnoreCase(siglaCurso)
                    && d[1].trim().equalsIgnoreCase(siglaUC);
            if (alvo) {
                try { if (Integer.parseInt(d[3].trim()) == anoLetivo) { removeu = true; continue; } }
                catch (NumberFormatException ignored) {}
            }
            novas.add(linha);
        }
        if (removeu) DALUtil.reescreverFicheiro(caminho(), novas);
        return removeu;
    }

    @Override
    public List<String> obterCursosPorUc(String siglaUC, int anoLetivo) {
        List<String> cursos = new ArrayList<>();
        for (String linha : DALUtil.lerFicheiro(caminho())) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] d = linha.split(";", -1);
            if (d.length >= 4 && d[1].trim().equalsIgnoreCase(siglaUC)) {
                try { if (Integer.parseInt(d[3].trim()) == anoLetivo) cursos.add(d[0].trim()); }
                catch (NumberFormatException ignored) {}
            }
        }
        return cursos;
    }

    @Override
    public List<String> obterSiglasUcsPorCursoEAno(String siglaCurso, int anoCurricular, int anoLetivo) {
        List<String> ucs = new ArrayList<>();
        for (String linha : DALUtil.lerFicheiro(caminho())) {
            if (linha.equalsIgnoreCase(CABECALHO)) continue;
            String[] d = linha.split(";", -1);
            if (d.length >= 4 && d[0].trim().equalsIgnoreCase(siglaCurso)) {
                try {
                    if (Integer.parseInt(d[2].trim()) == anoCurricular
                            && Integer.parseInt(d[3].trim()) == anoLetivo) {
                        ucs.add(d[1].trim());
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return ucs;
    }

    @Override
    public int contarUcsPorCursoEAno(String siglaCurso, int anoCurricular, int anoLetivo) {
        return obterSiglasUcsPorCursoEAno(siglaCurso, anoCurricular, anoLetivo).size();
    }
}
