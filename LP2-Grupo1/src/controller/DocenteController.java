package controller;

import model.*;
import view.DocenteView;
import utils.ImportadorCSV;
import utils.ExportadorCSV;

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
                    default: view.mostrarMensagem("Opção inválida.");
                }
            } catch (Exception e) {
                view.mostrarMensagem("Erro na leitura da opção. Tente novamente.");
            }
        }
    }

    private void listarMeusAlunos() {
        view.mostrarMensagem("\n--- OS MEUS ALUNOS ---");
        Estudante[] todos = ImportadorCSV.carregarTodosEstudantes(PASTA_BD);
        double somaDocente = 0;
        int totalNotasDocente = 0;
        boolean encontrou = false;

        if (todos == null) {
            view.mostrarMensagem("Erro ao carregar a lista de estudantes.");
            return;
        }

        for (Estudante e : todos) {
            if (e == null || e.getPercurso() == null) continue;
            boolean alunoDoDocente = false;

            for (int i = 0; i < e.getPercurso().getTotalUcsInscrito(); i++) {
                if (e.getPercurso().getUcsInscrito()[i] != null &&
                        lecionoEstaUC(e.getPercurso().getUcsInscrito()[i].getSigla())) {
                    alunoDoDocente = true;
                    break;
                }
            }

            if (alunoDoDocente) {
                encontrou = true;
                view.mostrarMensagem("Nº: " + e.getNumeroMecanografico() + " | Aluno: " + e.getNome());

                for (int i = 0; i < e.getPercurso().getTotalAvaliacoes(); i++) {
                    Avaliacao av = e.getPercurso().getHistoricoAvaliacoes()[i];
                    if (av != null && av.getUc() != null && lecionoEstaUC(av.getUc().getSigla())) {
                        for (int j = 0; j < av.getTotalAvaliacoesLancadas(); j++) {
                            somaDocente += av.getResultados()[j];
                            totalNotasDocente++;
                        }
                    }
                }
            }
        }

        if (!encontrou) {
            view.mostrarMensagem("Não tem alunos inscritos nas suas UCs.");
        } else if (totalNotasDocente > 0) {
            view.mostrarMensagem("Média das suas disciplinas: " + String.format("%.2f", (somaDocente / totalNotasDocente)));
        }
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

    /**
     * Extraído do Case 2: Gere o processo de procura de aluno e gravação de notas.
     */
    private void executarLancamentoNotas() {
        view.mostrarMensagem("\n--- LANÇAMENTO DE NOTAS ---");

        int numAluno = Integer.parseInt(view.pedirInput("Nº Aluno"));
        String siglaUc = view.pedirInput("Sigla UC");
        int anoLetivo = Integer.parseInt(view.pedirInput("Ano Letivo (ex: 2026)"));

        double nNormal = Double.parseDouble(view.pedirInput("Nota Normal (ou -1 se faltou)"));
        double nRecurso = Double.parseDouble(view.pedirInput("Nota Recurso (ou -1 se faltou)"));
        double nEspecial = Double.parseDouble(view.pedirInput("Nota Especial (ou -1 se faltou)"));

        Estudante aluno = ImportadorCSV.procurarEstudantePorNumMec(numAluno, PASTA_BD);

        if (aluno != null) {
            UnidadeCurricular uc = new UnidadeCurricular(siglaUc, "UC Lançada", 1, docente);
            Avaliacao aval = new Avaliacao(uc, anoLetivo);

            aval.adicionarResultado(nNormal);
            aval.adicionarResultado(nRecurso);
            aval.adicionarResultado(nEspecial);

            ExportadorCSV.adicionarAvaliacao(aval, aluno.getNumeroMecanografico(), PASTA_BD);

            view.mostrarMensagem("Notas lançadas e guardadas com sucesso na base de dados!");
        } else {
            view.mostrarMensagem("ERRO: Aluno com o número " + numAluno + " não encontrado.");
        }
    }

    private void alterarPassword() {
        view.mostrarMensagem("\n--- ALTERAR PASSWORD ---");
        String novaPass = view.pedirInput("Introduza a nova Password (ou prima Enter para cancelar)");

        if (!novaPass.trim().isEmpty()) {
            String passSegura = utils.SegurancaPasswords.gerarCredencialMista(novaPass);
            docente.setPassword(passSegura);
            ExportadorCSV.atualizarPasswordCentralizada(docente.getEmail(), passSegura, PASTA_BD);
            view.mostrarMensagem("Password alterada com sucesso!");
        } else {
            view.mostrarMensagem("Operação cancelada. A password não foi alterada.");
        }
    }
}