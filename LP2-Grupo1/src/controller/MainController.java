package controller;

import model.RepositorioDados;
import model.Estudante;
import model.Curso;
import utils.ExportadorCSV;
import utils.ImportadorCSV;
import utils.EmailService;
import utils.Validador;
import utils.EmailGenerator;
import utils.PasswordGenerator;
import utils.SegurancaPasswords;
import view.MainView;

/**
 * Controlador principal da aplicação ISSMF.
 * Processa a lógica de arranque, autenticação e recuperação de password.
 */
public class MainController {

    private static final String PASTA_BD = "bd";
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
     * Lógica de Auto-matrícula com encriptação de password e envio de email.
     * Cumpre os requisitos de segurança e integridade de dados.
     */
    public void executarAutoMatricula() {
        view.mostrarTituloAutoMatricula();

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

            if (!Validador.validarNif(nif)) {
                view.mostrarErroNifInvalido();
            } else if (duplicado) {
                view.mostrarErroNifDuplicado();
            }
        } while (!Validador.validarNif(nif) || duplicado);

        String morada = view.pedirInputString("Morada");

        String dataNasc;
        do {
            dataNasc = view.pedirInputString("Data de Nascimento (DD-MM-AAAA)");
            if (!Validador.isDataNascimentoValida(dataNasc)) view.mostrarErroDataInvalida();
        } while (!Validador.isDataNascimentoValida(dataNasc));

        // 4. Seleção de Curso
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

        // 5. Geração de Dados e Encriptação (Cumpre Requisitos de Segurança)
        int anoAtual = repositorio.getAnoAtual();
        int numMec = ImportadorCSV.obterProximoNumeroMecanografico(PASTA_BD, anoAtual);
        String emailInst = EmailGenerator.gerarEmailEstudante(numMec);

        // Password em texto limpo apenas para o email e visualização final
        String passLimpa = PasswordGenerator.gerarPasswordSegura();
        // Hash gerado para armazenamento seguro na base de dados
        String passHash = SegurancaPasswords.gerarCredencialMista(passLimpa);

        // 6. Criação do Modelo e Atribuição de Propinas
        Estudante novo = new Estudante(numMec, emailInst, passHash, nome, nif, morada, dataNasc, anoAtual);

        Curso curso = ImportadorCSV.procurarCurso(siglaCurso, PASTA_BD);
        if (curso != null) {
            // Atribui o valor da propina anual configurado no curso ao saldo devedor do aluno
            novo.setSaldoDevedor(curso.getValorPropinaAnual());
        }

        // 7. Persistência de Dados no CSV
        ExportadorCSV.adicionarEstudante(novo, PASTA_BD, siglaCurso);

        // 8. Envio de Credenciais
        EmailService.enviarCredenciaisTodos(nome, emailInst, passLimpa);

        view.mostrarSucessoAutoMatricula(emailInst, passLimpa);
    }
}