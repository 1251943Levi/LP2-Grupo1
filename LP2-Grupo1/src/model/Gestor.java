package model;

/**
 * Classe Model que representa o Gestor do sistema.
 * Herda todos os dados pessoais e credenciais de Utilizador.
 * Não duplica campos — delega completamente na classe pai.
 */
public class Gestor extends Utilizador {

    public Gestor(String email, String password, String nome,
                  String nif, String morada, String dataNascimento) {
        super(email, password, nome, nif, morada, dataNascimento);
    }
}