package model;

import model.Estudante;
import model.Docente;
import model.Utilizador;
import model.Curso;
import model.Departamento;
import model.UnidadeCurricular;

public class RepositorioDados {

    // --- ARRAYS DE ARMAZENAMENTO ---
    private Estudante[] estudantes;
    private Docente[] docentes;
    private Curso[] cursos;
    private Departamento[] departamentos;
    private UnidadeCurricular[] ucs;

    // --- VARIÁVEIS CONTADORAS ---
    private int totalEstudantes;
    private int totalDocentes;
    private int totalCursos;
    private int totalDepartamentos;
    private int totalUcs;

    // --- CONSTRUTOR ---
    public RepositorioDados() {
        // Inicialização dos arrays
        this.estudantes = new Estudante[100];
        this.docentes = new Docente[50];
        this.cursos = new Curso[20];
        this.departamentos = new Departamento[10];
        this.ucs = new UnidadeCurricular[100];

        // Inicialização dos contadores a zero
        this.totalEstudantes = 0;
        this.totalDocentes = 0;
        this.totalCursos = 0;
        this.totalDepartamentos = 0;
        this.totalUcs = 0;
    }

    // --- OPERAÇÕES DE ADIÇÃO PARA O CRUD ---
    public boolean adicionarEstudante(Estudante e) {
        if (totalEstudantes < estudantes.length) {
            estudantes[totalEstudantes] = e;
            totalEstudantes++;
            return true;
        }
        return false;
    }

    public boolean adicionarDocente(Docente d) {
        if (totalDocentes < docentes.length) {
            docentes[totalDocentes] = d;
            totalDocentes++;
            return true;
        }
        return false;
    }

    public boolean adicionarCurso(Curso c) {
        if (totalCursos < cursos.length) {
            cursos[totalCursos] = c;
            totalCursos++;
            return true;
        }
        return false;
    }

    public boolean adicionarDepartamento(Departamento d) {
        if (totalDepartamentos < departamentos.length) {
            departamentos[totalDepartamentos] = d;
            totalDepartamentos++;
            return true;
        }
        return false;
    }

    public boolean adicionarUnidadeCurricular(UnidadeCurricular uc) {
        if (totalUcs < ucs.length) {
            ucs[totalUcs] = uc;
            totalUcs++;
            return true;
        }
        return false;
    }

    // --- Autenticação ---
    public Utilizador autenticar(String email, String password) {
        // Procurar nos Estudantes
        for (int i = 0; i < totalEstudantes; i++) {
            if (estudantes[i].getEmail().equalsIgnoreCase(email) && estudantes[i].getPassword().equals(password)) {
                return estudantes[i];
            }
        }

        // Procurar nos Docentes
        for (int i = 0; i < totalDocentes; i++) {
            if (docentes[i].getEmail().equalsIgnoreCase(email) && docentes[i].getPassword().equals(password)) {
                return docentes[i];
            }
        }

        return null; // Login falhou
    }

    // --- GETTERS ---

    public Estudante[] getEstudantes() { return estudantes; }
    public int getTotalEstudantes() { return totalEstudantes; }

    public Docente[] getDocentes() { return docentes; }
    public int getTotalDocentes() { return totalDocentes; }

    public Curso[] getCursos() { return cursos; }
    public int getTotalCursos() { return totalCursos; }

    public Departamento[] getDepartamentos() { return departamentos; }
    public int getTotalDepartamentos() { return totalDepartamentos; }

    public UnidadeCurricular[] getUcs() { return ucs; }
    public int getTotalUcs() { return totalUcs; }
}
