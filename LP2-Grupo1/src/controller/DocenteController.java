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
            int opcao = view.mostrarMenu();
            switch (opcao) {
                case 1: listarMeusAlunos(); break;
                case 2: lancarNotas(); break;
                case 3: alterarPassword(); break;
                case 0: correr = false; break;
                default: view.mostrarMensagem("Opção inválida.");
            }
        }
    }

    private void listarMeusAlunos() {
        view.mostrarMensagem("\n--- OS MEUS ALUNOS ---");
        Estudante[] todos = ImportadorCSV.carregarTodosEstudantes(PASTA_BD);
        double somaDocente = 0;
        int totalNotasDocente = 0;
        boolean encontrou = false;

        for (Estudante e : todos) {
            if (e == null) continue;
            boolean alunoDoDocente = false;

            for (int i = 0; i < e.getPercurso().getTotalUcsInscrito(); i++) {
                if (lecionoEstaUC(e.getPercurso().getUcsInscrito()[i])) {
                    alunoDoDocente = true; break;
                }
            }

            if (alunoDoDocente) {
                encontrou = true;
                System.out.println("Nº: " + e.getNumeroMecanografico() + " | Aluno: " + e.getNome());

                for (int i = 0; i < e.getPercurso().getTotalAvaliacoes(); i++) {
                    Avaliacao av = e.getPercurso().getHistoricoAvaliacoes()[i];
                    if (lecionoEstaUC(av.getUc())) {
                        for (int j = 0; j < av.getTotalAvaliacoesLancadas(); j++) {
                            somaDocente += av.getResultados()[j];
                            totalNotasDocente++;
                        }
                    }
                }
            }
        }

        if (!encontrou) view.mostrarMensagem("Não tem alunos nas suas UCs.");
        else if (totalNotasDocente > 0) {
            view.mostrarMensagem("Média das suas disciplinas: " + String.format("%.2f", (somaDocente / totalNotasDocente)));
        }
    }

    private boolean lecionoEstaUC(UnidadeCurricular uc) {
        for (int i = 0; i < docente.getTotalUcsLecionadas(); i++) {
            if (docente.getUcsLecionadas()[i].getSigla().equalsIgnoreCase(uc.getSigla())) return true;
        }
        return false;
    }

    private void lancarNotas() {
        int numAluno = Integer.parseInt(view.pedirInput("Nº Aluno"));
        String siglaUc = view.pedirInput("Sigla UC");
        double nota = Double.parseDouble(view.pedirInput("Nota (0-20)"));

        Estudante aluno = ImportadorCSV.procurarEstudantePorNumMec(numAluno, PASTA_BD);
        if (aluno != null) {
            UnidadeCurricular uc = new UnidadeCurricular(siglaUc, "UC", 1, docente);
            Avaliacao aval = new Avaliacao(uc, 2026);
            aval.adicionarResultado(nota);
            ExportadorCSV.adicionarAvaliacao(aval, aluno.getNumeroMecanografico(), PASTA_BD);
            view.mostrarMensagem("Nota registada!");
        } else {
            view.mostrarMensagem("Aluno não encontrado.");
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