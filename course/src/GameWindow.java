import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
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

    private static final int[] DEPTHS = {DEPTH_EASY, DEPTH_MEDIUM, DEPTH_HARD};

    private final GameController controller = new GameController();
    private final CursorSet cursors = new CursorSet();   // курсоры лисы и гуся
    private final BoardPanel boardPanel;
    private final JLabel statusLabel = new JLabel();
    private final Timer computerTimer;          // таймер: даёт окну перерисоваться перед ходом компьютера
    private int depth = DEPTH_MEDIUM;           // выбранный уровень сложности
    private boolean resultShown = false;        // показали ли уже сообщение об окончании партии

    // Окно состоит из двух «карточек»: стартовое меню и сама игра. Показывается одна из них.
    private final CardLayout rootCards = new CardLayout();
    private final JPanel root = new JPanel(rootCards);
    private final MenuPanel menuPanel;
    private final JMenuBar menuBar;             // меню окна (показываем только во время игры)
    private final JRadioButtonMenuItem[] levelItems = new JRadioButtonMenuItem[3];
    private boolean lastGeeseHuman = true;      // с какими настройками началась последняя партия
    private boolean lastFoxHuman = false;

    public GameWindow() {
        super("Лиса и гуси");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Значок окна (в заголовке и на панели задач).
        BufferedImage icon = PieceImages.load("fox.png");
        if (icon != null) {
            setIconImage(icon);
        }

        // Карточка «игра»: поле и строка состояния.
        boardPanel = new BoardPanel(controller, cursors, this::afterChange);
        JPanel gamePanel = new JPanel(new BorderLayout());
        gamePanel.add(boardPanel, BorderLayout.CENTER);
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.PLAIN, 14f));
        statusLabel.setBorder(new EmptyBorder(8, 12, 8, 12));
        gamePanel.add(statusLabel, BorderLayout.SOUTH);

        // Карточка «меню».
        menuPanel = new MenuPanel(new MenuPanel.Listener() {
            @Override
            public void onStart(boolean geeseHuman, boolean foxHuman, int chosenDepth) {
                startFromMenu(geeseHuman, foxHuman, chosenDepth);
            }

            @Override
            public void onExit() {
                System.exit(0);
            }
        });

        root.add(menuPanel, "menu");
        root.add(gamePanel, "game");
        setContentPane(root);

        computerTimer = new Timer(300, e -> computerStep());
        computerTimer.setRepeats(false);

        menuBar = createMenu();
        pack();
        setLocationRelativeTo(null);
        // Сразу открываем окно на весь экран (панель задач не закрываем).
        setBounds(java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds());
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        showMenu();   // при запуске показываем стартовое меню
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
        game.add(menuItem("В главное меню", KeyStroke.getKeyStroke(KeyEvent.VK_M, ctrl), this::showMenu));
        game.add(menuItem("Выход", null, () -> System.exit(0)));
        bar.add(game);

        JMenu level = new JMenu("Сложность");
        ButtonGroup group = new ButtonGroup();
        String[] names = {"Лёгкая", "Средняя", "Сложная"};
        for (int i = 0; i < 3; i++) {
            levelItems[i] = levelItem(names[i], DEPTHS[i], group);
            level.add(levelItems[i]);
        }
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
        lastGeeseHuman = geeseHuman;
        lastFoxHuman = foxHuman;
        showGame();
        controller.newGame(geeseHuman, foxHuman, depth);
        boardPanel.clearSelection();
        resultShown = false;
        boardPanel.repaint();
        afterChange();
    }

    // Начало партии из стартового меню: запоминаем выбранную сложность и стартуем.
    private void startFromMenu(boolean geeseHuman, boolean foxHuman, int chosenDepth) {
        depth = chosenDepth;
        controller.setDepth(chosenDepth);
        for (int i = 0; i < 3; i++) {
            levelItems[i].setSelected(DEPTHS[i] == chosenDepth);   // галочка в меню «Сложность»
        }
        startNewGame(geeseHuman, foxHuman);
    }

    // Показывает стартовое меню. Партия, которая шла, просто остаётся позади.
    private void showMenu() {
        computerTimer.stop();
        setJMenuBar(null);
        setCursor(Cursor.getDefaultCursor());
        rootCards.show(root, "menu");
        menuPanel.reset();
        getRootPane().revalidate();
        repaint();
    }

    // Показывает игру (поле и меню окна).
    private void showGame() {
        if (getJMenuBar() == null) {
            setJMenuBar(menuBar);
        }
        rootCards.show(root, "game");
        getRootPane().revalidate();
        boardPanel.requestFocusInWindow();
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
            String[] options = {"Играть снова", "В главное меню"};
            int choice = JOptionPane.showOptionDialog(this, resultText(), "Игра окончена",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
            if (choice == 0) {
                startNewGame(lastGeeseHuman, lastFoxHuman);
            } else if (choice == 1) {
                showMenu();
            }
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
                    ? "Поставьте лису на свободную клетку"
                    : "Компьютер ставит лису...";
        } else {
            boolean foxTurn = state.getCurrentPlayer() == GameRules.PLAYER_FOX;
            String who = foxTurn ? "Ход лисы" : "Ход гусей";
            text = who + (controller.isHumanTurn() ? "" : " (компьютер думает...)");
        }
        text += "  |  Гусей: " + state.getGeeseCount() + " из " + GameRules.GEESE_AT_START
                + "  |  Ходов: " + controller.getMoveCount();
        if (controller.getLastMove() != null) {
            text += "  |  Последний: " + controller.getLastMove();
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

    // Окно с подробными правилами игры (тот же текст, что и в стартовом меню).
    private void showRules() {
        RulesPanel.showDialog(this);
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
