package controller;

import model.*;
import view.GestorView;
import utils.*;

public class GestorController {
    private RepositorioDados repo;
    private Gestor gestor;
    private GestorView view;
    private static final String PASTA_BD = "bd"; //

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
                case 1: executarRegistoEstudante(); break;
                case 2: menuGerirUcs(); break;
                case 3: menuGerirCursos(); break;
                case 4: menuEstatisticas(); break;
                case 5: view.mostrarMensagem("Avançar Ano Letivo - Em desenvolvimento."); break;
                case 0: correr = false; break;
                default: view.mostrarMensagem("Opção inválida.");
            }
        }
    }

    // --- GESTÃO DE UNIDADES CURRICULARES (UCs) ---

    private void menuGerirUcs() {
        boolean correr = true;
        while (correr) {
            int opcao = view.mostrarMenuCRUD("Unidades Curriculares");
            switch (opcao) {
                case 1: adicionarUc(); break;
                case 2: listarUcs(); break;
                case 3: editarUc(); break;
                case 4: removerUc(); break;
                case 0: correr = false; break;
                default: view.mostrarMensagem("Opção inválida.");
            }
        }
    }

    private void adicionarUc() {
        String siglaCurso = view.pedirSiglaCurso();
        int anoUc = Integer.parseInt(view.pedirAnoCurricular());

        if (repo.podeAdicionarUc(siglaCurso, anoUc, PASTA_BD)) { //
            String siglaUc = view.pedirSiglaUc();
            String nomeUc = view.pedirNomeUc();
            String docente = view.pedirSiglaDocente();

            String linhaUc = siglaUc + ";" + nomeUc + ";" + anoUc + ";" + docente + ";" + siglaCurso;
            ExportadorCSV.adicionarLinhaCSV("ucs.csv", linhaUc, PASTA_BD); //
            view.mostrarSucessoCriacao("UC");
        } else {
            view.mostrarErroLimiteUcs(anoUc);
        }
    }

    private void listarUcs() {
        view.mostrarMensagem(ImportadorCSV.listarTodasUcs(PASTA_BD)); //
    }

    private void editarUc() {
        String siglaEditar = view.pedirSiglaUc();
        if (ExportadorCSV.removerLinhaCSV("ucs.csv", siglaEditar, PASTA_BD)) { //
            view.mostrarMensagemModoEdicao();
            String novaLinha = siglaEditar + ";" + view.pedirNovoNome() + ";" +
                    view.pedirNovoAnoCurricular() + ";" +
                    view.pedirNovaSiglaDocente() + ";" +
                    view.pedirNovaSiglaCurso();
            ExportadorCSV.adicionarLinhaCSV("ucs.csv", novaLinha, PASTA_BD);
            view.mostrarSucessoAtualizacao("UC");
        } else {
            view.mostrarErroNaoEncontrado("UC");
        }
    }

    private void removerUc() {
        String siglaRemover = view.pedirSiglaUc();
        if (ExportadorCSV.removerLinhaCSV("ucs.csv", siglaRemover, PASTA_BD)) {
            view.mostrarSucessoRemocao("UC");
        } else {
            view.mostrarErroNaoEncontrado("UC");
        }
    }

    // --- GESTÃO DE CURSOS ---

    private void menuGerirCursos() {
        boolean correr = true;
        while (correr) {
            int opcao = view.mostrarMenuCRUD("Cursos");
            switch (opcao) {
                case 1: adicionarCurso(); break;
                case 2: listarCursos(); break;
                case 3: editarCurso(); break;
                case 4: removerCurso(); break;
                case 0: correr = false; break;
                default: view.mostrarMensagem("Opção inválida.");
            }
        }
    }

    private void adicionarCurso() {
        String linha = view.pedirSiglaCurso() + ";" + view.pedirNomeCurso() + ";" + view.pedirDepartamento();
        ExportadorCSV.adicionarLinhaCSV("cursos.csv", linha, PASTA_BD);
        view.mostrarSucessoCriacao("Curso");
    }

    private void listarCursos() {
        view.mostrarMensagem(ImportadorCSV.listarTodosCursos(PASTA_BD)); //
    }

    private void editarCurso() {
        String siglaEditar = view.pedirSiglaCurso();
        if (repo.podeEditarCurso(siglaEditar, PASTA_BD)) { //
            if (ExportadorCSV.removerLinhaCSV("cursos.csv", siglaEditar, PASTA_BD)) {
                view.mostrarMensagemModoEdicao();
                String novaLinha = siglaEditar + ";" + view.pedirNomeCurso() + ";" + view.pedirNovoDepartamento();
                ExportadorCSV.adicionarLinhaCSV("cursos.csv", novaLinha, PASTA_BD);
                view.mostrarSucessoAtualizacao("Curso");
            } else {
                view.mostrarErroNaoEncontrado("Curso");
            }
        } else {
            view.mostrarErroEdicaoCurso();
        }
    }

    private void removerCurso() {
        String siglaRemover = view.pedirSiglaCurso();
        if (repo.podeEditarCurso(siglaRemover, PASTA_BD)) {
            if (ExportadorCSV.removerLinhaCSV("cursos.csv", siglaRemover, PASTA_BD)) {
                view.mostrarSucessoRemocao("Curso");
            } else {
                view.mostrarErroNaoEncontrado("Curso");
            }
        } else {
            view.mostrarErroEdicaoCurso();
        }
    }

    // --- ESTATÍSTICAS E REGISTO ---

    private void menuEstatisticas() {
        boolean correr = true;
        while (correr) {
            int opcao = view.mostrarMenuEstatisticas();
            switch (opcao) {
                case 1: view.mostrarMensagem(Estatisticas.calcularMediaGlobal(PASTA_BD)); break; //
                case 2: view.mostrarMensagem(Estatisticas.obterMelhorAluno(PASTA_BD)); break;
                case 0: correr = false; break;
                default: view.mostrarMensagem("Opção inválida.");
            }
        }
    }

    private void executarRegistoEstudante() {
        view.mostrarTituloRegistoEstudante();
        int numMec = Integer.parseInt(view.pedirNumMecanografico());

        String nif;
        do { nif = view.pedirNif(); } while (!Validador.validarNif(nif)); //

        Estudante novo = new Estudante(numMec,
                EmailGenerator.gerarEmailEstudante(numMec),
                SegurancaPasswords.gerarCredencialMista(PasswordGenerator.gerarPasswordSegura()),
                view.pedirNome(), nif, view.pedirMorada(),
                view.pedirDataNascimento(),
                Integer.parseInt(view.pedirAnoInscricao()));

        ExportadorCSV.adicionarEstudante(novo, PASTA_BD, view.pedirSiglaCurso()); //
        view.mostrarSucessoRegistoEstudante(novo.getEmail(), "Gerada Automaticamente");
    }
}