package controller;

import model.*;
import view.GestorView;
import utils.*;

public class GestorController {
    private RepositorioDados repo;
    private Gestor gestor;
    private GestorView view;

    private static final String PASTA_BD = "LP2-Grupo1/bd";

    public GestorController(RepositorioDados repo, Gestor gestor) {
        this.repo = repo;
        this.gestor = gestor;
        this.view = new GestorView();
    }

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
                    case 5: iniciarAnoLetivo(); break;
                    case 6: listarDevedores(); break;
                    case 7: alterarPassword(); break;
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

    // --- GESTÃO DE ESTUDANTES E ADMINISTRAÇÃO ---

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

        String email = EmailGenerator.gerarEmailEstudante(numMec);
        String passLimpa = PasswordGenerator.gerarPasswordSegura();

        EmailService.enviarCredenciaisTodos(nome, email, passLimpa);

        String passSegura = SegurancaPasswords.gerarCredencialMista(passLimpa);

        Estudante novo = new Estudante(numMec, email, passSegura, nome, nif, morada, dataNasc, anoInscricao);

        Curso cursoEscolhido = ImportadorCSV.procurarCurso(siglaCurso, PASTA_BD);
        if (cursoEscolhido != null) {
            novo.setSaldoDevedor(cursoEscolhido.getValorPropinaAnual());
        }

        ExportadorCSV.adicionarEstudante(novo, PASTA_BD, siglaCurso);

        view.mostrarResumoRegistoEstudante(email);
    }

    private void alterarPassword() {
        view.mostrarCabecalhoAlterarPassword();
        String novaPass = view.pedirNovaPassword();

        if (!novaPass.trim().isEmpty()) {
            String passSegura = SegurancaPasswords.gerarCredencialMista(novaPass);
            gestor.setPassword(passSegura);
            ExportadorCSV.atualizarPasswordCentralizada(gestor.getEmail(), passSegura, PASTA_BD);
            view.mostrarSucessoAlteracaoPassword();
        } else {
            view.mostrarCancelamentoPassword();
        }
    }

    private void iniciarAnoLetivo() {
        view.mostrarCabecalhoArranqueAnoLetivo();

        String[] cursos = ImportadorCSV.obterListaCursos(PASTA_BD);
        if (cursos.length == 0) {
            view.mostrarErroCarregarDados("Cursos");
            return;
        }

        view.mostrarVerificacaoQuorum();

        for (String c : cursos) {
            String siglaCurso = c.split(" - ")[0];
            Curso curso = ImportadorCSV.procurarCurso(siglaCurso, PASTA_BD);
            if (curso == null) continue;

            int alunos1oAno = ExportadorCSV.contarEstudantesPorCursoEAno(siglaCurso, 1, PASTA_BD);
            int alunos2oAno = ExportadorCSV.contarEstudantesPorCursoEAno(siglaCurso, 2, PASTA_BD);
            int alunos3oAno = ExportadorCSV.contarEstudantesPorCursoEAno(siglaCurso, 3, PASTA_BD);

            if (alunos1oAno < 5 && alunos1oAno > 0) {
                view.mostrarErroQuorum(siglaCurso, alunos1oAno);
                curso.setEstado("Inativo");
            } else if (alunos1oAno >= 5 || alunos2oAno >= 1 || alunos3oAno >= 1) {
                view.mostrarSucessoQuorum(siglaCurso);
                curso.setEstado("Ativo");
            } else {
                curso.setEstado("Inativo");
            }

            ExportadorCSV.atualizarCurso(curso, PASTA_BD);
        }

        view.mostrarProcessamentoTransicoes();
        Estudante[] estudantes = ImportadorCSV.carregarTodosEstudantes(PASTA_BD);

        for (Estudante e : estudantes) {
            if (e == null) continue;

            if (e.getSaldoDevedor() != 0.0) {
                view.mostrarBloqueioDivida(e.getNumeroMecanografico(), e.getNome(), e.getAnoCurricular(), e.getSaldoDevedor());
            } else {
                if (e.getAnoCurricular() < 3) {
                    e.setAnoCurricular(e.getAnoCurricular() + 1);
                    view.mostrarTransicaoSucedida(e.getNumeroMecanografico(), e.getAnoCurricular());
                } else {
                    view.mostrarConclusaoCurso(e.getNumeroMecanografico());
                }
                ExportadorCSV.atualizarEstudante(e, PASTA_BD);
            }
        }

        repo.setAnoAtual(repo.getAnoAtual() + 1);
        view.mostrarSucessoAvancoAno(repo.getAnoAtual());
    }

    private void listarDevedores() {
        view.mostrarCabecalhoDevedores();
        Estudante[] estudantes = ImportadorCSV.carregarTodosEstudantes(PASTA_BD);
        boolean encontrou = false;

        if (estudantes == null || estudantes.length == 0) {
            view.mostrarErroCarregarDados("Estudantes");
            return;
        }

        for (Estudante e : estudantes) {
            if (e != null && e.getSaldoDevedor() > 0) {
                view.mostrarEstudanteDevedor(e.getNumeroMecanografico(), e.getNome(), e.getSaldoDevedor());
                encontrou = true;
            }
        }

        if (!encontrou) {
            view.mostrarSemDevedores();
        }
    }

    // --- ESTATÍSTICAS ---

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

    private void mostrarMediaGlobal() {
        view.mostrarCabecalhoMediaGlobal();
        Estudante[] estudantes = ImportadorCSV.carregarTodosEstudantes(PASTA_BD);

        if (estudantes == null) {
            view.mostrarErroCarregarDados("Estudantes");
            return;
        }

        double soma = 0;
        int totalNotas = 0;

        for (Estudante e : estudantes) {
            if (e == null || e.getPercurso() == null) continue;

            for (int i = 0; i < e.getPercurso().getTotalAvaliacoes(); i++) {
                Avaliacao av = e.getPercurso().getHistoricoAvaliacoes()[i];
                if (av != null && av.getResultados() != null) {
                    for (int j = 0; j < av.getTotalAvaliacoesLancadas(); j++) {
                        soma += av.getResultados()[j];
                        totalNotas++;
                    }
                }
            }
        }

        if (totalNotas == 0) {
            view.mostrarSemNotasRegistadas();
        } else {
            view.mostrarMediaGlobal(soma / totalNotas, totalNotas);
        }
    }

    private void mostrarMelhorAluno() {
        view.mostrarCabecalhoMelhorAluno();
        Estudante[] estudantes = ImportadorCSV.carregarTodosEstudantes(PASTA_BD);
        Estudante melhor = null;
        double maiorMedia = -1;

        if (estudantes == null) return;

        for (Estudante e : estudantes) {
            if (e == null || e.getPercurso() == null || e.getPercurso().getTotalAvaliacoes() == 0) continue;

            double somaMedias = 0;
            int totalAvaliacoes = e.getPercurso().getTotalAvaliacoes();

            for (int i = 0; i < totalAvaliacoes; i++) {
                Avaliacao av = e.getPercurso().getHistoricoAvaliacoes()[i];
                if (av != null) {
                    somaMedias += av.calcularMedia();
                }
            }

            double mediaAluno = somaMedias / totalAvaliacoes;
            if (mediaAluno > maiorMedia) {
                maiorMedia = mediaAluno;
                melhor = e;
            }
        }

        if (melhor != null) {
            view.mostrarInfoMelhorAluno(melhor.getNome(), melhor.getNumeroMecanografico(), maiorMedia);
        } else {
            view.mostrarSemAlunosAvaliados();
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
                case 5: associarUcACurso(); break;
                case 0: correr = false; break;
                default: view.mostrarOpcaoInvalida();
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
        String siglaCurso = cursos[escolha - 1].split(" - ")[0];

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
        view.mostrarResultadosListagem(ImportadorCSV.listarTodasUcs(PASTA_BD));
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
            view.mostrarErroCarregarDados("UCs");
            return;
        }

        String[] cursos = ImportadorCSV.obterListaCursos(PASTA_BD);
        if (cursos.length == 0) {
            view.mostrarAvisoSemCursos();
            return;
        }
        view.mostrarListaCursos(cursos);
        int escolhaCurso = view.pedirOpcaoCurso(cursos.length);
        String siglaCurso = cursos[escolhaCurso - 1].split(" - ")[0];

        if (repo.podeAdicionarUc(siglaCurso, ucExistente.getAnoCurricular(), PASTA_BD)) {
            String siglaDoc = (ucExistente.getDocenteResponsavel() != null) ? ucExistente.getDocenteResponsavel().getSigla() : "N/A";

            String novaLinha = ucExistente.getSigla() + ";" +
                    ucExistente.getNome() + ";" +
                    ucExistente.getAnoCurricular() + ";" +
                    siglaDoc + ";" +
                    siglaCurso;

            ExportadorCSV.adicionarLinhaCSV("ucs.csv", novaLinha, PASTA_BD);
            view.mostrarSucessoAssociacaoUc(ucExistente.getNome(), siglaCurso);
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
                default: view.mostrarOpcaoInvalida();
            }
        }
    }

    private void adicionarCurso() {
        String sigla = view.pedirSiglaCurso();
        String nome = view.pedirNomeCurso();
        String dep = view.pedirDepartamento();
        double propina = view.pedirValorDouble("Valor da Propina Anual (€)");

        String linha = sigla + ";" + nome + ";" + dep + ";" + propina + ";Inativo";
        ExportadorCSV.adicionarLinhaCSV("cursos.csv", linha, PASTA_BD);
        view.mostrarSucessoCriacao("Curso");
    }

    private void listarCursos() {
        view.mostrarResultadosListagem(ImportadorCSV.listarTodosCursos(PASTA_BD));
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

    private void listarUcsDoCurso() {
        String[] cursos = ImportadorCSV.obterListaCursos(PASTA_BD);
        if (cursos.length == 0) {
            view.mostrarErroNaoEncontrado("Cursos");
            return;
        }

        view.mostrarListaCursos(cursos);
        int escolha = view.pedirOpcaoCurso(cursos.length);
        String siglaCurso = cursos[escolha - 1].split(" - ")[0];

        view.mostrarResultadosListagem(ImportadorCSV.listarUcsPorCurso(siglaCurso, PASTA_BD));
    }
}