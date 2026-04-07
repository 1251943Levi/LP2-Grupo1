package controller;

import model.RepositorioDados;
import utils.ExportadorCSV;
import utils.ImportadorCSV;
import view.MainView;

/**
 * Controlador principal da aplicação ISSMF.
 * Atua como o intermediário entre a interface de utilizador (View) e os dados (Model),
 * gerindo o ciclo de vida da aplicação, a autenticação e a persistência de ficheiros.
 */
public class MainController {

    /** Caminho da pasta onde os ficheiros CSV são armazenados. */
    private static final String PASTA_BD = "LP2-Grupo1/bd";

    /** Referência para a vista principal para interação com o utilizador. */
    private final MainView view;

    /** Repositório central que armazena os dados em memória durante a execução. */
    private final RepositorioDados repositorio;

    /**
     * Construtor do MainController.
     * Inicializa o repositório de dados e vincula o controlador à sua respetiva View.
     * @param view A instância da MainView que comunica com este controlador.
     */
    public MainController(MainView view) {
        this.view = view;
        this.repositorio = new RepositorioDados();
    }

    /**
     * Executa as tarefas de inicialização do sistema.
     * Atualmente, foca-se no carregamento de dados a partir do sistema de ficheiros.
     */
    public void iniciarSistema() {
        carregarDadosIniciais();
    }

    /**
     * Tenta importar todos os dados dos ficheiros CSV localizados na pasta base.
     * Caso a pasta não exista, informa o utilizador através da View.
     */
    public void carregarDadosIniciais() {
        java.io.File pasta = new java.io.File(PASTA_BD);
        if (pasta.exists() && pasta.isDirectory()) {
            ImportadorCSV.importarTodos(PASTA_BD, repositorio);
        } else {
            view.mostrarMensagem(">> Pasta '" + PASTA_BD + "/' não encontrada. Sistema iniciado sem dados.");
        }
    }

    /**
     * Exporta o estado atual do repositório em memória para ficheiros CSV físicos.
     * Garante a persistência dos dados entre sessões.
     */
    public void guardarDados() {
        ExportadorCSV.exportarTodos(PASTA_BD, repositorio);
    }

    /**
     * Gere o processo de autenticação de utilizadores.
     * Valida credenciais administrativas estáticas ou consulta o repositório para
     * utilizadores dinâmicos (Estudantes e Docentes), redirecionando para o respetivo sub-controlador.
     * @param email Email introduzido pelo utilizador.
     * @param pass Palavra-chave introduzida pelo utilizador.
     * @param aExecutar Estado atual do ciclo principal da aplicação.
     * @return boolean O novo estado do ciclo (false se o sistema for encerrado).
     */
    public boolean processarLogin(String email, String pass, boolean aExecutar) {
        String credencialAdmin = "A67KdOiGgwLZQTdjXrCPUg==:1Emuaac5kl+mA0SKMMRX1m+5bpOXaLVPqcttF1EPyG4=";

        boolean isEmailAdmin = email.equals("admin@issmf.pt") || email.equals("backoffice@issmf.ipp.pt");

        if (isEmailAdmin && utils.SegurancaPasswords.verificarPassword(pass, credencialAdmin)) {

            view.mostrarMensagem("Login de Gestor detetado!");
            model.Gestor admin = new model.Gestor(
                    "backoffice@issmf.ipp.pt", credencialAdmin, "Admin Geral", "123456789", "Sede", "01-01-1980"
            );
            new GestorController(repositorio, admin).iniciar();
            guardarDados();
            return aExecutar;
        }

        model.Utilizador userLogado = repositorio.autenticar(email, pass);

        if (userLogado == null) {
            view.mostrarMensagem("Credenciais inválidas.");
        } else if (userLogado instanceof model.Estudante) {
            view.mostrarMensagem("Login de Estudante detetado!");
            new EstudanteController(repositorio, (model.Estudante) userLogado).iniciar();
            guardarDados();
        } else if (userLogado instanceof model.Docente) {
            view.mostrarMensagem("Login de Docente detetado!");
            new DocenteController(repositorio, (model.Docente) userLogado).iniciar();
            guardarDados();
        }

        return aExecutar;
    }

    /**
     * Gere o processo de recuperação de palavra-passe.
     * @param email Email do utilizador a recuperar.
     */
    public void recuperarPassword(String email) {
        model.Utilizador userEncontrado = null;

        for (int i = 0; i < repositorio.getTotalEstudantes(); i++) {
            if (repositorio.getEstudantes()[i] != null &&
                    repositorio.getEstudantes()[i].getEmail().equalsIgnoreCase(email)) {
                userEncontrado = repositorio.getEstudantes()[i];
                break;
            }
        }

        if (userEncontrado == null) {
            for (int i = 0; i < repositorio.getTotalDocentes(); i++) {
                if (repositorio.getDocentes()[i] != null &&
                        repositorio.getDocentes()[i].getEmail().equalsIgnoreCase(email)) {
                    userEncontrado = repositorio.getDocentes()[i];
                    break;
                }
            }
        }

        if (userEncontrado != null) {
            String novaPassLimpa = utils.PasswordGenerator.gerarPasswordSegura();
            String novaPassSegura = utils.SegurancaPasswords.gerarCredencialMista(novaPassLimpa);

            userEncontrado.setPassword(novaPassSegura);
            guardarDados();

            view.mostrarMensagem("Processo de recuperação concluído e dados guardados.");

            // Implementação de modulo de envio de email
            view.mostrarMensagem("[A aguardar módulo de email] -> Email pronto a ser despachado para: " + email);

        } else {
            view.mostrarMensagem("O email introduzido não foi encontrado no sistema.");
        }
    }
}