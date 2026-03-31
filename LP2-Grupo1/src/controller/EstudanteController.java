package controller;

import model.Estudante;
import model.RepositorioDados;

import view.EstudanteView;

public class EstudanteController {
    private RepositorioDados repositorio;
    private Estudante estudanteAtivo;
    private EstudanteView view;

    public EstudanteController(RepositorioDados repositorio, Estudante estudanteAtivo) {
        this.repositorio = repositorio;
        this.estudanteAtivo = estudanteAtivo;
        this.view = new EstudanteView();
    }

    public void iniciar() {
        boolean aExecutar = true;

        while (aExecutar) {
            int opcao = view.mostrarMenuPrincipal();

            switch (opcao) {
                case 1:
                    view.mostrarMensagem("\n--- DADOS PESSOAIS ---");
                    view.mostrarMensagem("Nome: " + estudanteAtivo.getNome());
                    view.mostrarMensagem("Email: " + estudanteAtivo.getEmail());
                    view.mostrarMensagem("NIF: " + estudanteAtivo.getNif());
                    view.mostrarMensagem("Morada: " + estudanteAtivo.getMorada());
                    view.mostrarMensagem("Data de Nascimento: " + estudanteAtivo.getDataNascimento());
                    break;

                case 2:
                    view.mostrarMensagem("\n--- ATUALIZAR DADOS ---");
                    String novaMorada = view.pedirInputString("Introduza a nova Morada");
                    estudanteAtivo.setMorada(novaMorada);

                    String novaPass = view.pedirInputString("Introduza a nova Password (ou deixe em branco para manter a atual)");
                    if (!novaPass.trim().isEmpty()) {
                        estudanteAtivo.setPassword(novaPass);
                    }

                    view.mostrarMensagem("Dados atualizados com sucesso!");
                    break;
                //Futuro encremento
               /* case 3:
                    view.mostrarMensagem("\n--- CERTIFICADO DE HABILITAÇÕES ---");
                    PercursoAcademico percurso = estudanteAtivo.getPercurso();

                    if (percurso == null || percurso.getTotalAvaliacoes() == 0) {
                        view.mostrarMensagem("Ainda não possui histórico de avaliações registadas.");
                    } else {
                        for (int i = 0; i < percurso.getTotalAvaliacoes(); i++) {
                            Avaliacao aval = percurso.getHistoricoAvaliacoes()[i];
                            double notaMaisAlta = -1.0;

                            for (int j = 0; j < aval.getTotalAvaliacoesLancadas(); j++) {
                                if (aval.getResultados()[j] > notaMaisAlta) {
                                    notaMaisAlta = aval.getResultados()[j];
                                }
                            }

                            if (notaMaisAlta != -1.0) {
                                String estado = (notaMaisAlta >= 10.0) ? "APROVADO" : "REPROVADO";
                                view.mostrarMensagem(aval.getUc().getSigla() + " | Melhor Nota: " + notaMaisAlta + " | " + estado);
                            } else {
                                view.mostrarMensagem(aval.getUc().getSigla() + " | Sem nota final registada");
                            }
                        }
                    }
                    break;*/

                case 0:
                    view.mostrarMensagem("A sair do portal do estudante...");
                    aExecutar = false;
                    break;

                default:
                    view.mostrarMensagem("Opção inválida. Tente novamente.");
            }
        }
    }
}
