package Controller;

import View.MainView;

public class MainController {

    // ---------- ATRIBUTOS ----------
    private MainView view;
    private RepositorioDados repositorio;

    // ---------- CONSTRUTOR ----------
    public MainController() {
        this.view = new MainView();
        this.repositorio = new RepositorioDados();


}
