package bll;

import model.Utilizador;
import model.RepositorioSessao;
import utils.ImportadorCSV;

public class AutenticacaoBLL {

    public boolean autenticar(String email, String passwordTextoLimpo) {
        // 1. A DAL (ImportadorCSV) lê o ficheiro e usa o SecurityUtil internamente
        Utilizador utilizador = ImportadorCSV.autenticarNoFicheiro(email, passwordTextoLimpo, "bd");

        if (utilizador != null) {
            // 2. Se as credenciais estiverem corretas, guarda no repositório centralizado
            RepositorioSessao.login(utilizador);
            return true;
        }

        return false;
    }
}