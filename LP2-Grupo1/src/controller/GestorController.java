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
            int opcao = view.mostrarMenu();
            switch (opcao) {
                case 1: executarRegistoEstudante(); break;
                case 2: iniciarAnoLetivo(); break;
                case 3: mostrarMediaGlobal(); break;
                case 4: mostrarMelhorAluno(); break;
                case 5: listarDevedores(); break;
                case 0: correr = false; break;
                default: view.mostrarMensagem("Opção inválida.");
            }
        }
    }

    private void mostrarMediaGlobal() {
        Estudante[] estudantes = ImportadorCSV.carregarTodosEstudantes(PASTA_BD);
        double soma = 0;
        int totalNotas = 0;

        for (Estudante e : estudantes) {
            if (e == null) continue;
            for (int i = 0; i < e.getPercurso().getTotalAvaliacoes(); i++) {
                Avaliacao av = e.getPercurso().getHistoricoAvaliacoes()[i];
                for (int j = 0; j < av.getTotalAvaliacoesLancadas(); j++) {
                    soma += av.getResultados()[j];
                    totalNotas++;
                }
            }
        }
        if (totalNotas == 0) view.mostrarMensagem("Sem notas registadas.");
        else view.mostrarMensagem("Média Global Institucional: " + String.format("%.2f", (soma / totalNotas)));
    }

    private void mostrarMelhorAluno() {
        Estudante[] estudantes = ImportadorCSV.carregarTodosEstudantes(PASTA_BD);
        Estudante melhor = null;
        double maiorMedia = -1;

        for (Estudante e : estudantes) {
            if (e == null || e.getPercurso().getTotalAvaliacoes() == 0) continue;
            double somaMedias = 0;
            for (int i = 0; i < e.getPercurso().getTotalAvaliacoes(); i++) {
                somaMedias += e.getPercurso().getHistoricoAvaliacoes()[i].calcularMedia();
            }
            double mediaAluno = somaMedias / e.getPercurso().getTotalAvaliacoes();
            if (mediaAluno > maiorMedia) {
                maiorMedia = mediaAluno;
                melhor = e;
            }
        }
        if (melhor != null) view.mostrarMensagem("Melhor Aluno: " + melhor.getNome() + " | Média: " + String.format("%.2f", maiorMedia));
        else view.mostrarMensagem("Nenhum aluno avaliado.");
    }
    /**
     * Regista um novo estudante no sistema.
     * Além de recolher os dados pessoais e gerar as credenciais de acesso,
     * associa o estudante a um Curso específico e inicializa automaticamente
     * o seu saldo devedor com o valor da propina anual desse curso.
     * Todos os dados são exportados e guardados imediatamente na base de dados (CSV).
     */
    private void executarRegistoEstudante() {
        view.mostrarMensagem("\n--- REGISTAR ESTUDANTE ---");
        int numMec = Integer.parseInt(view.pedirInput("Nº Mecanográfico"));
        String nome = view.pedirInput("Nome");
        String nif;
        do { nif = view.pedirInput("NIF (9 dígitos)"); } while (!Validador.validarNif(nif));
        String morada = view.pedirInput("Morada");
        String dataNasc = view.pedirInput("Data Nasc. (DD/MM/AAAA)");
        int anoInscricao = Integer.parseInt(view.pedirInput("Ano de Inscrição"));
        String siglaCurso = view.pedirInput("Sigla do Curso");

        String email = EmailGenerator.gerarEmailEstudante(numMec);
        String passLimpa = PasswordGenerator.gerarPasswordSegura();
        String passSegura = SegurancaPasswords.gerarCredencialMista(passLimpa);

        Estudante novo = new Estudante(numMec, email, passSegura, nome, nif, morada, dataNasc, anoInscricao);
        //Herda a propina do curso
        Curso cursoEscolhido = ImportadorCSV.procurarCurso(siglaCurso, PASTA_BD);
        if (cursoEscolhido != null) {
            novo.setSaldoDevedor(cursoEscolhido.getValorPropinaAnual());
        } else {
            novo.setSaldoDevedor(1000.0); // Valor caso o curso não seja encontrado
        }
        ExportadorCSV.adicionarEstudante(novo, PASTA_BD, siglaCurso);

        view.mostrarMensagem("Estudante Registado! Email: " + email + " | Pass: " + passLimpa);
    }
    /**
     * Lista todos os estudantes do instituto que possuam um saldo devedor superior a zero.
     * Este método faz o carregamento dos alunos de forma "On-Demand" (Lazy Loading) a partir
     * do CSV e imprime na consola o número mecanográfico, nome e o valor exato em dívida.
     */
    private void listarDevedores() {
        view.mostrarMensagem("\n--- LISTA DE DEVEDORES ---");
        Estudante[] estudantes = ImportadorCSV.carregarTodosEstudantes(PASTA_BD);
        boolean encontrou = false;

        for (Estudante e : estudantes) {
            if (e != null && e.getSaldoDevedor() > 0) {
                view.mostrarMensagem("Nº: " + e.getNumeroMecanografico() + " | Nome: " + e.getNome() + " | Dívida: " + String.format("%.2f", e.getSaldoDevedor()) + "€");
                encontrou = true;
            }
        }
        if (!encontrou) view.mostrarMensagem("Neste momento não existem alunos com propinas em atraso.");
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