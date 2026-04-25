package controller;

import model.*;
import view.GestorView;
import bll.GestorBLL;
import bll.EstudanteBLL;
import bll.UcBLL;
import utils.CancelamentoException;
import utils.Validador;
import java.util.List;

/**
 * Controlador responsável por gerir as interações e permissões do Gestor.
 * Liga a GestorView às BLLs correspondentes.
 */
public class GestorController {

    private final RepositorioDados repo;
    private final Gestor gestor;
    private final GestorView view;
    private final GestorBLL gestorBll;
    private final EstudanteBLL estudanteBll;
    private final UcBLL ucBll;

    public GestorController(RepositorioDados repo, Gestor gestor) {
        this.repo = repo;
        this.gestor = gestor;
        this.view = new GestorView();
        this.gestorBll = new GestorBLL();
        this.estudanteBll = new EstudanteBLL();
        this.ucBll  = new UcBLL();
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
                    case 1: executarRegistoEstudante();                    break;
                    case 2: menuGerirUcs();                                break;
                    case 3: menuGerirCursos();                             break;
                    case 4: menuEstatisticas();                            break;
                    case 5: gestorBll.avancarAnoLetivo(repo, view);       break;
                    case 6: listarDevedores();                             break;
                    case 7: alterarPassword();                             break;
                    case 8: executarRegistoDocente();                      break;
                    case 9: executarRegistoDepartamento();                 break;
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
     * Coordena o registo de um novo docente.
     * Validação de NIF delegada à GestorBLL (que consulta as DALs).
     */
    private void executarRegistoDocente() {
        try {
            view.mostrarTituloRegistoDocente();

            String nome;
            do {
                nome = view.pedirNome();
                if (!Validador.isNomeValido(nome)) view.mostrarErroNomeInvalido();
            } while (!Validador.isNomeValido(nome));

            // Sigla gerada automaticamente — não é pedida ao utilizador

            String nif;
            boolean nifInvalido, nifDuplicado;
            do {
                nif          = view.pedirNif();
                nifInvalido  = !Validador.validarNif(nif);
                nifDuplicado = !nifInvalido && gestorBll.isNifDuplicado(nif);
                if (nifInvalido)       view.mostrarErroNifInvalido();
                else if (nifDuplicado) view.mostrarErroNifDuplicado();
            } while (nifInvalido || nifDuplicado);

            String morada   = view.pedirMorada();
            String dataNasc = view.pedirDataNascimento();

            String[] resultado = gestorBll.registarDocente(nome, nif, morada, dataNasc);
            view.mostrarResumoRegistoDocente(resultado[0], resultado[1]);

        } catch (CancelamentoException e) {
            view.mostrarOperacaoCancelada();
        }
    }

    /**
     * Fluxo de registo de um novo Departamento.
     * Valida sigla não duplicada e nome não vazio.
     */
    private void executarRegistoDepartamento() {
        try {
            view.mostrarTituloRegistoDepartamento();

            String sigla;
            do {
                sigla = view.pedirSiglaDepartamento().toUpperCase().trim();
                if (sigla.isEmpty()) {
                    view.mostrarMensagem("ERRO: Sigla não pode estar vazia.");
                } else if (gestorBll.isDepartamentoDuplicado(sigla)) {
                    view.mostrarErroDepartamentoDuplicado();
                    sigla = "";
                }
            } while (sigla.isEmpty());

            String nome;
            do {
                nome = view.pedirNomeDepartamento().trim();
                if (nome.isEmpty()) view.mostrarMensagem("ERRO: Nome não pode estar vazio.");
            } while (nome.isEmpty());

            gestorBll.registarDepartamento(sigla, nome);
            view.mostrarResumoRegistoDepartamento(sigla, nome);

        } catch (CancelamentoException e) {
            view.mostrarOperacaoCancelada();
        }
    }


    /**
     * Coordena o registo de um novo estudante.
     * Número mecanográfico gerado automaticamente via EstudanteBLL.
     */
    private void executarRegistoEstudante() {
        try {
            view.mostrarTituloRegistoEstudante();
            int anoInscricao = repo.getAnoAtual();
            // numMec é calculado automaticamente pela BLL
            String nome;
            do {
                nome = view.pedirNome();
                if (!Validador.isNomeValido(nome)) view.mostrarErroNomeInvalido();
            } while (!Validador.isNomeValido(nome));

            String nif;
            boolean nifInvalido, nifDuplicado;
            do {
                nif          = view.pedirNif();
                nifInvalido  = !Validador.validarNif(nif);
                nifDuplicado = !nifInvalido && gestorBll.isNifDuplicado(nif);
                if (nifInvalido)       view.mostrarErroNifInvalido();
                else if (nifDuplicado) view.mostrarErroNifDuplicado();
            } while (nifInvalido || nifDuplicado);

            String morada = view.pedirMorada();

            String dataNasc;
            do {
                dataNasc = view.pedirDataNascimento();
                if (!Validador.isDataNascimentoValida(dataNasc)) view.mostrarErroDataInvalida();
            } while (!Validador.isDataNascimentoValida(dataNasc));

            String siglaCurso = obterSiglaCursoPelaView();
            if (siglaCurso == null) return;

            String email = gestorBll.registarEstudante(nome, nif, morada, dataNasc, siglaCurso, anoInscricao);
            view.mostrarResumoRegistoEstudante(email);

        } catch (CancelamentoException e) {
            view.mostrarOperacaoCancelada();
        }
    }


    // --- Métodos de Estatísticas e Listagens ---

    /**
     * Solicita à BLL os dados estatísticos globais e apresenta a média
     * institucional através da View.
     */
    private void mostrarMediaGlobal() {
        view.mostrarCabecalhoMediaGlobal();
        double[] stats = gestorBll.calcularEstatisticasGlobais();
        if (stats == null)          { view.mostrarErroCarregarDados("Estudantes"); return; }
        if (stats[1] == 0)          { view.mostrarSemNotasRegistadas();           return; }
        view.mostrarMediaGlobal(stats[0] / stats[1], (int) stats[1]);
    }

    /**
     * Obtém o estudante com melhor desempenho académico através da BLL
     * e exibe os seus detalhes.
     */
    private void mostrarMelhorAluno() {
        view.mostrarCabecalhoMelhorAluno();
        Object[] resultado = gestorBll.obterMelhorAluno();
        if (resultado != null) {
            Estudante melhor = (Estudante) resultado[0];
            double media     = (double) resultado[1];
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
        List<Estudante> devedores = gestorBll.obterListaDevedores();
        if (devedores.isEmpty()) { view.mostrarSemDevedores(); return; }
        for (Estudante e : devedores)
            view.mostrarEstudanteDevedor(
                    e.getNumeroMecanografico(), e.getNome(), e.getSaldoDevedor());
    }

    // --- Gestão de UCs ---

    /**
     * Recolhe dados da View para criar uma nova Unidade Curricular e
     * valida o limite máximo de UCs por ano via BLL.
     */
    private void adicionarUc() {
        try {
            String siglaCurso = obterSiglaCursoPelaView();
            if (siglaCurso == null || siglaCurso.isEmpty()) return;

            int anoUc;
            try {
                anoUc = Integer.parseInt(view.pedirAnoCurricular());
                if (anoUc < 1 || anoUc > 3) {
                    view.mostrarMensagem("ERRO: Ano curricular deve ser 1, 2 ou 3.");
                    return;
                }
            } catch (NumberFormatException ex) {
                view.mostrarMensagem("ERRO: Ano curricular inválido. Introduza um número entre 1 e 3.");
                return;
            }

            String siglaUc = view.pedirSiglaUc();
            String nomeUc  = view.pedirNomeUc();
            String docente = view.pedirSiglaDocente();

            if (gestorBll.adicionarUc(siglaCurso, anoUc, siglaUc, nomeUc, docente))
                view.mostrarSucessoCriacao("UC");
            else
                view.mostrarErroLimiteUcs(anoUc);
        } catch (CancelamentoException e) {
            view.mostrarOperacaoCancelada();
        }
    }

    /**
     * Permite a edição de uma UC existente, substituindo os dados antigos
     * pelos novos introduzidos pelo Gestor.
     */
    private void editarUc() {
        String[] ucs = ucBll.obterListaUcs();
        if (ucs.length == 0) { view.mostrarErroNaoEncontrado("UCs"); return; }

        view.mostrarListaUcs(ucs);
        int escolha = view.pedirOpcaoUc(ucs.length);
        if (escolha == -1) { view.mostrarOperacaoCancelada(); return; }
        String siglaAntiga = ucs[escolha - 1].split(" - ")[0];

        view.mostrarMensagemModoEdicao();
        boolean sucesso = gestorBll.editarUc(
                siglaAntiga,
                view.pedirSiglaUc(),
                view.pedirNovoNome(),
                view.pedirNovoAnoCurricular(),
                view.pedirNovaSiglaDocente(),
                view.pedirNovaSiglaCurso());

        if (sucesso) view.mostrarSucessoAtualizacao("UC");
    }

    private void removerUc() {
        String[] ucs = ucBll.obterListaUcs();
        if (ucs.length == 0) { view.mostrarErroNaoEncontrado("UCs"); return; }

        view.mostrarListaUcs(ucs);
        int escolha    = view.pedirOpcaoUc(ucs.length);
        if (escolha == -1) { view.mostrarOperacaoCancelada(); return; }
        String siglaUc = ucs[escolha - 1].split(" - ")[0];

        if (view.confirmarRemocaoBoolean(siglaUc)) {
            if (gestorBll.removerUc(siglaUc)) view.mostrarSucessoRemocao("UC");
            else                              view.mostrarErroRemocao("UC");
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
                case 0: correr = false;       break;
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
                case 1: adicionarUc();                                          break;
                case 2: view.mostrarResultadosListagem(gestorBll.listarTodasUcs()); break;
                case 3: editarUc();                                             break;
                case 4: removerUc();                                            break;
                case 0: correr = false;                                         break;
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
                    try {
                        gestorBll.adicionarCurso(
                                view.pedirSiglaCurso(),
                                view.pedirNomeCurso(),
                                view.pedirDepartamento(),
                                view.pedirValorDouble("Propina anual (€)"));
                        view.mostrarSucessoCriacao("Curso");
                    } catch (CancelamentoException e) { view.mostrarOperacaoCancelada(); }
                    break;
                case 2: view.mostrarResultadosListagem(gestorBll.listarTodosCursos()); break;
                case 3: editarCurso();    break;
                case 4: removerCurso();   break;
                case 5: listarUcsCurso(); break;
                case 0: correr = false;   break;
                default: view.mostrarOpcaoInvalida();
            }
        }
    }

    /** Permite editar o nome, departamento e propina de um curso sem alocações. */
    private void editarCurso() {
        try {
            String[] cursos = gestorBll.listarTodosCursos();
            if (cursos.length == 0) { view.mostrarErroNaoEncontrado("Cursos"); return; }
            view.mostrarListaCursos(cursos);
            int escolha = view.pedirOpcaoCurso(cursos.length);
            if (escolha == -1) { view.mostrarOperacaoCancelada(); return; }
            String sigla = cursos[escolha - 1].split(" - ")[0];

            if (!gestorBll.isCursoAlteravel(sigla)) {
                view.mostrarErroCursoComAlocacoes(); return;
            }
            view.mostrarMensagemModoEdicao();
            boolean sucesso = gestorBll.editarCurso(
                    sigla,
                    view.pedirNomeCurso(),
                    view.pedirDepartamento(),
                    view.pedirValorDouble("Nova Propina anual (€)"));
            if (sucesso) view.mostrarSucessoAtualizacao("Curso");
        } catch (CancelamentoException e) { view.mostrarOperacaoCancelada(); }
    }

    /** Remove um curso que não tenha estudantes nem docentes alocados. */
    private void removerCurso() {
        String[] cursos = gestorBll.listarTodosCursos();
        if (cursos.length == 0) { view.mostrarErroNaoEncontrado("Cursos"); return; }
        view.mostrarListaCursos(cursos);
        int escolha = view.pedirOpcaoCurso(cursos.length);
        if (escolha == -1) { view.mostrarOperacaoCancelada(); return; }
        String sigla = cursos[escolha - 1].split(" - ")[0];

        if (!gestorBll.isCursoAlteravel(sigla)) {
            view.mostrarErroCursoComAlocacoes(); return;
        }
        if (view.confirmarRemocaoBoolean(sigla)) {
            if (gestorBll.removerCurso(sigla)) view.mostrarSucessoRemocao("Curso");
            else                               view.mostrarErroRemocao("Curso");
        }
    }

    /** Lista as UCs de um curso agrupadas por ano curricular. */
    private void listarUcsCurso() {
        try {
            String siglaCurso = obterSiglaCursoPelaView();
            if (siglaCurso == null || siglaCurso.isEmpty()) return;
            view.mostrarMensagem(gestorBll.listarUcsPorCurso(siglaCurso));
            utils.Consola.pausar();
        } catch (CancelamentoException e) { view.mostrarOperacaoCancelada(); }
    }

    /**
     * Mostra a lista numerada de cursos para seleção e devolve a sigla escolhida.
     * A lista é obtida via GestorBLL — o Controller NÃO acede à DAL diretamente.
     */
    private String obterSiglaCursoPelaView() {
        String[] cursos = gestorBll.obterListaCursos();
        if (cursos.length > 0) {
            view.mostrarListaCursos(cursos);
            int escolha = view.pedirOpcaoCurso(cursos.length);
            if (escolha == -1) throw new CancelamentoException();
            return cursos[escolha - 1].split(" - ")[0];
        }
        return view.pedirSiglaCurso();
    }

    private void alterarPassword() {
        view.mostrarCabecalhoAlterarPassword();
        String novaPass = view.pedirNovaPassword();
        if (!novaPass.trim().isEmpty()) {
            gestorBll.alterarPasswordGestor(gestor, novaPass);
            view.mostrarSucessoAlteracaoPassword();
        } else {
            view.mostrarCancelamentoPassword();
        }
    }
}