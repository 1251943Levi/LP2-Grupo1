package controller;

import model.*;
import view.GestorView;
import utils.*;

public class GestorController {
    private RepositorioDados repo;
    private Gestor gestor;
    private GestorView view;
    private static final String PASTA_BD = "LP2-Grupo1/bd"; //

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
                case 1:
                    executarRegistoEstudante();
                    break;
                case 2:
                    menuGerirUcs();
                    break;
                case 3:
                    menuGerirCursos();
                    break;
                case 4:
                    menuEstatisticas();
                    break;
                case 5:
                    iniciarAnoLetivo();
                    break;
                case 6:
                    listarDevedores();
                    break;
                case 0:
                    correr = false;
                    break;
                default:
                    view.mostrarMensagem("Opção inválida.");
            }
        }
    }

    // --- GESTÃO DE UNIDADES CURRICULARES (UCs) ---

    private void menuGerirUcs() {
        boolean correr = true;
        while (correr) {
            int opcao = view.mostrarMenuCRUD("Unidades Curriculares");
            switch (opcao) {
                case 1:
                    adicionarUc();
                    break;
                case 2:
                    listarUcs();
                    break;
                case 3:
                    editarUc();
                    break;
                case 4:
                    removerUc();
                    break;
                case 5:
                    associarUcACurso();
                    break;

                case 0:
                    correr = false;
                    break;
                default:
                    view.mostrarMensagem("Opção inválida.");
            }
        }
    }

    private void adicionarUc() {
        String[] cursos = ImportadorCSV.obterListaCursos(PASTA_BD);
        if (cursos.length == 0) {
            view.mostrarAvisoSemCursos();
            return;
        }

        view.mostrarListaCursos(cursos);
        int escolha = view.pedirOpcaoCurso(cursos.length);
        String siglaCurso = cursos[escolha - 1].split(" - ")[0]; // Isola a Sigla

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
    }

    private void listarUcs() {
        view.mostrarMensagem(ImportadorCSV.listarTodasUcs(PASTA_BD));
    }

    private void listarDevedores() {
        view.mostrarMensagem("\n--- LISTA DE ESTUDANTES DEVEDORES ---");

        // Carrega todos os estudantes da base de dados
        Estudante[] estudantes = ImportadorCSV.carregarTodosEstudantes(PASTA_BD);
        boolean encontrou = false;

        if (estudantes.length == 0) {
            view.mostrarMensagem("Não existem estudantes registados no sistema.");
            return;
        }

        for (Estudante e : estudantes) {
            if (e != null && e.getSaldoDevedor() > 0) {
                String info = String.format("Nº %d | Nome: %-20s | Dívida: %.2f€",
                        e.getNumeroMecanografico(), e.getNome(), e.getSaldoDevedor());
                view.mostrarMensagem(info);
                encontrou = true;
            }
        }

        if (!encontrou) {
            view.mostrarMensagem("Excelente! Não existem estudantes com propinas em atraso.");
        }
    }

    private void editarUc() {
        String[] ucs = ImportadorCSV.obterListaUcs(PASTA_BD);
        if (ucs.length == 0) {
            view.mostrarErroNaoEncontrado("UCs");
            return;
        }

        view.mostrarListaUcs(ucs);
        int escolha = view.pedirOpcaoUc(ucs.length);
        String siglaEditar = ucs[escolha - 1].split(" - ")[0];

        if (ExportadorCSV.removerLinhaCSV("ucs.csv", siglaEditar, PASTA_BD)) {
            view.mostrarMensagemModoEdicao();
            String novaLinha = siglaEditar + ";" + view.pedirNovoNome() + ";" +
                    view.pedirNovoAnoCurricular() + ";" +
                    view.pedirNovaSiglaDocente() + ";" +
                    view.pedirNovaSiglaCurso();
            ExportadorCSV.adicionarLinhaCSV("ucs.csv", novaLinha, PASTA_BD);
            view.mostrarSucessoAtualizacao("UC");
        }
    }

    private void removerUc() {
        String[] ucs = ImportadorCSV.obterListaUcs(PASTA_BD);
        if (ucs.length == 0) {
            view.mostrarErroNaoEncontrado("UCs");
            return;
        }

        view.mostrarListaUcs(ucs);
        int escolha = view.pedirOpcaoUc(ucs.length);
        String siglaRemover = ucs[escolha - 1].split(" - ")[0];

        if (ExportadorCSV.removerLinhaCSV("ucs.csv", siglaRemover, PASTA_BD)) {
            view.mostrarSucessoRemocao("UC");
        }
    }

    private void associarUcACurso() {
        // 1. Listar e escolher a UC Existente
        String[] ucs = ImportadorCSV.obterListaUcs(PASTA_BD);
        if (ucs.length == 0) {
            view.mostrarErroNaoEncontrado("UCs");
            return;
        }
        view.mostrarListaUcs(ucs);
        int escolhaUc = view.pedirOpcaoUc(ucs.length);
        String siglaUc = ucs[escolhaUc - 1].split(" - ")[0];

        UnidadeCurricular ucExistente = ImportadorCSV.procurarUC(siglaUc, PASTA_BD);
        if (ucExistente == null) {
            view.mostrarMensagem(">> Erro ao carregar os dados da UC.");
            return;
        }

        // 2. Listar e escolher o Curso de Destino
        String[] cursos = ImportadorCSV.obterListaCursos(PASTA_BD);
        if (cursos.length == 0) {
            view.mostrarAvisoSemCursos();
            return;
        }
        view.mostrarListaCursos(cursos);
        int escolhaCurso = view.pedirOpcaoCurso(cursos.length);
        String siglaCurso = cursos[escolhaCurso - 1].split(" - ")[0]; // Isola a Sigla do Curso

        // 3. VALIDAR A REGRA DE NEGÓCIO: Só permite se houver menos de 5 UCs neste ano/curso
        if (repo.podeAdicionarUc(siglaCurso, ucExistente.getAnoCurricular(), PASTA_BD)) {

            String siglaDoc = (ucExistente.getDocenteResponsavel() != null) ? ucExistente.getDocenteResponsavel().getSigla() : "N/A";

            String novaLinha = ucExistente.getSigla() + ";" +
                    ucExistente.getNome() + ";" +
                    ucExistente.getAnoCurricular() + ";" +
                    siglaDoc + ";" +
                    siglaCurso;

            // Grava a nova associação no ficheiro ucs.csv
            ExportadorCSV.adicionarLinhaCSV("ucs.csv", novaLinha, PASTA_BD);
            view.mostrarMensagem(">> Sucesso: A UC '" + ucExistente.getNome() + "' foi associada ao curso " + siglaCurso + "!");

        } else {
            view.mostrarErroLimiteUcs(ucExistente.getAnoCurricular());
        }
    }

    // --- GESTÃO DE CURSOS ---

    private void menuGerirCursos() {
        boolean correr = true;
        while (correr) {
            int opcao = view.mostrarMenuCRUD("Cursos");
            switch (opcao) {
                case 1:
                    adicionarCurso();
                    break;
                case 2:
                    listarCursos();
                    break;
                case 3:
                    editarCurso();
                    break;
                case 4:
                    removerCurso();
                    break;
                case 5:
                    listarUcsDoCurso();
                    break;
                case 0:
                    correr = false;
                    break;
                default:
                    view.mostrarMensagem("Opção inválida.");
            }
        }
    }

    private void adicionarCurso() {

        String sigla = view.pedirSiglaCurso();
        String nome = view.pedirNomeCurso();
        String dep = view.pedirDepartamento();
        double propina = view.pedirValorDouble("Valor da Propina Anual (€)");
        // Ao adicionar um curso, apenas precisamos dos dados (não precisamos da lista)
        String linha = sigla + ";" + nome + ";" + dep + ";" + propina + ";Inativo";
        ExportadorCSV.adicionarLinhaCSV("cursos.csv", linha, PASTA_BD);
        view.mostrarSucessoCriacao("Curso");
    }

    private void listarCursos() {
        // Mostra a listagem formatada completa
        view.mostrarMensagem(ImportadorCSV.listarTodosCursos(PASTA_BD));
    }

    private void editarCurso() {
        String[] cursos = ImportadorCSV.obterListaCursos(PASTA_BD);
        if (cursos.length == 0) {
            view.mostrarErroNaoEncontrado("Cursos");
            return;
        }

        view.mostrarListaCursos(cursos);
        int escolha = view.pedirOpcaoCurso(cursos.length);
        String siglaEditar = cursos[escolha - 1].split(" - ")[0];

        if (repo.podeEditarCurso(siglaEditar, PASTA_BD)) {
            if (ExportadorCSV.removerLinhaCSV("cursos.csv", siglaEditar, PASTA_BD)) {
                view.mostrarMensagemModoEdicao();
                String novaLinha = siglaEditar + ";" + view.pedirNomeCurso() + ";" + view.pedirNovoDepartamento();
                ExportadorCSV.adicionarLinhaCSV("cursos.csv", novaLinha, PASTA_BD);
                view.mostrarSucessoAtualizacao("Curso");
            }
        } else {
            view.mostrarErroEdicaoCurso();
        }
    }

    private void removerCurso() {
        String[] cursos = ImportadorCSV.obterListaCursos(PASTA_BD);
        if (cursos.length == 0) {
            view.mostrarErroNaoEncontrado("Cursos");
            return;
        }

        view.mostrarListaCursos(cursos);
        int escolha = view.pedirOpcaoCurso(cursos.length);
        String siglaRemover = cursos[escolha - 1].split(" - ")[0];

        if (repo.podeEditarCurso(siglaRemover, PASTA_BD)) {
            if (ExportadorCSV.removerLinhaCSV("cursos.csv", siglaRemover, PASTA_BD)) {
                view.mostrarSucessoRemocao("Curso");
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
                case 1:
                    view.mostrarMensagem(Estatisticas.calcularMediaGlobal(PASTA_BD));
                    break; //
                case 2:
                    view.mostrarMensagem(Estatisticas.obterMelhorAluno(PASTA_BD));
                    break;
                case 0:
                    correr = false;
                    break;
                default:
                    view.mostrarMensagem("Opção inválida.");
            }
        }
    }

    private void executarRegistoEstudante() {
        view.mostrarTituloRegistoEstudante();

        // 1. GERAR NÚMERO AUTOMATICAMENTE com base no Ano Atual do sistema
        int anoInscricao = repo.getAnoAtual();
        int numMec = ImportadorCSV.obterProximoNumeroMecanografico(PASTA_BD, anoInscricao);
        view.mostrarNumMecanograficoAtribuido(numMec);

        // 2. PEDIR E VALIDAR NOME
        String nome;
        do {
            nome = view.pedirNome();
            if (!Validador.isNomeValido(nome)) {
                view.mostrarErroNomeInvalido();
            }
        } while (!Validador.isNomeValido(nome));

        // 3. PEDIR E VALIDAR NIF
        String nif;
        do {
            nif = view.pedirNif();
            if (!Validador.isNifValido(nif)) {
                view.mostrarErroNifInvalido();
            }
        } while (!Validador.isNifValido(nif));

        String morada = view.pedirMorada();

        // 4. PEDIR E VALIDAR DATA DE NASCIMENTO
        String dataNasc;
        do {
            dataNasc = view.pedirDataNascimento();
            if (!Validador.isDataNascimentoValida(dataNasc)) {
                view.mostrarErroDataInvalida();
            }
        } while (!Validador.isDataNascimentoValida(dataNasc));

        // 5. SELEÇÃO DE CURSO POR LISTA
        String siglaCurso = "";
        String[] listaCursos = ImportadorCSV.obterListaCursos(PASTA_BD);

        if (listaCursos.length > 0) {
            view.mostrarListaCursos(listaCursos);
            int escolha = view.pedirOpcaoCurso(listaCursos.length);

            siglaCurso = listaCursos[escolha - 1].split(" - ")[0];
        } else {
            view.mostrarAvisoSemCursos();
            siglaCurso = view.pedirSiglaCurso();
        }

        // 6. GERAR CREDENCIAIS AUTOMÁTICAS
        String email = EmailGenerator.gerarEmailEstudante(numMec);
        String passLimpa = PasswordGenerator.gerarPasswordSegura();
        String passSegura = SegurancaPasswords.gerarCredencialMista(passLimpa);

        // 7. DEFINIR SALDO DEVEDOR INICIAL COM BASE NO CURSO
        Estudante novo = new Estudante(numMec, email, passSegura, nome, nif, morada, dataNasc, anoInscricao);

        Curso cursoEscolhido = ImportadorCSV.procurarCurso(siglaCurso, PASTA_BD); //
        if (cursoEscolhido != null) {
            novo.setSaldoDevedor(cursoEscolhido.getValorPropinaAnual()); //
        }
        // 8. GUARDAR O ESTUDANTE
        ExportadorCSV.adicionarEstudante(novo, PASTA_BD, siglaCurso);
        view.mostrarSucessoRegistoEstudante(email, passLimpa);
    }

    private void listarUcsDoCurso() {
        // 1. Obtém e mostra a lista de cursos
        String[] cursos = ImportadorCSV.obterListaCursos(PASTA_BD);
        if (cursos.length == 0) {
            view.mostrarErroNaoEncontrado("Cursos");
            return;
        }

        view.mostrarListaCursos(cursos);

        // 2. Pede ao utilizador para selecionar o número
        int escolha = view.pedirOpcaoCurso(cursos.length);
        String siglaCurso = cursos[escolha - 1].split(" - ")[0];

        // 3. Imprime a listagem de UCs formatada
        view.mostrarMensagem(ImportadorCSV.listarUcsPorCurso(siglaCurso, PASTA_BD));
    }

    private void iniciarAnoLetivo() {
        view.mostrarMensagem("\n--- ARRANQUE DO ANO LETIVO ---");

        String[] cursos = ImportadorCSV.obterListaCursos(PASTA_BD);
        if (cursos.length == 0) {
            view.mostrarMensagem(">> Erro: Não existem cursos registados no sistema.");
            return;
        }

        view.mostrarMensagem("A verificar quórum dos cursos...");

        // VALIDAR QUÓRUM E ATUALIZAR ESTADO DOS CURSOS
        for (String c : cursos) {
            String siglaCurso = c.split(" - ")[0];
            Curso curso = ImportadorCSV.procurarCurso(siglaCurso, PASTA_BD);
            if (curso == null) continue;


            int alunos1oAno = ExportadorCSV.contarEstudantesPorCursoEAno(siglaCurso, 1, PASTA_BD);
            int alunos2oAno = ExportadorCSV.contarEstudantesPorCursoEAno(siglaCurso, 2, PASTA_BD);
            int alunos3oAno = ExportadorCSV.contarEstudantesPorCursoEAno(siglaCurso, 3, PASTA_BD);

            //  Mínimo 5 alunos no 1º ano. 2º e 3º ano podem arrancar com 1 aluno. Manter curso aberto ou abrir curso
            if (alunos1oAno < 5 && alunos1oAno > 0) {
                view.mostrarMensagem(">> ERRO: Curso " + siglaCurso + " abortado. Apenas " + alunos1oAno + " alunos no 1º Ano (Mínimo: 5).");
                curso.setEstado("Inativo");
            } else if (alunos1oAno >= 5 || alunos2oAno >= 1 || alunos3oAno >= 1) {
                view.mostrarMensagem(">> Curso " + siglaCurso + " cumpre os requisitos e está ATIVO.");
                curso.setEstado("Ativo");
            } else {
                curso.setEstado("Inativo"); // alunos matriculados
            }


            ExportadorCSV.atualizarCurso(curso, PASTA_BD);
        }

        // 2. TRANSITAR ALUNOS DE ANO (VALIDAÇÃO FINANCEIRA)
        view.mostrarMensagem("\nA processar transições de estudantes...");
        Estudante[] estudantes = ImportadorCSV.carregarTodosEstudantes(PASTA_BD);

        for (Estudante e : estudantes) {
            if (e == null) continue;

            // Bloquear se saldoDevedor não for 0.0,ignora média de notas
            if (e.getSaldoDevedor() != 0.0) {
                view.mostrarMensagem(">> BLOQUEIO: Aluno " + e.getNumeroMecanografico() + " (" + e.getNome() +
                        ") retido no " + e.getAnoCurricular() + "º Ano devido a dívida de " +
                        e.getSaldoDevedor() + "€. (Média ignorada)");
            } else {
                // Não tem dívidas, transita de ano
                if (e.getAnoCurricular() < 3) {
                    e.setAnoCurricular(e.getAnoCurricular() + 1);
                    view.mostrarMensagem(">> SUCESSO: Aluno " + e.getNumeroMecanografico() + " transitou para o " + e.getAnoCurricular() + "º Ano.");
                } else {
                    view.mostrarMensagem(">> SUCESSO: Aluno " + e.getNumeroMecanografico() + " concluiu o curso!");
                }
                // Guarda o novo ano curricular na base de dados
                ExportadorCSV.atualizarEstudante(e, PASTA_BD);
            }
        }

        //AVANÇAR O SISTEMA
        repo.setAnoAtual(repo.getAnoAtual() + 1);
        view.mostrarMensagem("\n>> Ano Letivo avançado com sucesso para " + repo.getAnoAtual() + "!");
    }
}