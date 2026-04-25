package view;

import model.Avaliacao;
import model.Docente;
import model.UnidadeCurricular;
import utils.Consola;

/**
 * Interface de utilizador do portal do Docente.
 * Apenas mostra informação e recolhe inputs — sem lógica de negócio.
 */
public class DocenteView {

    public int mostrarMenu() {
        Consola.imprimirCabecalho("Portal Docente — ISSMF");
        Consola.imprimirMenu(new String[]{
                "Consultar os Meus Alunos e Médias",
                "Lançar Notas",
                "Alterar Password",
                "Ver Dados Pessoais",
                "Ver as Minhas Unidades Curriculares"
        }, "Sair / Logout");
        return Consola.lerOpcaoMenu();
    }

    /**
     * Mostra a ficha completa do docente autenticado.
     */
    public void mostrarFichaDocente(Docente d) {
        Consola.imprimirTitulo("Dados Pessoais");
        Consola.imprimirInfo("Sigla:           " + d.getSigla());
        Consola.imprimirInfo("Nome:            " + d.getNome());
        Consola.imprimirInfo("Email:           " + d.getEmail());
        Consola.imprimirInfo("NIF:             " + d.getNif());
        Consola.imprimirInfo("Data Nascimento: " + d.getDataNascimento());
        Consola.imprimirInfo("Morada:          " + d.getMorada());
        Consola.pausar();
    }

    /**
     * Mostra a lista de UCs lecionadas pelo docente autenticado.
     */

    public void mostrarUcsDocente(Docente d) {
        Consola.imprimirTitulo("As Minhas Unidades Curriculares");
        if (d.getTotalUcsLecionadas() == 0) {
            Consola.imprimirInfo("Não tem unidades curriculares atribuídas.");
        } else {
            UnidadeCurricular[] ucs = d.getUcsLecionadas();
            for (int i = 0; i < d.getTotalUcsLecionadas(); i++) {
                if (ucs[i] != null) {
                    System.out.printf("  [%d] %-8s | %-35s | %dº Ano%n",
                            i + 1, ucs[i].getSigla(), ucs[i].getNome(), ucs[i].getAnoCurricular());
                }
            }
        }
        Consola.pausar();
    }

    // --- MÉTODOS DE LISTAGEM DE ALUNOS ---

    public void mostrarCabecalhoAlunos() {
        Consola.imprimirTitulo("Alunos e Médias");
    }

    public void mostrarLinha(String texto) { System.out.println("  " + texto); }


    // ---------- LANÇAMENTO DE NOTAS ----------

    public void mostrarCabecalhoLancamentoNotas() {
        Consola.imprimirCabecalho("Lançar Avaliações");
        Consola.imprimirDicaFormulario();
    }

    public int    pedirNumeroAluno()  { return Consola.lerInt("Nº Mecanográfico do Aluno"); }
    public String pedirSiglaUc()      { return Consola.lerString("Sigla da UC"); }
    public int    pedirAnoLetivo()    { return Consola.lerInt("Ano Letivo (ex: 2026)"); }
    public double pedirNotaNormal()   { return Consola.lerNota("Nota Normal"); }
    public double pedirNotaRecurso()  { return Consola.lerNota("Nota Recurso"); }
    public double pedirNotaEspecial() { return Consola.lerNota("Nota Especial"); }


    // ---------- PASSWORD ----------

    public String pedirNovaPassword() {
        Consola.imprimirTitulo("Alterar Password");
        Consola.imprimirDicaFormulario();
        return Consola.lerPassword("Nova Password");
    }

    // ---------- MENSAGENS ----------

    public void mostrarSucessoLancamento()        { Consola.imprimirSucesso("Avaliação registada com sucesso!"); }
    public void mostrarSucessoAlteracaoPassword() { Consola.imprimirSucesso("Password alterada com sucesso!"); }
    public void mostrarCancelamentoPassword()     { Consola.imprimirInfo("Operação cancelada."); }
    public void mostrarErroLeituraOpcao()         { Consola.imprimirErro("Erro de leitura. Tente novamente."); }
    public void mostrarOpcaoInvalida()            { Consola.imprimirErro("Opção inválida."); }
    public void mostrarDespedida()                { Consola.imprimirInfo("Logout efetuado. Até breve!"); }
    public void mostrarOperacaoCancelada()        { Consola.imprimirInfo("Operação cancelada. A regressar ao menu..."); }

    // --- métodos adicionados para compatibilidade com DocenteController ---
    public void mostrarAlunoComMedia(int numMec, String nome, double media) {
        System.out.printf("  [%d] %-30s | Média: %.1f%n", numMec, nome, media);
    }
    public void mostrarErroCarregarAlunos() { Consola.imprimirErro("Não foi possível carregar a lista de alunos.");
    }
}

