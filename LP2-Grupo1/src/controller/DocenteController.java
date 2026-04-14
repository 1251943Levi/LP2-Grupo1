package controller;

import model.*;
import view.DocenteView;
import utils.ImportadorCSV;
import utils.ExportadorCSV;
import utils.SegurancaPasswords;

public class DocenteController {
    private RepositorioDados repo;
    private Docente docente;
    private DocenteView view;

    private static final String PASTA_BD = "bd";

    public DocenteController(RepositorioDados repo, Docente docente) {
        this.repo = repo;
        this.docente = docente;
        this.view = new DocenteView();
        ImportadorCSV.carregarUcsDoDocente(this.docente, PASTA_BD);
    }

    public void iniciar() {
        boolean correr = true;
        while (correr) {
            try {
                int opcao = view.mostrarMenu();
                switch (opcao) {
                    case 1: listarMeusAlunos(); break;
                    case 2: executarLancamentoNotas(); break;
                    case 3: alterarPassword(); break;
                    case 0: correr = false; break;
                    default: view.mostrarOpcaoInvalida();
                }
            } catch (Exception e) {
                view.mostrarErroLeituraOpcao();
            }
        }
    }

    private void listarMeusAlunos() {
        // Prevenção de NullPointerException (Carrega os dados e valida logo)
        Estudante[] todos = ImportadorCSV.carregarTodosEstudantes(PASTA_BD);

        if (todos == null || todos.length == 0) {
            // Se o repositório estiver completamente vazio, envia array vazio para a View
            view.mostrarListaAlunos(new String[0]);
            return;
        }

        String[] bufferAlunos = new String[1000];
        int contador = 0;

        // Filtrar apenas os alunos que partilham UCs com este Docente
        for (Estudante e : todos) {
            if (e == null || e.getPercurso() == null) continue;

            for (int i = 0; i < e.getPercurso().getTotalUcsInscrito(); i++) {
                UnidadeCurricular ucInscrita = e.getPercurso().getUcsInscrito()[i];

                // Se a UC inscrita coincidir com alguma lecionada pelo docente
                if (ucInscrita != null && lecionoEstaUC(ucInscrita.getSigla())) {

                    // Formata a string conforme o requisito e guarda
                    bufferAlunos[contador] = "Nº: " + e.getNumeroMecanografico() +
                            " | Nome: " + e.getNome() +
                            " | UC: " + ucInscrita.getNome();
                    contador++;
                }
            }
        }

        String[] resultadoFinal = new String[contador];
        System.arraycopy(bufferAlunos, 0, resultadoFinal, 0, contador);
        view.mostrarListaAlunos(resultadoFinal);
    }

    private boolean lecionoEstaUC(String siglaUc) {
        if (siglaUc == null) return false;
        for (int i = 0; i < docente.getTotalUcsLecionadas(); i++) {
            UnidadeCurricular uc = docente.getUcsLecionadas()[i];
            if (uc != null && uc.getSigla().equalsIgnoreCase(siglaUc)) {
                return true;
            }
        }
        return false;
    }

    private UnidadeCurricular obterUcLecionada(String siglaUc) {
        for (int i = 0; i < docente.getTotalUcsLecionadas(); i++) {
            UnidadeCurricular uc = docente.getUcsLecionadas()[i];
            if (uc != null && uc.getSigla().equalsIgnoreCase(siglaUc)) {
                return uc;
            }
        }
        return null;
    }

    private void executarLancamentoNotas() {
        view.mostrarCabecalhoLancamentoNotas();

        try {
            int numAluno = view.pedirNumeroAluno();
            String siglaUc = view.pedirSiglaUc();
            int anoLetivo = view.pedirAnoLetivo();

            double nNormal = view.pedirNotaNormal();
            double nRecurso = view.pedirNotaRecurso();
            double nEspecial = view.pedirNotaEspecial();

            Estudante aluno = ImportadorCSV.procurarEstudantePorNumMec(numAluno, PASTA_BD);

            if (aluno != null) {
                UnidadeCurricular uc = new UnidadeCurricular(siglaUc, "UC Lançada", 1, docente);
                Avaliacao aval = new Avaliacao(uc, anoLetivo);

                aval.adicionarResultado(nNormal);
                aval.adicionarResultado(nRecurso);
                aval.adicionarResultado(nEspecial);

                ExportadorCSV.adicionarAvaliacao(aval, aluno.getNumeroMecanografico(), PASTA_BD);

                view.mostrarSucessoLancamento();
            } else {
                view.mostrarErroAlunoNaoEncontrado(numAluno);
            }
        } catch (NumberFormatException e) {
            view.mostrarErroLeituraOpcao(); // Protege caso o Docente escreva letras em vez de números nas notas
        }
    }

    private void alterarPassword() {
        view.mostrarCabecalhoAlterarPassword();
        String novaPass = view.pedirNovaPassword();

        if (!novaPass.trim().isEmpty()) {
            String passSegura = SegurancaPasswords.gerarCredencialMista(novaPass);
            docente.setPassword(passSegura);
            ExportadorCSV.atualizarPasswordCentralizada(docente.getEmail(), passSegura, PASTA_BD);
            view.mostrarSucessoAlteracaoPassword();
        } else {
            view.mostrarCancelamentoPassword();
        }
    }
}