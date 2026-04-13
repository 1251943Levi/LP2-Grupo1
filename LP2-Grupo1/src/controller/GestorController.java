package controller;

import model.*;
import view.GestorView;
import utils.*;

public class GestorController {
    private RepositorioDados repo;
    private Gestor gestor;
    private GestorView view;
    private static final String PASTA_BD = "bd";

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
                    case 2: view.mostrarMensagem("Avançar Ano Letivo - Em desenvolvimento."); break;
                    case 3: mostrarMediaGlobal(); break;
                    case 4: mostrarMelhorAluno(); break;
                    case 5: alterarPassword(); break;
                    case 0:
                        view.mostrarMensagem("A encerrar sessão do Gestor...");
                        repo.limparSessao(); // Limpar a sessão no logout
                        correr = false;
                        break;
                    default: view.mostrarMensagem("Opção inválida.");
                }
            } catch (Exception e) {
                view.mostrarMensagem("Erro na leitura da opção. Por favor, insira um número válido.");
            }
        }
    }

    private void mostrarMediaGlobal() {
        view.mostrarMensagem("\n--- MÉDIA GLOBAL INSTITUCIONAL ---");
        Estudante[] estudantes = ImportadorCSV.carregarTodosEstudantes(PASTA_BD);

        if (estudantes == null) {
            view.mostrarMensagem("Erro ao carregar os dados dos estudantes.");
            return;
        }

        double soma = 0;
        int totalNotas = 0;

        for (Estudante e : estudantes) {
            if (e == null || e.getPercurso() == null) continue; // Proteção contra NullPointerException

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
            view.mostrarMensagem("Ainda não existem notas registadas no sistema.");
        } else {
            view.mostrarMensagem("Média Global Institucional: " + String.format("%.2f", (soma / totalNotas)) + " valores (Baseado em " + totalNotas + " notas).");
        }
    }

    private void mostrarMelhorAluno() {
        view.mostrarMensagem("\n--- MELHOR ALUNO ---");
        Estudante[] estudantes = ImportadorCSV.carregarTodosEstudantes(PASTA_BD);
        Estudante melhor = null;
        double maiorMedia = -1;

        if (estudantes == null) return;

        for (Estudante e : estudantes) {
            // Verifica se o estudante é válido e tem percurso instanciado antes de invocar métodos
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
            view.mostrarMensagem("Melhor Aluno: " + melhor.getNome() + " (Nº " + melhor.getNumeroMecanografico() + ")");
            view.mostrarMensagem("Média de Curso: " + String.format("%.2f", maiorMedia) + " valores.");
        } else {
            view.mostrarMensagem("Ainda não existem alunos avaliados no sistema.");
        }
    }

    private void executarRegistoEstudante() {
        view.mostrarMensagem("\n--- REGISTAR ESTUDANTE ---");

        int numMec;
        try {
            numMec = Integer.parseInt(view.pedirInput("Nº Mecanográfico"));
        } catch (NumberFormatException e) {
            view.mostrarMensagem("Erro: O Nº Mecanográfico deve conter apenas algarismos. Operação cancelada.");
            return; // Aborta o registo para não corromper os dados
        }

        String nome = view.pedirInput("Nome");

        String nif;
        do {
            nif = view.pedirInput("NIF (9 dígitos numéricos)");
        } while (!Validador.validarNif(nif));

        String morada = view.pedirInput("Morada");
        String dataNasc = view.pedirInput("Data Nasc. (DD/MM/AAAA)");
        String siglaCurso = view.pedirInput("Sigla do Curso (Ex: EI, IG)");

        int anoInscricao = java.time.Year.now().getValue();
        String email = EmailGenerator.gerarEmailEstudante(numMec);
        String passLimpa = PasswordGenerator.gerarPasswordSegura();

        // Enviar email com a password limpa
        EmailService.enviarCredenciaisTodos(nome, email, passLimpa);

        // Gerar hash para guardar na BD
        String passSegura = SegurancaPasswords.gerarCredencialMista(passLimpa);
        passLimpa = null; // Boa prática de segurança! Remove a referência da memória.

        Estudante novo = new Estudante(
                numMec, email, passSegura, nome, nif, morada, dataNasc, anoInscricao
        );

        ExportadorCSV.adicionarEstudante(novo, PASTA_BD, siglaCurso);

        view.mostrarMensagem("\nEstudante registado com sucesso!");
        view.mostrarMensagem("E-mail institucional: " + email);
        view.mostrarMensagem("As credenciais de acesso foram enviadas para o email do estudante.");
    }

    private void alterarPassword() {
        view.mostrarMensagem("\n--- ALTERAR PASSWORD ---");
        String novaPass = view.pedirInput("Introduza a nova Password (ou prima Enter para cancelar)");

        if (!novaPass.trim().isEmpty()) {
            String passSegura = SegurancaPasswords.gerarCredencialMista(novaPass);
            gestor.setPassword(passSegura);
            ExportadorCSV.atualizarPasswordCentralizada(gestor.getEmail(), passSegura, PASTA_BD);
            view.mostrarMensagem("Password alterada com sucesso!");
        } else {
            view.mostrarMensagem("Operação cancelada. A password não foi alterada.");
        }
    }
}