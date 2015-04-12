package astava.samples.drawnmap;


import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        MainView view = new MainView();

        view.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        view.setSize(800, 600);
        view.setLocationRelativeTo(null);
        view.setVisible(true);
    }
}
