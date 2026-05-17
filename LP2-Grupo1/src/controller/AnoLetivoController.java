package controller;

import bll.AnoLetivoBLL;
import bll.EstadoInvalidoException;
import model.AnoLetivo;
import model.RepositorioDados;
import utils.CancelamentoException;
import view.AnoLetivoView;
import view.GestorView;

import java.util.List;

/**
 * Controlador do módulo Ano Letivo.
 * Orquestra AnoLetivoView e AnoLetivoBLL.
 *
 * Recebe a GestorView no método iniciar() porque a operação "Avançar"
 * delega na lógica existente que ainda usa GestorView para feedback durante a transição.
 */
public class AnoLetivoController {

    private final RepositorioDados repo;
    private final AnoLetivoView view;
    private final AnoLetivoBLL bll;

    public AnoLetivoController(RepositorioDados repo) {
        this.repo = repo;
        this.view = new AnoLetivoView();
        this.bll  = new AnoLetivoBLL();
    }

    /**
     * Loop do menu Ano Letivo. Chamado pelo GestorController quando o gestor
     * seleciona a opção correspondente no menu principal.
     * @param gestorView Vista do gestor, necessária para a operação "Avançar".
     */
    public void iniciar(GestorView gestorView) {
        boolean correr = true;
        while (correr) {
            int opcao = view.mostrarMenu();
            switch (opcao) {
                case 1: criar();              break;
                case 2: iniciarAno();         break;
                case 3: editar();             break;
                case 4: avancar(gestorView);  break;
                case 5: fechar();             break;
                case 6: listar();             break;
                case 7: estado();             break;
                case 0: correr = false;       break;
                default: view.mostrarErro("Opção inválida.");
            }
        }
    }

    // ---------- AÇÕES ----------

    private void criar() {
        try {
            int ano = view.pedirAno("Ano letivo a criar (ex: 2027)");
            bll.criar(ano);
            view.mostrarSucesso("Ano letivo " + ano + " criado em PLANEAMENTO.");
        } catch (CancelamentoException e) {
            view.mostrarMensagem("Operação cancelada.");
        } catch (EstadoInvalidoException e) {
            view.mostrarErro(e.getMessage());
        }
    }

    private void iniciarAno() {
        try {
            int ano = view.pedirAno("Ano letivo a iniciar");
            bll.iniciar(ano);
            view.mostrarSucesso("Ano letivo " + ano + " iniciado com sucesso.");
        } catch (CancelamentoException e) {
            view.mostrarMensagem("Operação cancelada.");
        } catch (EstadoInvalidoException e) {
            view.mostrarErro(e.getMessage());
        }
    }

    private void editar() {
        try {
            int anoAntigo = view.pedirAno("Ano atual a substituir");
            int anoNovo   = view.pedirAno("Novo número de ano");
            bll.editar(anoAntigo, anoNovo);
            view.mostrarSucesso("Ano " + anoAntigo + " alterado para " + anoNovo + ".");
        } catch (CancelamentoException e) {
            view.mostrarMensagem("Operação cancelada.");
        } catch (EstadoInvalidoException e) {
            view.mostrarErro(e.getMessage());
        }
    }

    private void avancar(GestorView gestorView) {
        try {
            bll.avancar(repo, gestorView);
            view.mostrarSucesso("Ano letivo avançado com sucesso.");
        } catch (EstadoInvalidoException e) {
            view.mostrarErro(e.getMessage());
        }
    }

    private void fechar() {
        try {
            int ano = view.pedirAno("Ano letivo a fechar");
            bll.fechar(ano);
            view.mostrarSucesso("Ano letivo " + ano + " fechado.");
        } catch (CancelamentoException e) {
            view.mostrarMensagem("Operação cancelada.");
        } catch (EstadoInvalidoException e) {
            view.mostrarErro(e.getMessage());
        }
    }

    private void listar() {
        List<AnoLetivo> anos = bll.listar();
        view.mostrarListaAnos(anos);
    }

    private void estado() {
        List<String> resumo = bll.obterEstadoResumo();
        view.mostrarResumo(resumo);
    }
}