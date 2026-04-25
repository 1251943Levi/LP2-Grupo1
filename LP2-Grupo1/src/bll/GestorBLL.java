package bll;

import dal.*;
import model.*;
import utils.*;
import view.GestorView;

import java.util.ArrayList;
import java.util.List;


/**
 * Camada de Lógica de Negócio (Business Logic Layer) para o perfil Gestor.
 * Esta classe centraliza as regras de decisão do sistema, cálculos estatísticos
 * e a orquestração entre os modelos e a persistência em ficheiros CSV.
 */
public class GestorBLL {

    private static final String PASTA_BD = "bd";


    // --- 1. GESTÃO DE CICLO DE VIDA (ANO LETIVO) ---

    /**
     * Avança o ano letivo aplicando as regras dos enunciados:
     *  - Quórum mínimo de 5 alunos no 1.º ano.
     *  - Bloqueio por dívida de propina.
     *  - Bloqueio por aproveitamento insuficiente.
     */
    public void avancarAnoLetivo(RepositorioDados repo, GestorView view) {
        view.mostrarCabecalhoArranqueAnoLetivo();

        String[] cursos = CursoDAL.obterListaCursos(PASTA_BD);
        if (cursos.length == 0) { view.mostrarErroCarregarDados("Cursos"); return; }

        view.mostrarVerificacaoQuorum();
        CursoBLL cursoBll = new CursoBLL();
        for (String c : cursos) {
            String sigla = c.split(" - ")[0];
            Curso curso  = cursoBll.procurarCursoCompleto(sigla);
            if (curso == null) continue;

            int a1 = EstudanteDAL.contarEstudantesPorCursoEAno(sigla, 1, PASTA_BD);
            int a2 = EstudanteDAL.contarEstudantesPorCursoEAno(sigla, 2, PASTA_BD);
            int a3 = EstudanteDAL.contarEstudantesPorCursoEAno(sigla, 3, PASTA_BD);

            if (a1 > 0 && a1 < 5) {
                view.mostrarErroQuorum(sigla, a1);
                curso.setEstado("Inativo");
            } else if (a1 >= 5 || a2 >= 1 || a3 >= 1) {
                view.mostrarSucessoQuorum(sigla);
                curso.setEstado("Ativo");
            } else {
                curso.setEstado("Inativo");
            }
            CursoDAL.atualizarCurso(curso, PASTA_BD);
        }

        view.mostrarProcessamentoTransicoes();
        List<Estudante> estudantes = new EstudanteBLL().carregarTodosCompleto();

        for (Estudante e : estudantes) {
            if (e == null) continue;

            if (e.getSaldoDevedor() > 0) {
                view.mostrarBloqueioDivida(
                        e.getNumeroMecanografico(), e.getNome(),
                        e.getAnoCurricular(), e.getSaldoDevedor());
                continue;
            }

            if (!e.getPercurso().temAproveitamentoSuficiente()) {
                double pct = e.getPercurso().calcularPercentagemAproveitamento();
                view.mostrarBloqueioAproveitamento(
                        e.getNumeroMecanografico(), e.getNome(),
                        e.getAnoCurricular(), pct);
                continue;
            }
            if (e.getAnoCurricular() < 3) {
                e.setAnoCurricular(e.getAnoCurricular() + 1);
                e.getPercurso().limparInscricoesAtivas();
                Curso cursoDoEstudante = new CursoBLL().procurarCursoCompleto(e.getSiglaCurso());
                if (cursoDoEstudante != null) {
                    e.setSaldoDevedor(cursoDoEstudante.getValorPropinaAnual());
                }
                view.mostrarTransicaoSucedida(e.getNumeroMecanografico(), e.getAnoCurricular());
            } else {
                view.mostrarConclusaoCurso(e.getNumeroMecanografico());
            }
            EstudanteDAL.atualizarEstudante(e, PASTA_BD);
        }

        repo.setAnoAtual(repo.getAnoAtual() + 1);
        view.mostrarSucessoAvancoAno(repo.getAnoAtual());
    }

    // --- 2. GESTÃO DE REGISTOS (DOCENTES E ESTUDANTES) ---

    /**
     * Regista um novo docente no sistema.
     * Gera automaticamente o e-mail, password segura e envia as credenciais.
     * * @param nome     Nome completo do docente.
     * @param sigla    Sigla identificadora (ex: "JDO").
     * @param nif      Número de Identificação Fiscal.
     * @param morada   Morada de residência.
     * @param dataNasc Data de nascimento (DD-MM-AAAA).
     * @return O e-mail institucional gerado para o docente.
     */
    public String registarDocente(String nome, String sigla, String nif,
                                  String morada, String dataNasc) {
        String email     = EmailGenerator.gerarEmailDocente(nome);
        String passLimpa = PasswordGenerator.gerarPasswordSegura();
        EmailService.enviarCredenciaisTodos(nome, email, passLimpa);
        String passHash  = SegurancaPasswords.gerarCredencialMista(passLimpa);
        DocenteDAL.adicionarDocente(new Docente(sigla, email, passHash, nome, nif, morada, dataNasc), PASTA_BD);
        CredencialDAL.adicionarCredencial(email, passHash, "DOCENTE", PASTA_BD);
        return email;
    }

    /**
     * Regista um novo estudante e associa o valor da propina anual ao seu saldo devedor.
     * * @param numMec       Número mecanográfico gerado.
     * @param nome         Nome completo.
     * @param nif          NIF validado.
     * @param morada       Morada.
     * @param dataNasc     Data de nascimento.
     * @param siglaCurso   Sigla do curso onde se matricula.
     * @param anoInscricao Ano letivo da matrícula.
     * @return O e-mail institucional gerado.
     */
    public String registarEstudante(int numMec, String nome, String nif, String morada,
                                    String dataNasc, String siglaCurso, int anoInscricao) {
        String email = EmailGenerator.gerarEmailEstudante(numMec);
        String passLimpa = PasswordGenerator.gerarPasswordSegura();
        EmailService.enviarCredenciaisTodos(nome, email, passLimpa);
        String passHash = SegurancaPasswords.gerarCredencialMista(passLimpa);
        Estudante novo = new Estudante(numMec, email, passHash, nome, nif, morada, dataNasc, anoInscricao);
        Curso curso = new CursoBLL().procurarCursoCompleto(siglaCurso);
        if (curso != null) novo.setSaldoDevedor(curso.getValorPropinaAnual());
        EstudanteDAL.adicionarEstudante(novo, siglaCurso, PASTA_BD);
        CredencialDAL.adicionarCredencial(email, passHash, "ESTUDANTE", PASTA_BD);
        return email;
    }

    // --- 3. ESTATÍSTICAS ---

    /**
     * Devolve os dados brutos da média global: [0] = soma, [1] = total de notas.
     * Delega o cálculo em Estatisticas.calcularDadosMediaGlobal().
     */
    public double[] calcularEstatisticasGlobais() {
        return Estatisticas.calcularDadosMediaGlobal();
    }

    /**
     * Devolve o melhor aluno: [0] = Estudante, [1] = Double (média).
     * Delega o cálculo em Estatisticas.calcularMelhorAluno().
     */
    public Object[] obterMelhorAluno() {
        return Estatisticas.calcularMelhorAluno();
    }

    // --- 4. GESTÃO DE ENTIDADES (UCs E CURSOS) ---

    /**
     * Adiciona uma nova Unidade Curricular verificando o limite de UCs por curso/ano.
     * * @return true se a UC foi adicionada com sucesso.
     */
    public boolean adicionarUc(String siglaCurso, int anoUc, String siglaUc,
                               String nomeUc, String siglaDocente, RepositorioDados repo) {
        if (UcDAL.contarUcsPorCursoEAno(siglaCurso, anoUc, PASTA_BD) >= 5) return false;
        Docente doc = DocenteDAL.procurarPorSigla(siglaDocente, PASTA_BD);
        UcDAL.adicionarUC(new UnidadeCurricular(siglaUc, nomeUc, anoUc, doc), siglaCurso, PASTA_BD);
        return true;
    }

    /**
     * Edita uma UC removendo o registo antigo e inserindo um novo.
     */
    public boolean editarUc(String siglaAntiga, String novaSigla, String nome,
                            String ano, String siglaDocente, String siglaCurso) {
        if (!UcDAL.removerUC(siglaAntiga, PASTA_BD)) return false;
        try {
            Docente doc = DocenteDAL.procurarPorSigla(siglaDocente, PASTA_BD);
            UcDAL.adicionarUC(new UnidadeCurricular(novaSigla, nome, Integer.parseInt(ano), doc),
                    siglaCurso, PASTA_BD);
            return true;
        } catch (NumberFormatException ex) { return false; }
    }

    /**
     * Cria um novo curso no sistema com estado inicial "Inativo".
     */
    public void adicionarCurso(String sigla, String nome, String siglaDep, double propina) {
        Departamento dep = DepartamentoDAL.procurarDepartamento(siglaDep, PASTA_BD);
        Curso c = new Curso(sigla, nome, dep, propina);
        c.setEstado("Inativo");
        CursoDAL.adicionarCurso(c, PASTA_BD);
    }

    public boolean removerUc(String siglaUc) {
        return UcDAL.removerUC(siglaUc, PASTA_BD);
    }

    /** Devolve array "SIGLA - Nome" de todos os cursos (para menus de seleção). */
    public String[] obterListaCursos() {
        return CursoDAL.obterListaCursos(PASTA_BD);
    }

    public String listarTodasUcs()    { return UcDAL.listarTodasUcs(PASTA_BD); }
    public String listarTodosCursos() { return CursoDAL.listarTodosCursos(PASTA_BD); }

    public List<Estudante> obterListaDevedores() {
        List<Estudante> devedores = new ArrayList<>();
        for (Estudante e : EstudanteDAL.carregarTodos(PASTA_BD))
            if (e != null && e.getSaldoDevedor() > 0) devedores.add(e);
        return devedores;
    }

    /**
     * Altera a password de um gestor, aplicando hashing e atualizando o ficheiro de credenciais.
     */
    public void alterarPasswordGestor(Gestor gestor, String novaPass) {
        String hash = SegurancaPasswords.gerarCredencialMista(novaPass);
        gestor.setPassword(hash);
        CredencialDAL.atualizarPassword(gestor.getEmail(), hash, PASTA_BD);
    }

    public boolean isNifDuplicado(String nif) {
        return EstudanteDAL.existeNif(nif, PASTA_BD) || DocenteDAL.existeNif(nif, PASTA_BD);
    }

    /**
     * Verifica se já existe um departamento com a sigla fornecida.
     */
    public boolean isDepartamentoDuplicado(String sigla) {
        return DepartamentoDAL.procurarDepartamento(sigla, PASTA_BD) != null;
    }

    /**
     * Regista um novo departamento no sistema.
     * @param sigla Sigla do departamento (ex: "DEIS").
     * @param nome  Nome completo do departamento.
     */
    public void registarDepartamento(String sigla, String nome) {
        Departamento dep = new Departamento(sigla.toUpperCase(), nome);
        DepartamentoDAL.adicionarDepartamento(dep, PASTA_BD);
    }
}