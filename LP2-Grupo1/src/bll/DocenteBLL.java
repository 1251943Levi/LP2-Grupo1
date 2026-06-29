package bll;

import common.ConfigApp;
import dal.UcDALFile;
import dal.UcDALSql;

import dal.AvaliacaoDAL;
import dal.AvaliacaoDALFile;
import dal.AvaliacaoDALSql;
import dal.DocenteDAL;
import dal.DocenteDALFile;
import dal.DocenteDALSql;
import dal.EstudanteDAL;
import dal.EstudanteDALFile;
import dal.EstudanteDALSql;
import dal.UcDAL;
import dal.InscricaoDAL;
import dal.InscricaoDALFile;
import dal.InscricaoDALSql;
import model.*;
import controller.LoginController;
import utils.Config;
import java.util.ArrayList;
import java.util.List;


/**
 * Lógica de negócio para o perfil Docente.
 * Gere o lançamento de avaliações com todas as validações necessárias,
 * a obtenção dos alunos associados e a alteração segura de credenciais.
 */
public class DocenteBLL {

    private static final String PASTA_BD = ConfigApp.PASTA_BD;
    private final UcDAL ucDAL = ConfigApp.isModoSql() ? new UcDALSql() : new UcDALFile();
    private final LoginController loginController = new LoginController();
    // A7: acesso ao módulo do estudante (lazy — evita efeitos colaterais no arranque)
    private EstudanteBLL moduloEstudante;
    private EstudanteBLL moduloEstudante() {
        if (moduloEstudante == null) moduloEstudante = new EstudanteBLL();
        return moduloEstudante;
    }
    private final DocenteDAL docenteDAL =
            ConfigApp.isModoSql() ? new DocenteDALSql() : new DocenteDALFile();
    private final InscricaoDAL inscricaoDAL =
            ConfigApp.isModoSql() ? new InscricaoDALSql() : new InscricaoDALFile();
    private final AvaliacaoDAL avaliacaoDAL =
            ConfigApp.isModoSql() ? new AvaliacaoDALSql() : new AvaliacaoDALFile();
    private final dal.MomentoDAL momentoDAL =
            ConfigApp.isModoSql() ? new dal.MomentoDALSql() : new dal.MomentoDALFile();
    private final dal.NotaDAL notaDAL =
            ConfigApp.isModoSql() ? new dal.NotaDALSql() : new dal.NotaDALFile();
    private final AvaliacaoBLL avaliacaoBll = new AvaliacaoBLL();

    public DocenteBLL() {
        inscricaoDAL.inicializar();
        avaliacaoDAL.inicializar();
    }

    /**
     * Verifica se uma UC pertence ao plano de lecionação do docente.
     */
    public boolean lecionaEstaUC(Docente docente, String siglaUc) {
        if (siglaUc == null) return false;
        for (int i = 0; i < docente.getTotalUcsLecionadas(); i++) {
            if (docente.getUcsLecionadas()[i].getSigla().equalsIgnoreCase(siglaUc)) return true;
        }
        return false;
    }

    /**
     * Lança uma nota de forma faseada.
     * Se já existir avaliação, anexa a nova nota (até ao limite de 3).
     * Se não existir, cria a primeira.
     *
     * FIX (Bug DocenteBLL):
     *   - Linha ~27: "carregarAvaliacoesPorAluno" → "obterAvaliacoesPorAluno"  (nome errado na DAL)
     *   - Linha ~275: "adicionarAvaliacao(avaliacao)" → "adicionarAvaliacao(avaliacao, numMec, PASTA_BD)" (args em falta)
     */
    public String lancarNota(int numMec, String siglaUc, int ano, double notaMomento, Docente d) {
        Estudante aluno = moduloEstudante().procurarPorNumMec(numMec);

        AnoLetivoBLL anoBll = new AnoLetivoBLL();
        if (anoBll.getEstadoAnoAtual() == EstadoAnoLetivo.PLANEAMENTO) {
            return "ERRO: Não é possível lançar notas enquanto o ano letivo está em PLANEAMENTO.";
        }

        if (aluno == null)
            return "ERRO: Aluno com nº " + numMec + " não encontrado.";

        if (!lecionaEstaUC(d, siglaUc))
            return "ERRO: A UC '" + siglaUc + "' não pertence às suas unidades curriculares.";

        // Card 4: só se aceita nota a estudantes inscritos na UC nesse ano letivo.
        if (!inscricaoDAL.obterAlunosPorUc(siglaUc, ano).contains(numMec))
            return "ERRO: O aluno " + numMec + " não está inscrito na UC '" + siglaUc
                    + "' no ano letivo " + ano + ".";

        UnidadeCurricular uc = new UcBLL().procurarUCCompleta(siglaUc);
        if (uc == null)
            return "ERRO: A UC '" + siglaUc + "' não foi encontrada no sistema.";

        int numMomentos = uc.getNumMomentos(); // 1 por omissão; >1 se configurado

        Avaliacao avaliacaoExistente = avaliacaoDAL.obterAvaliacao(numMec, siglaUc, ano);

        if (avaliacaoExistente != null) {
            if (avaliacaoExistente.getTotalAvaliacoesLancadas() >= numMomentos) {
                return "ERRO: O aluno já tem as " + numMomentos
                        + " nota(s) máxima(s) lançadas para esta UC.";
            }
            avaliacaoExistente.adicionarResultado(notaMomento);
            avaliacaoDAL.atualizarAvaliacao(avaliacaoExistente, numMec);
            return null;

        } else {
            Avaliacao novaAvaliacao = new Avaliacao(uc, ano);
            novaAvaliacao.adicionarResultado(notaMomento);
            avaliacaoDAL.adicionarAvaliacao(novaAvaliacao, numMec);
            return null;
        }
    }

    /**
     * Calcula a nota final de uma UC como média simples dos momentos lançados.
     * @param notas Lista com as notas de cada momento.
     * @return Média aritmética simples, ou 0.0 se a lista estiver vazia.
     */
    public double calcularNotaFinal(List<Double> notas) {
        if (notas == null || notas.isEmpty()) return 0.0;
        double soma = 0;
        for (double nota : notas) soma += nota;
        return soma / notas.size();
    }

    /**
     * Devolve o número de momentos de avaliação configurados para uma UC.
     * @param siglaUc Sigla da UC.
     * @return Número de momentos (mínimo 1).
     */
    public int obterNumMomentosDaUC(String siglaUc) {
        UnidadeCurricular uc = new UcBLL().procurarUCCompleta(siglaUc);
        return (uc != null) ? uc.getNumMomentos() : 1;
    }

    /**
     * Altera a password do docente com hashing e persistência.
     */
    public void alterarPassword(Docente docente, String novaPass) {
        loginController.atualizarPassword(docente.getEmail(), novaPass);
    }

    /**
     * Devolve os alunos associados ao docente (das suas UCs) com a média e a lista de UCs.
     * Cada elemento da lista é um Object[] de 3 posições: [Estudante, Double média, String ucs].
     *
     * FIX (compile): método estava em falta — causava "cannot find symbol method
     * obterAlunosDoDocenteComMedia(model.Docente)" no DocenteController (linhas 67 e 163).
     *
     * FIX (runtime): a versão original devolvia apenas 2 elementos [Estudante, média],
     * mas o DocenteController acede a par[2] (string de UCs) em listarMeusAlunos() e
     * listarAlunosPorUC() — o que provocaria ArrayIndexOutOfBoundsException.
     * Agora devolve sempre 3 posições.
     *
     * @param docente Docente autenticado.
     * @return Lista de [Estudante, média (Double), UCs inscritas (String)].
     */
    public List<Object[]> obterAlunosDoDocenteComMedia(Docente docente) {
        List<Object[]> resultado = new ArrayList<>();
        List<Integer> alunosAdicionados = new ArrayList<>(); // evita duplicados
        int anoAtual = Config.getAnoAtual();

        for (int i = 0; i < docente.getTotalUcsLecionadas(); i++) {
            UnidadeCurricular uc = docente.getUcsLecionadas()[i];
            if (uc == null) continue;

            List<Integer> numsMec = inscricaoDAL.obterAlunosPorUc(uc.getSigla(), anoAtual);
            for (int numMec : numsMec) {
                if (contemAluno(alunosAdicionados, numMec)) continue;

                Estudante aluno = moduloEstudante().procurarPorNumMec(numMec);                if (aluno == null) continue;

                carregarAvaliacoesSeNecessario(aluno);
                double media = calcularMediaAlunoNaUc(aluno, uc.getSigla());

                // par[2]: string das UCs em que o aluno está inscrito (usada em .contains())
                List<String> siglasInscritas =
                        inscricaoDAL.obterSiglasUcsPorAluno(numMec, anoAtual);
                String ucs = String.join(", ", siglasInscritas);

                resultado.add(new Object[]{aluno, media, ucs});
                alunosAdicionados.add(numMec);
            }
        }
        return resultado;
    }

    /** Verifica se um número mecanográfico já está na lista (controlo de duplicados). */
    private boolean contemAluno(List<Integer> lista, int numMec) {
        for (int m : lista) {
            if (m == numMec) return true;
        }
        return false;
    }

    /**
     * Devolve a lista de alunos inscritos numa UC no ano corrente.
     */
    public List<String> obterAlunosInscritosNaUc(String siglaUc) {
        List<Integer> nums = inscricaoDAL.obterAlunosPorUc(siglaUc, Config.getAnoAtual());
        List<String> alunosFormatados = new ArrayList<>();
        for (int num : nums) {
            Estudante e = moduloEstudante().procurarPorNumMec(num);            String nome = (e != null) ? e.getNome() : "Desconhecido";
            alunosFormatados.add(num + " - " + nome);
        }
        return alunosFormatados;
    }

    /**
     * Lista todos os docentes (dados básicos, sem UCs carregadas).
     */
    public List<Docente> listarTodos() {
        return docenteDAL.carregarTodos();
    }

    /**
     * Obtém um docente pela sua sigla (com dados básicos).
     */
    public Docente obterPorSigla(String sigla) {
        return docenteDAL.procurarPorSigla(sigla);
    }

    /**
     * Actualiza os dados de um docente (nome, morada, dataNascimento, NIF).
     */
    public boolean atualizarDocente(Docente docente) {
        if (docente == null) return false;
        return docenteDAL.atualizarDocente(docente);
    }

    /**
     * Verifica se um docente tem UCs atribuídas.
     */
    public boolean temUcAtribuida(String sigla) {
        return docenteDAL.temUcAtribuida(sigla);
    }

    /**
     * Remove um docente (apenas se não tiver UCs atribuídas).
     */
    public boolean removerDocente(String sigla) {
        if (temUcAtribuida(sigla)) return false;
        Docente d = docenteDAL.procurarPorSigla(sigla);
        boolean removido = docenteDAL.removerDocente(sigla);
        // A6: a credencial é removida pelo módulo de login (não pela DAL do docente).
        if (removido && d != null) loginController.eliminar(d.getEmail());
        return removido;
    }

    /**
     * Lança notas para todos os alunos inscritos numa UC, pedindo uma nota para cada um.
     * @return String com o relatório detalhado das operações.
     */
    public String lancarNotasEmLote(String siglaUc, int anoLetivo, Docente docente,
                                    java.util.function.Function<Integer, Double> obterNota) {

        AnoLetivoBLL anoBll = new AnoLetivoBLL();
        if (anoBll.getEstadoAnoAtual() == EstadoAnoLetivo.PLANEAMENTO) {
            return "ERRO: Não é possível lançar notas enquanto o ano letivo está em PLANEAMENTO.";
        }

        if (!lecionaEstaUC(docente, siglaUc)) {
            return "ERRO: Não lecciona a UC " + siglaUc;
        }
        UnidadeCurricular uc = new UcBLL().procurarUCCompleta(siglaUc);
        if (uc == null) return "ERRO: UC não encontrada.";

        List<Integer> alunosInscritos = inscricaoDAL.obterAlunosPorUc(siglaUc, anoLetivo);
        StringBuilder relatorio = new StringBuilder();
        int sucessos = 0, erros = 0;

        for (int numMec : alunosInscritos) {
            Estudante aluno = moduloEstudante().procurarPorNumMec(numMec);
            String nome = (aluno != null) ? aluno.getNome() : "Desconhecido";

            Double nota = obterNota.apply(numMec);
            if (nota == null) {
                relatorio.append(String.format(" %d - %s → Saltado pelo docente\n", numMec, nome));
                continue;
            }

            String resultado = lancarNota(numMec, siglaUc, anoLetivo, nota, docente);
            if (resultado == null) {
                sucessos++;
                relatorio.append(String.format("  %d - %s → Nota %.1f registada\n", numMec, nome, nota));
            } else {
                erros++;
                relatorio.append(String.format("  %d - %s → %s\n", numMec, nome, resultado));
            }
        }
        relatorio.insert(0, String.format("Resumo: %d sucessos, %d falhas, %d saltos.\n",
                sucessos, erros, alunosInscritos.size() - sucessos - erros));
        return relatorio.toString();
    }

    /**
     * Retorna uma lista de strings "numMec - nome" para os alunos inscritos numa UC.
     */
    public List<String> obterAlunosFormatados(String siglaUc, int anoLetivo) {
        List<Integer> nums = inscricaoDAL.obterAlunosPorUc(siglaUc, anoLetivo);
        List<String> formatados = new ArrayList<>();
        for (int num : nums) {
            Estudante e = moduloEstudante().procurarPorNumMec(num);
            String nome = (e != null) ? e.getNome() : "Desconhecido";
            formatados.add(num + " - " + nome);
        }
        return formatados;
    }

    /**
     * Card 4: lista os estudantes inscritos numa UC num ano letivo (numMec + nome).
     * Assinatura canónica para o lançamento de notas por momento de avaliação.
     */
    public List<String> listarEstudantesPorUC(String siglaUC, int anoLetivo) {
        return obterAlunosFormatados(siglaUC, anoLetivo);
    }

    // ── Métodos privados ──────────────────────────────────────────────

    /**
     * Calcula a média de um aluno numa UC específica.
     * FIX: usava "carregarAvaliacoesPorAluno" → correto é "obterAvaliacoesPorAluno".
     */
    private double calcularMediaAlunoNaUc(Estudante aluno, String siglaUc) {
        // Unificação: nota final (ponderada se a UC tiver momentos; senão, média do sistema antigo).
        return avaliacaoBll.notaFinal(aluno.getNumeroMecanografico(), siglaUc, Config.getAnoAtual());
    }

    /**
     * Carrega as avaliações de um estudante se ainda não estiverem carregadas.
     * FIX: era "AvaliacaoDAL.carregarAvaliacoesPorAluno" → correto: "obterAvaliacoesPorAluno".
     */
    private void carregarAvaliacoesSeNecessario(Estudante aluno) {
        if (aluno.getPercurso().getTotalAvaliacoes() == 0) {
            // FIX: nome correto do método na DAL é obterAvaliacoesPorAluno
            List<Avaliacao> avaliacoes = avaliacaoDAL.obterAvaliacoesPorAluno(
                    aluno.getNumeroMecanografico());
            for (Avaliacao av : avaliacoes) {
                aluno.getPercurso().registarAvaliacao(av);
            }
        }
    }

    /**
     * Define o número de momentos de avaliação para uma unidade curricular.
     * Apenas permitido quando o ano letivo ativo está em PLANEAMENTO.
     */
    public String definirMomentosAvaliacao(Docente docente, String siglaUc, int numMomentos) {
        AnoLetivoBLL anoBll = new AnoLetivoBLL();
        if (anoBll.getEstadoAnoAtual() != EstadoAnoLetivo.PLANEAMENTO) {
            return "Apenas é permitido definir momentos de avaliação quando o ano letivo está em PLANEAMENTO.";
        }
        if (!lecionaEstaUC(docente, siglaUc)) {
            return "Não leciona a UC " + siglaUc;
        }
        if (numMomentos < 1 || numMomentos > 3) {
            return "Número de momentos inválido. Deve ser 1, 2 ou 3.";
        }
        ucDAL.atualizarMomentos(siglaUc, numMomentos, PASTA_BD);
        return null;
    }

    /**
     * Card 3: define um momento de avaliação tipificado (com tipo, peso e data).
     * Valida: tipo obrigatório; peso entre 0 e 100; soma dos pesos da UC ≤ 100%.
     * @return null em caso de sucesso; mensagem de erro caso contrário.
     */
    public String definirMomento(String siglaUC, String nome, model.TipoMomento tipo,
                                 double peso, String dataRealizacao) {
        if (siglaUC == null || siglaUC.trim().isEmpty()) return "ERRO: UC inválida.";
        if (nome == null || nome.trim().isEmpty()) return "ERRO: o nome do momento é obrigatório.";
        if (tipo == null) return "ERRO: o tipo de momento é obrigatório.";
        if (peso <= 0 || peso > 100) return "ERRO: o peso deve estar entre 0 e 100.";
        double soma = momentoDAL.somaPesos(siglaUC);
        if (soma + peso > 100.0001) {
            return String.format("ERRO: a soma dos pesos excede 100%% (atual: %.0f%%, a adicionar: %.0f%%).", soma, peso);
        }
        momentoDAL.adicionar(new model.Momento(siglaUC, nome.trim(), tipo, peso, dataRealizacao));
        return null;
    }

    /** Card 3: tipos de momento disponíveis (para listagem ao criar um momento). */
    public model.TipoMomento[] tiposMomentoDisponiveis() {
        return model.TipoMomento.values();
    }

    /** Card 3: momentos de avaliação definidos para uma UC. */
    public java.util.List<model.Momento> listarMomentos(String siglaUC) {
        return momentoDAL.listarPorUc(siglaUC);
    }

    // ==================================================================
    // Card 5: notas por momento + nota final ponderada
    // ==================================================================

    /**
     * Lança a nota de um estudante num momento de avaliação.
     * Valida: ano não em PLANEAMENTO; docente leciona a UC; aluno inscrito;
     * nota 0–20; momento válido para a UC; e momento já realizado (data não futura).
     * @return null em caso de sucesso; mensagem de erro caso contrário.
     */
    public String lancarNotaMomento(int numMec, String siglaUC, int idMomento, double nota, Docente d) {
        AnoLetivoBLL anoBll = new AnoLetivoBLL();
        if (anoBll.getEstadoAnoAtual() == EstadoAnoLetivo.PLANEAMENTO)
            return "ERRO: não é possível lançar notas com o ano letivo em PLANEAMENTO.";
        if (!lecionaEstaUC(d, siglaUC))
            return "ERRO: a UC '" + siglaUC + "' não pertence às suas unidades curriculares.";
        int ano = Config.getAnoAtual();
        if (!inscricaoDAL.obterAlunosPorUc(siglaUC, ano).contains(numMec))
            return "ERRO: o aluno " + numMec + " não está inscrito na UC '" + siglaUC + "'.";
        if (nota < 0 || nota > 20)
            return "ERRO: a nota deve estar entre 0 e 20.";
        model.Momento m = momentoDAL.procurarPorId(idMomento);
        if (m == null || !m.getSiglaUC().equalsIgnoreCase(siglaUC))
            return "ERRO: momento inválido para a UC '" + siglaUC + "'.";
        if (momentoAindaNaoOcorreu(m))
            return "ERRO: o momento '" + m.getNome() + "' ainda não ocorreu (data: " + m.getDataRealizacao() + ").";
        notaDAL.guardar(new model.Nota(numMec, idMomento, siglaUC, nota));
        return null;
    }

    /** Edita uma nota já lançada (valida 0–20). */
    public String editarNota(int numMec, int idMomento, double novaNota) {
        if (novaNota < 0 || novaNota > 20) return "ERRO: a nota deve estar entre 0 e 20.";
        model.Nota existente = notaDAL.procurar(numMec, idMomento);
        if (existente == null) return "ERRO: não existe nota para editar.";
        existente.setValor(novaNota);
        notaDAL.guardar(existente);
        return null;
    }

    /** Consulta as notas de um aluno numa UC (por momento). */
    public java.util.List<model.Nota> consultarNotas(int numMec, String siglaUC) {
        return notaDAL.listarPorAlunoEUc(numMec, siglaUC);
    }

    /**
     * Nota final de uma UC = média ponderada das notas dos momentos pelos pesos.
     * Momentos sem nota são ignorados (a ponderação usa só os momentos avaliados).
     */
    public double calcularNotaFinalPonderada(int numMec, String siglaUC) {
        double somaPesos = 0, somaPond = 0;
        for (model.Momento m : momentoDAL.listarPorUc(siglaUC)) {
            model.Nota n = notaDAL.procurar(numMec, m.getId());
            if (n != null) {
                somaPond += n.getValor() * m.getPeso();
                somaPesos += m.getPeso();
            }
        }
        return somaPesos == 0 ? 0.0 : somaPond / somaPesos;
    }

    /** Aprovado na UC se a nota final ponderada for >= 9,5. */
    public boolean isAprovadoPonderado(int numMec, String siglaUC) {
        return calcularNotaFinalPonderada(numMec, siglaUC) >= 9.5;
    }

    /** true se o momento tem data futura (ainda não ocorreu). */
    private boolean momentoAindaNaoOcorreu(model.Momento m) {
        String data = m.getDataRealizacao();
        if (data == null || data.trim().isEmpty()) return false; // sem data: não bloqueia
        try {
            java.time.LocalDate d = java.time.LocalDate.parse(data.trim(),
                    java.time.format.DateTimeFormatter.ofPattern("dd-MM-uuuu"));
            return d.isAfter(java.time.LocalDate.now());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Devolve a lista de UCs do docente que ainda não têm momentos definidos
     * (momentos == 0). Usado para mostrar alerta no login quando o ano está
     * em PLANEAMENTO.
     */
    public List<String> obterUcsSemMomentos(Docente docente) {
        List<String> pendentes = new java.util.ArrayList<>();
        UnidadeCurricular[] ucs = docente.getUcsLecionadas();
        for (int i = 0; i < docente.getTotalUcsLecionadas(); i++) {
            UnidadeCurricular uc = ucs[i];
            if (uc != null && ucDAL.obterMomentos(uc.getSigla(), PASTA_BD) == 0) {
                pendentes.add(uc.getSigla() + " - " + uc.getNome());
            }
        }
        return pendentes;
    }

    /**
     * Devolve o número de momentos atualmente definidos para a UC.
     * 0 = ainda não definido.
     */
    public int obterMomentosUc(String siglaUc) {
        return ucDAL.obterMomentos(siglaUc, PASTA_BD);
    }
}