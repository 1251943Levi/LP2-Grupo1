package controller;

import dal.CursoDAL;
import model.*;
import view.MainView;
import bll.AutenticacaoBLL;
import utils.Validador;

/**
 * Controlador principal que orquestra o arranque do sistema, login e auto-matrícula.
 */
public class MainController {

    private static final String PASTA_BD = "bd";
    private final MainView view;
    private final RepositorioDados repositorio;
    private final AutenticacaoBLL bll;

    public MainController(MainView view) {
        this.view = view;
        this.repositorio = new RepositorioDados();
        this.bll = new AutenticacaoBLL();
    }

    /**
     * Garante que a estrutura de pastas da base de dados existe.
     */
    public void iniciarSistema() {
        java.io.File pasta = new java.io.File(PASTA_BD);
        if (!pasta.exists() && pasta.mkdirs()) {
            view.mostrarPastaCriada();
        }
    }

    /**
     * Processa a tentativa de login e redireciona para o controlador específico.
     */
    public void processarLogin(String email, String pass) {
        if (!email.contains("@issmf.pt")
                && !email.contains("@issmf.ipp.pt")
                && !Validador.validarSufixoLogin(email)) {
            view.mostrarErroLoginSufixo();
            return;
        }

        Utilizador user = bll.autenticar(email, pass);

        if (user == null) {
            view.mostrarCredenciaisInvalidas();
            return;
        }

        repositorio.setUtilizadorLogado(user);

        if (user instanceof Gestor) {
            view.mostrarLoginGestor();
            new GestorController(repositorio, (Gestor) user).iniciar();
        } else if (user instanceof Estudante) {
            view.mostrarLoginEstudante();
            new EstudanteController(repositorio, (Estudante) user).iniciar();
        } else if (user instanceof Docente) {
            view.mostrarLoginDocente();
            new DocenteController(repositorio, (Docente) user).iniciar();
        } else {
            view.mostrarCredenciaisInvalidas();
        }

        repositorio.limparSessao();
    }

    /**
     * Gere o fluxo de recuperação de password.
     */
    public void recuperarPassword(String email) {
        if (!Validador.isEmailInstitucionalValido(email)) {
            view.mostrarErroEmailInvalido();
            return;
        }
        bll.recuperarPassword(email);
        view.mostrarSucessoRecuperacao(email);
    }

    /**
     * Gere a recolha de dados para a auto-matrícula e delega o processamento à BLL.
     * A lista de cursos disponíveis é obtida diretamente via CursoDAL.
     */
    public void executarAutoMatricula() {
        view.mostrarTituloAutoMatricula();

        String nome;
        do {
            nome = view.pedirInputString("Nome Completo");
            if (!Validador.isNomeValido(nome)) view.mostrarErroNomeInvalido();
        } while (!Validador.isNomeValido(nome));

        String nif;
        do {
            nif = view.pedirInputString("NIF");
            if (!Validador.validarNif(nif)) view.mostrarErroNifInvalido();
        } while (!Validador.validarNif(nif));

        String morada = view.pedirInputString("Morada");

        String dataNasc;
        do {
            dataNasc = view.pedirInputString("Data de Nascimento (DD-MM-AAAA)");
            if (!Validador.isDataNascimentoValida(dataNasc)) view.mostrarErroDataInvalida();
        } while (!Validador.isDataNascimentoValida(dataNasc));

        String[] cursos = CursoDAL.obterListaCursos(PASTA_BD);
        if (cursos.length == 0) {
            view.mostrarErroSemCursos();
            return;
        }

        view.mostrarListaCursosDisponiveis(cursos);
        int escolha = view.pedirOpcaoCurso(cursos.length);
        String siglaCurso = cursos[escolha - 1].split(" - ")[0];

        String[] credenciais = bll.realizarAutoMatricula(
                nome, nif, morada, dataNasc, siglaCurso, repositorio.getAnoAtual());

        view.mostrarSucessoAutoMatricula(credenciais[0]);
    }

    /**
     * Valida se o e-mail tem o formato institucional correto antes de proceder ao login.
     */
    public boolean validarFormatoEmailLogin(String email) {
        if (email.equals("admin@issmf.pt") || email.equals("backoffice@issmf.ipp.pt")) {
            return true;
        }
        if (!Validador.validarSufixoLogin(email)) {
            view.mostrarErroLoginSufixo();
            return false;
        }
        return true;
    }
}