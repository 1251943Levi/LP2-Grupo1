package dal;

import common.ConfigApp;
import dal.db.ConnectionManager;

import java.io.File;
import java.util.List;

/**
 * Implementação SQL Server de {@link CursoUcDAL} (Card 2).
 * Tabela [curso_uc] com PK composta (siglaCurso, siglaUC, anoLetivo).
 * Inicialização: cria a tabela se não existir e importa de curso_uc.csv se vazia.
 */
public class CursoUcDALSql implements CursoUcDAL {

    private static final String TABELA = "curso_uc";
    private final ConnectionManager cm;

    public CursoUcDALSql() { this(new ConnectionManager()); }
    public CursoUcDALSql(ConnectionManager cm) { this.cm = cm; }

    @Override
    public void inicializar() {
        if (!cm.existeTabela(TABELA)) {
            cm.executarScript(
                    "CREATE TABLE [curso_uc] (\n"
                    + "    siglaCurso    NVARCHAR(10) NOT NULL REFERENCES [curso](sigla),\n"
                    + "    siglaUC       NVARCHAR(10) NOT NULL,\n"
                    + "    anoCurricular INT          NOT NULL,\n"
                    + "    anoLetivo     INT          NOT NULL,\n"
                    + "    PRIMARY KEY (siglaCurso, siglaUC, anoLetivo)\n"
                    + ");\n");
        }
        if (contar() == 0) importarDeCsv();
    }

    private int contar() {
        List<Integer> r = cm.select("SELECT COUNT(*) AS t FROM [curso_uc]", rs -> rs.getInt("t"));
        return r.isEmpty() ? 0 : r.get(0);
    }

    @Override
    public boolean existeAssociacao(String siglaCurso, String siglaUC, int anoLetivo) {
        List<Integer> r = cm.select(
                "SELECT COUNT(*) AS t FROM [curso_uc] WHERE siglaCurso = ? AND siglaUC = ? AND anoLetivo = ?",
                rs -> rs.getInt("t"), siglaCurso, siglaUC, anoLetivo);
        return !r.isEmpty() && r.get(0) > 0;
    }

    @Override
    public boolean associar(String siglaCurso, String siglaUC, int anoCurricular, int anoLetivo) {
        if (siglaCurso == null || siglaUC == null) return false;
        if (existeAssociacao(siglaCurso, siglaUC, anoLetivo)) return false; // rejeita duplicado
        cm.update("INSERT INTO [curso_uc] (siglaCurso, siglaUC, anoCurricular, anoLetivo) VALUES (?, ?, ?, ?)",
                siglaCurso.trim(), siglaUC.trim(), anoCurricular, anoLetivo);
        return true;
    }

    @Override
    public boolean removerAssociacao(String siglaCurso, String siglaUC, int anoLetivo) {
        return cm.update("DELETE FROM [curso_uc] WHERE siglaCurso = ? AND siglaUC = ? AND anoLetivo = ?",
                siglaCurso, siglaUC, anoLetivo) > 0;
    }

    @Override
    public List<String> obterCursosPorUc(String siglaUC, int anoLetivo) {
        return cm.select("SELECT siglaCurso FROM [curso_uc] WHERE siglaUC = ? AND anoLetivo = ?",
                rs -> rs.getString("siglaCurso"), siglaUC, anoLetivo);
    }

    @Override
    public List<String> obterSiglasUcsPorCursoEAno(String siglaCurso, int anoCurricular, int anoLetivo) {
        return cm.select(
                "SELECT siglaUC FROM [curso_uc] WHERE siglaCurso = ? AND anoCurricular = ? AND anoLetivo = ?",
                rs -> rs.getString("siglaUC"), siglaCurso, anoCurricular, anoLetivo);
    }

    @Override
    public int contarUcsPorCursoEAno(String siglaCurso, int anoCurricular, int anoLetivo) {
        List<Integer> r = cm.select(
                "SELECT COUNT(*) AS t FROM [curso_uc] WHERE siglaCurso = ? AND anoCurricular = ? AND anoLetivo = ?",
                rs -> rs.getInt("t"), siglaCurso, anoCurricular, anoLetivo);
        return r.isEmpty() ? 0 : r.get(0);
    }

    private void importarDeCsv() {
        String caminho = ConfigApp.PASTA_BD + File.separator + "curso_uc.csv";
        int total = 0;
        for (String linha : DALUtil.lerFicheiro(caminho)) {
            if (linha.toLowerCase().startsWith("siglacurso;")) continue;
            String[] d = linha.split(";", -1);
            if (d.length >= 4) {
                try {
                    associar(d[0].trim(), d[1].trim(),
                            Integer.parseInt(d[2].trim()), Integer.parseInt(d[3].trim()));
                    total++;
                } catch (NumberFormatException ignored) {}
            }
        }
        if (total > 0) System.out.println(">> Migração: " + total + " associação(ões) curso-UC importada(s) para SQL.");
    }
}
