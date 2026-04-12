package view;
import java.util.Scanner;

public class GestorView {
    private Scanner scanner = new Scanner(System.in);

    public int mostrarMenu() {
        mostrarMensagem("\n=== MENU GESTOR ===");
        mostrarMensagem("1 - Registar Novo Estudante");
        mostrarMensagem("2 - Gerir Unidades Curriculares");
        mostrarMensagem("3 - Gerir Cursos");
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
        if (entidade.equalsIgnoreCase("Unidades Curriculares")) {
            mostrarMensagem("5 - Associar UC Existente a um Curso");
        }
        if (entidade.equalsIgnoreCase("Cursos")) {
            mostrarMensagem("5 - Listar UCs do Curso por Ano");
        }
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
        return pedirInput("Data Nasc. (DD-MM-AAAA)");
    }

    public String pedirAnoInscricao() {
        return pedirInput("Ano de Inscrição");
    }

    public void mostrarErroEdicaoCurso() {
        mostrarMensagem(">> ERRO: Ação bloqueada! Já existem estudantes inscritos neste curso.");
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

    public void mostrarListaCursos(String[] cursos) {
        mostrarMensagem("\n--- SELEÇÃO DE CURSO ---");
        for (int i = 0; i < cursos.length; i++) {
            if (cursos[i] != null) {
                 mostrarMensagem((i + 1) + " - " + cursos[i]);
            }
        }
    }

    public int pedirOpcaoCurso(int max) {
        while (true) {
            try {
                int opcao = Integer.parseInt(pedirInput("Selecione o número do Curso"));
                if (opcao > 0 && opcao <= max) {
                    return opcao;
                }
                mostrarMensagem(">> Erro: Opção inválida. Escolha entre 1 e " + max + ".");
            } catch (NumberFormatException e) {
                mostrarMensagem(">> Erro: Introduza um número válido.");
            }
        }
    }

    public void mostrarAvisoSemCursos() {
        mostrarMensagem(">> Aviso: Não existem cursos registados. Introduza a sigla manualmente.");
    }

    public void mostrarNumMecanograficoAtribuido(int numMec) {
        mostrarMensagem("Nº Mecanográfico atribuído: " + numMec);
    }

    public void mostrarErroNomeInvalido() {
        mostrarMensagem("ERRO: Nome inválido. Utilize apenas letras e espaços.");
    }

    public void mostrarErroNifInvalido() {
        mostrarMensagem("ERRO: NIF inválido. Deve conter exatamente 9 dígitos.");
    }

    public void mostrarErroDataInvalida() {
        mostrarMensagem("ERRO: Data inválida. Utilize rigorosamente o formato DD-MM-AAAA.");
    }

    public void mostrarListaUcs(String[] ucs) {
        mostrarMensagem("\n--- SELEÇÃO DE UNIDADE CURRICULAR ---");
        for (int i = 0; i < ucs.length; i++) {
            if (ucs[i] != null) {
                mostrarMensagem((i + 1) + " - " + ucs[i]);
            }
        }
    }

    public int pedirOpcaoUc(int max) {
        while (true) {
            try {
                int opcao = Integer.parseInt(pedirInput("Selecione o número da UC"));
                if (opcao > 0 && opcao <= max) {
                    return opcao;
                }
                mostrarMensagem("ERRO: Opção inválida. Escolha entre 1 e " + max + ".");
            } catch (NumberFormatException e) {
                mostrarMensagem("ERRO: Introduza um número válido.");
            }
        }
    }

}