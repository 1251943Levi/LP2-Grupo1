package view;

import utils.Consola;
import utils.CancelamentoException;

import java.util.Scanner;


public class GestorView {
    private Scanner scanner = new Scanner(System.in);

    /**
     * Exibe uma mensagem genérica prefixada com ">> ".
     * @param msg Mensagem a exibir
     */
    public void mostrarMensagem(String msg) {
        System.out.println(">> " + msg);
    }

    /**
     * Solicita uma string ao utilizador.
     * @param msg Texto a apresentar antes da entrada
     * @return A string introduzida (nunca nula)
     */
    public String pedirInput(String msg) {
        return Consola.lerString(msg + ": ");
    }

    // --- MENUS ---

    /**
     * Apresenta o menu principal do Gestor e lê a opção escolhida.
     * @return Número da opção selecionada (0 a 8)
     */
    public int mostrarMenu() {
        mostrarMensagem("\n=== MENU GESTOR ===");
        mostrarMensagem("1 - Registar Novo Estudante");
        mostrarMensagem("2 - Gerir Unidades Curriculares");
        mostrarMensagem("3 - Gerir Cursos");
        mostrarMensagem("4 - Ver Estatísticas");
        mostrarMensagem("5 - Avançar Ano Letivo");
        mostrarMensagem("6 - Listar Devedores");
        mostrarMensagem("7 - Alterar Password");
        mostrarMensagem("8 - Registar Docente");
        mostrarMensagem("0 - Sair / Logout");
        return Consola.lerOpcaoMenu();
    }

    /**
     * Apresenta um sub‑menu CRUD genérico para UCs ou Cursos.
     * @param entidade "Unidades Curriculares" ou "Cursos"
     * @return Opção escolhida (0 a 5)
     */
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
        return Consola.lerOpcaoMenu();
    }

    /**
     * Apresenta o menu de estatísticas.
     * @return Opção escolhida (0 a 2)
     */
    public int mostrarMenuEstatisticas() {
        mostrarMensagem("\n--- ESTATÍSTICAS ---");
        mostrarMensagem("1 - Média Global Institucional");
        mostrarMensagem("2 - Melhor Aluno");
        mostrarMensagem("0 - Voltar");
        return Consola.lerOpcaoMenu();
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
    public String pedirDepartamento() { return pedirInput("Departamento"); }

    /**
     * Solicita um valor decimal.
     * @param msg Mensagem a exibir
     * @return O valor double introduzido
     */
    public double pedirValorDouble(String msg) {
        return Consola.lerDouble(msg + ": ");
    }

    // --- REGISTO DE DOCENTE ---
    public void mostrarTituloRegistoDocente() {
        System.out.println("\n=== REGISTAR NOVO DOCENTE ===");
        System.out.println("Por favor, insira os dados do docente abaixo.");
    }

    public void mostrarResumoRegistoDocente(String email) {
        System.out.println("\n>> REGISTO CONCLUÍDO COM SUCESSO!");
        System.out.println(">> O docente foi guardado no sistema.");
        System.out.println(">> Foi enviado um e-mail com as credenciais de acesso (Password Gerada) para: " + email);
    }

    // --- REGISTO DE ESTUDANTE ---

    public void mostrarTituloRegistoEstudante() {
        mostrarMensagem("\n--- REGISTAR ESTUDANTE ---");
    }

    public String pedirNome() { return pedirInput("Nome"); }
    public String pedirNif() { return pedirInput("NIF (9 dígitos)"); }
    public String pedirMorada() { return pedirInput("Morada"); }
    public String pedirDataNascimento() { return pedirInput("Data Nasc. (DD-MM-AAAA)"); }

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

    public void mostrarErroLimiteUcs(int ano) { mostrarMensagem("ERRO: Não é possível associar mais de 5 UCs ao " + ano + "º ano deste Curso."); }

    public void mostrarSucessoCriacao(String entidade) { mostrarMensagem("Sucesso: " + entidade + " adicionado(a) com sucesso ao sistema!"); }
    public void mostrarSucessoAtualizacao(String entidade) { mostrarMensagem("Sucesso: " + entidade + " atualizado(a) com sucesso!"); }
    public void mostrarSucessoRemocao(String entidade) { mostrarMensagem("Sucesso: " + entidade + " removido(a) com sucesso!"); }
    public void mostrarErroNaoEncontrado(String entidade) { mostrarMensagem("Erro: " + entidade + " não encontrado(a) na base de dados."); }

    public void mostrarMensagemModoEdicao() { mostrarMensagem("Registo encontrado! Introduza os novos dados:"); }
    public void mostrarAvisoSemCursos() { mostrarMensagem("Aviso: Não existem cursos registados no sistema."); }

    public void mostrarErroCarregarDados(String entidade) { mostrarMensagem("Erro ao carregar os dados de " + entidade + "."); }
    public void mostrarResultadosListagem(String resultados) { System.out.println(resultados); }

    // --- LISTAS DE SELEÇÃO NUMERADA ---

    public void mostrarListaCursos(String[] cursos) {
        mostrarMensagem("\n--- SELEÇÃO DE CURSO ---");
        for (int i = 0; i < cursos.length; i++) {
            if (cursos[i] != null) mostrarMensagem((i + 1) + " - " + cursos[i]);
        }
    }

    /**
     * Solicita ao utilizador que selecione um curso a partir de uma lista numerada.
     * @param max Número máximo de opções (índice do último curso)
     * @return Índice da opção escolhida (1..max) ou -1 se cancelado
     */
    public int pedirOpcaoCurso(int max) {
        while (true) {
            try {
                int opcao = Consola.lerInt("Selecione o número do Curso: ");
                if (opcao >= 1 && opcao <= max) return opcao;
                mostrarMensagem("Erro: Opção inválida. Escolha entre 1 e " + max + ".");
            } catch (CancelamentoException e) {
                return -1;
            }
        }
    }

    public void mostrarListaUcs(String[] ucs) {
        mostrarMensagem("\n--- SELEÇÃO DE UNIDADE CURRICULAR ---");
        for (int i = 0; i < ucs.length; i++) {
            if (ucs[i] != null) mostrarMensagem((i + 1) + " - " + ucs[i]);
        }
    }

    /**
     * Solicita ao utilizador que selecione uma UC a partir de uma lista numerada.
     * @param max Número total de UCs na lista
     * @return Índice escolhido (1..max) ou -1 se cancelado
     */
    public int pedirOpcaoUc(int max) {
        while (true) {
            try {
                int opcao = Consola.lerInt("Selecione o número da UC: ");
                if (opcao >= 1 && opcao <= max) return opcao;
                mostrarMensagem("ERRO: Opção inválida. Escolha entre 1 e " + max + ".");
            } catch (CancelamentoException e) {
                return -1;
            }
        }
    }

    // --- ALTERAR PASSWORD ---

    public void mostrarCabecalhoAlterarPassword() { mostrarMensagem("\n--- ALTERAR PASSWORD ---"); }

    /**
     * Solicita a nova password ao utilizador, sem eco na consola.
     * Não é cancelável (o Enter em branco é interpretado como cancelamento manual).
     * @return A nova password ou uma string vazia se o utilizador premir Enter
     */
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

    // --- MÉTODOS DE CONFIRMAÇÃO E REMOÇÃO DE UCs ---

    /**
     * Pede uma confirmação ao utilizador antes de apagar um registo importante.
     */
    public boolean confirmarRemocao(String item) {
        System.out.print("\nTem a certeza que deseja remover [" + item + "]? (S/N): ");
        String resposta = scanner.nextLine().trim().toUpperCase();
        return resposta.equals("S");
    }

    /**
     * Mostra uma mensagem de erro específica para quando a remoção falha no ficheiro.
     */
    public void mostrarErroRemocao(String entidade) { mostrarMensagem("Erro: Não foi possível remover " + entidade + ". O ficheiro pode estar em uso."); }

    public void mostrarOperacaoCancelada() {
        System.out.println(">> Operação cancelada. A regressar ao menu...");
    }
}