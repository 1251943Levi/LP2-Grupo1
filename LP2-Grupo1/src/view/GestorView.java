package view;

import model.Docente;
import model.Estudante;
import utils.CancelamentoException;
import utils.Consola;
import model.Departamento;
import model.Curso;
import java.util.List;



/**
 * View do Gestor. Usa Consola para toda a apresentação e leitura.
 * pedirInput() → Consola.lerString() — "0" lança CancelamentoException.
 * Menus → Consola.lerOpcaoMenu() — "0" é saída legítima.
 */
public class GestorView {

    // ---------- MENUS ----------

    // Na classe GestorView
    public int mostrarMenu() {
        Consola.imprimirCabecalho("Portal Gestor — ISSMF");
        Consola.imprimirMenu(new String[]{
                "Gerir Estudante",
                "Gerir Docente",
                "Gerir Departamento",
                "Gerir Curso",
                "Gerir Unidades Curriculares",
                "Ver Estatísticas",
                "Avançar Ano Letivo",
                "Consultar Histórico de Anos Anteriores",
                "Listar Devedores de Propinas",
                "Alterar Password"
        }, "Sair / Logout");
        return Consola.lerOpcaoMenu();
    }

    // ---------- SUBMENUS ----------
    public int mostrarSubMenuEstudante() {
        Consola.imprimirCabecalho("Gerir Estudantes");
        Consola.imprimirMenu(new String[]{
                "Criar Estudante",
                "Listar Estudantes",
                "Editar Estudante",
                "Apagar Estudante"
        }, "Voltar");
        return Consola.lerOpcaoMenu();
    }

    public int mostrarSubMenuDocente() {
        Consola.imprimirCabecalho("Gerir Docentes");
        Consola.imprimirMenu(new String[]{
                "Criar Docente",
                "Listar Docentes",
                "Editar Docente",
                "Apagar Docente"
        }, "Voltar");
        return Consola.lerOpcaoMenu();
    }

    public int mostrarSubMenuDepartamento() {
        Consola.imprimirCabecalho("Gerir Departamentos");
        Consola.imprimirMenu(new String[]{
                "Criar Departamento",
                "Listar Departamentos",
                "Editar Departamento",
                "Apagar Departamento"
        }, "Voltar");
        return Consola.lerOpcaoMenu();
    }

    public int mostrarSubMenuCurso() {
        Consola.imprimirCabecalho("Gerir Cursos");
        Consola.imprimirMenu(new String[]{
                "Criar Curso",
                "Listar Cursos",
                "Editar Curso",
                "Apagar Curso"
        }, "Voltar");
        return Consola.lerOpcaoMenu();
    }

    // ---------- MÉTODOS AUXILIARES DE APRESENTAÇÃO ----------

    public void mostrarTitulo(String titulo) {
        Consola.imprimirTitulo(titulo);
    }


    // ---------- MÉTODOS PARA ESTUDANTES ----------

    public int pedirNumeroEstudante() {
        return Consola.lerInt("Número Mecanográfico do Estudante");
    }

    public void mostrarEstudante(Estudante e) {
        System.out.printf("  %d - %s | %s | Ano: %d | Saldo: %.2f€%n",
                e.getNumeroMecanografico(), e.getNome(), e.getSiglaCurso(),
                e.getAnoCurricular(), e.getSaldoDevedor());
    }

    public void mostrarListaEstudantes(List<Estudante> estudantes) {
        Consola.imprimirTitulo("Lista de Estudantes");
        for (Estudante e : estudantes) {
            mostrarEstudante(e);
        }
        Consola.imprimirLinha();
    }

    public String pedirNovoNomeEstudante() {
        return lerStringOpcional("Novo Nome (Enter mantém o actual)");
    }

    public String pedirNovoNifEstudante() {
        return lerStringOpcional("Novo NIF (Enter mantém o actual)");
    }

    public String pedirNovaDataNascimentoEstudante() {
        return lerStringOpcional("Nova Data Nascimento (DD-MM-AAAA) (Enter mantém a actual)");
    }

    // ---------- MÉTODOS PARA DOCENTES ----------

    public String pedirSiglaDocenteParaGestao() {
        return lerStringOpcional("Sigla do Docente (ex: JMS)");
    }

    public void mostrarDocente(Docente d) {
        System.out.printf("  %s - %s | NIF: %s | %s%n",
                d.getSigla(), d.getNome(), d.getNif(), d.getEmail());
    }

    public void mostrarListaDocentes(List<Docente> docentes) {
        Consola.imprimirTitulo("Lista de Docentes");
        for (Docente d : docentes) {
            mostrarDocente(d);
        }
        Consola.imprimirLinha();
    }

    public String pedirNovoNomeDocente() {
        return lerStringOpcional("Novo Nome (Enter mantém o actual)");
    }

    public String pedirNovoNifDocente() {
        return lerStringOpcional("Novo NIF (Enter mantém o actual)");
    }

    public String pedirNovaMoradaDocente() {
        return lerStringOpcional("Nova Morada (Enter mantém a actual)");
    }

    public String pedirNovaDataNascimentoDocente() {
        return lerStringOpcional("Nova Data Nascimento (DD-MM-AAAA) (Enter mantém a actual)");
    }

    public void mostrarErroDocenteComUcs() {
        Consola.imprimirErro("Não é possível remover o docente pois lecciona uma ou mais UCs.");
    }

    // ---------- MÉTODOS PARA DEPARTAMENTOS ----------


    public void mostrarDepartamento(Departamento d) {
        System.out.printf("  %s - %s%n", d.getSigla(), d.getNome());
    }

    public void mostrarListaDepartamentos(List<Departamento> departamentos) {
        Consola.imprimirTitulo("Lista de Departamentos");
        for (Departamento d : departamentos) {
            mostrarDepartamento(d);
        }
        Consola.imprimirLinha();
    }

    public String pedirNovoSiglaDepartamento() {
        return lerStringOpcional("Nova Sigla (Enter mantém a actual)");
    }

    public String pedirNovoNomeDepartamento() {
        return lerStringOpcional("Novo Nome (Enter mantém o actual)");
    }

    // ---------- MÉTODOS PARA CURSOS ----------

    public double pedirPropinaCurso() {
        return Consola.lerDouble("Propina anual (€)");
    }

    public void mostrarCurso(Curso c) {
        String dep = (c.getDepartamento() != null) ? c.getDepartamento().getSigla() : "N/A";
        System.out.printf("  %s - %s | Dep: %s | Propina: %.2f€ | Estado: %s%n",
                c.getSigla(), c.getNome(), dep, c.getValorPropinaAnual(), c.getEstado());
    }

    public void mostrarListaCursos(List<Curso> cursos) {
        Consola.imprimirTitulo("Lista de Cursos");
        for (Curso c : cursos) {
            mostrarCurso(c);
        }
        Consola.imprimirLinha();
    }

    public String pedirNovoNomeCurso() {
        return lerStringOpcional("Novo Nome (Enter mantém o actual)");
    }

    public String pedirNovoSiglaDepartamentoCurso() {
        return lerStringOpcional("Nova Sigla do Departamento (Enter mantém a actual)");
    }

    public Double pedirNovaPropinaCurso() {
        String input = Consola.lerString("Nova Propina (€) (Enter mantém a actual)");
        if (input.isEmpty()) return null;
        try {
            return Double.parseDouble(input.replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void mostrarErroCursoExistente() {
        Consola.imprimirErro("Já existe um curso com esta sigla.");
    }

    public void mostrarErroSemDepartamentos() {
        Consola.imprimirErro("Não existem departamentos registados. Crie um departamento primeiro.");
    }

    public void mostrarErroDepartamentoNaoEncontrado() {
        Consola.imprimirErro("Departamento não encontrado. Introduza uma sigla existente.");
    }

    public void mostrarErroPropinaNegativa() {
        Consola.imprimirErro("Propina não pode ser negativa.");
    }

    public void mostrarErroCriacaoCurso() {
        Consola.imprimirErro("Erro ao criar curso (departamento inválido ou curso já existe).");
    }

    public void mostrarErroPropinaNegativaMantida() {
        Consola.imprimirErro("Propina não pode ser negativa. Mantido o valor anterior.");
    }

    public void mostrarErroPropinaDuasCasas() {
        Consola.imprimirErro("Propina deve ter no máximo 2 casas decimais. Mantido o valor anterior.");
    }


    public void mostrarErroAtualizacaoCurso() {
        Consola.imprimirErro("Erro ao actualizar curso.");
    }

    // ---------- MÉTODOS PARA DEPARTAMENTO ----------

    public void mostrarErroCriarDepartamento() {
        Consola.imprimirErro("Erro ao criar departamento.");
    }

    public void mostrarErroAtualizarDepartamento() {
        Consola.imprimirErro("Não foi possível actualizar (sigla já existe ou departamento não encontrado).");
    }

    public void mostrarErroRemoverDepartamentoComCursos() {
        Consola.imprimirErro("Não é possível remover o departamento pois existem cursos associados.");
    }

    // ---------- MENSAGENS DE ERRO ----------

    public void mostrarErroNifInvalidoOuDuplicado() {
        Consola.imprimirErro("NIF inválido ou já existente. Campo mantido.");
    }

    public void mostrarErroDataInexistente() {
        Consola.imprimirErro("Data de nascimento inválida (ex: 31-06-2005). Formato correcto: DD-MM-AAAA.");
    }

    public void mostrarErroDataFutura() {
        Consola.imprimirErro("Data de nascimento não pode ser futura.");
    }

    public void mostrarErroIdadeForaLimites() {
        Consola.imprimirErro("Idade deve estar entre 16 e 120 anos.");
    }

    /**
     * Lê uma string opcional – o utilizador pode premir Enter para manter o valor actual.
     * @param prompt Texto a exibir.
     * @return String introduzida (pode ser vazia) ou null se cancelado com "sair".
     */
    public String lerStringOpcional(String prompt) {
        System.out.print("  " + prompt + ": ");
        String input = new java.util.Scanner(System.in).nextLine().trim();
        if (input.equalsIgnoreCase("sair")) throw new CancelamentoException();
        return input; // pode ser vazia
    }

    public int mostrarMenuCRUD(String entidade) {
        boolean ehUC    = entidade.equalsIgnoreCase("Unidades Curriculares");
        boolean ehCurso = entidade.equalsIgnoreCase("Cursos");
        String[] opcoes;

        if (ehUC) {
            opcoes = new String[]{
                    "Adicionar " + entidade,
                    "Listar " + entidade,
                    "Editar " + entidade,
                    "Remover " + entidade,
                    "Associar UC Existente a um Curso",
                    "Remover UC de um Curso"
            };
        } else if (ehCurso) {
            opcoes = new String[]{
                    "Adicionar " + entidade,
                    "Listar " + entidade,
                    "Editar " + entidade,
                    "Remover " + entidade,
                    "Listar UCs do Curso por Ano"
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

    public void mostrarOpcaoNaoAssociarCurso() {
        System.out.println("  [0] Não associar a nenhum curso");
    }

    public void mostrarSucessoAssociacaoRemovida() {
        Consola.imprimirSucesso("Associação removida com sucesso.");
    }

    public void mostrarErroAssociacaoRemovida() {
        Consola.imprimirErro("Erro ao remover associação.");
    }

    public int mostrarMenuEstatisticas() {
        Consola.imprimirCabecalho("Estatísticas — ISSMF");
        Consola.imprimirMenu(new String[]{
                "Média Global Institucional",
                "Melhor Aluno"
        });
        return Consola.lerOpcaoMenu();
    }

    public int pedirAnoHistorico() {
        return utils.Consola.lerInt("Introduza o Ano Letivo a consultar (ex: 2025)");
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