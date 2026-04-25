package bll;

import dal.*;
import model.*;
import utils.SegurancaPasswords;

import java.util.List;


/**
 * Ponto único de autenticação e arranque de sessão.
 * Valida credenciais, hidrata o perfil completo do utilizador
 * e delega a auto-matrícula à MatriculaBLL.
 *
 * NÃO referencia ImportadorCSV nem ExportadorCSV.
 */
public class AutenticacaoBLL {

    private static final String PASTA_BD = "bd";

    private static final String CREDENCIAL_ADMIN =
            "A67KdOiGgwLZQTdjXrCPUg==:1Emuaac5kl+mA0SKMMRX1m+5bpOXaLVPqcttF1EPyG4=";


    /**
     * Autentica um utilizador validando as credenciais e devolvendo o perfil correto.
     * Para estudantes, hidrata completamente o percurso académico (inscrições + notas).
     * Para docentes, associa as UCs lecionadas.
     *
     * @return O objeto Utilizador (Estudante/Docente/Gestor) ou null se inválido.
     */
    public Utilizador autenticar(String email, String pass) {
        boolean isEmailAdmin = email.equals("admin@issmf.pt")
                || email.equals("backoffice@issmf.ipp.pt");
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

    public boolean recuperarPassword(String email) {
        String[] creds = CredencialDAL.obterCredenciais(email, PASTA_BD);
        if (creds == null) return false;
        new PasswordBLL().recuperarPassword(email);
        return true;
    }
    /**
     * Delega o processo de auto-matrícula à MatriculaBLL.
     * @return String[] com [0] = email gerado, [1] = password em texto limpo.
     */
    public String[] realizarAutoMatricula(String nome, String nif, String morada,
                                          String dataNasc, String siglaCurso, int anoAtual) {
        return new MatriculaBLL().realizarAutoMatricula(
                nome, nif, morada, dataNasc, siglaCurso, anoAtual);
    }

    public boolean isNifDuplicado(String nif) {
        return EstudanteDAL.existeNif(nif, PASTA_BD)
                || DocenteDAL.existeNif(nif, PASTA_BD);
    }
}