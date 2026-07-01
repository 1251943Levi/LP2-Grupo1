package bll;

import common.ConfigApp;
import dal.CursoDALFile;
import dal.CursoDALSql;
import dal.UcDALFile;
import dal.UcDALSql;

import dal.CursoDAL;
import dal.EstudanteDAL;
import dal.EstudanteDALFile;
import dal.EstudanteDALSql;
import dal.InscricaoDAL;
import dal.InscricaoDALFile;
import dal.InscricaoDALSql;
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

    private static final String PASTA_BD = ConfigApp.PASTA_BD;
    private final CursoDAL cursoDAL = ConfigApp.isModoSql() ? new CursoDALSql() : new CursoDALFile();
    private final UcDAL ucDAL = ConfigApp.isModoSql() ? new UcDALSql() : new UcDALFile();
    // A7: acesso ao módulo do estudante (lazy — evita efeitos colaterais no arranque)
    private EstudanteBLL moduloEstudante;
    private EstudanteBLL moduloEstudante() {
        if (moduloEstudante == null) moduloEstudante = new EstudanteBLL();
        return moduloEstudante;
    }
    private final LoginController loginController = new LoginController();
    private final InscricaoDAL inscricaoDAL =
            ConfigApp.isModoSql() ? new InscricaoDALSql() : new InscricaoDALFile();

    public MatriculaBLL() {
        inscricaoDAL.inicializar();
    }

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

        // A auto-matricula e permitida mesmo com o ano letivo iniciado.
        if (ucDAL.contarUcsPorCursoEAno(siglaCurso, 1, PASTA_BD) == 0) {
            return null;
        }
        int numMec = moduloEstudante().obterProximoNumeroMecanografico(anoAtual);
        String emailInst = EmailGenerator.gerarEmailEstudante(numMec);
        String passLimpa = PasswordGenerator.gerarPasswordSegura();

        Estudante novo = new Estudante(numMec, emailInst, "", nome, nif, morada, dataNasc, anoAtual);

        Curso curso = cursoDAL.procurarCurso(siglaCurso, PASTA_BD);
        if (curso != null) {
            novo.setSaldoDevedor(curso.getValorPropinaAnual());
        }
        novo.setSiglaCurso(siglaCurso);

        moduloEstudante().adicionarEstudante(novo, siglaCurso);

        loginController.criarCredencial(emailInst, passLimpa, "ESTUDANTE");
        for (String siglaUc : ucDAL.obterSiglasUcsPorCursoEAno(siglaCurso, 1, PASTA_BD)) {
            inscricaoDAL.adicionarInscricao(numMec, siglaUc, anoAtual);
        }
        EmailService.enviarCredenciaisTodos(nome, emailInst, passLimpa);

        return new String[]{emailInst, passLimpa};
    }
}