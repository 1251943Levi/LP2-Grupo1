package bll;

import dal.*;
import model.*;
import utils.SegurancaPasswords;

import java.util.List;


/**
 * Ponto único de autenticação no sistema.
 * Valida as credenciais, constrói o perfil correto consoante o tipo
 * de utilizador e inicia a sessão.
 */
public class AutenticacaoBLL {

    private static final String PASTA_BD = "bd";

    /**
     * Credencial PBKDF2 pré-gerada para o administrador de backoffice.
     * Permite acesso sem registo em ficheiro.
     */
    private static final String CREDENCIAL_ADMIN =
            "A67KdOiGgwLZQTdjXrCPUg==:1Emuaac5kl+mA0SKMMRX1m+5bpOXaLVPqcttF1EPyG4=";

    /**
     * Autentica um utilizador e devolve o seu perfil completo.
     * @param email Email institucional introduzido.
     * @param pass  Password em texto limpo a verificar.
     * @return O Utilizador do tipo correto, ou null se as credenciais forem inválidas.
     */
    public Utilizador autenticar(String email, String pass) {
        boolean isEmailAdmin = email.equals("backoffice@issmf.ipp.pt");
        if (isEmailAdmin && SegurancaPasswords.verificarPassword(pass, CREDENCIAL_ADMIN)) {
            return new Gestor("backoffice@issmf.ipp.pt", CREDENCIAL_ADMIN,
                    "Admin Geral", "123456789", "Sede", "01-01-1980");
        }

        String[] creds = CredencialDAL.obterCredenciais(email, PASTA_BD);
        if (creds == null || !SegurancaPasswords.verificarPassword(pass, creds[0])) return null;

        String tipo = creds[1];
        switch (tipo) {
            case "ESTUDANTE":
                return new EstudanteBLL().obterPerfilCompleto(email, creds[0]);

            case "DOCENTE":
                Docente d = DocenteDAL.procurarPorEmail(email, creds[0], PASTA_BD);
                if (d != null) {
                    List<UnidadeCurricular> ucs = UcDAL.obterUcsPorDocente(d, PASTA_BD);
                    ucs.forEach(d::adicionarUcLecionada);
                }
                return d;

            case "GESTOR":
                return GestorDAL.procurarPorEmail(email, creds[0], PASTA_BD);

            default:
                return null;
        }
    }

    /**
     * Recupera a password de um utilizador delegando na PasswordBLL.
     * @param email Email do utilizador que esqueceu a password.
     * @return true se o email existir no sistema.
     */
    public boolean recuperarPassword(String email) {
        String[] creds = CredencialDAL.obterCredenciais(email, PASTA_BD);
        if (creds == null) return false;
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
        return EstudanteDAL.existeNif(nif, PASTA_BD)
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