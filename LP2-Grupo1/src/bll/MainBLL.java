package bll;

import model.*;
import utils.*;

/**
 * Camada de Lógica de Negócio para o fluxo principal do sistema.
 * Centraliza processos de autenticação, recuperação de credenciais e
 * a lógica complexa de auto-matrícula de estudantes.
 */
public class MainBLL {

    private static final String PASTA_BD = "bd";

    /**
     * Tenta autenticar um utilizador com base nas credenciais fornecidas.
     * Verifica primeiro se é um administrador de sistema e, caso contrário,
     * procura nos registos de utilizadores (CSV).
     * * @param email E-mail introduzido.
     * @param pass Password em texto limpo.
     * @return Utilizador autenticado (Gestor, Estudante ou Docente) ou null se falhar.
     */
    public Utilizador autenticar(String email, String pass) {
        // 1. Verificação de conta de administração (Backoffice)
        String credencialAdmin = "A67KdOiGgwLZQTdjXrCPUg==:1Emuaac5kl+mA0SKMMRX1m+5bpOXaLVPqcttF1EPyG4=";
        boolean isEmailAdmin = email.equals("admin@issmf.pt") || email.equals("backoffice@issmf.ipp.pt");

        if (isEmailAdmin && SegurancaPasswords.verificarPassword(pass, credencialAdmin)) {
            return new Gestor(
                    "backoffice@issmf.ipp.pt", credencialAdmin,
                    "Admin Geral", "123456789", "Sede", "01-01-1980"
            );
        }

        // 2. Verificação nos ficheiros CSV via Importador
        return ImportadorCSV.autenticarNoFicheiro(email, pass, PASTA_BD);
    }

    /**
     * Processa a recuperação de password: gera uma nova, encripta e atualiza a BD.
     * * @param email E-mail do utilizador que esqueceu a password.
     */
    public void recuperarPassword(String email) {
        String novaPassLimpa = PasswordGenerator.gerarPasswordSegura();
        String novaPassSegura = SegurancaPasswords.gerarCredencialMista(novaPassLimpa);

        ExportadorCSV.atualizarPasswordCentralizada(email, novaPassSegura, PASTA_BD);
        EmailService.enviarRecuperacaoPassword("Utilizador", email, novaPassLimpa);
    }

    /**
     * Executa a lógica de negócio para a auto-matrícula de um novo estudante.
     * Gera e-mail, password encriptada, atribui propinas e persiste os dados.
     * * @return Um array de String contendo [emailGerado, passwordLimpa] para exibição final.
     */
    public String[] realizarAutoMatricula(String nome, String nif, String morada, String dataNasc, String siglaCurso, int anoAtual) {
        // Gerar dados automáticos
        int numMec = ImportadorCSV.obterProximoNumeroMecanografico(PASTA_BD, anoAtual);
        String emailInst = EmailGenerator.gerarEmailEstudante(numMec);
        String passLimpa = PasswordGenerator.gerarPasswordSegura();
        String passHash = SegurancaPasswords.gerarCredencialMista(passLimpa);

        // Criar modelo
        Estudante novo = new Estudante(numMec, emailInst, passHash, nome, nif, morada, dataNasc, anoAtual);

        // Atribuir propina com base no curso
        Curso curso = ImportadorCSV.procurarCurso(siglaCurso, PASTA_BD);
        if (curso != null) {
            novo.setSaldoDevedor(curso.getValorPropinaAnual());
        }

        // Persistência
        ExportadorCSV.adicionarEstudante(novo, PASTA_BD, siglaCurso);
        EmailService.enviarCredenciaisTodos(nome, emailInst, passLimpa);

        return new String[]{emailInst, passLimpa};
    }
}