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
                    String email = view.pedirInputString("Email");
                    String pass = view.pedirInputString("Password");

                    // 2. Hack Provisório para a Pessoa 2 (Gestor) conseguir trabalhar!
                    // Como ainda não há ficheiros CSV, deixamos o admin entrar por uma "porta das traseiras"
                    if (email.equals("admin@issmf.pt") && pass.equals("admin123")) {
                        view.mostrarMensagem("Login de Gestor detetado! A reencaminhar...");
                        // new controller.GestorController(repositorio, new model.Gestor("admin@issmf.pt", "admin123", "Admin", "000000000", "Sede", "01/01/1980")).iniciar();
                        break;
                    }

                    // 3. Autenticar Estudantes e Docentes normais
                    model.Utilizador userLogado = repositorio.autenticar(email, pass);

                    if (userLogado == null) {
                        view.mostrarMensagem("Erro: Credenciais inválidas!");

                    } else if (userLogado instanceof model.Estudante) {
                        view.mostrarMensagem("Login de Estudante detetado! A reencaminhar...");
                        // new controller.EstudanteController(repositorio, (model.Estudante) userLogado).iniciar();

                    } else if (userLogado instanceof model.Docente) {
                        view.mostrarMensagem("Login de Docente detetado! A reencaminhar...");
                        // new controller.DocenteController(repositorio, (model.Docente) userLogado).iniciar();
                    }
                    break;
                case 2:
                    view.mostrarMensagem("\n--- NOVO REGISTO DE ESTUDANTE ---");
                    // A chamada para criarEstudanteSemLogin() entra noutro commit
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