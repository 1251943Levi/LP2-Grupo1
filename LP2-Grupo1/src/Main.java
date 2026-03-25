
import Controller.maincontroller;

public class Main {
    public static void main(String[] args) {
        // --- Diretório para colocar o dados.csv ---
        System.out.println(">>> O Java está a procurar ficheiros na pasta: " + System.getProperty("user.dir"));

        maincontroller mc = new maincontroller();
        mc.iniciarSistema();
    }
}