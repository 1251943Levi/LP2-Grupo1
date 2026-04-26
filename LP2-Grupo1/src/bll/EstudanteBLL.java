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
 * Camada de Lógica de Negócio para o perfil Estudante.
 * Gere atualizações de perfil, segurança de credenciais e a hidratação
 * completa do percurso académico (inscrições, avaliações, histórico de pagamentos).
 */
public class EstudanteBLL {

    private static final String PASTA_BD = "bd";


    /**
     * Carrega o perfil completo do estudante após login.
     * Hidrata: inscrições em UCs, histórico de avaliações e histórico de pagamentos.
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
     * Devolve todos os estudantes com os dados básicos (sem percurso académico).
     * Adequado para listagens que não precisam de notas.
     */
    public List<Estudante> obterTodos() {
        return EstudanteDAL.carregarTodos(PASTA_BD);
    }

    /**
     * Devolve todos os estudantes com o percurso académico completamente hidratado
     * (inscrições + avaliações). Usado para estatísticas e avaliação de aproveitamento.
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
     * Processa a atualização da morada e grava no ficheiro.
     */
    public void atualizarMorada(Estudante estudante, String novaMorada) {
        estudante.setMorada(novaMorada);
        EstudanteDAL.atualizarEstudante(estudante, PASTA_BD);
    }

    /**
     * Aplica hashing à nova password e atualiza o sistema de credenciais.
     */
    public void alterarPassword(Estudante estudante, String novaPass) {
        String passSegura = SegurancaPasswords.gerarCredencialMista(novaPass);
        estudante.setPassword(passSegura);
        CredencialDAL.atualizarPassword(estudante.getEmail(), passSegura, PASTA_BD);
    }

    public int obterProximoNumeroMecanografico(int anoAtual) {
        return EstudanteDAL.obterProximoNumeroMecanografico(PASTA_BD, anoAtual);
    }


    /**
     * Carrega o histórico de pagamentos de propinas do estudante
     * a partir de pagamentos.csv.
     */
    private void carregarHistoricoPagamentos(Estudante e) {
        List<Pagamento> pagamentos =
                PagamentoDAL.carregarPagamentosPorAluno(e.getNumeroMecanografico(), PASTA_BD);
        for (Pagamento p : pagamentos) {
            e.adicionarPagamento(p);
        }
    }
    private void carregarInscricoes(Estudante e) {
        List<String> siglas = InscricaoDAL.obterSiglasUcsPorAluno(
                e.getNumeroMecanografico(), PASTA_BD);
        for (String sigla : siglas) {
            UnidadeCurricular uc = UcDAL.procurarUC(sigla, PASTA_BD);
            if (uc != null) e.getPercurso().inscreverEmUc(uc);
        }
    }

    /**
     * Carrega avaliações do CSV e associa ao percurso do estudante.
     */
    private void carregarAvaliacoes(Estudante e) {
        List<Avaliacao> avaliacoes =
                AvaliacaoDAL.obterAvaliacoesPorAluno(e.getNumeroMecanografico(), PASTA_BD);
        for (Avaliacao av : avaliacoes) {
            e.getPercurso().registarAvaliacao(av);
        }
    }
}