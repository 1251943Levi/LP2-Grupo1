package view;

import java.util.Scanner;

public class GestorView {
    private Scanner scanner = new Scanner(System.in);

    public void mostrarMensagem(String msg) {
        System.out.println(">> " + msg);
    }
    
    public String pedirInput(String msg) {
        System.out.print(msg + ": ");
        return scanner.nextLine().trim();
    }

    // --- MENUS ---

    public int mostrarMenu() {
        mostrarMensagem("\n=== MENU GESTOR ===");
        mostrarMensagem("1 - Registar Novo Estudante");
        mostrarMensagem("2 - Gerir Unidades Curriculares");
        mostrarMensagem("3 - Gerir Cursos");
        mostrarMensagem("4 - Ver Estatísticas");
        mostrarMensagem("5 - Avançar Ano Letivo");
        mostrarMensagem("6 - Listar Devedores");
        mostrarMensagem("7 - Alterar Password");
        mostrarMensagem("0 - Sair / Logout");
        try {
            return Integer.parseInt(pedirInput("Opção"));
        } catch (Exception e) {
            return -1;
        }
    }

    public int mostrarMenuCRUD(String entidade) {
        mostrarMensagem("\n--- GERIR " + entidade.toUpperCase() + " ---");
        mostrarMensagem("1 - Adicionar " + entidade);
        mostrarMensagem("2 - Listar " + entidade);
        mostrarMensagem("3 - Editar " + entidade);
        mostrarMensagem("4 - Remover " + entidade);
        if (entidade.equalsIgnoreCase("Unidades Curriculares")) {
            mostrarMensagem("5 - Associar UC Existente a um Curso");
        }
        if (entidade.equalsIgnoreCase("Cursos")) {
            mostrarMensagem("5 - Listar UCs do Curso por Ano");
        }
        mostrarMensagem("0 - Voltar");
        try {
            return Integer.parseInt(pedirInput("Opção"));
        } catch (Exception e) {
            return -1;
        }
    }

    public int mostrarMenuEstatisticas() {
        mostrarMensagem("\n--- ESTATÍSTICAS ---");
        mostrarMensagem("1 - Média Global Institucional");
        mostrarMensagem("2 - Melhor Aluno");
        mostrarMensagem("0 - Voltar");
        try {
            return Integer.parseInt(pedirInput("Opção"));
        } catch (Exception e) {
            return -1;
        }
    }

    // --- INPUTS GENÉRICOS ---

    public String pedirSiglaCurso() { return pedirInput("Sigla do Curso"); }
    public String pedirAnoCurricular() { return pedirInput("Ano Curricular (ex: 1, 2, 3)"); }
    public String pedirSiglaUc() { return pedirInput("Sigla da UC (ex: POO, BD)"); }
    public String pedirNomeUc() { return pedirInput("Nome da UC"); }
    public String pedirSiglaDocente() { return pedirInput("Sigla do Docente Responsável"); }
    public String pedirNovoNome() { return pedirInput("Novo Nome"); }
    public String pedirNovoAnoCurricular() { return pedirInput("Novo Ano Curricular"); }
    public String pedirNovaSiglaDocente() { return pedirInput("Nova Sigla Docente"); }
    public String pedirNovaSiglaCurso() { return pedirInput("Nova Sigla Curso"); }
    public String pedirNomeCurso() { return pedirInput("Nome do Curso"); }
    public String pedirDepartamento() { return pedirInput("Departamento (ex: DEIS)"); }
    public String pedirNovoDepartamento() { return pedirInput("Novo Departamento"); }

    public double pedirValorDouble(String msg) {
        try {
            return Double.parseDouble(pedirInput(msg));
        } catch (Exception e) {
            return 0.0;
        }
    }

    // --- REGISTO DE ESTUDANTE ---

    public void mostrarTituloRegistoEstudante() {
        mostrarMensagem("\n--- REGISTAR ESTUDANTE ---");
    }

    public String pedirNumMecanografico() { return pedirInput("Nº Mecanográfico"); }
    public String pedirNome() { return pedirInput("Nome"); }
    public String pedirNif() { return pedirInput("NIF (9 dígitos)"); }
    public String pedirMorada() { return pedirInput("Morada"); }
    public String pedirDataNascimento() { return pedirInput("Data Nasc. (DD-MM-AAAA)"); }
    public String pedirAnoInscricao() { return pedirInput("Ano de Inscrição"); }

    public void mostrarNumMecanograficoAtribuido(int numMec) {
        mostrarMensagem("Nº Mecanográfico atribuído: " + numMec);
    }

    public void mostrarResumoRegistoEstudante(String email) {
        mostrarMensagem("\nEstudante registado com sucesso!");
        mostrarMensagem("E-mail institucional: " + email);
        mostrarMensagem("As credenciais de acesso foram enviadas para o email do estudante.");
    }

    // --- MENSAGENS DE SUCESSO, ERRO E AVISO ---

    public void mostrarOpcaoInvalida() { mostrarMensagem("Opção inválida."); }
    public void mostrarErroLeituraOpcao() { mostrarMensagem("Erro na leitura da opção. Por favor, insira um número válido."); }
    public void mostrarDespedida() { mostrarMensagem("A encerrar sessão do Gestor..."); }

    public void mostrarErroNomeInvalido() { mostrarMensagem("ERRO: Nome inválido. Utilize apenas letras e espaços."); }
    public void mostrarErroNifInvalido() { mostrarMensagem("ERRO: NIF inválido. Deve conter exatamente 9 dígitos."); }
    public void mostrarErroDataInvalida() { mostrarMensagem("ERRO: Data inválida. Utilize rigorosamente o formato DD-MM-AAAA."); }
    public void mostrarErroNifDuplicado() { mostrarMensagem("ERRO: Este NIF já se encontra registado num aluno existente."); }

    public void mostrarErroEdicaoCurso() { mostrarMensagem("ERRO: Ação bloqueada! Existem estudantes inscritos neste curso."); }
    public void mostrarErroLimiteUcs(int ano) { mostrarMensagem("ERRO: Não é possível associar mais de 5 UCs ao " + ano + "º ano deste Curso."); }

    public void mostrarSucessoCriacao(String entidade) { mostrarMensagem("Sucesso: " + entidade + " adicionado(a) com sucesso ao sistema!"); }
    public void mostrarSucessoAtualizacao(String entidade) { mostrarMensagem("Sucesso: " + entidade + " atualizado(a) com sucesso!"); }
    public void mostrarSucessoRemocao(String entidade) { mostrarMensagem("Sucesso: " + entidade + " removido(a) com sucesso!"); }
    public void mostrarErroNaoEncontrado(String entidade) { mostrarMensagem("Erro: " + entidade + " não encontrado(a) na base de dados."); }

    public void mostrarMensagemModoEdicao() { mostrarMensagem("Registo encontrado! Introduza os novos dados:"); }
    public void mostrarAvisoSemCursos() { mostrarMensagem("Aviso: Não existem cursos registados no sistema."); }

    public void mostrarErroCarregarDados(String entidade) { mostrarMensagem("Erro ao carregar os dados de " + entidade + "."); }
    public void mostrarSucessoAssociacaoUc(String nomeUc, String siglaCurso) { mostrarMensagem("Sucesso: A UC '" + nomeUc + "' foi associada ao curso " + siglaCurso + "!"); }
    public void mostrarResultadosListagem(String resultados) { System.out.println(resultados); }

    // --- LISTAS DE SELEÇÃO NUMERADA ---

    public void mostrarListaCursos(String[] cursos) {
        mostrarMensagem("\n--- SELEÇÃO DE CURSO ---");
        for (int i = 0; i < cursos.length; i++) {
            if (cursos[i] != null) mostrarMensagem((i + 1) + " - " + cursos[i]);
        }
    }

    public int pedirOpcaoCurso(int max) {
        while (true) {
            try {
                int opcao = Integer.parseInt(pedirInput("Selecione o número do Curso"));
                if (opcao > 0 && opcao <= max) return opcao;
                mostrarMensagem("Erro: Opção inválida. Escolha entre 1 e " + max + ".");
            } catch (NumberFormatException e) {
                mostrarMensagem("Erro: Introduza um número válido.");
            }
        }
    }

    public void mostrarListaUcs(String[] ucs) {
        mostrarMensagem("\n--- SELEÇÃO DE UNIDADE CURRICULAR ---");
        for (int i = 0; i < ucs.length; i++) {
            if (ucs[i] != null) mostrarMensagem((i + 1) + " - " + ucs[i]);
        }
    }

    public int pedirOpcaoUc(int max) {
        while (true) {
            try {
                int opcao = Integer.parseInt(pedirInput("Selecione o número da UC"));
                if (opcao > 0 && opcao <= max) return opcao;
                mostrarMensagem("ERRO: Opção inválida. Escolha entre 1 e " + max + ".");
            } catch (NumberFormatException e) {
                mostrarMensagem("ERRO: Introduza um número válido.");
            }
        }
    }

    // --- ALTERAR PASSWORD ---

    public void mostrarCabecalhoAlterarPassword() { mostrarMensagem("\n--- ALTERAR PASSWORD ---"); }

    public String pedirNovaPassword() {
        System.out.print("Introduza a nova Password (ou prima Enter para cancelar): ");
        if (System.console() != null) {
            char[] passwordChars = System.console().readPassword();
            return new String(passwordChars).trim();
        } else {
            return scanner.nextLine().trim();
        }
    }

    public void mostrarSucessoAlteracaoPassword() { mostrarMensagem("Password alterada com sucesso!"); }
    public void mostrarCancelamentoPassword() { mostrarMensagem("Operação cancelada. A password não foi alterada."); }

    // --- ANO LETIVO ---

    public void mostrarCabecalhoArranqueAnoLetivo() { mostrarMensagem("\n--- ARRANQUE DO ANO LETIVO ---"); }
    public void mostrarVerificacaoQuorum() { mostrarMensagem("A verificar quórum dos cursos..."); }
    public void mostrarErroQuorum(String siglaCurso, int alunos) { mostrarMensagem("ERRO: Curso " + siglaCurso + " abortado. Apenas " + alunos + " alunos no 1º Ano (Mínimo: 5)."); }
    public void mostrarSucessoQuorum(String siglaCurso) { mostrarMensagem("Curso " + siglaCurso + " cumpre os requisitos e está ATIVO."); }
    public void mostrarProcessamentoTransicoes() { mostrarMensagem("\nA processar transições de estudantes..."); }
    public void mostrarBloqueioDivida(int num, String nome, int ano, double divida) { mostrarMensagem("BLOQUEIO: Aluno " + num + " (" + nome + ") retido no " + ano + "º Ano devido a dívida de " + divida + "€. (Média ignorada)"); }
    public void mostrarTransicaoSucedida(int num, int novoAno) { mostrarMensagem("SUCESSO: Aluno " + num + " transitou para o " + novoAno + "º Ano."); }
    public void mostrarConclusaoCurso(int num) { mostrarMensagem("SUCESSO: Aluno " + num + " concluiu o curso!"); }
    public void mostrarSucessoAvancoAno(int ano) { mostrarMensagem("\nAno Letivo avançado com sucesso para " + ano + "!"); }

    // --- DEVEDORES ---

    public void mostrarCabecalhoDevedores() { mostrarMensagem("\n--- LISTA DE ESTUDANTES DEVEDORES ---"); }
    public void mostrarEstudanteDevedor(int num, String nome, double divida) {
        mostrarMensagem(String.format("Nº %d | Nome: %-20s | Dívida: %.2f€", num, nome, divida));
    }
    public void mostrarSemDevedores() { mostrarMensagem("Excelente! Não existem estudantes com propinas em atraso."); }

    // --- ESTATÍSTICAS ---

    public void mostrarCabecalhoMediaGlobal() { mostrarMensagem("\n--- MÉDIA GLOBAL INSTITUCIONAL ---"); }
    public void mostrarSemNotasRegistadas() { mostrarMensagem("Ainda não existem notas registadas no sistema."); }
    public void mostrarMediaGlobal(double media, int totalNotas) { mostrarMensagem("Média Global Institucional: " + String.format("%.2f", media) + " valores (Baseado em " + totalNotas + " notas)."); }

    public void mostrarCabecalhoMelhorAluno() { mostrarMensagem("\n--- MELHOR ALUNO ---"); }
    public void mostrarInfoMelhorAluno(String nome, int num, double media) {
        mostrarMensagem("Melhor Aluno: " + nome + " (Nº " + num + ")");
        mostrarMensagem("Média de Curso: " + String.format("%.2f", media) + " valores.");
    }
    public void mostrarSemAlunosAvaliados() { mostrarMensagem("Ainda não existem alunos avaliados no sistema."); }
}