package controller;

import model.Gestor;
import model.Estudante;
import model.RepositorioDados;
import utils.*;
import view.GestorView;

/**
 * Controlador responsável por gerir as ações e o fluxo do utilizador com perfil de Gestor.
 * Atua como intermediário entre a GestorView e os dados persistidos no disco (CSV),
 * suportando a nova arquitetura relacional e "On-Demand".
 */
public class GestorController {

    /** Repositório usado agora apenas para manter o estado da sessão atual. */
    private RepositorioDados repo;

    /** O objeto Gestor que tem a sessão iniciada. */
    private Gestor gestor;

    /** A interface de visualização específica para o Gestor. */
    private GestorView view;

    /** Caminho base para a pasta da base de dados. */
    private static final String PASTA_BD = "bd";

    /**
     * Construtor do controlador do Gestor.
     * @param repo O repositório centralizado que guarda a sessão ativa.
     * @param gestor O utilizador Gestor atualmente autenticado.
     */
    public GestorController(RepositorioDados repo, Gestor gestor) {
        this.repo = repo;
        this.gestor = gestor;
        this.view = new GestorView();
    }

    /**
     * Inicia o ciclo principal de execução do painel do Gestor.
     * Apresenta o menu, recolhe as opções e executa as ações de negócio,
     * como registar novos estudantes guardando-os diretamente nos ficheiros CSV.
     */
    public void iniciar() {
        boolean correr = true;
        while (correr) {
            int opcao = view.mostrarMenu();
            switch (opcao) {
                case 1:
                    view.mostrarMensagem("\n--- REGISTAR ESTUDANTE ---");

                    // 1. Vai buscar o ano letivo atual ao Repositório!
                    int anoInscricao = repo.getAnoAtual();

                    // 2. Gera o número passando o ano atual para criar o prefixo correto
                    int numMec = ImportadorCSV.obterProximoNumeroMecanografico(PASTA_BD, anoInscricao);

                    view.mostrarMensagem("Ano de Inscrição automático: " + anoInscricao);
                    view.mostrarMensagem("Nº Mecanográfico automático: " + numMec);

                    // Validação do Nome
                    String nome;
                    do {
                        nome = view.pedirNome();
                        if (!Validador.isNomeValido(nome)) view.mostrarErroNomeInvalido();
                    } while (!Validador.isNomeValido(nome));

                    // Validação do NIF
                    String nif;
                    do {
                        nif = view.pedirNif();
                        if (!Validador.isNifValido(nif)) view.mostrarErroNifInvalido();
                    } while (!Validador.isNifValido(nif));

                    String morada = view.pedirInput("Morada");

                    // Validação da Data de Nascimento
                    String dataNasc;
                    do {
                        dataNasc = view.pedirDataNascimento();
                        if (!Validador.isDataNascimentoValida(dataNasc)) view.mostrarErroDataInvalida();
                    } while (!Validador.isDataNascimentoValida(dataNasc));

                    // --- SELEÇÃO DE CURSO ---
                    String[] listaCursos = ImportadorCSV.obterListaCursos(PASTA_BD);
                    String siglaCurso = "";

                    if (listaCursos.length == 0) {
                        view.mostrarAvisoSemCursos();
                        siglaCurso = view.pedirSiglaCursoManual();
                    } else {
                        view.mostrarListaCursos(listaCursos);
                        int opcaoCurso = view.pedirOpcaoCurso(listaCursos.length);
                        siglaCurso = listaCursos[opcaoCurso - 1].split(" - ")[0];
                        view.mostrarCursoSelecionado(siglaCurso);
                    }
                    // ---------------------------------

                    // Geração de Credenciais
                    String email = EmailGenerator.gerarEmailEstudante(numMec);
                    String passLimpa = PasswordGenerator.gerarPasswordSegura();
                    String passSegura = utils.SegurancaPasswords.gerarCredencialMista(passLimpa);

                    // Criação e gravação do Estudante
                    Estudante novo = new Estudante(numMec, email, passSegura, nome, nif, morada, dataNasc, anoInscricao);
                    ExportadorCSV.adicionarEstudante(novo, PASTA_BD, siglaCurso);

                    view.mostrarMensagem("Sucesso! Estudante guardado no disco.");
                    view.mostrarMensagem("Email: " + email + " | Password Temporária: " + passLimpa);
                    break;

                case 2:
                    view.mostrarMensagem("Avançar Ano Letivo - Em desenvolvimento para o próximo sprint.");
                    break;
                case 0:
                    correr = false;
                    break;
            }
        }
    }
}