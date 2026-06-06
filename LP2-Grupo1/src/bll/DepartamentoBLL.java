package bll;

import common.ConfigApp;

import dal.DepartamentoDAL;
import model.Departamento;
import java.util.List;
import model.Curso;

public class DepartamentoBLL {

    private static final String PASTA_BD = ConfigApp.PASTA_BD;

    public List<Departamento> listarTodos() {
        return DepartamentoDAL.carregarTodos(PASTA_BD);
    }

    public Departamento obterPorSigla(String sigla) {
        return DepartamentoDAL.procurarDepartamento(sigla, PASTA_BD);
    }

    public boolean adicionarDepartamento(String sigla, String nome) {
        if (sigla == null || nome == null) return false;
        if (obterPorSigla(sigla) != null) return false; // evita duplicados
        Departamento dep = new Departamento(sigla.toUpperCase(), nome);
        DepartamentoDAL.adicionarDepartamento(dep, PASTA_BD);
        return true;
    }

    public boolean atualizarDepartamento(String sigla, String novaSigla, String novoNome) {
        Departamento existente = obterPorSigla(sigla);
        if (existente == null) return false;

        String siglaFinal = (novaSigla != null && !novaSigla.isEmpty()) ? novaSigla.toUpperCase() : existente.getSigla();
        String nomeFinal = (novoNome != null && !novoNome.isEmpty()) ? novoNome : existente.getNome();

        // Se a sigla mudou, verificar se a nova sigla já existe (excepto se for a mesma)
        if (!siglaFinal.equals(existente.getSigla()) && obterPorSigla(siglaFinal) != null) {
            return false; // nova sigla já em uso
        }

        Departamento depAtualizado = new Departamento(siglaFinal, nomeFinal);
        DepartamentoDAL.atualizarDepartamento(depAtualizado, PASTA_BD);
        return true;
    }

    public boolean removerDepartamento(String sigla) {
        // Verificar se existem cursos associados a este departamento
        List<Curso> cursos = new CursoBLL().listarTodos();
        for (Curso c : cursos) {
            if (c.getDepartamento() != null && c.getDepartamento().getSigla().equalsIgnoreCase(sigla)) {
                return false; // não remove porque há cursos dependentes
            }
        }
        return DepartamentoDAL.removerDepartamento(sigla, PASTA_BD);
    }
}