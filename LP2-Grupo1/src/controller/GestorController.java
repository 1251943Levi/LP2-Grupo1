package controller;

import model.*;
import view.GestorView;
import utils.*;
import bll.GestorBLL;

/**
 * Controlador responsável por gerir as interações e permissões do Gestor.
 * Atua como intermediário entre a interface (GestorView), as regras de negócio (GestorBLL)
 * e o repositório de dados.
 */
public class GestorController {
    private RepositorioDados repo;
    private Gestor gestor;
    private GestorView view;
    private GestorBLL bll;

    private static final String PASTA_BD = "bd";

    /**
     * Construtor do GestorController.
     * * @param repo   O repositório central de dados da aplicação.
     * @param gestor O modelo do gestor que iniciou a sessão.
     */
    public GestorController(RepositorioDados repo, Gestor gestor) {
        this.repo = repo;
        this.gestor = gestor;
        this.view = new GestorView();
        this.bll = new GestorBLL();
    }

    /**
     * Inicia o ciclo principal de execução do menu do Gestor.
     * Gere a navegação principal e o logout.
     */
    public void iniciar() {
        boolean correr = true;
        while (correr) {
            try {
                int opcao = view.mostrarMenu();
                switch (opcao) {
                    case 1: executarRegistoEstudante(); break;
                    case 2: menuGerirUcs(); break;
                    case 3: menuGerirCursos(); break;
                    case 4: menuEstatisticas(); break;
                    case 5: bll.avancarAnoLetivo(repo, view); break;
                    case 6: listarDevedores(); break;
                    case 7: alterarPassword(); break;
                    case 8: executarRegistoDocente(); break;
                    case 0:
                        view.mostrarDespedida();
                        repo.limparSessao();
                        correr = false;
                        break;
                    default:
                        view.mostrarOpcaoInvalida();
                }
            } catch (Exception e) {
                view.mostrarErroLeituraOpcao();
            }
        }
    }

    // --- Métodos de Registo ---

    /**
     * Coordena o fluxo de registo de um novo docente, incluindo recolha de dados,
     * validação de NIF e delegação da criação para a BLL.
     */
    private void executarRegistoDocente() {
        view.mostrarTituloRegistoDocente();

        String nome;
        do {
            nome = view.pedirNome();
            if (!Validador.isNomeValido(nome)) view.mostrarErroNomeInvalido();
        } while (!Validador.isNomeValido(nome));

        String sigla = view.pedirSiglaDocente();

        String nif;
        boolean duplicado;
        do {
            nif = view.pedirNif();
            duplicado = Validador.isNifDuplicado(nif, PASTA_BD);
            if (!Validador.validarNif(nif)) {
                view.mostrarErroNifInvalido();
            } else if (duplicado) {
                view.mostrarErroNifDuplicado();
            }
        } while (!Validador.validarNif(nif) || duplicado);

        String morada = view.pedirMorada();
        String dataNasc = view.pedirDataNascimento();

        String emailGerado = bll.registarDocente(nome, sigla, nif, morada, dataNasc);
        view.mostrarResumoRegistoDocente(emailGerado);
    }

    /**
     * Coordena o fluxo de registo de um novo estudante, gerindo a atribuição
     * automática de número mecanográfico e a seleção do curso.
     */
    private void executarRegistoEstudante() {
        view.mostrarTituloRegistoEstudante();

        int anoInscricao = repo.getAnoAtual();
        int numMec = ImportadorCSV.obterProximoNumeroMecanografico(PASTA_BD, anoInscricao);
        view.mostrarNumMecanograficoAtribuido(numMec);

        String nome;
        do {
            nome = view.pedirNome();
            if (!Validador.isNomeValido(nome)) view.mostrarErroNomeInvalido();
        } while (!Validador.isNomeValido(nome));

        String nif;
        boolean duplicado;
        do {
            nif = view.pedirNif();
            duplicado = Validador.isNifDuplicado(nif, PASTA_BD);
            if (!Validador.validarNif(nif)) {
                view.mostrarErroNifInvalido();
            } else if (duplicado) {
                view.mostrarErroNifDuplicado();
            }
        } while (!Validador.validarNif(nif) || duplicado);

        String morada = view.pedirMorada();
        String dataNasc;
        do {
            dataNasc = view.pedirDataNascimento();
            if (!Validador.isDataNascimentoValida(dataNasc)) view.mostrarErroDataInvalida();
        } while (!Validador.isDataNascimentoValida(dataNasc));

        String siglaCurso = obterSiglaCursoPelaView();

        String emailGerado = bll.registarEstudante(numMec, nome, nif, morada, dataNasc, siglaCurso, anoInscricao);
        view.mostrarResumoRegistoEstudante(emailGerado);
    }

    // --- Métodos de Estatísticas e Listagens ---

    /**
     * Solicita à BLL os dados estatísticos globais e apresenta a média
     * institucional através da View.
     */
    private void mostrarMediaGlobal() {
        view.mostrarCabecalhoMediaGlobal();
        double[] stats = bll.calcularEstatisticasGlobais();

        if (stats == null) {
            view.mostrarErroCarregarDados("Estudantes");
        } else if (stats[1] == 0) {
            view.mostrarSemNotasRegistadas();
        } else {
            view.mostrarMediaGlobal(stats[0] / stats[1], (int) stats[1]);
        }
    }

    /**
     * Obtém o estudante com melhor desempenho académico através da BLL
     * e exibe os seus detalhes.
     */
    private void mostrarMelhorAluno() {
        view.mostrarCabecalhoMelhorAluno();
        Object[] resultado = bll.obterMelhorAluno();

        if (resultado != null) {
            Estudante melhor = (Estudante) resultado[0];
            double media = (double) resultado[1];
            view.mostrarInfoMelhorAluno(melhor.getNome(), melhor.getNumeroMecanografico(), media);
        } else {
            view.mostrarSemAlunosAvaliados();
        }
    }

    /**
     * Lista todos os estudantes que possuem saldo devedor (propinas em atraso).
     */
    private void listarDevedores() {
        view.mostrarCabecalhoDevedores();
        Estudante[] estudantes = ImportadorCSV.carregarTodosEstudantes(PASTA_BD);
        boolean encontrou = false;

        if (estudantes != null) {
            for (Estudante e : estudantes) {
                if (e != null && e.getSaldoDevedor() > 0) {
                    view.mostrarEstudanteDevedor(e.getNumeroMecanografico(), e.getNome(), e.getSaldoDevedor());
                    encontrou = true;
                }
            }
        }
        if (!encontrou) view.mostrarSemDevedores();
    }

    // --- Gestão de UCs ---

    /**
     * Recolhe dados da View para criar uma nova Unidade Curricular e
     * valida o limite máximo de UCs por ano via BLL.
     */
    private void adicionarUc() {
        String siglaCurso = obterSiglaCursoPelaView();
        if (siglaCurso.isEmpty()) return;

        int anoUc = Integer.parseInt(view.pedirAnoCurricular());
        String siglaUc = view.pedirSiglaUc();
        String nomeUc = view.pedirNomeUc();
        String docente = view.pedirSiglaDocente();

        if (bll.adicionarUc(siglaCurso, anoUc, siglaUc, nomeUc, docente, repo)) {
            view.mostrarSucessoCriacao("UC");
        } else {
            view.mostrarErroLimiteUcs(anoUc);
        }
    }

    /**
     * Permite a edição de uma UC existente, substituindo os dados antigos
     * pelos novos introduzidos pelo Gestor.
     */
    private void editarUc() {
        String[] ucs = ImportadorCSV.obterListaUcs(PASTA_BD);
        if (ucs.length == 0) { view.mostrarErroNaoEncontrado("UCs"); return; }

        view.mostrarListaUcs(ucs);
        int escolha = view.pedirOpcaoUc(ucs.length);
        String siglaAntiga = ucs[escolha - 1].split(" - ")[0];

        view.mostrarMensagemModoEdicao();
        boolean sucesso = bll.editarUc(siglaAntiga, view.pedirSiglaUc(), view.pedirNovoNome(),
                view.pedirNovoAnoCurricular(), view.pedirNovaSiglaDocente(),
                view.pedirNovaSiglaCurso());

        if (sucesso) view.mostrarSucessoAtualizacao("UC");
    }

    // --- Métodos Auxiliares ---

    /**
     * Mostra uma lista numerada de cursos para seleção e retorna a sigla do curso escolhido.
     * * @return A sigla do curso selecionado (ex: "LEI").
     */
    private String obterSiglaCursoPelaView() {
        String[] listaCursos = ImportadorCSV.obterListaCursos(PASTA_BD);
        if (listaCursos.length > 0) {
            view.mostrarListaCursos(listaCursos);
            int escolha = view.pedirOpcaoCurso(listaCursos.length);
            return listaCursos[escolha - 1].split(" - ")[0];
        } else {
            view.mostrarAvisoSemCursos();
            return view.pedirSiglaCurso();
        }
    }

    /**
     * Processa a alteração de password do Gestor atualmente logado.
     */
    private void alterarPassword() {
        view.mostrarCabecalhoAlterarPassword();
        String novaPass = view.pedirNovaPassword();
        if (!novaPass.trim().isEmpty()) {
            bll.alterarPasswordGestor(gestor, novaPass);
            view.mostrarSucessoAlteracaoPassword();
        } else {
            view.mostrarCancelamentoPassword();
        }
    }

    /**
     * Gere o sub-menu dedicado a consultas estatísticas.
     */
    private void menuEstatisticas() {
        boolean correr = true;
        while (correr) {
            int opcao = view.mostrarMenuEstatisticas();
            switch (opcao) {
                case 1: mostrarMediaGlobal(); break;
                case 2: mostrarMelhorAluno(); break;
                case 0: correr = false; break;
                default: view.mostrarOpcaoInvalida();
            }
        }
    }

    /**
     * Gere o sub-menu para operações CRUD (Criar, Ler, Atualizar, Remover) em UCs.
     */
    private void menuGerirUcs() {
        boolean correr = true;
        while (correr) {
            int opcao = view.mostrarMenuCRUD("Unidades Curriculares");
            switch (opcao) {
                case 1: adicionarUc(); break;
                case 2: view.mostrarResultadosListagem(ImportadorCSV.listarTodasUcs(PASTA_BD)); break;
                case 3: editarUc(); break;
                case 4: removerUc(); break;
                case 0: correr = false; break;
                default: view.mostrarOpcaoInvalida();
            }
        }
    }

    /**
     * Gere o sub-menu para operações CRUD em Cursos.
     */
    private void menuGerirCursos() {
        boolean correr = true;
        while (correr) {
            int opcao = view.mostrarMenuCRUD("Cursos");
            switch (opcao) {
                case 1:
                    bll.adicionarCurso(view.pedirSiglaCurso(), view.pedirNomeCurso(),
                            view.pedirDepartamento(), view.pedirValorDouble("Propina"));
                    view.mostrarSucessoCriacao("Curso");
                    break;
                case 2: view.mostrarResultadosListagem(ImportadorCSV.listarTodosCursos(PASTA_BD)); break;
                case 0: correr = false; break;
                default: view.mostrarOpcaoInvalida();
            }
        }
    }

    /**
     * Gere o fluxo de remoção de uma Unidade Curricular, solicitando confirmação
     * antes de delegar a eliminação à BLL.
     */
    private void removerUc() {
        String[] ucs = ImportadorCSV.obterListaUcs(PASTA_BD);

        if (ucs.length == 0) {
            view.mostrarErroNaoEncontrado("UCs");
            return;
        }

        view.mostrarListaUcs(ucs);
        int escolha = view.pedirOpcaoUc(ucs.length);

        String siglaUc = ucs[escolha - 1].split(" - ")[0];

        if (view.confirmarRemocao(siglaUc)) {
            if (bll.removerUc(siglaUc)) {
                view.mostrarSucessoRemocao("UC");
            } else {
                view.mostrarErroRemocao("UC");
            }
        }
    }
}