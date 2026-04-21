package controller;

import model.*;
import view.MainView;
import bll.MainBLL;
import utils.Validador;
import utils.ImportadorCSV;

/**
 * Controlador principal que orquestra o arranque do sistema, login e auto-matrícula.
 */
public class MainController {

    private static final String PASTA_BD = "bd";
    private final MainView view;
    private final RepositorioDados repositorio;
    private final MainBLL bll;

    public MainController(MainView view) {
        this.view = view;
        this.repositorio = new RepositorioDados();
        this.bll = new MainBLL();
    }

    /**
     * Garante que a estrutura de pastas da base de dados existe.
     */
    public void iniciarSistema() {
        java.io.File pasta = new java.io.File(PASTA_BD);
        if (!pasta.exists()) {
            pasta.mkdirs();
            view.mostrarPastaCriada();
        }
    }

    /**
     * Processa a tentativa de login e redireciona para o controlador específico.
     */
    public void processarLogin(String email, String pass) {
        // Validação de formato de e-mail (UI Concern)
        if (!email.contains("@issmf.pt") && !email.contains("@issmf.ipp.pt") && !Validador.validarSufixoLogin(email)) {
            view.mostrarErroLoginSufixo();
            return;
        }

        // Delegar autenticação para a BLL
        Utilizador user = bll.autenticar(email, pass);

        if (user == null) {
            view.mostrarCredenciaisInvalidas();
            return;
        }

        // Guardar na sessão e abrir menu correspondente
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
     * Gere a recolha de dados para a auto-matrícula e delega a criação para a BLL.
     */
    public void executarAutoMatricula() {
        view.mostrarTituloAutoMatricula();

        // Recolha e Validação de Inputs (Responsabilidade do Controller/View)
        String nome;
        do {
            nome = view.pedirInputString("Nome Completo");
            if (!Validador.isNomeValido(nome)) view.mostrarErroNomeInvalido();
        } while (!Validador.isNomeValido(nome));

        String nif;
        boolean duplicado;
        do {
            nif = view.pedirInputString("NIF");
            duplicado = Validador.isNifDuplicado(nif, PASTA_BD);
            if (!Validador.validarNif(nif)) view.mostrarErroNifInvalido();
            else if (duplicado) view.mostrarErroNifDuplicado();
        } while (!Validador.validarNif(nif) || duplicado);

        String morada = view.pedirInputString("Morada");

        String dataNasc;
        do {
            dataNasc = view.pedirInputString("Data de Nascimento (DD-MM-AAAA)");
            if (!Validador.isDataNascimentoValida(dataNasc)) view.mostrarErroDataInvalida();
        } while (!Validador.isDataNascimentoValida(dataNasc));

        // Seleção de Curso
        String[] cursos = ImportadorCSV.obterListaCursos(PASTA_BD);
        if (cursos.length == 0) {
            view.mostrarErroSemCursos();
            return;
        }

        view.mostrarListaCursosDisponiveis(cursos);
        int escolha = view.pedirOpcaoCurso(cursos.length);
        String siglaCurso = cursos[escolha - 1].split(" - ")[0];

        // Delegar o processamento pesado para a BLL
        String[] credenciais = bll.realizarAutoMatricula(nome, nif, morada, dataNasc, siglaCurso, repositorio.getAnoAtual());

        view.mostrarSucessoAutoMatricula(credenciais[0], credenciais[1]);
    }

    /**
     * Valida se o e-mail tem o formato institucional correto antes de proceder ao login.
     * @param email O e-mail inserido pelo utilizador.
     * @return true se o e-mail for válido ou for uma conta de administração.
     */
    public boolean validarFormatoEmailLogin(String email) {
        // Se for e-mail de admin, passa sempre
        if (email.equals("admin@issmf.pt") || email.equals("backoffice@issmf.ipp.pt")) {
            return true;
        }

        // Caso contrário, usa o validador para verificar o sufixo institucional
        if (!utils.Validador.validarSufixoLogin(email)) {
            view.mostrarErroLoginSufixo();
            return false;
        }
        return true;
    }
}