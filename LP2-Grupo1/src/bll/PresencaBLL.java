package bll;

import common.ConfigApp;
import dal.*;
import model.Aula;
import model.Estudante;
import model.Presenca;
import utils.Config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PresencaBLL {
    private final PresencaDAL dal;
    private final EstudanteBLL estudanteBll = new EstudanteBLL();

    public PresencaBLL() {
        this.dal = ConfigApp.isModoSql() ? new PresencaDALSql() : new PresencaDALFile();
        dal.inicializar();
    }

    /**
     * Docente marca presença numa aula (abre a aula para os alunos)
     */
    public void marcarPresencaDocente(int idAula) {
        if (dal.docenteJaMarcou(idAula)) {
            throw new EstadoInvalidoException("O docente já marcou presença para esta aula.");
        }
        dal.marcarDocente(idAula);
    }

    /**
     * Estudante marca presença numa aula
     */
    public void marcarPresencaEstudante(int numMec, int idAula) {
        if (!dal.docenteJaMarcou(idAula)) {
            throw new EstadoInvalidoException("O docente ainda não registou esta aula.");
        }
        if (dal.existePresencaAluno(numMec, idAula)) {
            throw new EstadoInvalidoException("Estudante já registou presença nesta aula.");
        }
        // Verificar se o estudante está inscrito na UC da aula (opcional)
        Aula aula = new HorarioBLL().buscarPorId(idAula);
        if (aula == null) {
            throw new EstadoInvalidoException("Aula não encontrada.");
        }
        // Verificar inscrição (pode ser feito via InscricaoDAL)
        // Por simplicidade, assumimos que está inscrito.

        Presenca presenca = new Presenca();
        presenca.setIdAula(idAula);
        presenca.setNumMec(numMec);
        presenca.setEstado("PRESENTE");
        presenca.setDocenteMarcou(false);
        presenca.setDataHoraRegisto(LocalDateTime.now());
        dal.adicionar(presenca);
    }

    /**
     * Lista todas as presenças de uma aula
     */
    public List<Presenca> listarPresencasPorAula(int idAula) {
        return dal.listarPorAula(idAula);
    }

    /**
     * Lista presenças de um aluno
     */
    public List<Presenca> listarPresencasPorAluno(int numMec) {
        return dal.listarPorAluno(numMec);
    }

    /**
     * Verifica se um estudante já marcou presença numa aula
     */
    public boolean estudanteMarcouPresenca(int numMec, int idAula) {
        return dal.existePresencaAluno(numMec, idAula);
    }

    /**
     * Verifica se o docente já marcou presença para a aula
     */
    public boolean docenteMarcouPresenca(int idAula) {
        return presencasEstaoAbertas(idAula);
    }

    /**
     * Obtém o relatório de presenças de uma aula como lista de linhas formatadas.
     * @return Lista de strings, cada uma representando uma linha do relatório.
     */
    public List<String> obterRelatorioPresencas(int idAula) {
        List<String> linhas = new ArrayList<>();
        List<Presenca> presencas = listarPresencasPorAula(idAula);

        // 1. Obter informação da aula
        HorarioBLL horarioBll = new HorarioBLL();
        Aula aula = horarioBll.buscarPorId(idAula);
        if (aula == null) {
            linhas.add("Aula não encontrada.");
            return linhas;
        }

        // 2. Obter TODOS os estudantes e filtrar pelo curso da aula
        List<Estudante> todosEstudantes = estudanteBll.carregarTodos();
        List<Integer> numMecAlunos = new ArrayList<>();
        for (Estudante e : todosEstudantes) {
            if (e.getSiglaCurso() != null && e.getSiglaCurso().equalsIgnoreCase(aula.getSiglaCurso())) {
                numMecAlunos.add(e.getNumeroMecanografico());
            }
        }

        if (numMecAlunos.isEmpty()) {
            linhas.add("Nenhum aluno inscrito neste curso.");
            return linhas;
        }

        // 3. Criar mapa de presenças (numMec -> estado)
        Map<Integer, String> mapaPresencas = new HashMap<>();
        for (Presenca p : presencas) {
            if (p.getNumMec() != -1) {
                mapaPresencas.put(p.getNumMec(), p.getEstado());
            }
        }

        // 4. Construir relatório
        linhas.add(String.format("%-10s | %-25s | %-12s", "NumMec", "Nome", "Estado"));
        linhas.add("─────────────────────────────────────────────────────────────────");

        for (int numMec : numMecAlunos) {
            Estudante e = estudanteBll.procurarPorNumMec(numMec);
            String nome = (e != null) ? e.getNome() : "Desconhecido";
            if (nome.length() > 25) nome = nome.substring(0, 22) + "...";

            String estado = mapaPresencas.getOrDefault(numMec, "FALTA");
            linhas.add(String.format("%-10d | %-25s | %-12s", numMec, nome, estado));
        }

        return linhas;
    }

    public void abrirPresencas(int idAula) {
        if (presencasEstaoAbertas(idAula)) {
            throw new EstadoInvalidoException("Presenças já estão abertas.");
        }
        // Remover registos antigos do docente (se houver)
        removerRegistoDocente(idAula);
        // Criar novo com status ABERTO
        Presenca p = new Presenca();
        p.setIdAula(idAula);
        p.setNumMec(-1);
        p.setEstado("DOCENTE");
        p.setDocenteMarcou(true);
        p.setStatusDocente("ABERTO");
        p.setDataHoraRegisto(LocalDateTime.now());
        dal.adicionar(p);
    }

    public void fecharPresencas(int idAula) {
        if (!presencasEstaoAbertas(idAula)) {
            throw new EstadoInvalidoException("Presenças já estão fechadas.");
        }
        // Atualizar status para FECHADO
        List<Presenca> registos = dal.listarPorAula(idAula);
        for (Presenca p : registos) {
            if (p.getNumMec() == -1) {
                p.setStatusDocente("FECHADO");
                dal.atualizar(p);
                break;
            }
        }
    }

    // bll/PresencaBLL.java

    /**
     * Verifica se as presenças estão abertas para uma aula (docente abriu e não fechou).
     */
    public boolean presencasEstaoAbertas(int idAula) {
        List<Presenca> registos = dal.listarPorAula(idAula);
        for (Presenca p : registos) {
            if (p.getNumMec() == -1 && "ABERTO".equals(p.getStatusDocente())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica se o docente já abriu presenças (estado ABERTO ou FECHADO).
     * Para saber se está aberto, use presencasEstaoAbertas().
     */
    public boolean docenteJaMarcou(int idAula) {
        List<Presenca> registos = dal.listarPorAula(idAula);
        for (Presenca p : registos) {
            if (p.getNumMec() == -1 && p.getStatusDocente() != null) {
                return true;
            }
        }
        return false;
    }



    private void removerRegistoDocente(int idAula) {
        List<Presenca> registos = dal.listarPorAula(idAula);
        for (Presenca p : registos) {
            if (p.getNumMec() == -1) {
                dal.remover(p.getId());
                break;
            }
        }
    }

    /**
     * Devolve uma lista formatada com as presenças do estudante.
     */
    public List<String> obterPresencasDoEstudante(int numMec) {
        List<String> linhas = new ArrayList<>();
        Estudante estudante = estudanteBll.procurarPorNumMec(numMec);
        if (estudante == null) {
            linhas.add("Estudante não encontrado.");
            return linhas;
        }

        // 1. Obter TODAS as aulas do estudante (horário)
        HorarioBLL horarioBll = new HorarioBLL();
        int anoAtual = Config.getAnoAtual();
        List<Aula> todasAulas = horarioBll.listarHorarioEstudante(
                estudante.getSiglaCurso(),
                estudante.getAnoCurricular(),
                anoAtual
        );

        if (todasAulas.isEmpty()) {
            linhas.add("Não tem aulas agendadas.");
            return linhas;
        }

        // 2. Obter as presenças que o estudante já marcou
        List<Presenca> presencas = dal.listarPorAluno(numMec);
        Set<Integer> idsPresencas = new HashSet<>();
        for (Presenca p : presencas) {
            idsPresencas.add(p.getIdAula());
        }

        // 3. Filtrar apenas as aulas que já passaram (até hoje)
        LocalDate hoje = LocalDate.now();
        List<Aula> aulasPassadas = new ArrayList<>();
        for (Aula a : todasAulas) {
            if (a.getData().isBefore(hoje) || a.getData().isEqual(hoje)) {
                aulasPassadas.add(a);
            }
        }

        if (aulasPassadas.isEmpty()) {
            linhas.add("Ainda não teve nenhuma aula.");
            return linhas;
        }

        // 4. Construir relatório
        DateTimeFormatter fmtData = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        linhas.add(String.format("%-6s | %-12s | %-10s | %-8s", "Aula", "Data", "Hora", "Estado"));
        linhas.add("─────────────────────────────────────────────────────────────");

        int presentes = 0, faltas = 0;
        for (Aula a : aulasPassadas) {
            String estado = idsPresencas.contains(a.getId()) ? "PRESENTE" : "FALTA";
            if (estado.equals("PRESENTE")) presentes++;
            else faltas++;

            String data = a.getData().format(fmtData);
            String hora = a.getHoraInicio() + "-" + a.getHoraFim();
            linhas.add(String.format("%-6d | %-12s | %-10s | %-8s",
                    a.getId(), data, hora, estado));
        }

        // 5. Estatísticas
        linhas.add("");
        linhas.add(String.format("  Total de aulas: %d  |  Presentes: %d  |  Faltas: %d",
                aulasPassadas.size(), presentes, faltas));

        return linhas;
    }
}