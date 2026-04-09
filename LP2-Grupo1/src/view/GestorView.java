package view;
import java.util.Scanner;

public class GestorView {
    private Scanner scanner = new Scanner(System.in);

    public int mostrarMenu() {
        mostrarMensagem("\n=== MENU GESTOR ===");
        mostrarMensagem("1 - Registar Novo Estudante");
        mostrarMensagem("2 - Gerir Unidades Curriculares (CRUD)");
        mostrarMensagem("3 - Gerir Cursos (CRUD)");
        mostrarMensagem("4 - Ver Estatísticas");
        mostrarMensagem("5 - Avançar Ano Letivo");
        mostrarMensagem("0 - Sair / Logout");
        try { return Integer.parseInt(pedirInput("Opção")); } catch (Exception e) { return -1; }
    }

    public int mostrarMenuCRUD(String entidade) {
        mostrarMensagem("\n--- GERIR " + entidade.toUpperCase() + " ---");
        mostrarMensagem("1 - Adicionar " + entidade);
        mostrarMensagem("2 - Listar " + entidade);
        mostrarMensagem("3 - Editar " + entidade);
        mostrarMensagem("4 - Remover " + entidade);
        mostrarMensagem("0 - Voltar");
        try { return Integer.parseInt(pedirInput("Opção")); } catch (Exception e) { return -1; }
    }

    public int mostrarMenuEstatisticas() {
        mostrarMensagem("\n--- ESTATÍSTICAS ---");
        mostrarMensagem("1 - Média Global Institucional");
        mostrarMensagem("2 - Melhor Aluno");
        mostrarMensagem("0 - Voltar");
        try { return Integer.parseInt(pedirInput("Opção")); } catch (Exception e) { return -1; }
    }

    public String pedirInput(String msg) {
        System.out.print(msg + ": ");
        return scanner.nextLine();
    }

    public void mostrarMensagem(String msg) {
        System.out.println(">> " + msg);
    }

    public String pedirSiglaCurso() {
        return pedirInput("Sigla do Curso");
    }

    public String pedirAnoCurricular() {
        return pedirInput("Ano Curricular (ex: 1, 2, 3)");
    }

    public String pedirSiglaUc() {
        return pedirInput("Sigla da UC (ex: POO, BD)");
    }

    public String pedirNomeUc() {
        return pedirInput("Nome da UC");
    }

    public String pedirSiglaDocente() {
        return pedirInput("Sigla do Docente Responsável");
    }

    public String pedirNovoNome() {
        return pedirInput("Novo Nome");
    }

    public String pedirNovoAnoCurricular() {
        return pedirInput("Novo Ano Curricular");
    }

    public String pedirNovaSiglaDocente() {
        return pedirInput("Nova Sigla Docente");
    }

    public String pedirNovaSiglaCurso() {
        return pedirInput("Nova Sigla Curso");
    }

    public String pedirNomeCurso() {
        return pedirInput("Nome do Curso");
    }

    public String pedirDepartamento() {
        return pedirInput("Departamento (ex: DEIS)");
    }

    public String pedirNovoDepartamento() {
        return pedirInput("Novo Departamento");
    }

    public void mostrarTituloRegistoEstudante() {
        mostrarMensagem("\n--- REGISTAR ESTUDANTE ---");
    }

    public String pedirNumMecanografico() {
        return pedirInput("Nº Mecanográfico");
    }

    public String pedirNome() {
        return pedirInput("Nome");
    }

    public String pedirNif() {
        return pedirInput("NIF (9 dígitos)");
    }

    public String pedirMorada() {
        return pedirInput("Morada");
    }

    public String pedirDataNascimento() {
        return pedirInput("Data Nasc. (DD/MM/AAAA)");
    }

    public String pedirAnoInscricao() {
        return pedirInput("Ano de Inscrição");
    }

    public void mostrarErroEdicaoCurso() {
        mostrarMensagem(">> ERRO: Ação bloqueada! Já existem estudantes inscritos neste curso (Risco de corrupção de histórico).");
    }

    public void mostrarErroLimiteUcs(int ano) {
        mostrarMensagem(">> ERRO: Não é possível associar mais de 5 UCs ao " + ano + "º ano deste Curso.");
    }

    public void mostrarSucessoCriacao(String entidade) {
        mostrarMensagem(">> Sucesso: " + entidade + " adicionado(a) com sucesso ao sistema!");
    }

    public void mostrarSucessoAtualizacao(String entidade) {
        mostrarMensagem(">> Sucesso: " + entidade + " atualizado(a) com sucesso!");
    }

    public void mostrarSucessoRemocao(String entidade) {
        mostrarMensagem(">> Sucesso: " + entidade + " removido(a) com sucesso!");
    }

    public void mostrarErroNaoEncontrado(String entidade) {
        mostrarMensagem(">> Erro: " + entidade + " não encontrado(a) na base de dados.");
    }

    public void mostrarMensagemModoEdicao() {
        mostrarMensagem(">> Registo encontrado! Introduza os novos dados:");
    }

    public void mostrarSucessoRegistoEstudante(String email, String passwordLimpa) {
        mostrarMensagem(">> Estudante Registado com Sucesso!");
        mostrarMensagem(">> Email: " + email + " | Password Temporária: " + passwordLimpa);
    }
}