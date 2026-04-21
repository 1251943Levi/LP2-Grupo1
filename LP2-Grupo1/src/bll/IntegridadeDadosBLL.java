package bll;

import utils.ImportadorCSV;
import utils.ExportadorCSV;

/**
 * Camada de lógica para validações complexas de integridade que cruzam múltiplos ficheiros.
 */
public class IntegridadeBLL {

    /**
     * Valida se um curso pode receber mais uma UC (Limite de 5 por ano).
     */
    public boolean podeAdicionarUc(String siglaCurso, int ano, String pastaBase) {
        int ucsAtuais = ImportadorCSV.contarUcsPorCursoEAno(siglaCurso, ano, pastaBase);
        return ucsAtuais < 5;
    }

    /**
     * Impede a edição/remoção de cursos que já tenham atividade (alunos ou UCs).
     */
    public boolean podeEditarCurso(String siglaCurso, String pastaBD) {
        int totalAlunos = ExportadorCSV.contarEstudantesPorCursoEAno(siglaCurso, 1, pastaBD) +
                ExportadorCSV.contarEstudantesPorCursoEAno(siglaCurso, 2, pastaBD) +
                ExportadorCSV.contarEstudantesPorCursoEAno(siglaCurso, 3, pastaBD);

        if (totalAlunos > 0) return false;

        String ucs = ImportadorCSV.listarUcsPorCurso(siglaCurso, pastaBD);
        boolean temUcs = ucs != null && !ucs.contains("Não existem") && !ucs.contains("Não foram encontradas");

        return !temUcs;
    }
}