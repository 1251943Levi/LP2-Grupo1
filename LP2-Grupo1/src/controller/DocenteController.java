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
                    case 2: lancarNotas(); break;
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

    private void lancarNotas() {
        try {
            int numAluno = Integer.parseInt(view.pedirInput("Nº Aluno"));
            String siglaUc = view.pedirInput("Sigla UC");

            UnidadeCurricular ucReal = obterUcLecionada(siglaUc);
            if (ucReal == null) {
                view.mostrarMensagem("Erro: Não leciona nenhuma unidade curricular com a sigla '" + siglaUc + "'.");
                return;
            }

            double nota = Double.parseDouble(view.pedirInput("Nota (0-20)"));

            if (nota < 0 || nota > 20) {
                view.mostrarMensagem("Erro: A nota inserida é inválida. Deve estar entre 0 e 20.");
                return;
            }

            Estudante aluno = ImportadorCSV.procurarEstudantePorNumMec(numAluno, PASTA_BD);
            if (aluno != null) {
                int anoAtual = java.time.Year.now().getValue(); // Evita usar "2026" hardcoded
                Avaliacao aval = new Avaliacao(ucReal, anoAtual);
                aval.adicionarResultado(nota);

                ExportadorCSV.adicionarAvaliacao(aval, aluno.getNumeroMecanografico(), PASTA_BD);
                view.mostrarMensagem("Nota registada com sucesso!");
            } else {
                view.mostrarMensagem("Erro: Aluno não encontrado no sistema.");
            }
        } catch (NumberFormatException e) {
            view.mostrarMensagem("Erro: Formato inválido. Certifique-se de que introduz apenas números no Nº de Aluno e na Nota.");
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