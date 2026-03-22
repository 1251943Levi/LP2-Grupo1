package Model;

import java.util.ArrayList;
import java.util.List;

public class Departamento {
    //atributos
    private String sigla;
    private String nome;

    //Associar a lista de Unidades Curriculares e o Docente Responsável.
    private Docente docenteResponsavel;
    private List<UnidadeCurricular> unidadesCurriculares;

    public Departamento(String sigla, String nome, Docente docenteResponsavel){
        this.sigla = sigla;
        this.nome = nome;
        this.docenteResponsavel = docenteResponsavel;
        this.unidadesCurriculares = new ArrayList<>(); // inicializa lista vazia
    }

    public String getSigla() {
        return sigla;
    }

    public void setSigla(String sigla) {
        this.sigla = sigla;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Docente getDocenteResponsavel() {
        return docenteResponsavel;
    }

    public void setDocenteResponsavel(Docente docenteResponsavel) {
        this.docenteResponsavel = docenteResponsavel;
    }

    public List<UnidadeCurricular> getUnidadesCurriculares() {
        return unidadesCurriculares;
    }

    public void setUnidadesCurriculares(List<UnidadeCurricular> unidadesCurriculares) {
        this.unidadesCurriculares = unidadesCurriculares;
    }

    //Metodo extra para adicionar UC á lista em vez de substituir
    public void adicionarUnidadeCurricular(UnidadeCurricular uc){
        this.unidadeCurriculares.add(uc);
    }


}
