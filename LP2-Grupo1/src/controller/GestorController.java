package controller;

import model.*;
import view.GestorView;
import utils.*;

public class GestorController {
    private RepositorioDados repo;
    private Gestor gestor;
    private GestorView view;
    private static final String PASTA_BD = "bd"; // Caminho corrigido

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

    // --- SUB-MENUS DE CRUD E REGRAS DE NEGÓCIO ---

    private void menuGerirUcs() {
        boolean correr = true;
        while (correr) {
            int opcao = view.mostrarMenuCRUD("Unidades Curriculares");
            switch (opcao) {
                case 1: // ADICIONAR UC
                    String siglaCurso = view.pedirSiglaCurso();
                    int anoUc = Integer.parseInt(view.pedirAnoCurricular());

                    if (repo.podeAdicionarUc(siglaCurso, anoUc, PASTA_BD)) {
                        String siglaUc = view.pedirSiglaUc();
                        String nomeUc = view.pedirNomeUc();
                        String docente = view.pedirSiglaDocente();

                        String linhaUc = siglaUc + ";" + nomeUc + ";" + anoUc + ";" + docente + ";" + siglaCurso;
                        ExportadorCSV.adicionarLinhaCSV("ucs.csv", linhaUc, PASTA_BD);
                        view.mostrarSucessoCriacao("UC");
                    } else {
                        view.mostrarErroLimiteUcs(anoUc);
                    }
                    break;

                case 2: // LISTAR UCS
                    view.mostrarMensagem(ImportadorCSV.listarTodasUcs(PASTA_BD));
                    break;

                case 3: // EDITAR UC
                    String siglaEditar = view.pedirSiglaUc();

                    if (ExportadorCSV.removerLinhaCSV("ucs.csv", siglaEditar, PASTA_BD)) {
                        view.mostrarMensagemModoEdicao();
                        String novoNome = view.pedirNovoNome();
                        int novoAno = Integer.parseInt(view.pedirNovoAnoCurricular());
                        String novoDocente = view.pedirNovaSiglaDocente();
                        String novoCurso = view.pedirNovaSiglaCurso();

                        String novaLinha = siglaEditar + ";" + novoNome + ";" + novoAno + ";" + novoDocente + ";" + novoCurso;
                        ExportadorCSV.adicionarLinhaCSV("ucs.csv", novaLinha, PASTA_BD);
                        view.mostrarSucessoAtualizacao("UC");
                    } else {
                        view.mostrarErroNaoEncontrado("UC");
                    }
                    break;

                case 4: // REMOVER UC
                    String siglaRemover = view.pedirSiglaUc();
                    if (ExportadorCSV.removerLinhaCSV("ucs.csv", siglaRemover, PASTA_BD)) {
                        view.mostrarSucessoRemocao("UC");
                    } else {
                        view.mostrarErroNaoEncontrado("UC");
                    }
                    break;

                case 0: correr = false; break;
                default: view.mostrarMensagem("Opção inválida.");
            }
        }
    }

    private void menuGerirCursos() {
        boolean correr = true;
        while (correr) {
            int opcao = view.mostrarMenuCRUD("Cursos");
            switch (opcao) {
                case 1: // ADICIONAR CURSO
                    String sigla = view.pedirSiglaCurso();
                    String nome = view.pedirNomeCurso();
                    String dep = view.pedirDepartamento();

                    ExportadorCSV.adicionarLinhaCSV("cursos.csv", sigla + ";" + nome + ";" + dep, PASTA_BD);
                    view.mostrarSucessoCriacao("Curso");
                    break;

                case 2: // LISTAR CURSOS
                    view.mostrarMensagem(ImportadorCSV.listarTodosCursos(PASTA_BD));
                    break;

                case 3: // EDITAR CURSO
                    String siglaEditar = view.pedirSiglaCurso();

                    if (repo.podeEditarCurso(siglaEditar, PASTA_BD)) {
                        if (ExportadorCSV.removerLinhaCSV("cursos.csv", siglaEditar, PASTA_BD)) {
                            view.mostrarMensagemModoEdicao();
                            String novoNome = view.pedirNomeCurso();
                            String novoDep = view.pedirNovoDepartamento();

                            ExportadorCSV.adicionarLinhaCSV("cursos.csv", siglaEditar + ";" + novoNome + ";" + novoDep, PASTA_BD);
                            view.mostrarSucessoAtualizacao("Curso");
                        } else {
                            view.mostrarErroNaoEncontrado("Curso");
                        }
                    } else {
                        view.mostrarErroEdicaoCurso();
                    }
                    break;

                case 4: // REMOVER CURSO
                    String siglaRemover = view.pedirSiglaCurso();

                    if (repo.podeEditarCurso(siglaRemover, PASTA_BD)) {
                        if (ExportadorCSV.removerLinhaCSV("cursos.csv", siglaRemover, PASTA_BD)) {
                            view.mostrarSucessoRemocao("Curso");
                        } else {
                            view.mostrarErroNaoEncontrado("Curso");
                        }
                    } else {
                        view.mostrarErroEdicaoCurso(); // Aviso de que não pode apagar cursos com alunos
                    }
                    break;

                case 0: correr = false; break;
                default: view.mostrarMensagem("Opção inválida.");
            }
        }
    }

    private void menuEstatisticas() {
        boolean correr = true;
        while (correr) {
            int opcao = view.mostrarMenuEstatisticas();
            switch (opcao) {
                case 1:
                    view.mostrarMensagem(Estatisticas.calcularMediaGlobal(PASTA_BD));
                    break;
                case 2:
                    view.mostrarMensagem(Estatisticas.obterMelhorAluno(PASTA_BD));
                    break;
                case 0: correr = false; break;
                default: view.mostrarMensagem("Opção inválida.");
            }
        }
    }

    private void executarRegistoEstudante() {
        view.mostrarTituloRegistoEstudante();

        int numMec = Integer.parseInt(view.pedirNumMecanografico());
        String nome = view.pedirNome();

        String nif;
        do {
            nif = view.pedirNif();
        } while (!Validador.validarNif(nif));

        String morada = view.pedirMorada();
        String dataNasc = view.pedirDataNascimento();
        int anoInscricao = Integer.parseInt(view.pedirAnoInscricao());
        String siglaCurso = view.pedirSiglaCurso();

        // Geração de Credenciais
        String email = EmailGenerator.gerarEmailEstudante(numMec);
        String passLimpa = PasswordGenerator.gerarPasswordSegura();
        String passSegura = SegurancaPasswords.gerarCredencialMista(passLimpa);

        // Criação e Gravação
        Estudante novo = new Estudante(numMec, email, passSegura, nome, nif, morada, dataNasc, anoInscricao);
        ExportadorCSV.adicionarEstudante(novo, PASTA_BD, siglaCurso);

        // Mensagem final de sucesso (chamando a view!)
        view.mostrarSucessoRegistoEstudante(email, passLimpa);
    }
}