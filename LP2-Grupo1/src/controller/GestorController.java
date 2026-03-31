package controller;

import model.Gestor;
import model.Estudante;
import model.RepositorioDados;
import view.GestorView;
import utils.EmailGenerator;
import utils.PasswordGenerator;
import utils.Validador;

public class GestorController {
    private RepositorioDados repo;
    private Gestor gestor;
    private GestorView view;

    public GestorController(RepositorioDados repo, Gestor gestor) {
        this.repo = repo;
        this.gestor = gestor;
        this.view = new GestorView();
    }

    public void iniciar() {
        boolean correr = true;
        while (correr) {
            int opcao = view.mostrarMenu();
            switch (opcao) {
                case 1:
                    view.mostrarMensagem("\n--- REGISTAR ESTUDANTE ---");
                    int numMec = Integer.parseInt(view.pedirInput("Nº Mecanográfico"));
                    String nome = view.pedirInput("Nome");

                    String nif;
                    do {
                        nif = view.pedirInput("NIF (9 dígitos)");
                    } while (!Validador.validarNif(nif));

                    String morada = view.pedirInput("Morada");
                    String dataNasc = view.pedirInput("Data Nasc. (DD/MM/AAAA)");
                    int anoInscricao = Integer.parseInt(view.pedirInput("Ano de Inscrição"));

                    // GERAÇÃO AUTOMÁTICA!
                    String email = EmailGenerator.gerarEmailEstudante(numMec);
                    String pass = PasswordGenerator.gerarPassword();

                    Estudante novo = new Estudante(numMec, email, pass, nome, nif, morada, dataNasc, anoInscricao);
                    if (repo.adicionarEstudante(novo)) {
                        view.mostrarMensagem("Sucesso! Email: " + email + " | Password: " + pass);
                    }
                    break;
                case 2:
                    view.mostrarMensagem("Avançar Ano Letivo - Em desenvolvimento para o próximo sprint.");
                    break;
                case 0:
                    correr = false; break;
            }
        }
    }
}

