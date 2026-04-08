package controller;

import model.RepositorioDados;
import utils.ExportadorCSV;
import utils.ImportadorCSV;
import view.MainView;

/**
 * Controlador principal da aplicação ISSMF.
 * Adaptado para Arquitetura Relacional com Lazy Loading (On-Demand).
 * Já não carrega arrays de dados para a memória no arranque.
 */
public class MainController {

    private static final String PASTA_BD = "LP2-Grupo1/bd";
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
     * Processa o login do utilizador validando diretamente nos ficheiros .csv.
     */
    public boolean processarLogin(String email, String pass, boolean aExecutar) {
        String credencialAdmin = "A67KdOiGgwLZQTdjXrCPUg==:1Emuaac5kl+mA0SKMMRX1m+5bpOXaLVPqcttF1EPyG4=";
        boolean isEmailAdmin = email.equals("admin@issmf.pt") || email.equals("backoffice@issmf.ipp.pt");

        // 1. Validação do Admin Estático
        if (isEmailAdmin && utils.SegurancaPasswords.verificarPassword(pass, credencialAdmin)) {
            view.mostrarMensagem("Login de Gestor detetado!");
            model.Gestor admin = new model.Gestor("backoffice@issmf.ipp.pt", credencialAdmin, "Admin Geral", "123456789", "Sede", "01-01-1980");
            repositorio.setUtilizadorLogado(admin); // Guarda na sessão
            new GestorController(repositorio, admin).iniciar();
            repositorio.limparSessao();
            return aExecutar;
        }

        // 2. Validação On-Demand (consulta o ficheiro credenciais.csv)
        model.Utilizador userLogado = ImportadorCSV.autenticarNoFicheiro(email, pass, PASTA_BD);

        if (userLogado == null) {
            view.mostrarMensagem("Credenciais inválidas ou utilizador não encontrado.");
        } else if (userLogado instanceof model.Estudante) {
            view.mostrarMensagem("Login de Estudante detetado!");
            repositorio.setUtilizadorLogado(userLogado);
            new EstudanteController(repositorio, (model.Estudante) userLogado).iniciar();
            repositorio.limparSessao(); // Limpa a RAM no logout
        } else if (userLogado instanceof model.Docente) {
            view.mostrarMensagem("Login de Docente detetado!");
            repositorio.setUtilizadorLogado(userLogado);
            new DocenteController(repositorio, (model.Docente) userLogado).iniciar();
            repositorio.limparSessao(); // Limpa a RAM no logout
        }

        return aExecutar;
    }

    /**
     * Recupera a palavra-passe atualizando diretamente a tabela de credenciais.
     */
    public void recuperarPassword(String email) {
        // Gera a password aleatória (limpa e segura com Hash)
        String novaPassLimpa = utils.PasswordGenerator.gerarPasswordSegura();
        String novaPassSegura = utils.SegurancaPasswords.gerarCredencialMista(novaPassLimpa);

        // Atualiza diretamente no ficheiro "credenciais.csv" sem carregar arrays!
        ExportadorCSV.atualizarPasswordCentralizada(email, novaPassSegura, PASTA_BD);

        view.mostrarMensagem("Processo de recuperação de conta registado no sistema.");

       // aguarda implementação de modulo de email
        view.mostrarMensagem("[A aguardar módulo de email] -> Email pronto a ser despachado para: " + email);
        view.mostrarMensagem("A password temporária enviada por email será: " + novaPassLimpa);
    }
}