package bll;

import dal.CredencialDAL;
import dal.CursoDAL;
import dal.EstudanteDAL;
import model.Curso;
import model.Estudante;
import utils.EmailGenerator;
import utils.EmailService;
import utils.PasswordGenerator;
import utils.SegurancaPasswords;

/**
 * Lógica de negócio para o processo de auto-matrícula.
 * NÃO referencia ImportadorCSV nem ExportadorCSV.
 */
public class MatriculaBLL {

    private static final String PASTA_BD = "bd";

    /**
     * Executa todo o fluxo de auto-matrícula:
     * gera número mecanográfico, e-mail, password, cria o estudante e persiste tudo.
     */
    public String[] realizarAutoMatricula(String nome, String nif, String morada,
                                          String dataNasc, String siglaCurso, int anoAtual) {
        int numMec = EstudanteDAL.obterProximoNumeroMecanografico(PASTA_BD, anoAtual);
        String emailInst = EmailGenerator.gerarEmailEstudante(numMec);
        String passLimpa = PasswordGenerator.gerarPasswordSegura();
        String passHash = SegurancaPasswords.gerarCredencialMista(passLimpa);

        Estudante novo = new Estudante(numMec, emailInst, passHash, nome, nif, morada, dataNasc, anoAtual);

        Curso curso = CursoDAL.procurarCurso(siglaCurso, PASTA_BD);
        if (curso != null) {
            novo.setSaldoDevedor(curso.getValorPropinaAnual());
        }
        novo.setSiglaCurso(siglaCurso);

        // Persistência separada: dados académicos e credenciais de acesso
        EstudanteDAL.adicionarEstudante(novo, siglaCurso, PASTA_BD);
        CredencialDAL.adicionarCredencial(emailInst, passHash, "ESTUDANTE", PASTA_BD);

        EmailService.enviarCredenciaisTodos(nome, emailInst, passLimpa);

        return new String[]{emailInst, passLimpa};
    }
}
