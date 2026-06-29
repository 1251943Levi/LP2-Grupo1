package bll;

import common.ConfigApp;
import dal.CursoDALFile;
import dal.CursoDALSql;
import dal.UcDALFile;
import dal.UcDALSql;

import dal.DepartamentoDAL;
import dal.DepartamentoDALFile;
import dal.DepartamentoDALSql;
import dal.EstudanteDAL;
import dal.EstudanteDALFile;
import dal.EstudanteDALSql;
import model.Curso;
import model.Departamento;
import model.EstadoCurricular;
import model.UnidadeCurricular;
import dal.CursoDAL;
import dal.UcDAL;

import java.util.List;

/**
 * Lógica de negócio para a entidade Curso.
 * Responsável por construir o objeto Curso com o departamento associado,
 * evitando que os controllers acedam diretamente à DAL.
 */
public class CursoBLL {

    private static final String PASTA_BD = ConfigApp.PASTA_BD;
    private final CursoDAL cursoDAL = ConfigApp.isModoSql() ? new CursoDALSql() : new CursoDALFile();
    private final UcDAL ucDAL = ConfigApp.isModoSql() ? new UcDALSql() : new UcDALFile();
    // A7: acesso ao módulo do estudante (lazy — evita efeitos colaterais no arranque)
    private EstudanteBLL moduloEstudante;
    private EstudanteBLL moduloEstudante() {
        if (moduloEstudante == null) moduloEstudante = new EstudanteBLL();
        return moduloEstudante;
    }
    private final DepartamentoDAL departamentoDAL =
            ConfigApp.isModoSql() ? new DepartamentoDALSql() : new DepartamentoDALFile();

    /**
     * Constrói e devolve um objeto Curso completamente preenchido.
     * @param sigla Sigla do curso a pesquisar.
     * @return O Curso com departamento associado, ou null se não existir.
     */
    public Curso procurarCursoCompleto(String sigla) {
        String[] dados = cursoDAL.obterDadosBrutosCurso(sigla, PASTA_BD);

        if (dados == null) return null;

        String siglaCurso = dados[0].trim();
        String nomeCurso = dados[1].trim();
        String siglaDepartamento = dados[2].trim();

        double propina = 0.0;
        if (dados.length >= 4) {
            try {
                propina = Double.parseDouble(dados[3].trim());
            } catch (NumberFormatException ignored) {}
        }

        Departamento dep = departamentoDAL.procurarPorSigla(siglaDepartamento);

        Curso curso = new Curso(siglaCurso, nomeCurso, dep, propina);

        if (dados.length >= 5 && !dados[4].trim().isEmpty()) {
            curso.setEstado(dados[4].trim());
        }

        return curso;
    }

    /**
     * Valida se um curso está pronto para receber matrículas,
     * verificando se tem pelo menos 1 UC configurada no 1º ano.
     * @param siglaCurso Sigla do curso a verificar.
     * @return true se tiver pelo menos 1 UC no 1º ano.
     */
    public boolean verificarCursoTemUcs(String siglaCurso) {
        return ucDAL.contarUcsPorCursoEAno(siglaCurso, 1, PASTA_BD) > 0;
    }

    /**
     * F1/F2: calcula o estado de um curso a partir de TODAS as restrições
     * (UCs, propina e quórum), não apenas dos estudantes.
     *  - INATIVO se foi desativado manualmente (mantém-se);
     *  - ATIVO se tem UCs no 1.º ano, propina definida (>0) e quórum cumprido
     *    (sem alunos no 1.º ano, ou pelo menos 5);
     *  - SEM_CONDICOES caso contrário (criado mas sem condições para abrir).
     * @param siglaCurso Sigla do curso a avaliar.
     * @return O {@link EstadoCurricular} calculado.
     */
    public EstadoCurricular avaliarEstado(String siglaCurso) {
        Curso curso = procurarCursoCompleto(siglaCurso);
        if (curso == null) return EstadoCurricular.SEM_CONDICOES;
        if (curso.getEstadoCurricular() == EstadoCurricular.INATIVO) return EstadoCurricular.INATIVO;

        boolean temUcs    = ucDAL.contarUcsPorCursoEAno(siglaCurso, 1, PASTA_BD) > 0;
        boolean propinaOk = curso.getValorPropinaAnual() > 0;
        int a1            = moduloEstudante().contarEstudantesPorCursoEAno(siglaCurso, 1);
        boolean quorumOk  = (a1 == 0) || (a1 >= 5);
        boolean ucsProntas = ucsDoPrimeiroAnoProntas(siglaCurso);

        return (temUcs && propinaOk && quorumOk && ucsProntas) ? EstadoCurricular.ATIVO : EstadoCurricular.SEM_CONDICOES;
    }

    /** As UCs do 1.º ano têm docente responsável e momentos definidos? */
    private boolean ucsDoPrimeiroAnoProntas(String siglaCurso) {
        UcBLL ucBll = new UcBLL();
        for (String siglaUc : ucDAL.obterSiglasUcsPorCursoEAno(siglaCurso, 1, PASTA_BD)) {
            UnidadeCurricular uc = ucBll.procurarUCCompleta(siglaUc);
            if (uc == null || uc.getDocenteResponsavel() == null || uc.getNumMomentos() <= 0) return false;
        }
        return true;
    }

    /**
     * Recalcula o estado do curso (via {@link #avaliarEstado}) e persiste-o.
     * Deve ser chamado após operações que alterem as condições (UCs, propina, alunos).
     * @param siglaCurso Sigla do curso a atualizar.
     */
    public void atualizarEstadoCurricular(String siglaCurso) {
        Curso curso = procurarCursoCompleto(siglaCurso);
        if (curso == null) return;
        curso.setEstadoCurricular(avaliarEstado(siglaCurso));
        cursoDAL.atualizarCurso(curso, PASTA_BD);
    }

    /**
     * A3: motivo(s) por que um curso não está apto a abrir.
     * @return string vazia se está apto (ATIVO); caso contrário, os motivos separados por "; ".
     */
    public String motivoNaoApto(String siglaCurso) {
        Curso curso = procurarCursoCompleto(siglaCurso);
        if (curso == null) return "curso inexistente";
        if (curso.getEstadoCurricular() == EstadoCurricular.INATIVO) return "curso inativo (desativado manualmente)";

        java.util.List<String> motivos = new java.util.ArrayList<>();
        if (ucDAL.contarUcsPorCursoEAno(siglaCurso, 1, PASTA_BD) == 0) motivos.add("sem UCs no 1.º ano");
        if (curso.getValorPropinaAnual() <= 0) motivos.add("propina por definir");
        int a1 = moduloEstudante().contarEstudantesPorCursoEAno(siglaCurso, 1);
        if (a1 > 0 && a1 < 5) motivos.add("quórum insuficiente no 1.º ano (" + a1 + "/5)");
        if (!ucsDoPrimeiroAnoProntas(siglaCurso)) motivos.add("UCs do 1.º ano sem docente/momentos definidos");

        return motivos.isEmpty() ? "" : String.join("; ", motivos);
    }

    /**
     * Valida se o primeiro ano de um curso pode ser iniciado.
     * @param siglaCurso Sigla do curso a verificar.
     * @return true se tiver 5 ou mais alunos
     */
    public boolean verificarQuorumPrimeiroAno(String siglaCurso) {
        int total = moduloEstudante().contarEstudantesPorCursoEAno(siglaCurso, 1);        return total >= 5;
    }

    /**
     * Lista todos os cursos (dados básicos).
     */
    public List<Curso> listarTodos() {
        return cursoDAL.carregarTodos(PASTA_BD);
    }

    /**
     * Obtém um curso pela sigla (completo).
     */
    public Curso obterPorSigla(String sigla) {
        return procurarCursoCompleto(sigla);
    }

    /**
     * Cria um novo curso. O departamento deve existir e não pode ser nulo.
     * @param sigla Sigla do curso.
     * @param nome Nome do curso.
     * @param siglaDepartamento Sigla do departamento a que pertence.
     * @param propina Valor da propina anual.
     * @return true se criado com sucesso, false se o departamento não existir ou curso já existir.
     */
    public boolean adicionarCurso(String sigla, String nome, String siglaDepartamento, double propina) {
        if (sigla == null || nome == null || siglaDepartamento == null) return false;
        // Verificar se já existe curso com essa sigla
        if (procurarCursoCompleto(sigla) != null) return false;

        Departamento dep = departamentoDAL.procurarPorSigla(siglaDepartamento);
        if (dep == null) return false; // departamento obrigatório

        Curso curso = new Curso(sigla, nome, dep, propina);
        curso.setEstadoCurricular(EstadoCurricular.SEM_CONDICOES); // criado sem condições para abrir
        cursoDAL.adicionarCurso(curso, PASTA_BD);
        return true;
    }

    /**
     * Actualiza os dados de um curso (nome, departamento e propina).
     * A sigla permanece inalterada.
     * @param sigla Sigla do curso a actualizar.
     * @param novoNome Novo nome (pode ser null para manter).
     * @param novaSiglaDep Nova sigla do departamento (pode ser null para manter).
     * @param novaPropina Nova propina (valores negativos ou null mantêm a actual).
     * @return true se a actualização foi bem-sucedida.
     */
    public boolean atualizarCurso(String sigla, String novoNome, String novaSiglaDep, Double novaPropina) {
        Curso existente = procurarCursoCompleto(sigla);
        if (existente == null) return false;

        String nomeFinal = (novoNome != null && !novoNome.isEmpty()) ? novoNome : existente.getNome();

        Departamento depFinal = existente.getDepartamento();
        if (novaSiglaDep != null && !novaSiglaDep.isEmpty()) {
            Departamento novoDep = departamentoDAL.procurarPorSigla(novaSiglaDep);
            if (novoDep != null) depFinal = novoDep;
        }

        double propinaFinal = existente.getValorPropinaAnual();
        if (novaPropina != null && novaPropina >= 0) {
            propinaFinal = novaPropina;
        }

        // Criar novo curso com os dados actualizados
        Curso cursoAtualizado = new Curso(sigla, nomeFinal, depFinal, propinaFinal);
        cursoAtualizado.setEstado(existente.getEstado());

        cursoDAL.atualizarCurso(cursoAtualizado, PASTA_BD);
        return true;
    }

    /**
     * Remove um curso apenas se não tiver alocações (estudantes ou UCs).
     * @param sigla Sigla do curso.
     * @return true se removido.
     */
    public boolean removerCurso(String sigla) {
        if (!isAlteravel(sigla)) return false;
        return cursoDAL.removerCurso(sigla, PASTA_BD);
    }

    /**
     * Verifica se um curso pode ser editado ou removido (sem estudantes nem UCs).
     * @param sigla Sigla do curso.
     * @return true se não tiver alocações.
     */
    public boolean isAlteravel(String sigla) {
        // Chamadas corrigidas (usando a instância e sem o PASTA_BD)
        int totalAlunos = moduloEstudante().contarEstudantesPorCursoEAno(sigla, 1)
                + moduloEstudante().contarEstudantesPorCursoEAno(sigla, 2)
                + moduloEstudante().contarEstudantesPorCursoEAno(sigla, 3);
        int totalUcs = ucDAL.contarUcsPorCursoEAno(sigla, 1, PASTA_BD)
                + ucDAL.contarUcsPorCursoEAno(sigla, 2, PASTA_BD)
                + ucDAL.contarUcsPorCursoEAno(sigla, 3, PASTA_BD);
        return totalAlunos == 0 && totalUcs == 0;
    }
}