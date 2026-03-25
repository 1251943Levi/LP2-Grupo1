
import Controller.MainController;

public class Main {
    public static void main(String[] args) {
        // --- Diretório para colocar o dados.csv ---
        System.out.println(">>> O Java está a procurar ficheiros na pasta: " + System.getProperty("user.dir"));

        MainController mc = new MainController();
        mc.iniciarSistema();
    }
}