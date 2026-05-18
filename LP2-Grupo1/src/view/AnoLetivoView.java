package view;

import model.AnoLetivo;
import utils.Consola;

import java.util.List;

/**
 * View do módulo Ano Letivo.
 * Apenas apresenta informação e recolhe inputs.
 */
public class AnoLetivoView {

    // ---------- MENU ----------

    public int mostrarMenu() {
        Consola.imprimirCabecalho("Gestão de Ano Letivo");
        Consola.imprimirMenu(new String[]{
                "Criar Ano Letivo",
                "Iniciar Ano Letivo",
                "Editar Ano Letivo",
                "Avançar Ano Letivo",
                "Fechar Ano Letivo",
                "Listar Anos Letivos",
                "Estado"
        }, "Voltar");
        return Consola.lerOpcaoMenu();
    }

    // ---------- INPUTS ----------

    /**
     * Lê um ano letivo. "0" lança CancelamentoException.
     * */
    public int pedirAno(String mensagem) {
        return Consola.lerInt(mensagem);
    }

    // ---------- LISTAGENS ----------

    public void mostrarListaAnos(List<AnoLetivo> anos) {
        Consola.imprimirTitulo("Anos Letivos Registados");
        if (anos == null || anos.isEmpty()) {
            Consola.imprimirInfo("Nenhum ano letivo registado.");
        } else {
            for (AnoLetivo a : anos) {
                System.out.printf("  %d  -  %s%n", a.getAno(), a.getEstado());
            }
        }
        Consola.imprimirLinha();
        Consola.pausar();
    }

    public void mostrarResumo(List<String> linhas) {
        Consola.imprimirTitulo("Estado do Ano Letivo");
        if (linhas == null || linhas.isEmpty()) {
            Consola.imprimirInfo("Sem informação disponível.");
        } else {
            for (String l : linhas) Consola.imprimirInfo(l);
        }
        Consola.imprimirLinha();
        Consola.pausar();
    }

    // ---------- MENSAGENS ----------

    public void mostrarSucesso(String msg)  { Consola.imprimirSucesso(msg); Consola.pausar(); }
    public void mostrarErro(String msg)     { Consola.imprimirErro(msg);    Consola.pausar(); }
    public void mostrarMensagem(String msg) { Consola.imprimirInfo(msg);    Consola.pausar(); }
}