package view;

import utils.Consola;

/**
 * View do Gestor. Usa Consola para toda a apresentação e leitura.
 * pedirInput() → Consola.lerString() — "0" lança CancelamentoException.
 * Menus → Consola.lerOpcaoMenu() — "0" é saída legítima.
 */
public class GestorView {

    // ---------- MENUS ----------

    /**
     * Apresenta o menu principal do Gestor e lê a opção escolhida.
     * @return Número da opção selecionada (0 a 8)
     */
    public int mostrarMenu() {
        Consola.imprimirCabecalho("Portal Gestor — ISSMF");
        Consola.imprimirMenu(new String[]{
                "Registar Novo Estudante",
                "Registar Docente",
                "Registar Departamento"
                "Gerir Unidades Curriculares",
                "Gerir Cursos",
                "Ver Estatísticas",
                "Avançar Ano Letivo",
                "Listar Devedores de Propinas",
                "Alterar Password",
        }, "Sair / Logout");
        return Consola.lerOpcaoMenu();
    }

    /**
     * Apresenta um sub‑menu CRUD genérico para UCs ou Cursos.
     * @param entidade "Unidades Curriculares" ou "Cursos"
     * @return Opção escolhida (0 a 5)
     */
    public int mostrarMenuCRUD(String entidade) {
        boolean ehUC    = entidade.equalsIgnoreCase("Unidades Curriculares");
        boolean ehCurso = entidade.equalsIgnoreCase("Cursos");
        String[] opcoes;
        if (ehUC || ehCurso) {
            opcoes = new String[]{
                    "Adicionar " + entidade,
                    "Listar " + entidade,
                    "Editar " + entidade,
                    "Remover " + entidade,
                    ehUC ? "Associar UC Existente a um Curso" : "Listar UCs do Curso por Ano"
            };
        } else {
            opcoes = new String[]{
                    "Adicionar " + entidade,
                    "Listar " + entidade,
                    "Editar " + entidade,
                    "Remover " + entidade
            };
        }
        Consola.imprimirCabecalho("Gerir " + entidade);
        Consola.imprimirMenu(opcoes);
        return Consola.lerOpcaoMenu();
    }

    /**
     * Apresenta o menu de estatísticas.
     * @return Opção escolhida (0 a 2)
     */
    public int mostrarMenuEstatisticas() {
        Consola.imprimirCabecalho("Estatísticas — ISSMF");
        Consola.imprimirMenu(new String[]{
                "Média Global Institucional",
                "Melhor Aluno"
        });
        return Consola.lerOpcaoMenu();
    }

    // ---------- INPUTS GENÉRICOS ----------

    /** Lê um campo de texto — "0" lança CancelamentoException. */
    public String pedirInput(String msg) { return Consola.lerString(msg); }

    public double pedirValorDouble(String msg) { return Consola.lerDouble(msg); }

    public int pedirOpcaoCurso(int max) {
        while (true) {
            try {
                int opcao = Consola.lerInt("Número do Curso (1-" + max + ")");
                if (opcao >= 1 && opcao <= max) return opcao;
                Consola.imprimirErro("Opção fora do intervalo. Escolha entre 1 e " + max + ".");
            } catch (utils.CancelamentoException e) { return -1; }
        }
    }

    public int pedirOpcaoUc(int max) {
        while (true) {
            try {
                int opcao = Consola.lerInt("Número da UC (1-" + max + ")");
                if (opcao >= 1 && opcao <= max) return opcao;
                Consola.imprimirErro("Opção fora do intervalo. Escolha entre 1 e " + max + ".");
            } catch (utils.CancelamentoException e) { return -1; }
        }
    }

    // ---------- CAMPOS DE FORMULÁRIO ----------

    public String pedirNome()             { return pedirInput("Nome Completo"); }
    public String pedirNif()              { return pedirInput("NIF"); }
    public String pedirMorada()           { return pedirInput("Morada"); }
    public String pedirDataNascimento()   { return pedirInput("Data de Nascimento (DD-MM-AAAA)"); }
    public String pedirSiglaCurso()       { return pedirInput("Sigla do Curso"); }
    public String pedirAnoCurricular()    { return pedirInput("Ano Curricular (ex: 1, 2, 3)"); }
    public String pedirSiglaUc()          { return pedirInput("Sigla da UC (ex: POO, BD)"); }
    public String pedirNomeUc()           { return pedirInput("Nome da UC"); }
    public String pedirSiglaDocente()     { return pedirInput("Sigla do Docente Responsável"); }
    public String pedirNovaSiglaDocente() { return pedirInput("Nova Sigla Docente"); }
    public String pedirNovoNome()         { return pedirInput("Novo Nome"); }
    public String pedirNovoAnoCurricular(){ return pedirInput("Novo Ano Curricular"); }
    public String pedirNovaSiglaCurso()   { return pedirInput("Nova Sigla Curso"); }
    public String pedirNomeCurso()        { return pedirInput("Nome do Curso"); }
    public String pedirDepartamento()     { return pedirInput("Departamento (ex: DEIS)"); }

    // ---------- REGISTO DEPARTAMENTO ----------

    public void   mostrarTituloRegistoDepartamento() {
        Consola.imprimirCabecalho("Registar Departamento");
        Consola.imprimirDicaFormulario();
    }
    public String pedirSiglaDepartamento()  { return pedirInput("Sigla do Departamento (ex: DEIS)"); }
    public String pedirNomeDepartamento()   { return pedirInput("Nome do Departamento"); }
    public void   mostrarResumoRegistoDepartamento(String sigla, String nome) {
        Consola.imprimirSucesso("Departamento '" + nome + "' (" + sigla + ") registado com sucesso!");
        Consola.pausar();
    }
    public void mostrarErroDepartamentoDuplicado() { Consola.imprimirErro("Já existe um departamento com essa sigla."); }

    // ---------- REGISTO DOCENTE ----------

    public void mostrarTituloRegistoDocente() {
        Consola.imprimirCabecalho("Registar Docente");
        Consola.imprimirDicaFormulario();
    }
    public void mostrarResumoRegistoDocente(String email, String sigla) {
        Consola.imprimirSucesso("Docente registado com sucesso!");
        Consola.imprimirInfo("Email institucional: " + email);
        Consola.imprimirInfo("Sigla atribuída:      " + sigla);
        Consola.pausar();
    }

    // ---------- REGISTO ESTUDANTE ----------

    public void mostrarTituloRegistoEstudante() {
        Consola.imprimirCabecalho("Registar Estudante");
        Consola.imprimirDicaFormulario();
    }
    public void mostrarNumMecanograficoAtribuido(int num) { Consola.imprimirInfo("Nº Mecanográfico atribuído: " + num); }
    public void mostrarResumoRegistoEstudante(String email) {
        Consola.imprimirSucesso("Estudante registado! Email institucional: " + email);
        Consola.pausar();
    }

    // ---------- PASSWORD ----------

    public void mostrarCabecalhoAlterarPassword() {
        Consola.imprimirCabecalho("Alterar Password");
        Consola.imprimirDicaFormulario();
    }
    public String pedirNovaPassword() {
        return Consola.lerPassword("Nova Password");
    }

    // ---------- ESTATÍSTICAS ----------

    public void mostrarCabecalhoMediaGlobal() { Consola.imprimirTitulo("Média Global Institucional"); }
    public void mostrarMediaGlobal(double media, int total) {
        Consola.imprimirInfo(String.format("Média global: %.2f valores  (baseada em %d avaliações)", media, total));
        Consola.pausar();
    }
    public void mostrarSemNotasRegistadas()  { Consola.imprimirInfo("Sem notas registadas no sistema."); }

    public void mostrarCabecalhoMelhorAluno() { Consola.imprimirTitulo("Melhor Aluno da Instituição"); }
    public void mostrarInfoMelhorAluno(String nome, int numMec, double media) {
        Consola.imprimirInfo(String.format("%-30s | Nº %d | Média: %.2f", nome, numMec, media));
        Consola.pausar();
    }
    public void mostrarSemAlunosAvaliados()  { Consola.imprimirInfo("Nenhum aluno com avaliações registadas."); }

    // ---------- DEVEDORES ----------

    public void mostrarCabecalhoDevedores()  { Consola.imprimirTitulo("Alunos Devedores de Propinas"); }
    public void mostrarEstudanteDevedor(int numMec, String nome, double divida) {
        System.out.printf("  [%d] %-30s | Dívida: %.2f€%n", numMec, nome, divida);
    }
    public void mostrarSemDevedores()        { Consola.imprimirInfo("Nenhum aluno devedor."); }

    // ---------- LISTAGENS ----------

    public void mostrarListaCursos(String[] cursos) {
        Consola.imprimirTitulo("Cursos Disponíveis");
        for (int i = 0; i < cursos.length; i++) System.out.println("  [" + (i + 1) + "] " + cursos[i]);
        Consola.imprimirLinha();
    }

    public void mostrarListaUcs(String[] ucs) {
        Consola.imprimirTitulo("Unidades Curriculares");
        for (int i = 0; i < ucs.length; i++) System.out.println("  [" + (i + 1) + "] " + ucs[i]);
        Consola.imprimirLinha();
    }

    public void mostrarResultadosListagem(String[] resultados) {
        Consola.imprimirTitulo("Resultados");
        if (resultados == null || resultados.length == 0) { Consola.imprimirInfo("Sem resultados."); }
        else { for (String r : resultados) System.out.println("  " + r); }
        Consola.imprimirLinha();
        Consola.pausar();
    }

    public boolean confirmarRemocaoBoolean(String sigla) {
        return Consola.lerSimNao("Confirmar remoção de '" + sigla + "'?");
    }

    // ---------- AVANÇAR ANO LETIVO ----------

    public void mostrarCabecalhoArranqueAnoLetivo() {
        Consola.imprimirCabecalho("Avançar Ano Letivo");
    }
    public void mostrarVerificacaoQuorum() {
        Consola.imprimirInfo("A verificar quórum mínimo por curso...");
    }
    public void mostrarErroQuorum(String siglaCurso, int totalAlunos) {
        Consola.imprimirErro(String.format("Curso %-6s — quórum insuficiente (%d aluno(s), mínimo 5). Curso marcado como Inativo.", siglaCurso, totalAlunos));
    }
    public void mostrarSucessoQuorum(String siglaCurso) {
        Consola.imprimirInfo(String.format("Curso %-6s — quórum OK. Curso marcado como Ativo.", siglaCurso));
    }
    public void mostrarProcessamentoTransicoes() {
        Consola.imprimirInfo("A processar transições de ano dos estudantes...");
        Consola.imprimirLinha();
    }
    public void mostrarBloqueioDivida(int numMec, String nome, int ano, double divida) {
        Consola.imprimirErro(String.format("[%d] %-30s | %dº Ano | Bloqueado — dívida de %.2f€", numMec, nome, ano, divida));
    }
    public void mostrarBloqueioAproveitamento(int numMec, String nome, int ano, double pct) {
        Consola.imprimirErro(String.format("[%d] %-30s | %dº Ano | Bloqueado — aproveitamento %.0f%%", numMec, nome, ano, pct * 100));
    }
    public void mostrarTransicaoSucedida(int numMec, int novoAno) {
        Consola.imprimirSucesso(String.format("[%d] Transitou para o %dº ano.", numMec, novoAno));
    }
    public void mostrarConclusaoCurso(int numMec) {
        Consola.imprimirSucesso(String.format("[%d] Curso concluído com sucesso!", numMec));
    }
    public void mostrarSucessoAvancoAno(int novoAno) {
        Consola.imprimirLinha();
        Consola.imprimirSucesso("Ano letivo avançado. Ano atual: " + novoAno);
        Consola.pausar();
    }

    public boolean perguntarVerListagem(String entidade) {
        return utils.Consola.lerSimNao("Deseja ver a listagem de " + entidade + " disponíveis?");
    }

    // ---------- MENSAGENS ----------

    public void mostrarMensagem(String msg)           { System.out.println("  " + msg); }
    public void mostrarErroCursoComAlocacoes()        { Consola.imprimirErro("Este curso tem estudantes ou docentes alocados e não pode ser alterado."); Consola.pausar(); }
    public void mostrarErroNomeInvalido()             { Consola.imprimirErro("Nome inválido (apenas letras)."); }
    public void mostrarErroNifInvalido()              { Consola.imprimirErro("NIF inválido (9 dígitos)."); }
    public void mostrarErroNifDuplicado()             { Consola.imprimirErro("Este NIF já se encontra registado no sistema."); }
    public void mostrarErroDataInvalida()             { Consola.imprimirErro("Formato de data inválido (DD-MM-AAAA)."); }
    public void mostrarErroCarregarDados(String ent)  { Consola.imprimirErro("Não foi possível carregar os dados de " + ent + "."); }
    public void mostrarErroNaoEncontrado(String ent)  { Consola.imprimirErro("Nenhuma " + ent + " encontrada."); }
    public void mostrarErroLimiteUcs(int ano)         { Consola.imprimirErro("Limite de 5 UCs para o " + ano + "º ano atingido."); }
    public void mostrarErroRemocao(String ent)        { Consola.imprimirErro("Não foi possível remover: " + ent + "."); }
    public void mostrarSucessoCriacao(String ent)     { Consola.imprimirSucesso(ent + " criada com sucesso!"); Consola.pausar(); }
    public void mostrarSucessoAtualizacao(String ent) { Consola.imprimirSucesso(ent + " atualizada com sucesso!"); Consola.pausar(); }
    public void mostrarSucessoRemocao(String ent)     { Consola.imprimirSucesso(ent + " removida com sucesso!"); Consola.pausar(); }
    public void mostrarMensagemModoEdicao()           { Consola.imprimirInfo("Introduza os novos valores ('sair' para cancelar):"); }
    public void mostrarSucessoAlteracaoPassword()     { Consola.imprimirSucesso("Password alterada com sucesso!"); Consola.pausar(); }
    public void mostrarCancelamentoPassword()         { Consola.imprimirInfo("Operação cancelada."); }
    public void mostrarErroLeituraOpcao()             { Consola.imprimirErro("Erro de leitura. Tente novamente."); }
    public void mostrarOpcaoInvalida()                { Consola.imprimirErro("Opção inválida."); }
    public void mostrarDespedida()                    { Consola.imprimirInfo("Logout efetuado. Até breve!"); }
    public void mostrarOperacaoCancelada()            { Consola.imprimirInfo("Operação cancelada. A regressar ao menu..."); }
}