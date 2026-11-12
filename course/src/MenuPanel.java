import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

// Стартовое меню игры. Три страницы:
//   main     - главная: против компьютера, против человека, правила, выход;
//   computer - настройки игры против компьютера: за кого играем и какая сложность;
//   rules    - подробные правила.
// Управление: мышь, а также стрелки (переход между кнопками), Enter (нажать), Esc (назад).
public class MenuPanel extends JPanel {
    // Что меню сообщает окну игры.
    public interface Listener {
        // Начать партию. geeseHuman / foxHuman: true, если за эту сторону играет человек.
        void onStart(boolean geeseHuman, boolean foxHuman, int depth);

        // Выйти из программы.
        void onExit();
    }

    private static final String PAGE_MAIN = "main";
    private static final String PAGE_COMPUTER = "computer";
    private static final String PAGE_RULES = "rules";

    private static final int[] DEPTHS = {3, 5, 7};   // глубина поиска для лёгкой, средней, сложной
    private static final String[] DEPTH_NAMES = {"Лёгкая", "Средняя", "Сложная"};
    private static final String[] DEPTH_HINTS = {
            "Компьютер просчитывает 3 хода вперёд. Хорошо, чтобы освоить правила.",
            "Компьютер просчитывает 5 ходов вперёд. Обычная партия.",
            "Компьютер просчитывает 7 ходов вперёд. Играет внимательно, ошибок почти не прощает."};

    private static final String[] SIDE_HINTS = {
            "Вы ходите первыми и стараетесь запереть лису. Лису поставит компьютер.",
            "Вы выбираете стартовую клетку лисы и стараетесь съесть 11 гусей. Гуси (компьютер) ходят первыми."};

    private final Listener listener;
    private final PieceImages images = new PieceImages();
    private final BackgroundImage background = new BackgroundImage();
    private final CardLayout cards = new CardLayout();
    private final JPanel pages = new JPanel(cards);   // страницы меню лежат друг на друге
    private String currentPage = PAGE_MAIN;

    // Настройки игры против компьютера.
    private boolean playAsGeese = true;
    private int depthIndex = 1;                       // по умолчанию средняя
    private JLabel depthHint;                         // подсказка под сложностью
    private JLabel sideHint;                          // подсказка под выбором стороны

    // Кнопки, на которые ставим фокус при показе страницы.
    private MenuStyle.Button mainFirstButton;
    private MenuStyle.Button computerStartButton;
    private MenuStyle.Button rulesBackButton;
    private JScrollBar rulesScrollBar;

    public MenuPanel(Listener listener) {
        this.listener = listener;
        setLayout(new GridBagLayout());          // карточка по центру окна
        setPreferredSize(new Dimension(640, 676));

        // Карточка: светлый скруглённый прямоугольник с текстом и кнопками.
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(MenuStyle.CARD);
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g.setColor(new Color(0xB9AE9C));
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
                g.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(14, 18, 14, 18));
        card.setPreferredSize(new Dimension(580, 620));

        pages.setOpaque(false);
        pages.add(createMainPage(), PAGE_MAIN);
        pages.add(createComputerPage(), PAGE_COMPUTER);
        pages.add(createRulesPage(), PAGE_RULES);
        card.add(pages, BorderLayout.CENTER);
        add(card);

        // Esc: из вложенной страницы назад в главное меню.
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "back");
        getActionMap().put("back", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!PAGE_MAIN.equals(currentPage)) {
                    showPage(PAGE_MAIN);
                }
            }
        });
        showPage(PAGE_MAIN);
    }

    // ---------- Фон ----------
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        BufferedImage image = background.get(Math.max(getWidth(), getHeight()));
        if (image != null) {
            g.drawImage(image, (getWidth() - image.getWidth()) / 2, (getHeight() - image.getHeight()) / 2, null);
        } else {
            g.setColor(new Color(0xBFE3A5));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    // ---------- Переключение страниц ----------

    // Показывает страницу и ставит фокус на главную кнопку страницы.
    public void showPage(String name) {
        currentPage = name;
        cards.show(pages, name);
        SwingUtilities.invokeLater(() -> {
            if (PAGE_MAIN.equals(name) && mainFirstButton != null) {
                mainFirstButton.requestFocusInWindow();
            } else if (PAGE_COMPUTER.equals(name) && computerStartButton != null) {
                computerStartButton.requestFocusInWindow();
            } else if (PAGE_RULES.equals(name) && rulesBackButton != null) {
                rulesBackButton.requestFocusInWindow();
            }
        });
    }

    // Вызывается окном, когда меню снова показывается: открываем главную страницу.
    public void reset() {
        showPage(PAGE_MAIN);
    }

    // ---------- Главная страница ----------
    private JComponent createMainPage() {
        JPanel page = column();

        JLabel title = new JLabel("Лиса и гуси");
        title.setFont(new Font(Font.SERIF, Font.BOLD, 44));
        title.setForeground(MenuStyle.ORANGE_DOWN);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        page.add(title);
        page.add(Box.createVerticalStrut(4));

        BufferedImage picture = PieceImages.load("menu_picture.png");
        if (picture != null) {
            JLabel pictureLabel = new JLabel(new ImageIcon(MenuStyle.scaleToWidth(picture, 340)));
            pictureLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            page.add(pictureLabel);
        }
        page.add(Box.createVerticalStrut(12));

        mainFirstButton = bigButton("Играть против компьютера", () -> showPage(PAGE_COMPUTER));
        MenuStyle.Button human = bigButton("Играть против человека", () -> listener.onStart(true, true, DEPTHS[depthIndex]));
        MenuStyle.Button rules = bigButton("Правила игры", () -> showPage(PAGE_RULES));
        MenuStyle.Button exit = bigButton("Выход", listener::onExit);
        // Ряд из двух кнопок: статистика и смена темы (день, вечер, зима).
        MenuStyle.Button stats = new MenuStyle.Button("Статистика");
        MenuStyle.Button theme = new MenuStyle.Button("Тема: " + Settings.THEME_NAMES[Settings.getTheme()]);
        stats.addActionListener(e -> StatsDialog.show(this));
        theme.addActionListener(e -> {
            Settings.setTheme((Settings.getTheme() + 1) % Settings.THEME_NAMES.length);
            theme.setText("Тема: " + Settings.THEME_NAMES[Settings.getTheme()]);
            repaint();   // перерисовываем меню с новым фоном
        });
        JPanel smallRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        smallRow.setOpaque(false);
        for (MenuStyle.Button b : new MenuStyle.Button[]{stats, theme}) {
            b.setPreferredSize(new Dimension(185, 50));
            smallRow.add(b);
        }
        smallRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        smallRow.setMaximumSize(new Dimension(380, 50));

        MenuStyle.Button[] buttons = {mainFirstButton, human, rules};
        for (MenuStyle.Button b : buttons) {
            page.add(b);
            page.add(Box.createVerticalStrut(10));
            installKeys(b);
        }
        page.add(smallRow);
        page.add(Box.createVerticalStrut(10));
        installKeys(stats);
        installKeys(theme);
        page.add(exit);
        page.add(Box.createVerticalStrut(10));
        installKeys(exit);

        JLabel tip = new JLabel("Стрелки и Enter — управление с клавиатуры");
        tip.setFont(MenuStyle.font(Font.ITALIC, 12));
        tip.setForeground(new Color(0x6B6256));
        tip.setAlignmentX(Component.CENTER_ALIGNMENT);
        page.add(tip);
        return page;
    }

    // ---------- Страница настроек игры против компьютера ----------
    private JComponent createComputerPage() {
        JPanel page = column();
        page.add(Box.createVerticalGlue());
        page.add(pageTitle("Игра против компьютера"));
        page.add(Box.createVerticalStrut(30));

        // За кого играем.
        page.add(sectionLabel("Вы играете за:"));
        page.add(Box.createVerticalStrut(8));
        sideHint = hint(SIDE_HINTS[0]);
        ButtonGroup sideGroup = new ButtonGroup();
        MenuStyle.Toggle geese = new MenuStyle.Toggle("Гуси", iconFrom(images.getGoose(30)));
        MenuStyle.Toggle fox = new MenuStyle.Toggle("Лиса", iconFrom(images.getFox(30)));
        geese.setSelected(true);
        sideGroup.add(geese);
        sideGroup.add(fox);
        geese.addActionListener(e -> {
            playAsGeese = true;
            setHint(sideHint, SIDE_HINTS[0]);
        });
        fox.addActionListener(e -> {
            playAsGeese = false;
            setHint(sideHint, SIDE_HINTS[1]);
        });
        page.add(buttonRow(geese, fox));
        page.add(Box.createVerticalStrut(6));
        page.add(sideHint);

        page.add(Box.createVerticalStrut(22));

        // Сложность.
        page.add(sectionLabel("Сложность:"));
        page.add(Box.createVerticalStrut(8));
        depthHint = hint(DEPTH_HINTS[depthIndex]);
        ButtonGroup depthGroup = new ButtonGroup();
        MenuStyle.Toggle[] levels = new MenuStyle.Toggle[3];
        for (int i = 0; i < 3; i++) {
            final int index = i;
            levels[i] = new MenuStyle.Toggle(DEPTH_NAMES[i], null);
            levels[i].setFont(MenuStyle.font(Font.BOLD, 16));
            depthGroup.add(levels[i]);
            levels[i].addActionListener(e -> {
                depthIndex = index;
                setHint(depthHint, DEPTH_HINTS[index]);
            });
        }
        levels[depthIndex].setSelected(true);
        page.add(buttonRow(levels));
        page.add(Box.createVerticalStrut(6));
        page.add(depthHint);

        page.add(Box.createVerticalStrut(30));
        computerStartButton = bigButton("Начать игру", () ->
                listener.onStart(playAsGeese, !playAsGeese, DEPTHS[depthIndex]));
        MenuStyle.Button back = bigButton("Назад", () -> showPage(PAGE_MAIN));
        page.add(computerStartButton);
        page.add(Box.createVerticalStrut(10));
        page.add(back);
        page.add(Box.createVerticalGlue());

        installKeys(geese);
        installKeys(fox);
        for (MenuStyle.Toggle level : levels) {
            installKeys(level);
        }
        installKeys(computerStartButton);
        installKeys(back);
        return page;
    }

    // ---------- Страница правил ----------
    private JComponent createRulesPage() {
        JPanel page = new JPanel(new BorderLayout());
        page.setOpaque(false);

        JScrollPane scroll = new JScrollPane(RulesPanel.createContent(images));
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        rulesScrollBar = scroll.getVerticalScrollBar();
        page.add(scroll, BorderLayout.CENTER);

        rulesBackButton = bigButton("Назад", () -> showPage(PAGE_MAIN));
        rulesBackButton.setMaximumSize(new Dimension(260, 48));
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        bottom.setOpaque(false);
        bottom.add(rulesBackButton);
        page.add(bottom, BorderLayout.SOUTH);

        // На странице правил стрелки и PageUp/PageDown прокручивают текст.
        bindScroll(page, KeyEvent.VK_UP, -40);
        bindScroll(page, KeyEvent.VK_DOWN, 40);
        bindScroll(page, KeyEvent.VK_PAGE_UP, -300);
        bindScroll(page, KeyEvent.VK_PAGE_DOWN, 300);
        bindKey(rulesBackButton, KeyEvent.VK_ENTER, rulesBackButton::doClick);
        return page;
    }

    // Привязывает клавишу к прокрутке текста правил.
    private void bindScroll(JComponent where, int key, int delta) {
        String name = "scroll" + key;
        where.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key, 0), name);
        where.getActionMap().put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rulesScrollBar.setValue(rulesScrollBar.getValue() + delta);
            }
        });
    }

    // ---------- Помощники для сборки страниц ----------

    // Пустая страница: элементы друг под другом по центру.
    private JPanel column() {
        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setOpaque(false);
        return page;
    }

    // Большая кнопка на всю ширину страницы (до 380 пикселей).
    private MenuStyle.Button bigButton(String text, Runnable action) {
        MenuStyle.Button b = new MenuStyle.Button(text);
        b.setPreferredSize(new Dimension(380, 50));
        b.setMaximumSize(new Dimension(380, 50));
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.addActionListener(e -> action.run());
        return b;
    }

    // Заголовок страницы.
    private JLabel pageTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(Font.SERIF, Font.BOLD, 30));
        label.setForeground(MenuStyle.ORANGE_DOWN);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    // Подпись перед группой кнопок.
    private JLabel sectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(MenuStyle.font(Font.BOLD, 18));
        label.setForeground(MenuStyle.TEXT);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    // Мелкая подсказка под кнопками (текст по центру, до двух строк; высота всегда одинаковая,
    // чтобы кнопки не прыгали при смене текста).
    private JLabel hint(String text) {
        JLabel label = RulesPanel.htmlLabel(hintHtml(text), 480, 0, 0, 40);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    // Меняет текст подсказки.
    private void setHint(JLabel label, String text) {
        label.setText("<html>" + hintHtml(text) + "</html>");
    }

    private static String hintHtml(String text) {
        return "<div style='font-family:SansSerif;font-size:11pt;font-style:italic;color:#6B6256;text-align:center'>"
                + text + "</div>";
    }

    // Ряд кнопок-переключателей одинакового размера.
    private JComponent buttonRow(MenuStyle.Toggle... toggles) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        row.setOpaque(false);
        for (MenuStyle.Toggle t : toggles) {
            t.setPreferredSize(new Dimension(toggles.length == 2 ? 160 : 120, 46));
            row.add(t);
        }
        row.setAlignmentX(Component.CENTER_ALIGNMENT);
        row.setMaximumSize(new Dimension(500, 50));
        return row;
    }

    // Делает значок из картинки (или null, если картинки нет).
    private static ImageIcon iconFrom(BufferedImage image) {
        return image == null ? null : new ImageIcon(image);
    }

    // ---------- Клавиатура ----------

    // Стрелки вверх и влево: к предыдущей кнопке, вниз и вправо: к следующей, Enter: нажать.
    private void installKeys(AbstractButton b) {
        bindKey(b, KeyEvent.VK_ENTER, b::doClick);
        bindKey(b, KeyEvent.VK_UP, b::transferFocusBackward);
        bindKey(b, KeyEvent.VK_LEFT, b::transferFocusBackward);
        bindKey(b, KeyEvent.VK_DOWN, b::transferFocus);
        bindKey(b, KeyEvent.VK_RIGHT, b::transferFocus);
    }

    // Привязывает клавишу к действию на кнопке.
    private void bindKey(JComponent where, int key, Runnable action) {
        String name = "menuKey" + key;
        where.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(key, 0), name);
        where.getActionMap().put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }
}
