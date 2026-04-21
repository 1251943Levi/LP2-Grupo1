package bll;

import model.*;
import utils.*;
import view.GestorView;

/**
 * Camada de Lógica de Negócio (Business Logic Layer) para o perfil Gestor.
 * Esta classe centraliza as regras de decisão do sistema, cálculos estatísticos
 * e a orquestração entre os modelos e a persistência em ficheiros CSV.
 */
public class GestorBLL {

    private static final String PASTA_BD = "bd";

    // --- 1. GESTÃO DE CICLO DE VIDA (ANO LETIVO) ---

    /**
     * Executa a transição global do sistema para um novo ano letivo.
     * <p>
     * O processo envolve:
     * 1. Validar o quórum de cursos (mínimo 5 alunos no 1º ano).
     * 2. Verificar dívidas de estudantes (bloqueia transição se houver saldo devedor).
     * 3. Promover estudantes de ano ou registar a conclusão do curso.
     * </p>
     * * @param repo O repositório de dados para atualizar o ano atual do sistema.
     * @param view A vista para emitir relatórios do processamento em tempo real.
     */
    public void avancarAnoLetivo(RepositorioDados repo, GestorView view) {
        view.mostrarCabecalhoArranqueAnoLetivo();

        // Verificação de Quórum
        String[] cursos = ImportadorCSV.obterListaCursos(PASTA_BD);
        if (cursos.length == 0) {
            view.mostrarErroCarregarDados("Cursos");
            return;
        }

        view.mostrarVerificacaoQuorum();
        for (String c : cursos) {
            String siglaCurso = c.split(" - ")[0];
            Curso curso = ImportadorCSV.procurarCurso(siglaCurso, PASTA_BD);
            if (curso == null) continue;

            int alunos1oAno = ExportadorCSV.contarEstudantesPorCursoEAno(siglaCurso, 1, PASTA_BD);
            int alunos2oAno = ExportadorCSV.contarEstudantesPorCursoEAno(siglaCurso, 2, PASTA_BD);
            int alunos3oAno = ExportadorCSV.contarEstudantesPorCursoEAno(siglaCurso, 3, PASTA_BD);

            if (alunos1oAno < 5 && alunos1oAno > 0) {
                view.mostrarErroQuorum(siglaCurso, alunos1oAno);
                curso.setEstado("Inativo");
            } else if (alunos1oAno >= 5 || alunos2oAno >= 1 || alunos3oAno >= 1) {
                view.mostrarSucessoQuorum(siglaCurso);
                curso.setEstado("Ativo");
            } else {
                curso.setEstado("Inativo");
            }
            ExportadorCSV.atualizarCurso(curso, PASTA_BD);
        }

        // Transição de Estudantes
        view.mostrarProcessamentoTransicoes();
        Estudante[] estudantes = ImportadorCSV.carregarTodosEstudantes(PASTA_BD);

        if (estudantes != null) {
            for (Estudante e : estudantes) {
                if (e == null) continue;

                if (e.getSaldoDevedor() != 0.0) {
                    view.mostrarBloqueioDivida(e.getNumeroMecanografico(), e.getNome(), e.getAnoCurricular(), e.getSaldoDevedor());
                } else {
                    if (e.getAnoCurricular() < 3) {
                        e.setAnoCurricular(e.getAnoCurricular() + 1);
                        view.mostrarTransicaoSucedida(e.getNumeroMecanografico(), e.getAnoCurricular());
                    } else {
                        view.mostrarConclusaoCurso(e.getNumeroMecanografico());
                    }
                    ExportadorCSV.atualizarEstudante(e, PASTA_BD);
                }
            }
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
    public String registarDocente(String nome, String sigla, String nif, String morada, String dataNasc) {
        String email = EmailGenerator.gerarEmailDocente(nome);
        String passLimpa = PasswordGenerator.gerarPasswordSegura();

        EmailService.enviarCredenciaisTodos(nome, email, passLimpa);

        String passSegura = SegurancaPasswords.gerarCredencialMista(passLimpa);
        Docente novoDocente = new Docente(sigla, email, passSegura, nome, nif, morada, dataNasc);

        ExportadorCSV.adicionarDocente(novoDocente, PASTA_BD);
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
    public String registarEstudante(int numMec, String nome, String nif, String morada, String dataNasc, String siglaCurso, int anoInscricao) {
        String email = EmailGenerator.gerarEmailEstudante(numMec);
        String passLimpa = PasswordGenerator.gerarPasswordSegura();

        EmailService.enviarCredenciaisTodos(nome, email, passLimpa);

        String passSegura = SegurancaPasswords.gerarCredencialMista(passLimpa);
        Estudante novo = new Estudante(numMec, email, passSegura, nome, nif, morada, dataNasc, anoInscricao);

        Curso cursoEscolhido = ImportadorCSV.procurarCurso(siglaCurso, PASTA_BD);
        if (cursoEscolhido != null) {
            novo.setSaldoDevedor(cursoEscolhido.getValorPropinaAnual());
        }

        ExportadorCSV.adicionarEstudante(novo, PASTA_BD, siglaCurso);
        return email;
    }

    // --- 3. ESTATÍSTICAS ---

    /**
     * Calcula a soma de todas as notas e o número total de avaliações no sistema.
     * * @return Array de double onde [0] é a soma das notas e [1] o total de notas.
     */
    public double[] calcularEstatisticasGlobais() {
        Estudante[] estudantes = ImportadorCSV.carregarTodosEstudantes(PASTA_BD);
        if (estudantes == null) return null;

        double soma = 0;
        int totalNotas = 0;

        for (Estudante e : estudantes) {
            if (e == null || e.getPercurso() == null) continue;
            for (int i = 0; i < e.getPercurso().getTotalAvaliacoes(); i++) {
                Avaliacao av = e.getPercurso().getHistoricoAvaliacoes()[i];
                if (av != null && av.getResultados() != null) {
                    for (int j = 0; j < av.getTotalAvaliacoesLancadas(); j++) {
                        soma += av.getResultados()[j];
                        totalNotas++;
                    }
                }
            }
        }
        return new double[]{soma, totalNotas};
    }

    /**
     * Determina qual o estudante com a média global mais alta.
     * * @return Array de Object onde [0] é o objeto Estudante e [1] é a sua média (Double).
     */
    public Object[] obterMelhorAluno() {
        Estudante[] estudantes = ImportadorCSV.carregarTodosEstudantes(PASTA_BD);
        if (estudantes == null) return null;

        Estudante melhor = null;
        double maiorMedia = -1;

        for (Estudante e : estudantes) {
            if (e == null || e.getPercurso() == null || e.getPercurso().getTotalAvaliacoes() == 0) continue;
            double somaMedias = 0;
            int totalAvaliacoes = e.getPercurso().getTotalAvaliacoes();

            for (int i = 0; i < totalAvaliacoes; i++) {
                Avaliacao av = e.getPercurso().getHistoricoAvaliacoes()[i];
                if (av != null) somaMedias += av.calcularMedia();
            }

            double mediaAluno = somaMedias / totalAvaliacoes;
            if (mediaAluno > maiorMedia) {
                maiorMedia = mediaAluno;
                melhor = e;
            }
        }
        return melhor != null ? new Object[]{melhor, maiorMedia} : null;
    }

    // --- 4. GESTÃO DE ENTIDADES (UCs E CURSOS) ---

    /**
     * Adiciona uma nova Unidade Curricular verificando o limite de UCs por curso/ano.
     * * @return true se a UC foi adicionada com sucesso.
     */
    public boolean adicionarUc(String siglaCurso, int anoUc, String siglaUc, String nomeUc, String docente, RepositorioDados repo) {
        if (repo.podeAdicionarUc(siglaCurso, anoUc, PASTA_BD)) {
            String linhaUc = siglaUc + ";" + nomeUc + ";" + anoUc + ";" + docente + ";" + siglaCurso;
            ExportadorCSV.adicionarLinhaCSV("ucs.csv", linhaUc, PASTA_BD);
            return true;
        }
        return false;
    }

    /**
     * Edita uma UC removendo o registo antigo e inserindo um novo.
     */
    public boolean editarUc(String siglaAntiga, String novaSigla, String nome, String ano, String docente, String curso) {
        if (ExportadorCSV.removerLinhaCSV("ucs.csv", siglaAntiga, PASTA_BD)) {
            String novaLinha = novaSigla + ";" + nome + ";" + ano + ";" + docente + ";" + curso;
            ExportadorCSV.adicionarLinhaCSV("ucs.csv", novaLinha, PASTA_BD);
            return true;
        }
        return false;
    }

    /**
     * Cria um novo curso no sistema com estado inicial "Inativo".
     */
    public void adicionarCurso(String sigla, String nome, String dep, double propina) {
        String linha = sigla + ";" + nome + ";" + dep + ";" + propina + ";Inativo";
        ExportadorCSV.adicionarLinhaCSV("cursos.csv", linha, PASTA_BD);
    }

    /**
     * Remove fisicamente uma UC do ficheiro CSV.
     */
    public boolean removerUc(String siglaUc) {
        return ExportadorCSV.removerLinhaCSV("ucs.csv", siglaUc, PASTA_BD);
    }

    // --- 5. SEGURANÇA ---

    /**
     * Altera a password de um gestor, aplicando hashing e atualizando o ficheiro de credenciais.
     */
    public void alterarPasswordGestor(Gestor gestor, String novaPass) {
        String passSegura = SegurancaPasswords.gerarCredencialMista(novaPass);
        gestor.setPassword(passSegura);
        ExportadorCSV.atualizarPasswordCentralizada(gestor.getEmail(), passSegura, PASTA_BD);
    }
}