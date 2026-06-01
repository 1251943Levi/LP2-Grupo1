package bll;

import dal.*;
import model.*;
import controller.LoginController;
import utils.SegurancaPasswords;

import java.util.List;


/**
 * Ponto único de autenticação no sistema.
 * Valida as credenciais via {@link LoginController} (SQL ou ficheiro conforme
 * config.properties) e constrói o perfil correto consoante o tipo de utilizador.
 */
public class AutenticacaoBLL {

    private static final String PASTA_BD = "bd";
    private final EstudanteDAL estudanteDAL = new EstudanteDAL(PASTA_BD);

    /**
     * Credencial PBKDF2 do administrador de backoffice hardcoded.
     * Mantida como fallback para a conta especial que não está em logins.*.
     */
    private static final String CREDENCIAL_ADMIN =
            "A67KdOiGgwLZQTdjXrCPUg==:1Emuaac5kl+mA0SKMMRX1m+5bpOXaLVPqcttF1EPyG4=";

    private final LoginController loginController = new LoginController();

    /**
     * Autentica um utilizador e devolve o seu perfil completo.
     * Delega a verificação de credenciais no LoginController.
     */
    public Utilizador autenticar(String email, String pass) {
        // conta especial de backoffice (hardcoded, não está em logins.*)
        if (email.equals("backoffice@issmf.ipp.pt")
                && SegurancaPasswords.verificarPassword(pass, CREDENCIAL_ADMIN)) {
            return new Gestor("backoffice@issmf.ipp.pt", CREDENCIAL_ADMIN,
                    "Admin Geral", "123456789", "Sede", "01-01-1980");
        }

        LoginModel login = loginController.autenticar(email, pass);
        if (login == null) return null;

        String hash = login.getPasswordHash();
        switch (login.getTipoUtilizador()) {
            case "ESTUDANTE":
                return new EstudanteBLL().obterPerfilCompleto(email, hash);

            case "DOCENTE":
                Docente d = DocenteDAL.procurarPorEmail(email, hash, PASTA_BD);
                if (d != null) {
                    List<UnidadeCurricular> ucs = UcDAL.obterUcsPorDocente(d, PASTA_BD);
                    ucs.forEach(d::adicionarUcLecionada);
                }
                return d;

            case "GESTOR":
                return GestorDAL.procurarPorEmail(email, hash, PASTA_BD);

            default:
                return null;
        }
    }

    /**
     * Recupera a password de um utilizador delegando na PasswordBLL.
     */
    public boolean recuperarPassword(String email) {
        if (!loginController.existe(email)) return false;
        new PasswordBLL().recuperarPassword(email);
        return true;
    }


    /**
     * Executa o processo de auto-matrícula delegando na MatriculaBLL.
     * @param nome       Nome do novo estudante.
     * @param nif        NIF do novo estudante.
     * @param morada     Morada de residência.
     * @param dataNasc   Data de nascimento (DD-MM-AAAA).
     * @param siglaCurso Sigla do curso escolhido.
     * @param anoAtual   Ano letivo atual.
     * @return Array [email, passwordLimpa] com as credenciais geradas.
     */
    public String[] realizarAutoMatricula(String nome, String nif, String morada,
                                          String dataNasc, String siglaCurso, int anoAtual) {
        return new MatriculaBLL().realizarAutoMatricula(
                nome, nif, morada, dataNasc, siglaCurso, anoAtual);
    }

    /**
     * Verifica se um NIF já está registado no sistema.
     * @param nif NIF a verificar.
     * @return true se o NIF já existir.
     */
    public boolean isNifDuplicado(String nif) {
        return estudanteDAL.existeNif(nif)
                || DocenteDAL.existeNif(nif, PASTA_BD);
    }

    /**
     * Devolve a lista de cursos disponíveis para a auto-matrícula.
     * @return Array "SIGLA - Nome" de todos os cursos.
     */
    public String[] obterListaCursos() {
        return dal.CursoDAL.obterListaCursos(PASTA_BD);
    }
}