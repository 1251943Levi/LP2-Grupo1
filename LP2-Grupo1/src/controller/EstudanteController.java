package controller;

import model.Estudante;
import model.RepositorioDados;
import view.EstudanteView;

/**
 * Controller responsável por gerir as interações do perfil do Estudante.
 * Faz a ponte entre o modelo (Dados) e a interface (View).
 */
public class EstudanteController {
    private RepositorioDados repositorio;
    private Estudante estudanteAtivo;
    private EstudanteView view;

    // O Construtor recebe o Repositório e o Aluno que acabou de fazer Login
    public EstudanteController(RepositorioDados repositorio, Estudante estudanteAtivo) {
        this.repositorio = repositorio;
        this.estudanteAtivo = estudanteAtivo;
        this.view = new EstudanteView();
    }

    /**
     * Inicia o ciclo de vida do menu do estudante.
     * Mantém o utilizador no sistema até que a opção de saída (0) seja selecionada.
     */
    public void iniciar() {
        boolean aExecutar = true;

        while (aExecutar) {
            // Obtém a escolha do utilizador através da View
            int opcao = view.mostrarMenuPrincipal();

            switch (opcao) {
                case 1:
                    // Exibição de leitura: Acede diretamente aos atributos do objeto estudanteAtivo
                    view.mostrarMensagem("\n--- DADOS PESSOAIS ---");
                    view.mostrarMensagem("Nome: " + estudanteAtivo.getNome());
                    view.mostrarMensagem("Email: " + estudanteAtivo.getEmail());
                    view.mostrarMensagem("NIF: " + estudanteAtivo.getNif());
                    view.mostrarMensagem("Morada: " + estudanteAtivo.getMorada());
                    view.mostrarMensagem("Data de Nascimento: " + estudanteAtivo.getDataNascimento());
                    break;

                case 2:
                    // Fluxo de atualização de dados (Escrita)
                    view.mostrarMensagem("\n--- ATUALIZAR DADOS ---");
                    String novaMorada = view.pedirInputString("Introduza a nova Morada");
                    estudanteAtivo.setMorada(novaMorada);

                    // Validação simples: só altera a password se o campo não estiver vazio
                    String novaPass = view.pedirInputString("Introduza a nova Password (ou deixe em branco para manter a atual)");
                    if (!novaPass.trim().isEmpty()) {
                        estudanteAtivo.setPassword(novaPass);
                    }

                    view.mostrarMensagem("Dados atualizados com sucesso!");
                    break;

                //Retirar em estado de comentario quando se inserir os metodos
                /*case 3:
                    view.mostrarMensagem("\n--- CERTIFICADO DE HABILITAÇÕES ---");
                    // Vamos buscar a "mochila" do aluno
                    PercursoAcademico percurso = estudanteAtivo.getPercursoAcademico();

                    if (percurso == null || percurso.getTotalAvaliacoes() == 0) {
                        view.mostrarMensagem("Ainda não possui histórico de avaliações registadas.");
                    } else {
                        // Percorrer todas as disciplinas que ele já fez
                        for (int i = 0; i < percurso.getTotalAvaliacoes(); i++) {
                            Avaliacao aval = percurso.getHistoricoAvaliacoes()[i];
                            double notaMaisAlta = -1.0;

                            // Procurar a melhor nota entre a Normal, Recurso e Especial
                            for (int j = 0; j < aval.getTotalAvaliacoesLancadas(); j++) {
                                if (aval.getResultados()[j] > notaMaisAlta) {
                                    notaMaisAlta = aval.getResultados()[j];
                                }
                            }

                            // Se a nota for diferente de -1.0 (falta), imprimimos no ecrã
                            if (notaMaisAlta != -1.0) {
                                String estado = (notaMaisAlta >= 10.0) ? "APROVADO" : "REPROVADO";
                                view.mostrarMensagem(aval.getUc().getSigla() + " | Melhor Nota: " + notaMaisAlta + " | " + estado);
                            } else {
                                view.mostrarMensagem(aval.getUc().getSigla() + " | Sem nota final registada");
                            }
                        }
                    }
                    break;
                */

                case 0:
                    // Termina o loop while e volta ao menu de login (ou encerra)
                    view.mostrarMensagem("A sair do portal do estudante...");
                    aExecutar = false;
                    break;

                default:
                    // Tratamento de inputs fora do intervalo esperado
                    view.mostrarMensagem("Opção inválida. Tente novamente.");
            }
        }
    }
}