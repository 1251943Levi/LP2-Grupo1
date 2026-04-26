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
            if (e.getAnoCurricular() > 3) continue; // já graduado — excluído do ciclo

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
                int novoAno = e.getAnoCurricular() + 1;
                e.setAnoCurricular(novoAno);
                e.getPercurso().limparInscricoesAtivas();

                // Remover inscrições de UCs aprovadas — só as chumbadas transitam
                for (String siglaAprov : obterSiglasUcsAprovadas(e)) {
                    InscricaoDAL.removerInscricao(e.getNumeroMecanografico(), siglaAprov, PASTA_BD);
                }
                // Inscrever nas UCs do novo ano
                for (String siglaUc : UcDAL.obterSiglasUcsPorCursoEAno(e.getSiglaCurso(), novoAno, PASTA_BD)) {
                    InscricaoDAL.adicionarInscricao(e.getNumeroMecanografico(), siglaUc, PASTA_BD);
                }
                // Repor propina do novo ano letivo
                Curso cursoDoEstudante = new CursoBLL().procurarCursoCompleto(e.getSiglaCurso());
                if (cursoDoEstudante != null) {
                    e.setSaldoDevedor(cursoDoEstudante.getValorPropinaAnual());
                }
                view.mostrarTransicaoSucedida(e.getNumeroMecanografico(), novoAno);
            } else {
                e.setAnoCurricular(4); // marca como graduado — nunca mais processado neste ciclo
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
     * A sigla é gerada automaticamente a partir do nome, garantindo unicidade.
     * Gera automaticamente o e-mail, password segura e envia as credenciais.
     *
     * @param nome     Nome completo do docente.
     * @param nif      Número de Identificação Fiscal.
     * @param morada   Morada de residência.
     * @param dataNasc Data de nascimento (DD-MM-AAAA).
     * @return String[] com [0] = e-mail institucional, [1] = sigla gerada.
     */
    public String[] registarDocente(String nome, String nif, String morada, String dataNasc) {
        String sigla    = gerarSiglaUnica(nome);
        String email    = EmailGenerator.gerarEmailDocente(sigla);
        String passLimpa = PasswordGenerator.gerarPasswordSegura();
        EmailService.enviarCredenciaisTodos(nome, email, passLimpa);
        String passHash = SegurancaPasswords.gerarCredencialMista(passLimpa);
        DocenteDAL.adicionarDocente(new Docente(sigla, email, passHash, nome, nif, morada, dataNasc), PASTA_BD);
        CredencialDAL.adicionarCredencial(email, passHash, "DOCENTE", PASTA_BD);
        return new String[]{email, sigla};
    }

    /**
     * Gera uma sigla de 3 caracteres única a partir do nome do docente.
     * Algoritmo: iniciais das primeiras 3 palavras do nome.
     * Em caso de colisão, substitui o último caractere por um dígito (1..99).
     */
    private String gerarSiglaUnica(String nome) {
        String[] partes = nome.trim().toUpperCase().replaceAll("[^A-Z ]", "").split("\\s+");
        StringBuilder base = new StringBuilder();

        for (int i = 0; i < Math.min(3, partes.length); i++) {
            if (!partes[i].isEmpty()) base.append(partes[i].charAt(0));
        }
        // preencher até 3 caracteres com letras do primeiro nome
        int idx = 1;
        while (base.length() < 3 && partes.length > 0 && idx < partes[0].length()) {
            base.append(partes[0].charAt(idx++));
        }
        while (base.length() < 3) base.append('X');

        String candidata = base.toString().substring(0, 3);
        if (!DocenteDAL.existeSigla(candidata, PASTA_BD)) return candidata;

        // colisão: substituir último char por dígito
        for (int n = 1; n <= 99; n++) {
            String tentativa = candidata.substring(0, 2) + n;
            if (!DocenteDAL.existeSigla(tentativa, PASTA_BD)) return tentativa;
        }
        return candidata; // fallback improvável
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
    public String registarEstudante(String nome, String nif, String morada,
                                    String dataNasc, String siglaCurso, int anoInscricao) {
        int numMec = EstudanteDAL.obterProximoNumeroMecanografico(PASTA_BD, anoInscricao);
        String email = EmailGenerator.gerarEmailEstudante(numMec);
        String passLimpa = PasswordGenerator.gerarPasswordSegura();
        EmailService.enviarCredenciaisTodos(nome, email, passLimpa);
        String passHash = SegurancaPasswords.gerarCredencialMista(passLimpa);
        Estudante novo = new Estudante(numMec, email, passHash, nome, nif, morada, dataNasc, anoInscricao);
        Curso curso = new CursoBLL().procurarCursoCompleto(siglaCurso);
        if (curso != null) novo.setSaldoDevedor(curso.getValorPropinaAnual());
        EstudanteDAL.adicionarEstudante(novo, siglaCurso, PASTA_BD);
        CredencialDAL.adicionarCredencial(email, passHash, "ESTUDANTE", PASTA_BD);
        // Inscrever automaticamente nas UCs do 1.º ano do curso
        for (String siglaUc : UcDAL.obterSiglasUcsPorCursoEAno(siglaCurso, 1, PASTA_BD)) {
            InscricaoDAL.adicionarInscricao(numMec, siglaUc, PASTA_BD);
        }
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
                               String nomeUc, String siglaDocente) {
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

    /**
     * Verifica se um curso pode ser alterado/removido.
     * Um curso NÃO pode ser alterado quando tem estudantes OU docentes (UCs) alocados.
     * Regra do enunciado v1.0, pág. 2.
     */
    public boolean isCursoAlteravel(String sigla) {
        int totalAlunos =
                EstudanteDAL.contarEstudantesPorCursoEAno(sigla, 1, PASTA_BD)
                        + EstudanteDAL.contarEstudantesPorCursoEAno(sigla, 2, PASTA_BD)
                        + EstudanteDAL.contarEstudantesPorCursoEAno(sigla, 3, PASTA_BD);
        int totalUcs =
                UcDAL.contarUcsPorCursoEAno(sigla, 1, PASTA_BD)
                        + UcDAL.contarUcsPorCursoEAno(sigla, 2, PASTA_BD)
                        + UcDAL.contarUcsPorCursoEAno(sigla, 3, PASTA_BD);
        return totalAlunos == 0 && totalUcs == 0;
    }

    /**
     * Edita o nome, departamento e propina de um curso sem alocações.
     * @return false se o curso tem estudantes ou docentes alocados.
     */
    public boolean editarCurso(String sigla, String novoNome, String siglaDep, double novaPropina) {
        if (!isCursoAlteravel(sigla)) return false;
        Curso original = new CursoBLL().procurarCursoCompleto(sigla);
        if (original == null) return false;
        Departamento dep = DepartamentoDAL.procurarDepartamento(siglaDep, PASTA_BD);
        Curso atualizado = new Curso(sigla, novoNome, dep, novaPropina);
        atualizado.setEstado(original.getEstado());
        CursoDAL.atualizarCurso(atualizado, PASTA_BD);
        return true;
    }

    /**
     * Remove um curso sem alocações.
     * @return false se o curso tem estudantes ou docentes alocados.
     */
    public boolean removerCurso(String sigla) {
        if (!isCursoAlteravel(sigla)) return false;
        return CursoDAL.removerCurso(sigla, PASTA_BD);
    }

    /**
     * Devolve a listagem formatada das UCs de um curso agrupadas por ano.
     * Mantém a camada Controller isolada da DAL (regra MVC).
     */
    public String listarUcsPorCurso(String siglaCurso) {
        return UcDAL.listarUcsPorCurso(siglaCurso, PASTA_BD);
    }

    /**
     * Helper privado: devolve as siglas das UCs em que o estudante já obteve aprovação.
     * Usado ao avançar de ano para remover essas inscrições do CSV.
     */
    private List<String> obterSiglasUcsAprovadas(Estudante e) {
        List<String> aprovadas = new ArrayList<>();
        for (int i = 0; i < e.getPercurso().getTotalAvaliacoes(); i++) {
            model.Avaliacao av = e.getPercurso().getHistoricoAvaliacoes()[i];
            if (av != null && av.isAprovado() && av.getUc() != null) {
                String sigla = av.getUc().getSigla();
                if (!aprovadas.contains(sigla)) aprovadas.add(sigla);
            }
        }
        return aprovadas;
    }

    /** Devolve array "SIGLA - Nome" de todos os cursos (para menus de seleção). */
    public String[] obterListaCursos() {
        return CursoDAL.obterListaCursos(PASTA_BD);
    }

    public String[] listarTodasUcs()    { return UcDAL.obterListaUcs(PASTA_BD); }
    public String[] listarTodosCursos() { return CursoDAL.obterListaCursos(PASTA_BD); }

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