package controller;


import view.MainView;
import model.RepositorioDados;

public class MainController {

    private MainView view;
    private RepositorioDados repositorio;

    public MainController() {
        this.view = new MainView();
        this.repositorio = new RepositorioDados();
    }

    public void iniciarSistema() {
        boolean aExecutar = true;
        view.mostrarMensagem("Bem-vindo ao Sistema do ISSMF!");

        while (aExecutar) {
            int opcao = view.mostrarMenu();

            switch (opcao) {
                case 1:

                    String email = view.pedirInputString("Email").trim();
                    String pass = view.pedirInputString("Password").trim();

                    if ((email.equals("admin@issmf.pt") || email.equals("backoffice@issmf.ipp.pt")) && pass.equals("admin123")) {
                        view.mostrarMensagem("Login de Gestor detetado! A reencaminhar...");

                        model.Gestor admin = new model.Gestor("backoffice@issmf.ipp.pt", "admin123", "Admin Geral", "123456789", "Sede", "01-01-1980");

                        GestorController gestorController = new GestorController(repositorio, admin);
                        gestorController.iniciar();
                        break;
                    }

                    model.Utilizador userLogado = repositorio.autenticar(email, pass);

                    if (userLogado == null) {
                        view.mostrarMensagem("Erro: Credenciais inválidas! (Lembre-se de usar a Opção 3 primeiro)");

                    } else if (userLogado instanceof model.Estudante) {
                        view.mostrarMensagem("Login de Estudante detetado! A reencaminhar...");

                        EstudanteController estudanteController = new EstudanteController(repositorio, (model.Estudante) userLogado);
                        estudanteController.iniciar();

                    } else if (userLogado instanceof model.Docente) {
                        view.mostrarMensagem("Login de Docente detetado! A reencaminhar...");

                        DocenteController docenteController = new DocenteController(repositorio, (model.Docente) userLogado);
                        docenteController.iniciar();
                    }
                    break;
                case 2:
                    view.mostrarMensagem("\n--- NOVO REGISTO DE ESTUDANTE ---");
                    break;
                case 3:
                    view.mostrarMensagem("\n--- IMPORTAÇÃO DE DADOS ---");
                    String nomeIntroduzido = view.pedirInputString("Introduza o nome do ficheiro (ex: dados.csv)");

                    java.io.File ficheiro = new java.io.File(nomeIntroduzido);

                    if (!ficheiro.exists()) {
                        ficheiro = new java.io.File("bd/" + nomeIntroduzido);
                    }

                    if (ficheiro.exists()) {
                        try {
                            utils.ImportadorCSV.importarDados(ficheiro.getPath(), repositorio);
                            view.mostrarMensagem("SUCESSO: Dados importados de " + ficheiro.getPath());
                        } catch (Exception e) {
                            view.mostrarMensagem("FALHA: Erro ao processar o conteúdo do ficheiro.");
                        }
                    } else {
                        view.mostrarMensagem("FALHA: O ficheiro '" + nomeIntroduzido + "' não foi encontrado.");
                    }
                    break;
                case 0:
                    view.mostrarMensagem("A encerrar o sistema...");
                    aExecutar = false;
                    break;
                default:
                    view.mostrarMensagem("Opção inválida.");
            }
        }
    }
}