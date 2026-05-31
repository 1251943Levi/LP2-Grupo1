package bll;

import dal.CursoDAL;
import dal.EstudanteDAL;
import dal.InscricaoDAL;
import dal.UcDAL;
import model.Curso;
import model.Estudante;
import controller.LoginController;
import utils.EmailGenerator;
import utils.EmailService;
import utils.PasswordGenerator;

/**
 * Lógica de negócio do processo de auto-matrícula de novos estudantes.
 * Executa toda a sequência de criação de conta: geração do número mecanográfico,
 * email e password; persistência dos dados e credenciais; inscrição automática
 * nas UCs do 1.º ano do curso; e envio das credenciais por email.
 */
public class MatriculaBLL {

    private static final String PASTA_BD = "bd";
    private final LoginController loginController = new LoginController();

    /**
     * Realiza o processo completo de auto-matrícula de um novo estudante.
     * @param nome       Nome completo do estudante.
     * @param nif        NIF com 9 dígitos.
     * @param morada     Morada de residência.
     * @param dataNasc   Data de nascimento (DD-MM-AAAA).
     * @param siglaCurso Sigla do curso em que o estudante se matricula.
     * @param anoAtual   Ano letivo atual, usado para gerar o número mecanográfico.
     * @return Array [email, passwordLimpa] com as credenciais geradas.
     */
    public String[] realizarAutoMatricula(String nome, String nif, String morada,
                                          String dataNasc, String siglaCurso, int anoAtual) {

        if (UcDAL.contarUcsPorCursoEAno(siglaCurso, 1, PASTA_BD) == 0) {
            return null;
        }
        int numMec = EstudanteDAL.obterProximoNumeroMecanografico(PASTA_BD, anoAtual);
        String emailInst = EmailGenerator.gerarEmailEstudante(numMec);
        String passLimpa = PasswordGenerator.gerarPasswordSegura();

        Estudante novo = new Estudante(numMec, emailInst, "", nome, nif, morada, dataNasc, anoAtual);

        Curso curso = CursoDAL.procurarCurso(siglaCurso, PASTA_BD);
        if (curso != null) {
            novo.setSaldoDevedor(curso.getValorPropinaAnual());
        }
        novo.setSiglaCurso(siglaCurso);

        EstudanteDAL.adicionarEstudante(novo, siglaCurso, PASTA_BD);
        loginController.criarCredencial(emailInst, passLimpa, "ESTUDANTE");
        for (String siglaUc : UcDAL.obterSiglasUcsPorCursoEAno(siglaCurso, 1, PASTA_BD)) {
            InscricaoDAL.adicionarInscricao(numMec, siglaUc, anoAtual, PASTA_BD);
        }
        EmailService.enviarCredenciaisTodos(nome, emailInst, passLimpa);

        return new String[]{emailInst, passLimpa};
    }
}