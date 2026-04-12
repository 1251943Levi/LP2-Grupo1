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
                case 1: executarRegistoEstudante(); break;
                case 2: iniciarAnoLetivo(); break;
                case 2: menuGerirUcs(); break;
                case 3: menuGerirCursos(); break;
                case 4: menuEstatisticas(); break;
                case 5: listarDevedores(); break;
                case 0: correr = false; break;
                default: view.mostrarMensagem("Opção inválida.");
            }
        }
    }

    // --- GESTÃO DE UNIDADES CURRICULARES (UCs) ---
    private void mostrarMediaGlobal() {
        Estudante[] estudantes = ImportadorCSV.carregarTodosEstudantes(PASTA_BD);
        double soma = 0;
        int totalNotas = 0;

    private void menuGerirUcs() {
        boolean correr = true;
        while (correr) {
            int opcao = view.mostrarMenuCRUD("Unidades Curriculares");
            switch (opcao) {
                case 1: adicionarUc(); break;
                case 2: listarUcs(); break;
                case 3: editarUc(); break;
                case 4: removerUc(); break;
                case 5: associarUcACurso(); break;
                case 0: correr = false; break;
                default: view.mostrarMensagem("Opção inválida.");
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
                case 1: adicionarCurso(); break;
                case 2: listarCursos(); break;
                case 3: editarCurso(); break;
                case 4: removerCurso(); break;
                case 5: listarUcsDoCurso(); break;
                case 0: correr = false; break;
                default: view.mostrarMensagem("Opção inválida.");
            }
        }
    }

    private void adicionarCurso() {
        // Ao adicionar um curso, apenas precisamos dos dados (não precisamos da lista)
        String linha = view.pedirSiglaCurso() + ";" + view.pedirNomeCurso() + ";" + view.pedirDepartamento();
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
                case 1: view.mostrarMensagem(Estatisticas.calcularMediaGlobal(PASTA_BD)); break; //
                case 2: view.mostrarMensagem(Estatisticas.obterMelhorAluno(PASTA_BD)); break;
                case 0: correr = false; break;
                default: view.mostrarMensagem("Opção inválida.");
            }
        }
    }
    /**
     * Regista um novo estudante no sistema.
     * Além de recolher os dados pessoais e gerar as credenciais de acesso,
     * associa o estudante a um Curso específico e inicializa automaticamente
     * o seu saldo devedor com o valor da propina anual desse curso.
     * Todos os dados são exportados e guardados imediatamente na base de dados (CSV).
     */
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

        // 7. GUARDAR O ESTUDANTE
        Estudante novo = new Estudante(numMec, email, passSegura, nome, nif, morada, dataNasc, anoInscricao);
        //Herda a propina do curso
        Curso cursoEscolhido = ImportadorCSV.procurarCurso(siglaCurso, PASTA_BD);
        if (cursoEscolhido != null) {
            novo.setSaldoDevedor(cursoEscolhido.getValorPropinaAnual());
        } else {
            novo.setSaldoDevedor(1000.0); // Valor caso o curso não seja encontrado
        }
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

    /**
     * Valida o quórum de um curso e tenta passá-lo ao estado Ativo.
     * Processa a transição de ano dos alunos matriculados, bloqueando devedores.
     */
    private void iniciarAnoLetivo() {
        view.mostrarMensagem("\n--- ARRANQUE DE ANO LETIVO E PROGRESSÃO ---");
        String siglaCurso = view.pedirInput("Sigla do Curso para arrancar");

        Curso curso = ImportadorCSV.procurarCurso(siglaCurso, PASTA_BD);
        if (curso == null) {
            view.mostrarMensagem("Curso não encontrado no sistema.");
            return;
        }

        Estudante[] todos = ImportadorCSV.carregarTodosEstudantes(PASTA_BD);
        int alunos1Ano = 0, alunos2Ano = 0, alunos3Ano = 0;

        // Contagem de Quórum por Ano Curricular
        for (Estudante e : todos) {
            if (e != null && e.getSiglaCurso() != null && e.getSiglaCurso().equalsIgnoreCase(siglaCurso)) {
                if (e.getAnoCurricular() == 1) alunos1Ano++;
                else if (e.getAnoCurricular() == 2) alunos2Ano++;
                else if (e.getAnoCurricular() == 3) alunos3Ano++;
            }
        }

        // Validação do 1º Ano
        if (alunos1Ano < 5) {
            view.mostrarMensagem("ERRO FATAL: O curso " + siglaCurso + " tem apenas " + alunos1Ano + " alunos no 1º ano.");
            view.mostrarMensagem("Quórum mínimo não atingido (5). Arranque abortado e curso mantém-se INATIVO.");
            curso.setEstado("Inativo");
            return;
        }

        // Para os 2º e 3º anos, a regra aceita arrancar com 1 aluno ou mais.
        view.mostrarMensagem("Quórum verificado! 1º Ano: " + alunos1Ano + " | 2º Ano: " + alunos2Ano + " | 3º Ano: " + alunos3Ano);
        curso.setEstado("Ativo");
        view.mostrarMensagem("Estado do Curso atualizado para: ATIVO.");

        // Simulação Progressão Escolar (Avançar de Ano)
        view.mostrarMensagem("\nA processar progressão de alunos...");
        int promovidos = 0, retidos = 0;

        for (Estudante e : todos) {
            if (e != null && e.getSiglaCurso() != null && e.getSiglaCurso().equalsIgnoreCase(siglaCurso)) {

                // Validação Financeira (Ignora notas)
                if (e.getSaldoDevedor() > 0.0) {
                    view.mostrarMensagem("BLOQUEADO: Aluno " + e.getNumeroMecanografico() + " não transita devido a propinas em atraso (" + String.format("%.2f", e.getSaldoDevedor()) + "€).");
                    retidos++;
                } else {
                    // Transita de ano (Se ainda não estiver no último ano do curso)
                    if (e.getAnoCurricular() < curso.getDuracaoAnos()) {
                        e.setAnoCurricular(e.getAnoCurricular() + 1);
                        ExportadorCSV.atualizarEstudante(e, PASTA_BD);
                        view.mostrarMensagem("SUCESSO: Aluno " + e.getNumeroMecanografico() + " avançou para o " + e.getAnoCurricular() + "º Ano.");
                        promovidos++;
                    } else {
                        view.mostrarMensagem("INFO: Aluno " + e.getNumeroMecanografico() + " já se encontra no último ano (Finalista).");
                    }
                }
            }
        }
        view.mostrarMensagem("\nResumo do Arranque: " + promovidos + " alunos promovidos | " + retidos + " alunos retidos por dívida.");
    }
}