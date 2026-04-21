package bll;

import model.Estudante;
import utils.ExportadorCSV;
import utils.SegurancaPasswords;

/**
 * Camada de Lógica de Negócio para o perfil Estudante.
 * Gere pagamentos, atualizações de perfil e segurança de credenciais.
 */
public class EstudanteBLL {

    private static final String PASTA_BD = "bd";

    /**
     * Processa a atualização da morada e grava no ficheiro.
     */
    public void atualizarMorada(Estudante estudante, String novaMorada) {
        estudante.setMorada(novaMorada);
        ExportadorCSV.atualizarEstudante(estudante, PASTA_BD);
    }

    /**
     * Aplica hashing à nova password e atualiza o sistema de credenciais.
     */
    public void alterarPassword(Estudante estudante, String novaPass) {
        String passSegura = SegurancaPasswords.gerarCredencialMista(novaPass);
        estudante.setPassword(passSegura);
        ExportadorCSV.atualizarPasswordCentralizada(estudante.getEmail(), passSegura, PASTA_BD);
    }

    /**
     * Processa um pagamento, atualiza o saldo do objeto e persiste no CSV.
     * @return true se o pagamento foi processado.
     */
    public boolean processarPagamento(Estudante estudante, double valor) {
        if (valor <= 0 || valor > estudante.getSaldoDevedor()) {
            return false;
        }
        estudante.efetuarPagamento(valor);
        ExportadorCSV.atualizarEstudante(estudante, PASTA_BD);
        return true;
    }
}