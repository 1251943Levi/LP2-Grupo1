package controller;

import model.*;
import view.DocenteView;

public class DocenteController {

    private RepositorioDados repo;
    private Docente docente;
    private DocenteView view;

    public DocenteController(RepositorioDados repo, Docente docente) {
        this.repo = repo;
        this.docente = docente;
        this.view = new DocenteView();
    }

    public void iniciar() {

        boolean correr = true;

        while (correr) {

            int opcao = view.mostrarMenu();

            switch (opcao) {

                case 1:
                    view.mostrarMensagem("A listar UCs lecionadas...");
                    // (Aqui depois podes melhorar para mostrar mesmo as UCs)
                    break;

                case 2:
                    view.mostrarMensagem("\n--- LANÇAMENTO DE NOTAS ---");

                    int numAluno = Integer.parseInt(view.pedirInput("Nº Aluno"));
                    String siglaUc = view.pedirInput("Sigla UC");

                    double nNormal = Double.parseDouble(
                            view.pedirInput("Nota Normal (ou -1 se faltou)")
                    );

                    double nRecurso = Double.parseDouble(
                            view.pedirInput("Nota Recurso (ou -1 se faltou)")
                    );

                    double nEspecial = Double.parseDouble(
                            view.pedirInput("Nota Especial (ou -1 se faltou)")
                    );

                    // 🔍 Procurar aluno
                    Estudante aluno = null;
                    for (int i = 0; i < repo.getTotalEstudantes(); i++) {
                        if (repo.getEstudantes()[i].getNumeroMecanografico() == numAluno) {
                            aluno = repo.getEstudantes()[i];
                            break;
                        }
                    }

                    // 🔍 Procurar UC
                    UnidadeCurricular uc = null;
                    for (int i = 0; i < repo.getTotalUcs(); i++) {
                        if (repo.getUcs()[i].getSigla().equalsIgnoreCase(siglaUc)) {
                            uc = repo.getUcs()[i];
                            break;
                        }
                    }

                    // ✅ Lançar notas
                    if (aluno != null && uc != null) {

                        Avaliacao aval = new Avaliacao(uc, 2026);

                        aval.adicionarResultado(nNormal);
                        aval.adicionarResultado(nRecurso);
                        aval.adicionarResultado(nEspecial);

                        aluno.getPercurso().registarAvaliacao(aval);

                        view.mostrarMensagem("Notas lançadas com sucesso!");

                    } else {
                        view.mostrarMensagem("Aluno ou UC não encontrados.");
                    }

                    break;

                case 0:
                    correr = false;
                    break;

                default:
                    view.mostrarMensagem("Opção inválida.");
            }
        }
    }
}