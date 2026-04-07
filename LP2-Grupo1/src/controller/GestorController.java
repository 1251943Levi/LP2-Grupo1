package controller;

import model.Gestor;
import model.Estudante;
import model.RepositorioDados;
import view.GestorView;
import utils.EmailGenerator;
import utils.PasswordGenerator;
import utils.Validador;
import utils.ExportadorCSV;

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
    private static final String PASTA_BD = "LP2-Grupo1/bd";

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
                    int numMec = Integer.parseInt(view.pedirInput("Nº Mecanográfico"));
                    String nome = view.pedirInput("Nome");

                    String nif;
                    do {
                        nif = view.pedirInput("NIF (9 dígitos)");
                    } while (!Validador.validarNif(nif));

                    String morada = view.pedirInput("Morada");
                    String dataNasc = view.pedirInput("Data Nasc. (DD/MM/AAAA)");
                    int anoInscricao = Integer.parseInt(view.pedirInput("Ano de Inscrição"));

                    String siglaCurso = view.pedirInput("Sigla do Curso (ex: EI, IG)");

                    String email = EmailGenerator.gerarEmailEstudante(numMec);
                    String passLimpa = PasswordGenerator.gerarPasswordSegura();

                    String passSegura = utils.SegurancaPasswords.gerarCredencialMista(passLimpa);

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