package bll;

import dal.AvaliacaoDAL;
import dal.CredencialDAL;
import dal.EstudanteDAL;
import dal.InscricaoDAL;
import dal.PagamentoDAL;
import dal.UcDAL;
import model.Avaliacao;
import model.Estudante;
import model.Pagamento;
import model.UnidadeCurricular;
import utils.SegurancaPasswords;
import java.util.ArrayList;
import java.util.List;

/**
 * Lógica de negócio para o perfil Estudante.
 * Responsável por carregar e instanciar o percurso académico completo
 * (inscrições em UCs, avaliações e histórico de pagamentos) após a autenticação.
 */
public class EstudanteBLL {

    private static final String PASTA_BD = "bd";


    /**
     * Carrega o perfil completo de um estudante após login.
     * Carrega sequencialmente: inscrições, avaliações e pagamentos.
     * @param email Email do estudante.
     * @param hash  Hash PBKDF2 da palavra-chave.
     * @return Estudante com percurso completo, ou null se não existir.
     */
    public Estudante obterPerfilCompleto(String email, String hash) {
        Estudante e = EstudanteDAL.carregarPerfil(email, hash, PASTA_BD);
        if (e == null) return null;

        carregarInscricoes(e);
        carregarAvaliacoes(e);
        carregarHistoricoPagamentos(e);
        return e;
    }

    /**
     * Devolve todos os estudantes com dados básicos.
     * @return Lista de estudantes.
     */
    public List<Estudante> obterTodos() {
        return EstudanteDAL.carregarTodos(PASTA_BD);
    }

    /**
     * Devolve todos os estudantes com percurso académico completo.
     * Usado em estatísticas e no cálculo de aproveitamento.
     * @return Lista de estudantes com percurso carregado.
     */
    public List<Estudante> carregarTodosCompleto() {
        List<Estudante> base = EstudanteDAL.carregarTodos(PASTA_BD);
        List<Estudante> hidratados = new ArrayList<>();

        for (Estudante e : base) {
            if (e == null) continue;
            carregarInscricoes(e);
            carregarAvaliacoes(e);
            hidratados.add(e);
        }
        return hidratados;
    }


    /**
     * Atualiza a morada do estudante e persiste a alteração.
     * @param estudante  Estudante a atualizar.
     * @param novaMorada Nova morada de residência.
     */
    public void atualizarMorada(Estudante estudante, String novaMorada) {
        estudante.setMorada(novaMorada);
        EstudanteDAL.atualizarEstudante(estudante, PASTA_BD);
    }

    /**
     * Altera a password do estudante com hashing e persistência.
     * @param estudante Estudante cujo acesso se altera.
     * @param novaPass  Nova password em texto limpo.
     */
    public void alterarPassword(Estudante estudante, String novaPass) {
        String passSegura = SegurancaPasswords.gerarCredencialMista(novaPass);
        estudante.setPassword(passSegura);
        CredencialDAL.atualizarPassword(estudante.getEmail(), passSegura, PASTA_BD);
    }

    /**
     * Calcula o próximo número mecanográfico disponível para um dado ano letivo.
     * @param anoAtual Ano letivo atual.
     * @return Próximo número mecanográfico no formato AAAA####.
     */
    public int obterProximoNumeroMecanografico(int anoAtual) {
        return EstudanteDAL.obterProximoNumeroMecanografico(PASTA_BD, anoAtual);
    }


    /**
     * Carrega o histórico de pagamentos do estudante a partir do ficheiro
     * e adiciona-os ao array de pagamentos do estudante.
     * @param e Estudante cujo histórico se pretende carregar.
     */
    private void carregarHistoricoPagamentos(Estudante e) {
        List<Pagamento> pagamentos =
                PagamentoDAL.carregarPagamentosPorAluno(e.getNumeroMecanografico(), PASTA_BD);
        for (Pagamento p : pagamentos) {
            e.adicionarPagamento(p);
        }
    }

    /**
     * Carrega as inscrições ativas do estudante a partir do ficheiro
     * e inscreve-o nas respetivas UCs do percurso académico.
     * @param e Estudante cujas inscrições se pretendem carregar.
     */
    private void carregarInscricoes(Estudante e) {
        List<String> siglas = InscricaoDAL.obterSiglasUcsPorAluno(
                e.getNumeroMecanografico(), PASTA_BD);
        for (String sigla : siglas) {
            UnidadeCurricular uc = new UcBLL().procurarUCCompleta(sigla);
            if (uc != null) e.getPercurso().inscreverEmUc(uc);
        }
    }

    /**
     * Carrega o historial de avaliações do estudante a partir do ficheiro
     * e regista-as no percurso académico.
     * @param e Estudante cujas avaliações se pretendem carregar.
     */
    private void carregarAvaliacoes(Estudante e) {
        List<Avaliacao> avaliacoes =
                AvaliacaoDAL.obterAvaliacoesPorAluno(e.getNumeroMecanografico(), PASTA_BD);
        for (Avaliacao av : avaliacoes) {
            e.getPercurso().registarAvaliacao(av);
        }
    }

    /**
     * Retorna uma lista formatada com o ano curricular e as UCs em que o estudante está inscrito.
     * @param e Estudante autenticado
     * @return String formatada para exibição
     */
    public String obterInfoInscricoes(Estudante e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Ano Curricular: ").append(e.getAnoCurricular()).append("º\n");
        sb.append("UCs inscrito:\n");
        UnidadeCurricular[] ucs = e.getPercurso().getUcsInscrito(); // Nota: esse getter pode não existir; vamos ver abaixo.
        int total = e.getPercurso().getTotalUcsInscrito();
        if (total == 0) {
            sb.append("  Nenhuma UC encontrada.\n");
        } else {
            for (int i = 0; i < total; i++) {
                sb.append("  - ").append(ucs[i].getSigla()).append(": ").append(ucs[i].getNome()).append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Devolve uma string formatada com as notas do estudante por UC.
     * @param e Estudante autenticado
     * @return String com as notas (UC, notas lançadas, média, estado)
     */
    public String obterNotasDoEstudante(Estudante e) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Notas do aluno: %s (%d)\n", e.getNome(), e.getNumeroMecanografico()));
        sb.append("------------------------------------------------------------\n");

        int totalAvaliacoes = e.getPercurso().getTotalAvaliacoes();
        if (totalAvaliacoes == 0) {
            sb.append("Nenhuma avaliação registada.\n");
            return sb.toString();
        }

        Avaliacao[] historico = e.getPercurso().getHistoricoAvaliacoes();
        for (int i = 0; i < totalAvaliacoes; i++) {
            Avaliacao av = historico[i];
            if (av == null || av.getUc() == null) continue;

            String sigla = av.getUc().getSigla();
            String nomeUC = av.getUc().getNome();
            int ano = av.getAnoLetivo();
            double[] notas = av.getResultados();
            int totalNotas = av.getTotalAvaliacoesLancadas();

            sb.append(String.format("UC: %s - %s (Ano %d)\n", sigla, nomeUC, ano));
            if (totalNotas == 0) {
                sb.append("  Sem notas lançadas.\n");
            } else {
                sb.append("  Notas: ");
                for (int j = 0; j < totalNotas; j++) {
                    sb.append(String.format("%.1f", notas[j]));
                    if (j < totalNotas - 1) sb.append(", ");
                }
                double media = av.calcularMedia();
                sb.append(String.format(" | Média: %.1f | %s\n", media, av.isAprovado() ? "APROVADO" : "REPROVADO"));
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}