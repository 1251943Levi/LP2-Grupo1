package bll;

import dal.*;
import model.*;
import utils.*;
import view.GestorView;

import java.util.ArrayList;
import java.util.List;


/**
 * Lógica de negócio do perfil Gestor.
 * Centraliza as operações de backoffice: avanço do ano letivo,
 * registo de utilizadores, gestão de cursos e UCs,
 * estatísticas e listagem de devedores.
 */
public class GestorBLL {

    private static final String PASTA_BD = "bd";


    /**
     * Avança o ano letivo para todos os estudantes do sistema.
     * Por cada estudante: bloqueia se houver dívida de propina,
     * bloqueia se o aproveitamento for inferior a 60%, remove inscrições
     * em UCs aprovadas, inscreve nas UCs do novo ano e atualiza a propina.
     * Ao concluir o 3.º ano, marca o estudante como graduado (anoCurricular = 4).
     * Verifica também o quórum mínimo de 5 alunos no 1.º ano de cada curso.
     * @param repo Repositório de sessão; o ano letivo é incrementado no final.
     * @param view Vista do gestor para feedback de cada passo.
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
            if (e.getAnoCurricular() > 3) continue;

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

                for (String siglaAprov : obterSiglasUcsAprovadas(e)) {
                    InscricaoDAL.removerInscricao(e.getNumeroMecanografico(), siglaAprov, PASTA_BD);
                }
                for (String siglaUc : UcDAL.obterSiglasUcsPorCursoEAno(e.getSiglaCurso(), novoAno, PASTA_BD)) {
                    InscricaoDAL.adicionarInscricao(e.getNumeroMecanografico(), siglaUc, PASTA_BD);
                }
                Curso cursoDoEstudante = new CursoBLL().procurarCursoCompleto(e.getSiglaCurso());
                if (cursoDoEstudante != null) {
                    e.setSaldoDevedor(cursoDoEstudante.getValorPropinaAnual());
                }
                view.mostrarTransicaoSucedida(e.getNumeroMecanografico(), novoAno);
            } else {
                e.setAnoCurricular(4);
                view.mostrarConclusaoCurso(e.getNumeroMecanografico());
            }
            EstudanteDAL.atualizarEstudante(e, PASTA_BD);
        }

        repo.setAnoAtual(repo.getAnoAtual() + 1);
        view.mostrarSucessoAvancoAno(repo.getAnoAtual());
    }

    /**
     * Regista um novo docente no sistema.
     * A sigla é gerada automaticamente a partir das iniciais do nome,
     * garantindo unicidade. O email e a password são gerados e enviados
     * ao docente sem visualização na consola.
     * @param nome     Nome completo do docente.
     * @param nif      NIF com 9 dígitos.
     * @param morada   Morada de residência.
     * @param dataNasc Data de nascimento (DD-MM-AAAA).
     * @return Array [email, sigla] com as credenciais atribuídas.
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
     * Regista um novo estudante no sistema.
     * Gera número mecanográfico, email e password; cria a propina inicial;
     * inscreve nas UCs do 1.º ano; envia as credenciais por email.
     * @param nome         Nome completo.
     * @param nif          NIF com 9 dígitos.
     * @param morada       Morada de residência.
     * @param dataNasc     Data de nascimento (DD-MM-AAAA).
     * @param siglaCurso   Sigla do curso.
     * @param anoInscricao Ano letivo da matrícula.
     * @return Email institucional gerado.
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

        for (String siglaUc : UcDAL.obterSiglasUcsPorCursoEAno(siglaCurso, 1, PASTA_BD)) {
            InscricaoDAL.adicionarInscricao(numMec, siglaUc, PASTA_BD);
        }
        return email;
    }

    /**
     * Regista um novo departamento no sistema.
     * @param sigla Sigla do departamento.
     * @param nome  Nome completo.
     */
    public void registarDepartamento(String sigla, String nome) {
        Departamento dep = new Departamento(sigla.toUpperCase(), nome);
        DepartamentoDAL.adicionarDepartamento(dep, PASTA_BD);
    }

    // ─────────────────────────── CURSOS E UCs ──────────────────────────

    /**
     * Adiciona uma nova UC a um curso, respeitando o limite de 5 UCs por ano.
     * @param siglaCurso   Sigla do curso.
     * @param anoUc        Ano curricular da UC (1, 2 ou 3).
     * @param siglaUc      Sigla da nova UC.
     * @param nomeUc       Nome completo.
     * @param siglaDocente Sigla do docente responsável.
     * @return true se a UC foi adicionada; false se o limite foi atingido.
     */
    public boolean adicionarUc(String siglaCurso, int anoUc, String siglaUc,
                               String nomeUc, String siglaDocente) {
        if (UcDAL.contarUcsPorCursoEAno(siglaCurso, anoUc, PASTA_BD) >= 5) return false;
        Docente doc = DocenteDAL.procurarPorSigla(siglaDocente, PASTA_BD);
        UcDAL.adicionarUC(new UnidadeCurricular(siglaUc, nomeUc, anoUc, doc), siglaCurso, PASTA_BD);
        return true;
    }

    /**
     * Edita uma UC existente substituindo o registo antigo pelo novo.
     * @return true se a edição foi bem-sucedida.
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
     * Remove uma UC pela sua sigla.
     * @return true se a UC existia e foi removida.
     */
    public boolean removerUc(String siglaUc) {
        return UcDAL.removerUC(siglaUc, PASTA_BD);
    }


    /**
     * Cria um novo curso no estado "Inativo".
     * @param sigla    Sigla identificadora.
     * @param nome     Nome completo.
     * @param siglaDep Sigla do departamento.
     * @param propina  Valor da propina anual em euros.
     */
    public void adicionarCurso(String sigla, String nome, String siglaDep, double propina) {
        Departamento dep = DepartamentoDAL.procurarDepartamento(siglaDep, PASTA_BD);
        Curso c = new Curso(sigla, nome, dep, propina);
        c.setEstado("Inativo");
        CursoDAL.adicionarCurso(c, PASTA_BD);
    }

    /**
     * Verifica se um curso pode ser editado ou removido.
     * Um curso não pode ser alterado enquanto tiver estudantes ou UCs alocadas.
     * @param sigla Sigla do curso a verificar.
     * @return true se o curso não tiver alocações.
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
     * @return false se o curso tiver alocações ou não existir.
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
     * @return false se o curso tiver alocações.
     */
    public boolean removerCurso(String sigla) {
        if (!isCursoAlteravel(sigla)) return false;
        return CursoDAL.removerCurso(sigla, PASTA_BD);
    }


    /**
     * Devolve o plano de estudos de um curso agrupado por ano curricular.
     * @param siglaCurso Sigla do curso.
     * @return String formatada com as UCs por ano.
     */
    public String listarUcsPorCurso(String siglaCurso) {
        return UcDAL.listarUcsPorCurso(siglaCurso, PASTA_BD);
    }


    // ─────────────────────────── ESTATÍSTICAS ──────────────────────────

    /**
     * Calcula a soma total de notas e o número de momentos lançados.
     * @return Array [soma, total] para calcular a média global.
     */
    public double[] calcularEstatisticasGlobais() {
        return Estatisticas.calcularDadosMediaGlobal();
    }

    /**
     * Devolve o estudante com melhor média global.
     * @return Array [Estudante, Double] com o melhor aluno e a sua média.
     */
    public Object[] obterMelhorAluno() {
        return Estatisticas.calcularMelhorAluno();
    }

    // ─────────────────────────── LISTAGENS ─────────────────────────────


    /** @return Array "SIGLA - Nome" de todos os cursos. */
    public String[] obterListaCursos() {
        return CursoDAL.obterListaCursos(PASTA_BD);
    }

    /** @return Array "SIGLA - Nome" de todas as UCs. */
    public String[] listarTodasUcs()    { return UcDAL.obterListaUcs(PASTA_BD); }

    /** @return Array "SIGLA - Nome" de todos os cursos. */
    public String[] listarTodosCursos() { return CursoDAL.obterListaCursos(PASTA_BD); }

    /**
     * Devolve os estudantes com saldo devedor positivo.
     * @return Lista de estudantes com dívida de propina.
     */
    public List<Estudante> obterListaDevedores() {
        List<Estudante> devedores = new ArrayList<>();
        for (Estudante e : EstudanteDAL.carregarTodos(PASTA_BD))
            if (e != null && e.getSaldoDevedor() > 0) devedores.add(e);
        return devedores;
    }


    // ─────────────────────── SEGURANÇA E VALIDAÇÃO ─────────────────────


    /**
     * Altera a password do gestor com hashing e persistência.
     * @param gestor   Gestor autenticado.
     * @param novaPass Nova password em texto limpo.
     */
    public void alterarPasswordGestor(Gestor gestor, String novaPass) {
        String hash = SegurancaPasswords.gerarCredencialMista(novaPass);
        gestor.setPassword(hash);
        CredencialDAL.atualizarPassword(gestor.getEmail(), hash, PASTA_BD);
    }

    /**
     * Verifica se um NIF já está registado em estudantes ou docentes.
     * @param nif NIF a verificar.
     * @return true se o NIF já existir.
     */
    public boolean isNifDuplicado(String nif) {
        return EstudanteDAL.existeNif(nif, PASTA_BD) || DocenteDAL.existeNif(nif, PASTA_BD);
    }

    /**
     * Verifica se já existe um departamento com a sigla fornecida.
     * @param sigla Sigla a verificar.
     * @return true se o departamento já existir.
     */
    public boolean isDepartamentoDuplicado(String sigla) {
        return DepartamentoDAL.procurarDepartamento(sigla, PASTA_BD) != null;
    }

    /**
     * Gera uma sigla de exatamente 3 letras única para um docente.
     * * Passo 1: Extrai as iniciais das primeiras 3 palavras do nome (ex: Ana Sofia Gomes -> ASG).
     * Passo 2: Em caso de colisão, mantém as duas primeiras letras e itera a 3ª letra
     * usando as restantes letras do nome do docente (ex: ASG -> ASO -> ASF...).
     * Passo 3: Se as letras do nome se esgotarem, tenta usar qualquer letra do alfabeto (A-Z).
     * Passo 4: Em último recurso extremo, combina o alfabeto na 2ª e 3ª posições.
     *
     * @param nome O nome completo inserido pelo utilizador.
     * @return Uma sigla única de 3 letras maiúsculas (ex: "ASG" ou "ASO").
     */
    private String gerarSiglaUnica(String nome) {
        String nomeLimpo = nome.trim().toUpperCase().replaceAll("[^A-Z ]", "");
        String[] palavras = nomeLimpo.split("\\s+");
        StringBuilder base = new StringBuilder();

        for (int i = 0; i < Math.min(3, palavras.length); i++) {
            if (!palavras[i].isEmpty()) {
                base.append(palavras[i].charAt(0));
            }
        }

        int idx = 1;
        while (base.length() < 3 && palavras.length > 0 && idx < palavras[0].length()) {
            base.append(palavras[0].charAt(idx++));
        }

        while (base.length() < 3) {
            base.append('X');
        }

        String candidata = base.toString().substring(0, 3);

        if (!DocenteDAL.existeSigla(candidata, PASTA_BD)) {
            return candidata;
        }

        String prefixo = candidata.substring(0, 2);
        String todasLetrasDoNome = nomeLimpo.replace(" ", "");

        for (int i = 0; i < todasLetrasDoNome.length(); i++) {
            String tentativa = prefixo + todasLetrasDoNome.charAt(i);
            if (!tentativa.equals(candidata) && !DocenteDAL.existeSigla(tentativa, PASTA_BD)) {
                return tentativa;
            }
        }

        for (char c = 'A'; c <= 'Z'; c++) {
            String tentativa = prefixo + c;
            if (!DocenteDAL.existeSigla(tentativa, PASTA_BD)) {
                return tentativa;
            }
        }

        String primeiraLetra = candidata.substring(0, 1);
        for (char c2 = 'A'; c2 <= 'Z'; c2++) {
            for (char c3 = 'A'; c3 <= 'Z'; c3++) {
                String tentativa = primeiraLetra + c2 + c3;
                if (!DocenteDAL.existeSigla(tentativa, PASTA_BD)) {
                    return tentativa;
                }
            }
        }

        return candidata;
    }


    /**
     * Devolve as siglas das UCs em que o estudante já obteve aprovação.
     * Usado na transição de ano para remover essas inscrições do ficheiro.
     * @param e Estudante com percurso académico carregado.
     * @return Lista de siglas de UCs aprovadas.
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

}