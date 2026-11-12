package frontend;

import backend.*;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.swing.JComponent;
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
    private boolean statsRecorded = false;      // записали ли уже итог партии в статистику
    private boolean resultShown = false;        // показали ли уже сообщение об окончании партии

    // Окно состоит из двух «карточек»: стартовое меню и сама игра. Показывается одна из них.
    private final CardLayout rootCards = new CardLayout();
    private final JPanel root = new JPanel(rootCards);
    private final MenuPanel menuPanel;
    private StatsPanel statsPanel;              // страница статистики внутри игры
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
        root.add(createStatsScreen(), "stats");
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
        game.add(menuItem("Подсказка", KeyStroke.getKeyStroke(KeyEvent.VK_H, 0), boardPanel::showHint));
        game.add(menuItem("Отменить ход", KeyStroke.getKeyStroke(KeyEvent.VK_Z, ctrl), this::undo));
        game.addSeparator();
        game.add(menuItem("Статистика", null, this::showStats));
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

        JMenu theme = new JMenu("Тема");
        ButtonGroup themeGroup = new ButtonGroup();
        for (int i = 0; i < Settings.THEME_NAMES.length; i++) {
            final int index = i;
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(Settings.THEME_NAMES[i], i == Settings.getTheme());
            item.addActionListener(e -> {
                Settings.setTheme(index);
                repaint();
            });
            themeGroup.add(item);
            theme.add(item);
        }
        bar.add(theme);

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
        boardPanel.stopAnimation();
        boardPanel.clearSelection();
        resultShown = false;
        statsRecorded = false;
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

    // Страница статистики поверх игры: фон, карточка и сама статистика. Партия при этом остаётся как была.
    private JComponent createStatsScreen() {
        statsPanel = new StatsPanel(this::closeStats);
        JPanel screen = new JPanel(new java.awt.GridBagLayout()) {
            private final BackgroundImage background = new BackgroundImage();

            @Override
            protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g);
                BufferedImage image = background.get(Math.max(getWidth(), getHeight()));
                if (image != null) {
                    g.drawImage(image, (getWidth() - image.getWidth()) / 2, (getHeight() - image.getHeight()) / 2, null);
                }
            }
        };
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(java.awt.Graphics g0) {
                java.awt.Graphics2D g = (java.awt.Graphics2D) g0.create();
                g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(MenuStyle.CARD);
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g.setColor(new java.awt.Color(0xB9AE9C));
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
                g.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(14, 18, 14, 18));
        card.setPreferredSize(new java.awt.Dimension(580, 560));
        card.add(statsPanel, BorderLayout.CENTER);
        screen.add(card);
        // Esc тоже закрывает статистику.
        screen.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeStats");
        screen.getActionMap().put("closeStats", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                closeStats();
            }
        });
        return screen;
    }

    // Показывает статистику. Компьютер на это время не ходит.
    private void showStats() {
        computerTimer.stop();
        rootCards.show(root, "stats");
        statsPanel.refresh();
    }

    // Возвращает из статистики в игру.
    private void closeStats() {
        rootCards.show(root, "game");
        boardPanel.requestFocusInWindow();
        if (controller.isComputerTurn()) {
            computerTimer.restart();   // вернулись: компьютер продолжает свой ход
        }
    }

    // Отменяет ход.
    private void undo() {
        if (controller.canUndo() && !computerTimer.isRunning()) {
            controller.undo();
            boardPanel.stopAnimation();
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
            recordStats();
            // Окно с итогом показываем чуть позже, чтобы успела закончиться анимация последнего хода.
            Timer later = new Timer(500, e -> showResultDialog());
            later.setRepeats(false);
            later.start();
        }
    }

    // Окно «Игра окончена» с выбором: сыграть снова или в главное меню.
    private void showResultDialog() {
        if (!controller.isGameOver()) {
            return;   // за это время успели начать новую игру или отменить ход
        }
        String[] options = {"Играть снова", "В главное меню"};
        int choice = JOptionPane.showOptionDialog(this, resultText(), "Игра окончена",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
        if (choice == 0) {
            startNewGame(lastGeeseHuman, lastFoxHuman);
        } else if (choice == 1) {
            showMenu();
        }
    }

    // Записывает итог партии в статистику (отдельно: человек против компьютера и два человека).
    // Сложность берём ту, что выбрана сейчас.
    private void recordStats() {
        if (statsRecorded) {
            return;
        }
        statsRecorded = true;
        int result = controller.getResult();
        if (lastGeeseHuman && lastFoxHuman) {         // два человека
            if (result == GameRules.RESULT_GEESE_WIN) {
                Settings.addHumanResult(Settings.HH_GEESE);
            } else if (result == GameRules.RESULT_FOX_WINS) {
                Settings.addHumanResult(Settings.HH_FOX);
            } else {
                Settings.addHumanResult(Settings.HH_DRAW);
            }
            return;
        }
        int humanWinResult = lastGeeseHuman ? GameRules.RESULT_GEESE_WIN : GameRules.RESULT_FOX_WINS;
        int kind;
        if (result == humanWinResult) {
            kind = Settings.WIN;
        } else if (result == GameRules.RESULT_DRAW) {
            kind = Settings.DRAW;
        } else {
            kind = Settings.LOSS;
        }
        int level = 1;
        for (int i = 0; i < DEPTHS.length; i++) {
            if (DEPTHS[i] == depth) {
                level = i;
            }
        }
        Settings.addResult(level, kind);
    }

    // Ставит курсор персонажа на всё окно (кроме поля, у поля свой курсор: см. BoardPanel.updateCursor).
    private void updateCursor() {
        int kind = controller.isComputerTurn() ? CursorSet.BUSY : CursorSet.NORMAL;
        setCursor(cursors.get(controller.getCursorPlayer(), kind));
        boardPanel.updateCursor();
    }

    // Ход компьютера (вызывается таймером).
    private void computerStep() {
        int movesBefore = controller.getMoveCount();
        controller.doComputerStep();
        if (controller.getMoveCount() > movesBefore) {
            boardPanel.animateLastMove();      // плавно двигаем фишку компьютера
        }
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
                + "  H — подсказка (компьютер советует ход)\n"
                + "  Esc — снять выбор\n"
                + "  Ctrl+Z — отменить ход\n"
                + "  F2 / F3 / F4 — новая игра (за гусей / за лису / два игрока)\n"
                + "  F1 — правила";
        JOptionPane.showMessageDialog(this, text, "Управление", JOptionPane.INFORMATION_MESSAGE);
    }
}
