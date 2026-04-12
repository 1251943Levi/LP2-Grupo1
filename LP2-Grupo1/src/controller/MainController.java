package controller;

import model.RepositorioDados;
import utils.ExportadorCSV;
import utils.ImportadorCSV;
import view.MainView;
import utils.Validador;
import utils.EmailService;

/**
 * Controlador principal da aplicação ISSMF.
 * Adaptado para Arquitetura Relacional com Lazy Loading (On-Demand).
 * Já não carrega arrays de dados para a memória no arranque.
 */
public class MainController {

    private static final String PASTA_BD = "bd";
    private final MainView view;

    /** Agora o repositório serve apenas para manter a sessão (saber quem está logado) */
    private final RepositorioDados repositorio;

    public MainController(MainView view) {
        this.view = view;
        this.repositorio = new RepositorioDados();
    }

    /**
     * Inicia o sistema.
     * Como usamos Lazy Loading, apenas garantimos que a pasta de Base de Dados existe.
     * Já não existe o "ImportadorCSV.importarTodos()".
     */
    public void iniciarSistema() {
        java.io.File pasta = new java.io.File(PASTA_BD);
        if (!pasta.exists() || !pasta.isDirectory()) {
            pasta.mkdirs();
            view.mostrarMensagem(">> Pasta de base de dados criada.");
        }
    }

    /**
     * O método guardarDados geral fica vazio porque a gravação é feita
     * automaticamente (On-Demand) pelos outros controladores quando há alterações.
     */
    public void guardarDados() {
        // Nada a fazer aqui.
    }

    /**
     * Processa o login do utilizador com validação de domínio institucional.
     * A barreira de e-mail é a primeira instrução — falha imediata se o domínio
     * não for reconhecido, sem chegar sequer a consultar o ficheiro de credenciais.
     */
    public boolean processarLogin(String email, String pass, boolean aExecutar) {

        boolean isEmailAdmin = email.equals("admin@issmf.pt") || email.equals("backoffice@issmf.ipp.pt");

        if (!isEmailAdmin && !Validador.validarSufixoLogin(email)) {
            view.mostrarMensagem("ERRO DE LOGIN: O endereço de e-mail tem de conter obrigatoriamente o sufixo '@issmf.ipp.pt'.");
            return aExecutar;
        }

        String  credencialAdmin = "A67KdOiGgwLZQTdjXrCPUg==:1Emuaac5kl+mA0SKMMRX1m+5bpOXaLVPqcttF1EPyG4=";

        if (isEmailAdmin && utils.SegurancaPasswords.verificarPassword(pass, credencialAdmin)) {
            view.mostrarMensagem("Login de Gestor detetado!");
            model.Gestor admin = new model.Gestor(
                    "backoffice@issmf.ipp.pt", credencialAdmin,
                    "Admin Geral", "123456789", "Sede", "01-01-1980"
            );
            repositorio.setUtilizadorLogado(admin);
            new GestorController(repositorio, admin).iniciar();
            repositorio.limparSessao();
            return aExecutar;
        }

        model.Utilizador userLogado =
                ImportadorCSV.autenticarNoFicheiro(email, pass, PASTA_BD);

        if (userLogado == null) {
            view.mostrarMensagem("Credenciais inválidas ou utilizador não encontrado.");

        } else if (userLogado instanceof model.Estudante) {
            view.mostrarMensagem("Login de Estudante detetado!");
            repositorio.setUtilizadorLogado(userLogado);
            new EstudanteController(repositorio, (model.Estudante) userLogado).iniciar();
            repositorio.limparSessao();

        } else if (userLogado instanceof model.Docente) {
            view.mostrarMensagem("Login de Docente detetado!");
            repositorio.setUtilizadorLogado(userLogado);
            new DocenteController(repositorio, (model.Docente) userLogado).iniciar();
            repositorio.limparSessao();
        }

        return aExecutar;
    }

    /**
     * Recupera a palavra-passe: gera uma nova, envia-a por e-mail
     * e persiste o hash no ficheiro de credenciais.
     * A password NUNCA é impressa na consola.
     */
    public void recuperarPassword(String email) {

        if (!Validador.isEmailInstitucionalValido(email)) {
            view.mostrarMensagem("E-mail inválido: o domínio não é reconhecido pelo sistema.");
            return;
        }

        String novaPassLimpa  = utils.PasswordGenerator.gerarPasswordSegura();
        String novaPassSegura = utils.SegurancaPasswords.gerarCredencialMista(novaPassLimpa);

        ExportadorCSV.atualizarPasswordCentralizada(email, novaPassSegura, PASTA_BD);

        EmailService.enviarRecuperacaoPassword("Utilizador", email, novaPassLimpa);

        novaPassLimpa = null;

        view.mostrarMensagem("Processo de recuperação concluído.");
        view.mostrarMensagem("[SISTEMA] Credenciais enviadas por e-mail para: " + email);
    }
}