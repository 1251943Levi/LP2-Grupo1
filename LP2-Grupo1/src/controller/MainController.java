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
                    view.mostrarMensagem("\n--- LOGIN DO SISTEMA ---");
                    break;
                case 2:
                    view.mostrarMensagem("\n--- NOVO REGISTO DE ESTUDANTE ---");
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