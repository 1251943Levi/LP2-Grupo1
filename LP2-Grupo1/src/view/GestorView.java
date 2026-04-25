package view;

import java.util.Scanner;

/**
 * Interface de utilizador do portal do Gestor.
 * Apenas mostra informação e recolhe inputs — sem lógica de negócio.
 */
public class GestorView {

    private final Scanner scanner = new Scanner(System.in);

    public void mostrarMensagem(String msg) { System.out.println(">> " + msg); }

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
        mostrarMensagem("8 - Registar Docente");
        mostrarMensagem("9 - Registar Departamento");
        mostrarMensagem("0 - Sair / Logout");
        try { return Integer.parseInt(pedirInput("Opção")); }
        catch (Exception e) { return -1; }
    }

    public void mostrarTituloRegistoDepartamento() {
        System.out.println("\n=== REGISTAR NOVO DEPARTAMENTO ===");
    }

    public String pedirSiglaDepartamento() {
        return pedirInput("Sigla do Departamento (ex: DEIS)");
    }

    public String pedirNomeDepartamento() {
        return pedirInput("Nome do Departamento");
    }

    public void mostrarResumoRegistoDepartamento(String sigla, String nome) {
        mostrarMensagem("Departamento '" + nome + "' (" + sigla + ") registado com sucesso!");
    }

    public void mostrarErroDepartamentoDuplicado() {
        mostrarMensagem("ERRO: Já existe um departamento com essa sigla.");
    }

    public int mostrarMenuCRUD(String entidade) {
        mostrarMensagem("\n--- GERIR " + entidade.toUpperCase() + " ---");
        mostrarMensagem("1 - Adicionar " + entidade);
        mostrarMensagem("2 - Listar " + entidade);
        mostrarMensagem("3 - Editar " + entidade);
        mostrarMensagem("4 - Remover " + entidade);
        if (entidade.equalsIgnoreCase("Unidades Curriculares"))
            mostrarMensagem("5 - Associar UC Existente a um Curso");
        if (entidade.equalsIgnoreCase("Cursos"))
            mostrarMensagem("5 - Listar UCs do Curso por Ano");
        mostrarMensagem("0 - Voltar");
        try { return Integer.parseInt(pedirInput("Opção")); }
        catch (Exception e) { return -1; }
    }

    public int mostrarMenuEstatisticas() {
        mostrarMensagem("\n--- ESTATÍSTICAS ---");
        mostrarMensagem("1 - Média Global Institucional");
        mostrarMensagem("2 - Melhor Aluno");
        mostrarMensagem("0 - Voltar");
        try { return Integer.parseInt(pedirInput("Opção")); }
        catch (Exception e) { return -1; }
    }

    // --- INPUTS GENÉRICOS ---

    public String pedirSiglaCurso()       { return pedirInput("Sigla do Curso"); }
    public String pedirAnoCurricular()    { return pedirInput("Ano Curricular (ex: 1, 2, 3)"); }
    public String pedirSiglaUc()          { return pedirInput("Sigla da UC (ex: POO, BD)"); }
    public String pedirNomeUc()           { return pedirInput("Nome da UC"); }
    public String pedirSiglaDocente()     { return pedirInput("Sigla do Docente Responsável"); }
    public String pedirNovoNome()         { return pedirInput("Novo Nome"); }
    public String pedirNovoAnoCurricular(){ return pedirInput("Novo Ano Curricular"); }
    public String pedirNovaSiglaDocente() { return pedirInput("Nova Sigla Docente"); }
    public String pedirNovaSiglaCurso()   { return pedirInput("Nova Sigla Curso"); }
    public String pedirNomeCurso()        { return pedirInput("Nome do Curso"); }
    public String pedirDepartamento()     { return pedirInput("Departamento (ex: DEIS)"); }
    public String pedirNovoDepartamento() { return pedirInput("Novo Departamento"); }
    public String pedirNome()             { return pedirInput("Nome"); }
    public String pedirNif()              { return pedirInput("NIF (9 dígitos)"); }
    public String pedirMorada()           { return pedirInput("Morada"); }
    public String pedirDataNascimento()   { return pedirInput("Data Nasc. (DD-MM-AAAA)"); }
    public String pedirAnoInscricao()     { return pedirInput("Ano de Inscrição"); }
    public String pedirNumMecanografico() { return pedirInput("Nº Mecanográfico"); }

    public double pedirValorDouble(String msg) {
        try { return Double.parseDouble(pedirInput(msg)); }
        catch (Exception e) { return 0.0; }
    }

    // --- REGISTO DE DOCENTE ---
    public void mostrarTituloRegistoDocente() {
        System.out.println("\n=== REGISTAR NOVO DOCENTE ===");
        System.out.println("Por favor, insira os dados do docente abaixo.");
    }

    public void mostrarResumoRegistoDocente(String email) {
        System.out.println("\n>> REGISTO CONCLUÍDO COM SUCESSO!");
        System.out.println(">> O docente foi guardado no sistema.");
        System.out.println(">> Credenciais enviadas para: " + email);
    }

    // --- REGISTO DE ESTUDANTE ---

    public void mostrarTituloRegistoEstudante() {
        mostrarMensagem("\n--- REGISTAR ESTUDANTE ---");
    }

    public void mostrarNumMecanograficoAtribuido(int numMec) {
        mostrarMensagem("Nº Mecanográfico atribuído: " + numMec);
    }

    public void mostrarResumoRegistoEstudante(String email) {
        mostrarMensagem("\nEstudante registado com sucesso!");
        mostrarMensagem("E-mail institucional: " + email);
        mostrarMensagem("As credenciais de acesso foram enviadas para o email do estudante.");
    }

    // --- MENSAGENS DE SUCESSO, ERRO E AVISO ---

    public void mostrarOpcaoInvalida()   { mostrarMensagem("Opção inválida."); }
    public void mostrarErroLeituraOpcao(){ mostrarMensagem("Erro na leitura. Por favor, insira um número válido."); }
    public void mostrarDespedida()       { mostrarMensagem("A encerrar sessão do Gestor..."); }

    public void mostrarErroNomeInvalido()  { mostrarMensagem("ERRO: Nome inválido. Utilize apenas letras e espaços."); }
    public void mostrarErroNifInvalido()   { mostrarMensagem("ERRO: NIF inválido. Deve conter exatamente 9 dígitos."); }
    public void mostrarErroDataInvalida()  { mostrarMensagem("ERRO: Data inválida. Use o formato DD-MM-AAAA."); }
    public void mostrarErroNifDuplicado()  { mostrarMensagem("ERRO: Este NIF já se encontra registado no sistema."); }

    public void mostrarErroEdicaoCurso()         { mostrarMensagem("ERRO: Existem estudantes inscritos neste curso."); }
    public void mostrarErroLimiteUcs(int ano)    { mostrarMensagem("ERRO: Não é possível ter mais de 5 UCs no " + ano + "º ano deste Curso."); }

    public void mostrarSucessoCriacao(String e)  { mostrarMensagem("Sucesso: " + e + " adicionado(a) com sucesso!"); }
    public void mostrarSucessoAtualizacao(String e){ mostrarMensagem("Sucesso: " + e + " atualizado(a) com sucesso!"); }
    public void mostrarSucessoRemocao(String e)  { mostrarMensagem("Sucesso: " + e + " removido(a) com sucesso!"); }
    public void mostrarErroNaoEncontrado(String e){ mostrarMensagem("Erro: " + e + " não encontrado(a)."); }
    public void mostrarErroCarregarDados(String e){ mostrarMensagem("Erro ao carregar os dados de " + e + "."); }
    public void mostrarMensagemModoEdicao()      { mostrarMensagem("Registo encontrado! Introduza os novos dados:"); }
    public void mostrarAvisoSemCursos()          { mostrarMensagem("Aviso: Não existem cursos registados no sistema."); }
    public void mostrarSucessoAssociacaoUc(String uc, String curso) {
        mostrarMensagem("Sucesso: UC '" + uc + "' associada ao curso " + curso + "!");
    }
    public void mostrarResultadosListagem(String r) { System.out.println(r); }

    public void mostrarListaCursos(String[] cursos) {
        mostrarMensagem("\n--- SELEÇÃO DE CURSO ---");
        for (int i = 0; i < cursos.length; i++)
            if (cursos[i] != null) mostrarMensagem((i + 1) + " - " + cursos[i]);
    }

    public int pedirOpcaoCurso(int max) {
        while (true) {
            try {
                int op = Integer.parseInt(pedirInput("Selecione o número do Curso"));
                if (op > 0 && op <= max) return op;
                mostrarMensagem("Erro: Opção inválida. Escolha entre 1 e " + max + ".");
            } catch (NumberFormatException e) { mostrarMensagem("Erro: Introduza um número válido."); }
        }
    }

    public void mostrarListaUcs(String[] ucs) {
        mostrarMensagem("\n--- SELEÇÃO DE UNIDADE CURRICULAR ---");
        for (int i = 0; i < ucs.length; i++)
            if (ucs[i] != null) mostrarMensagem((i + 1) + " - " + ucs[i]);
    }

    public int pedirOpcaoUc(int max) {
        while (true) {
            try {
                int op = Integer.parseInt(pedirInput("Selecione o número da UC"));
                if (op > 0 && op <= max) return op;
                mostrarMensagem("ERRO: Escolha entre 1 e " + max + ".");
            } catch (NumberFormatException e) { mostrarMensagem("ERRO: Introduza um número válido."); }
        }
    }

    // --- ALTERAR PASSWORD ---

    public void mostrarCabecalhoAlterarPassword() { mostrarMensagem("\n--- ALTERAR PASSWORD ---"); }

    public String pedirNovaPassword() {
        System.out.print("Nova Password (Enter para cancelar): ");
        if (System.console() != null)
            return new String(System.console().readPassword()).trim();
        return scanner.nextLine().trim();
    }

    public void mostrarSucessoAlteracaoPassword() { mostrarMensagem("Password alterada com sucesso!"); }
    public void mostrarCancelamentoPassword()      { mostrarMensagem("Operação cancelada."); }

    // --- ANO LETIVO ---

    public void mostrarCabecalhoArranqueAnoLetivo() { mostrarMensagem("\n--- ARRANQUE DO ANO LETIVO ---"); }
    public void mostrarVerificacaoQuorum()          { mostrarMensagem("A verificar quórum dos cursos..."); }

    public void mostrarErroQuorum(String sigla, int alunos) {
        mostrarMensagem("BLOQUEIO: Curso " + sigla + " — apenas " + alunos
                + " aluno(s) no 1º Ano (mínimo: 5). Curso marcado como Inativo.");
    }

    public void mostrarSucessoQuorum(String sigla) {
        mostrarMensagem("OK: Curso " + sigla + " cumpre o quórum e está ATIVO.");
    }

    public void mostrarProcessamentoTransicoes() {
        mostrarMensagem("\nA processar transições de estudantes...");
    }

    public void mostrarBloqueioDivida(int num, String nome, int ano, double divida) {
        mostrarMensagem(String.format(
                "BLOQUEIO [Dívida]  | Nº %d %-20s | %dº Ano | Dívida: %.2f€",
                num, "(" + nome + ")", ano, divida));
    }
    public void mostrarBloqueioAproveitamento(int num, String nome, int ano, double percentagem) {
        mostrarMensagem(String.format(
                "BLOQUEIO [Aproveito] | Nº %d %-20s | %dº Ano | Aproveitamento: %.0f%% (mínimo: >60%%)",
                num, "(" + nome + ")", ano, percentagem * 100));
    }


    public void mostrarTransicaoSucedida(int num, int novoAno) {
        mostrarMensagem("TRANSIÇÃO OK | Nº " + num + " → " + novoAno + "º Ano.");
    }

    public void mostrarConclusaoCurso(int num) {
        mostrarMensagem("CONCLUSÃO    | Nº " + num + " concluiu o curso!");
    }

    public void mostrarSucessoAvancoAno(int ano) {
        mostrarMensagem("\nAno Letivo avançado com sucesso para " + ano + "!");
    }

    // --- DEVEDORES ---

    public void mostrarCabecalhoDevedores() { mostrarMensagem("\n--- LISTA DE ESTUDANTES DEVEDORES ---"); }

    public void mostrarEstudanteDevedor(int num, String nome, double divida) {
        mostrarMensagem(String.format("Nº %d | %-25s | Dívida: %.2f€", num, nome, divida));
    }

    public void mostrarSemDevedores() { mostrarMensagem("Não existem estudantes com propinas em atraso."); }

    // --- ESTATÍSTICAS ---

    public void mostrarCabecalhoMediaGlobal()  { mostrarMensagem("\n--- MÉDIA GLOBAL INSTITUCIONAL ---"); }
    public void mostrarSemNotasRegistadas()    { mostrarMensagem("Ainda não existem notas registadas no sistema."); }
    public void mostrarMediaGlobal(double m, int t) {
        mostrarMensagem("Média: " + String.format("%.2f", m) + " valores (baseada em " + t + " notas).");
    }
    public void mostrarCabecalhoMelhorAluno()  { mostrarMensagem("\n--- MELHOR ALUNO ---"); }
    public void mostrarInfoMelhorAluno(String nome, int num, double media) {
        mostrarMensagem("Melhor Aluno: " + nome + " (Nº " + num + ")");
        mostrarMensagem("Média: " + String.format("%.2f", media) + " valores.");
    }
    public void mostrarSemAlunosAvaliados() { mostrarMensagem("Nenhum aluno avaliado no sistema."); }


    // --- MÉTODOS DE CONFIRMAÇÃO E REMOÇÃO DE UCs ---

    public boolean confirmarRemocao(String item) {
        System.out.print("\nTem a certeza que deseja remover [" + item + "]? (S/N): ");
        return scanner.nextLine().trim().equalsIgnoreCase("S");
    }

    public void mostrarErroRemocao(String e) {
        mostrarMensagem("Erro: Não foi possível remover " + e + ".");
    }
}

