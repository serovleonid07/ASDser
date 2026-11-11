import javax.swing.SwingUtilities;

// Точка входа: запускает окно игры.
// Запуск: java -cp out Main
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameWindow window = new GameWindow();
            window.setVisible(true);
        });
    }
}
