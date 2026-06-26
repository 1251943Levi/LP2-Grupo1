package bll;

import common.ConfigApp;
import dal.JustificacaoDAL;
import dal.JustificacaoDALFile;
import dal.JustificacaoDALSql;
import dal.TipoJustificacaoDAL;
import dal.TipoJustificacaoDALFile;
import dal.TipoJustificacaoDALSql;
import dal.EstatutoDAL;
import dal.EstatutoDALFile;
import dal.EstatutoDALSql;
import model.EstatutoEstudante;
import model.Justificacao;
import model.TipoJustificacao;
import model.Estudante;
import model.Aula;
import utils.Config;

import java.time.LocalDateTime;
import java.util.List;

public class JustificacaoBLL {
    private final JustificacaoDAL justificacaoDAL;
    private final TipoJustificacaoDAL tipoDAL;
    private final EstatutoDAL estatutoDAL;
    private final EstudanteBLL estudanteBll = new EstudanteBLL();
    private final HorarioBLL horarioBll = new HorarioBLL();

    public JustificacaoBLL() {
        this.justificacaoDAL = ConfigApp.isModoSql() ? new JustificacaoDALSql() : new JustificacaoDALFile();
        this.tipoDAL = ConfigApp.isModoSql() ? new TipoJustificacaoDALSql() : new TipoJustificacaoDALFile();
        this.estatutoDAL = ConfigApp.isModoSql() ? new EstatutoDALSql() : new EstatutoDALFile();
        justificacaoDAL.inicializar();
        tipoDAL.inicializar();
        estatutoDAL.inicializar();
    }

    /** Estatutos disponiveis no catalogo (mostrados ao submeter justificacao). */
    public List<EstatutoEstudante> listarEstatutosDisponiveis() {
        return estatutoDAL.listarTodos();
    }

    /** Estatutos atribuidos a um estudante. */
    public List<EstatutoEstudante> listarEstatutosDoEstudante(int numMec) {
        return estatutoDAL.listarPorEstudante(numMec);
    }

    public List<TipoJustificacao> listarTiposJustificacao() {
        return tipoDAL.listarTodos();
    }

    public void criarJustificacao(int numMec, int idAula, int idTipoJustificacao) {
        // Verificar se o aluno existe
        Estudante e = estudanteBll.procurarPorNumMec(numMec);
        if (e == null) throw new EstadoInvalidoException("Aluno não encontrado.");

        // Verificar se a aula existe
        Aula aula = horarioBll.buscarPorId(idAula);
        if (aula == null) throw new EstadoInvalidoException("Aula não encontrada.");

        // A aula já deve ter ocorrido (data passada)
        if (aula.getData().isAfter(LocalDateTime.now().toLocalDate())) {
            throw new EstadoInvalidoException("Não é possível justificar uma aula futura.");
        }

        // Verificar se já existe justificação para esta aula (pendente ou aceite)
        List<Justificacao> existentes = justificacaoDAL.listarPorAula(idAula);
        for (Justificacao j : existentes) {
            if (j.getNumMec() == numMec && !"REJEITADA".equalsIgnoreCase(j.getEstado())) {
                throw new EstadoInvalidoException("Já existe uma justificação para esta aula (estado: " + j.getEstado() + ").");
            }
        }

        Justificacao just = new Justificacao();
        just.setNumMec(numMec);
        just.setIdAula(idAula);
        just.setIdTipoJustificacao(idTipoJustificacao);
        just.setEstado("PENDENTE");
        just.setDataCriacao(LocalDateTime.now());
        justificacaoDAL.adicionar(just);
    }

    public List<Justificacao> listarPendentes() {
        return justificacaoDAL.listarPendentes();
    }

    public void processarJustificacao(int idJustificacao, boolean aceite, String observacao) {
        Justificacao j = justificacaoDAL.buscarPorId(idJustificacao);
        if (j == null) throw new EstadoInvalidoException("Justificação não encontrada.");
        if (!"PENDENTE".equalsIgnoreCase(j.getEstado())) {
            throw new EstadoInvalidoException("Esta justificação já foi processada.");
        }

        j.setDataResposta(LocalDateTime.now());
        j.setObservacao(observacao);

        if (aceite) {
            j.setEstado("APROVADA");
            // Registrar presença justificada
            PresencaBLL presencaBll = new PresencaBLL();
            presencaBll.registarJustificacaoPresenca(j.getNumMec(), j.getIdAula());
        } else {
            j.setEstado("REJEITADA");
        }

        justificacaoDAL.atualizar(j);
    }

    // Método para o estudante ver as suas justificações (estado)
    public List<Justificacao> listarPorAluno(int numMec) {
        return justificacaoDAL.listarPorAluno(numMec);
    }

    public void adicionarTipo(TipoJustificacao tipo) {
        if (tipo == null) throw new EstadoInvalidoException("Tipo inválido.");
        if (tipo.getNome() == null || tipo.getNome().trim().isEmpty())
            throw new EstadoInvalidoException("O nome do tipo é obrigatório.");
        tipoDAL.adicionar(tipo);
    }

    public void removerTipo(int id) {
        // Verificar se já há justificações associadas
        List<Justificacao> justs = justificacaoDAL.listarTodas();
        for (Justificacao j : justs) {
            if (j.getIdTipoJustificacao() == id) {
                throw new EstadoInvalidoException("Não é possível remover um tipo que já tem justificações associadas.");
            }
        }
        tipoDAL.remover(id);
    }
}