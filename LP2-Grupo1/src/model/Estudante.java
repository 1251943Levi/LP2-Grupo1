package model;

/**
 * Classe Model que representa um Estudante no sistema.
 * Estende a classe Utilizador para herdar dados pessoais e credenciais.
 */
public class Estudante extends Utilizador {

    private final int numeroMecanografico;
    private final int anoPrimeiraInscricao;
    private int anoCurricular;
    private final PercursoAcademico percurso;
    private double saldoDevedor;
    private String siglaCurso;
    private final Pagamento[] historicoPagamentos;
    private int totalPagamentos;


    /**
     * Construtor completo para o Estudante.
     */
    public Estudante(int numeroMecanografico, String email, String password, String nome,
                     String nif, String morada, String dataNascimento, int anoPrimeiraInscricao) {
        super(email, password, nome, nif, morada, dataNascimento);
        this.numeroMecanografico  = numeroMecanografico;
        this.anoPrimeiraInscricao = anoPrimeiraInscricao;
        this.anoCurricular        = 1;
        this.percurso             = new PercursoAcademico();
        this.historicoPagamentos  = new Pagamento[100];
        this.totalPagamentos      = 0;
    }

    // ---------- GETTERS ----------
    public int               getNumeroMecanografico()  { return numeroMecanografico; }
    public int               getAnoPrimeiraInscricao() { return anoPrimeiraInscricao; }
    public int               getAnoCurricular()        { return anoCurricular; }
    public PercursoAcademico getPercurso()             { return percurso; }
    public double            getSaldoDevedor()         { return saldoDevedor; }
    public String            getSiglaCurso()           { return siglaCurso; }
    public Pagamento[]       getHistoricoPagamentos()  { return historicoPagamentos; }
    public int               getTotalPagamentos()      { return totalPagamentos; }


    // ---------- SETTERS ----------
    public void setAnoCurricular(int anoCurricular)  { this.anoCurricular = anoCurricular; }
    public void setSaldoDevedor(double saldoDevedor) { this.saldoDevedor = saldoDevedor; }
    public void setSiglaCurso(String siglaCurso)     { this.siglaCurso = siglaCurso; }



    /**
     * Regista um pagamento no histórico em memória do estudante.
     * A persistência em ficheiro é feita pela PagamentoBLL via PagamentoDAL.
     */
    public void adicionarPagamento(Pagamento pagamento) {
        if (totalPagamentos < historicoPagamentos.length) {
            historicoPagamentos[totalPagamentos] = pagamento;
            totalPagamentos++;
        }
    }

    /**
     * Deduz um valor ao saldo devedor do estudante.
     *
     * @param valor Valor a ser subtraído da dívida.
     */
    public void efetuarPagamento(double valor) {
        if (valor > 0 && valor <= this.saldoDevedor) {
            this.saldoDevedor -= valor;
        }
    }

    @Override
    public String toString() {
        return numeroMecanografico + " - " + getNome();
    }
}