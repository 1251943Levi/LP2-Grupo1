package controller;

import model.RepositorioDados;
import utils.ExportadorCSV;
import utils.ImportadorCSV;
import view.MainView;
import utils.Validador;
import utils.EmailService;

/**
 * Controlador principal da aplicação ISSMF.
 * Processa a lógica de arranque, autenticação e recuperação de password.
 */
public class MainController {

    private static final String PASTA_BD = "LP2-Grupo1/bd";
    private final MainView view;
    private final RepositorioDados repositorio;

    // Recebe a View no construtor para poder mandar imprimir as mensagens corretas
    public MainController(MainView view) {
        this.view = view;
        this.repositorio = new RepositorioDados();
    }

    public void iniciarSistema() {
        java.io.File pasta = new java.io.File(PASTA_BD);
        if (!pasta.exists() || !pasta.isDirectory()) {
            pasta.mkdirs();
            view.mostrarPastaCriada();
        }
    }

    public void guardarDados() {
        // Nada a fazer aqui. A gravação é feita On-Demand.
    }

    public boolean validarFormatoEmailLogin(String email) {
        boolean isEmailAdmin = email.equals("admin@issmf.pt") || email.equals("backoffice@issmf.ipp.pt");

        if (!isEmailAdmin && !Validador.validarSufixoLogin(email)) {
            view.mostrarErroLoginSufixo();
            return false;
        }
        return true;
    }

    public void processarLogin(String email, String pass) {
        boolean isEmailAdmin = email.equals("admin@issmf.pt") || email.equals("backoffice@issmf.ipp.pt");

        if (!isEmailAdmin && !Validador.validarSufixoLogin(email)) {
            return;
        }

        String credencialAdmin = "A67KdOiGgwLZQTdjXrCPUg==:1Emuaac5kl+mA0SKMMRX1m+5bpOXaLVPqcttF1EPyG4=";

        if (isEmailAdmin && utils.SegurancaPasswords.verificarPassword(pass, credencialAdmin)) {
            view.mostrarLoginGestor();
            model.Gestor admin = new model.Gestor(
                    "backoffice@issmf.ipp.pt", credencialAdmin,
                    "Admin Geral", "123456789", "Sede", "01-01-1980"
            );
            repositorio.setUtilizadorLogado(admin);
            new GestorController(repositorio, admin).iniciar();
            repositorio.limparSessao();
            return;
        }

        model.Utilizador userLogado = ImportadorCSV.autenticarNoFicheiro(email, pass, PASTA_BD);

        if (userLogado == null) {
            view.mostrarCredenciaisInvalidas();

        } else if (userLogado instanceof model.Estudante) {
            view.mostrarLoginEstudante();
            repositorio.setUtilizadorLogado(userLogado);
            new EstudanteController(repositorio, (model.Estudante) userLogado).iniciar();
            repositorio.limparSessao();

        } else if (userLogado instanceof model.Docente) {
            view.mostrarLoginDocente();
            repositorio.setUtilizadorLogado(userLogado);
            new DocenteController(repositorio, (model.Docente) userLogado).iniciar();
            repositorio.limparSessao();
        }
    }

    public void recuperarPassword(String email) {
        if (!Validador.isEmailInstitucionalValido(email)) {
            view.mostrarErroEmailInvalido();
            return;
        }

        String novaPassLimpa  = utils.PasswordGenerator.gerarPasswordSegura();
        String novaPassSegura = utils.SegurancaPasswords.gerarCredencialMista(novaPassLimpa);

        ExportadorCSV.atualizarPasswordCentralizada(email, novaPassSegura, PASTA_BD);
        EmailService.enviarRecuperacaoPassword("Utilizador", email, novaPassLimpa);

        view.mostrarSucessoRecuperacao(email);
    }

    /**
     * Lógica de Auto-matrícula (Mantendo o resto do controlador inalterado).
     */
    public void executarAutoMatricula() {
        view.mostrarTituloAutoMatricula();

        // 1. Validações usando o teu Validador.java
        String nome;
        do {
            nome = view.pedirInputString("Nome Completo");
            if (!utils.Validador.isNomeValido(nome)) view.mostrarErroNomeInvalido();
        } while (!utils.Validador.isNomeValido(nome));

        String nif;
        do {
            nif = view.pedirInputString("NIF");
            if (!utils.Validador.validarNif(nif)) view.mostrarErroNifInvalido();
        } while (!utils.Validador.validarNif(nif));

        String morada = view.pedirInputString("Morada");

        String dataNasc;
        do {
            dataNasc = view.pedirInputString("Data de Nascimento (DD-MM-AAAA)");
            if (!utils.Validador.isDataNascimentoValida(dataNasc)) view.mostrarErroDataInvalida();
        } while (!utils.Validador.isDataNascimentoValida(dataNasc));

        // 2. Seleção de curso (Lógica On-Demand)
        String[] cursos = ImportadorCSV.obterListaCursos(PASTA_BD);
        if (cursos.length == 0) {
            view.mostrarErroSemCursos();
            return;
        }

        view.mostrarListaCursosDisponiveis(cursos);
        int escolha = view.pedirOpcaoCurso(cursos.length);
        if (escolha < 1 || escolha > cursos.length) {
            view.mostrarOpcaoInvalida();
            return;
        }
        String siglaCurso = cursos[escolha - 1].split(" - ")[0];

        // 3. Geração de dados académicos
        int anoAtual = repositorio.getAnoAtual();
        int numMec = ImportadorCSV.obterProximoNumeroMecanografico(PASTA_BD, anoAtual);
        String emailInst = utils.EmailGenerator.gerarEmailEstudante(numMec);
        String passLimpa = utils.PasswordGenerator.gerarPasswordSegura();
        String passHash = utils.SegurancaPasswords.gerarCredencialMista(passLimpa);

        // 4. Gravação direta no CSV
        model.Estudante novo = new model.Estudante(numMec, emailInst, passHash, nome, nif, morada, dataNasc, anoAtual);
        model.Curso curso = ImportadorCSV.procurarCurso(siglaCurso, PASTA_BD);
        if (curso != null) novo.setSaldoDevedor(curso.getValorPropinaAnual());

        ExportadorCSV.adicionarEstudante(novo, PASTA_BD, siglaCurso);

        // 5. Envio de e-mail com credenciais (Requisito)
        EmailService.enviarCredenciaisTodos(nome, emailInst, passLimpa);

        view.mostrarSucessoAutoMatricula(emailInst, passLimpa);
    }
}