import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.awt.event.KeyEvent;
import java.awt.Toolkit;

// Главное окно: поле, строка состояния и меню.
public class GameWindow extends JFrame {
    private static final int DEPTH_EASY = 3;
    private static final int DEPTH_MEDIUM = 5;
    private static final int DEPTH_HARD = 7;

    private final GameController controller = new GameController();
    private final CursorSet cursors = new CursorSet();   // курсоры лисы и гуся
    private final BoardPanel boardPanel;
    private final JLabel statusLabel = new JLabel();
    private final Timer computerTimer;          // таймер: даёт окну перерисоваться перед ходом компьютера
    private int depth = DEPTH_MEDIUM;           // выбранный уровень сложности
    private boolean resultShown = false;        // показали ли уже сообщение об окончании партии

    public GameWindow() {
        super("Лиса и гуси");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        boardPanel = new BoardPanel(controller, cursors, this::afterChange);
        add(boardPanel, BorderLayout.CENTER);

        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.PLAIN, 15f));
        statusLabel.setBorder(new EmptyBorder(8, 12, 8, 12));
        add(statusLabel, BorderLayout.SOUTH);

        computerTimer = new Timer(300, e -> computerStep());
        computerTimer.setRepeats(false);

        setJMenuBar(createMenu());
        pack();
        setLocationRelativeTo(null);

        startNewGame(true, false);
    }

    // ---------- Меню ----------
    private JMenuBar createMenu() {
        int ctrl = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        JMenuBar bar = new JMenuBar();

        JMenu game = new JMenu("Игра");
        game.add(menuItem("Новая игра: я за гусей", KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), () -> startNewGame(true, false)));
        game.add(menuItem("Новая игра: я за лису", KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), () -> startNewGame(false, true)));
        game.add(menuItem("Новая игра: два игрока", KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0), () -> startNewGame(true, true)));
        game.addSeparator();
        game.add(menuItem("Отменить ход", KeyStroke.getKeyStroke(KeyEvent.VK_Z, ctrl), this::undo));
        game.addSeparator();
        game.add(menuItem("Выход", null, () -> System.exit(0)));
        bar.add(game);

        JMenu level = new JMenu("Сложность");
        ButtonGroup group = new ButtonGroup();
        level.add(levelItem("Лёгкая", DEPTH_EASY, group));
        level.add(levelItem("Средняя", DEPTH_MEDIUM, group));
        level.add(levelItem("Сложная", DEPTH_HARD, group));
        bar.add(level);

        JMenu help = new JMenu("Справка");
        help.add(menuItem("Правила игры", KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), this::showRules));
        help.add(menuItem("Управление", null, this::showControls));
        bar.add(help);
        return bar;
    }

    // Создаёт пункт меню с действием.
    private JMenuItem menuItem(String text, KeyStroke key, Runnable action) {
        JMenuItem item = new JMenuItem(text);
        if (key != null) {
            item.setAccelerator(key);
        }
        item.addActionListener(e -> action.run());
        return item;
    }

    // Создаёт переключатель уровня сложности.
    private JRadioButtonMenuItem levelItem(String text, int value, ButtonGroup group) {
        JRadioButtonMenuItem item = new JRadioButtonMenuItem(text, value == depth);
        item.addActionListener(e -> {
            depth = value;
            controller.setDepth(value);
        });
        group.add(item);
        return item;
    }

    // ---------- Игровые действия ----------

    // Начинает новую партию.
    private void startNewGame(boolean geeseHuman, boolean foxHuman) {
        computerTimer.stop();
        controller.newGame(geeseHuman, foxHuman, depth);
        boardPanel.clearSelection();
        resultShown = false;
        boardPanel.repaint();
        afterChange();
    }

    // Отменяет ход.
    private void undo() {
        if (controller.canUndo() && !computerTimer.isRunning()) {
            controller.undo();
            boardPanel.clearSelection();
            resultShown = false;
            boardPanel.repaint();
            afterChange();
        }
    }

    // Вызывается после любого изменения: обновляет строку состояния и запускает компьютер, если его ход.
    private void afterChange() {
        updateStatus();
        updateCursor();
        if (controller.isComputerTurn()) {
            computerTimer.restart();
        } else if (controller.isGameOver() && !resultShown) {
            resultShown = true;
            JOptionPane.showMessageDialog(this, resultText(), "Игра окончена", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Ставит курсор персонажа на всё окно (кроме поля, у поля свой курсор: см. BoardPanel.updateCursor).
    private void updateCursor() {
        int kind = controller.isComputerTurn() ? CursorSet.BUSY : CursorSet.NORMAL;
        setCursor(cursors.get(controller.getCursorPlayer(), kind));
        boardPanel.updateCursor();
    }

    // Ход компьютера (вызывается таймером).
    private void computerStep() {
        controller.doComputerStep();
        boardPanel.repaint();
        afterChange();
    }

    // ---------- Тексты ----------

    // Обновляет строку состояния.
    private void updateStatus() {
        GameState state = controller.getState();
        String text;
        if (controller.isGameOver()) {
            text = resultText();
        } else if (controller.needFoxPlacement()) {
            text = controller.isHumanTurn()
                    ? "Выберите стартовую клетку для лисы (щёлкните по свободной клетке)"
                    : "Компьютер выбирает клетку для лисы...";
        } else {
            boolean foxTurn = state.getCurrentPlayer() == GameRules.PLAYER_FOX;
            String who = foxTurn ? "Ход лисы" : "Ход гусей";
            text = who + (controller.isHumanTurn() ? "" : " (компьютер думает...)");
        }
        text += "   |   Гусей на поле: " + state.getGeeseCount() + " из " + GameRules.GEESE_AT_START
                + "   |   Ходов: " + controller.getMoveCount();
        if (controller.getLastMove() != null) {
            text += "   |   Последний ход: " + controller.getLastMove();
        }
        statusLabel.setText(text);
    }

    // Текст о результате партии.
    private String resultText() {
        int result = controller.getResult();
        if (result == GameRules.RESULT_FOX_WINS) {
            return "Победила лиса!";
        }
        if (result == GameRules.RESULT_GEESE_WIN) {
            return "Победили гуси! Лиса заперта.";
        }
        return "Ничья (достигнут предел в " + GameRules.MAX_MOVES + " ходов).";
    }

    // Окно с правилами игры.
    private void showRules() {
        String text = "Лиса и гуси\n\n"
                + "Поле в форме креста. У гусей 17 фишек, они стоят в нижней части поля.\n"
                + "Перед началом игрок-лиса ставит свою фишку в любую свободную клетку.\n"
                + "Гуси ходят первыми.\n\n"
                + "Гусь: на одну свободную клетку вверх, вниз, влево или вправо.\n"
                + "Лиса: так же, а ещё по диагонали. Кроме того, лиса может перепрыгнуть\n"
                + "через соседнего гуся на свободную клетку за ним и съесть его.\n\n"
                + "Гуси выигрывают, если лиса не может сделать ход.\n"
                + "Лиса выигрывает, если на поле осталось 6 гусей или меньше.\n"
                + "Если за " + GameRules.MAX_MOVES + " ходов никто не победил, объявляется ничья.";
        JOptionPane.showMessageDialog(this, text, "Правила игры", JOptionPane.INFORMATION_MESSAGE);
    }

    // Окно с описанием управления.
    private void showControls() {
        String text = "Мышь: щёлкните по своей фишке, затем по зелёной точке (шаг)\n"
                + "или красному кольцу (прыжок со взятием).\n\n"
                + "Клавиатура:\n"
                + "  стрелки — двигать синий курсор по полю\n"
                + "  Enter или пробел — выбрать фишку / сделать ход\n"
                + "  Esc — снять выбор\n"
                + "  Ctrl+Z — отменить ход\n"
                + "  F2 / F3 / F4 — новая игра (за гусей / за лису / два игрока)\n"
                + "  F1 — правила";
        JOptionPane.showMessageDialog(this, text, "Управление", JOptionPane.INFORMATION_MESSAGE);
    }
}
