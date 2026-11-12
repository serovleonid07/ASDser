package frontend;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

// Страница статистики. Две вкладки: «Человек — Бот» (по сложностям) и «Человек — Человек».
// Это обычная панель внутри окна (не отдельное окно): её кладут в главное меню и в игру.
// Результаты рисуем сами: цветные полоски показывают долю побед, ничьих и поражений.
public class StatsPanel extends JPanel {
    private static final Color COLOR_WIN = new Color(0x2E9E4F);     // победы (зелёный)
    private static final Color COLOR_DRAW = new Color(0xB0A898);    // ничьи (серый)
    private static final Color COLOR_LOSS = new Color(0xC62828);    // поражения (красный)
    private static final Color COLOR_TRACK = new Color(0xE3DCCB);   // пустая полоска
    private static final String[] LEVEL_NAMES = {"Лёгкая", "Средняя", "Сложная"};

    private final PieceImages images = new PieceImages();
    private final Chart chart = new Chart();
    private final MenuStyle.Toggle botTab;
    private final MenuStyle.Toggle humanTab;
    private final MenuStyle.Button resetButton;
    private final MenuStyle.Button backButton;
    private boolean resetAsked = false;      // нажали ли «Сбросить» один раз (ждём подтверждения)
    private boolean showHuman = false;       // какая вкладка открыта

    // onBack: что сделать по кнопке «Назад».
    public StatsPanel(Runnable onBack) {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(Box.createVerticalGlue());
        JLabel title = new JLabel("Статистика");
        title.setFont(new Font(Font.SERIF, Font.BOLD, 30));
        title.setForeground(MenuStyle.ORANGE_DOWN);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(title);
        add(Box.createVerticalStrut(14));

        // Вкладки.
        botTab = new MenuStyle.Toggle("Человек — Бот", null);
        humanTab = new MenuStyle.Toggle("Человек — Человек", null);
        ButtonGroup group = new ButtonGroup();
        group.add(botTab);
        group.add(humanTab);
        botTab.setSelected(true);
        for (MenuStyle.Toggle tab : new MenuStyle.Toggle[]{botTab, humanTab}) {
            tab.setFont(MenuStyle.font(Font.BOLD, 16));
            tab.setPreferredSize(new Dimension(212, 44));
        }
        botTab.addActionListener(e -> selectTab(false));
        humanTab.addActionListener(e -> selectTab(true));
        JPanel tabs = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        tabs.setOpaque(false);
        tabs.add(botTab);
        tabs.add(humanTab);
        tabs.setMaximumSize(new Dimension(500, 48));
        tabs.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(tabs);
        add(Box.createVerticalStrut(10));

        chart.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(chart);
        add(Box.createVerticalStrut(10));

        // Кнопки внизу (такой же ширины, как кнопки меню).
        resetButton = bigButton("Сбросить статистику", this::onResetPressed);
        backButton = bigButton("Назад", onBack);
        add(resetButton);
        add(Box.createVerticalStrut(8));
        add(backButton);
        add(Box.createVerticalGlue());

        // Клавиатура: стрелки ходят по кнопкам, Enter нажимает.
        for (javax.swing.AbstractButton b : new javax.swing.AbstractButton[]{botTab, humanTab, resetButton, backButton}) {
            bindKey(b, KeyEvent.VK_ENTER, b::doClick);
            bindKey(b, KeyEvent.VK_UP, b::transferFocusBackward);
            bindKey(b, KeyEvent.VK_LEFT, b::transferFocusBackward);
            bindKey(b, KeyEvent.VK_DOWN, b::transferFocus);
            bindKey(b, KeyEvent.VK_RIGHT, b::transferFocus);
        }
    }

    // Вызывается, когда страницу показывают: обновляем числа и сбрасываем подтверждение.
    public void refresh() {
        resetAsked = false;
        resetButton.setText("Сбросить статистику");
        chart.repaint();
        (showHuman ? humanTab : botTab).requestFocusInWindow();
    }

    // Кнопка, на которую ставят фокус при показе страницы.
    public JComponent getFocusButton() {
        return backButton;
    }

    // Переключает вкладку.
    private void selectTab(boolean human) {
        showHuman = human;
        chart.repaint();
    }

    // «Сбросить»: первое нажатие просит подтверждения, второе обнуляет статистику.
    private void onResetPressed() {
        if (!resetAsked) {
            resetAsked = true;
            resetButton.setText("Точно сбросить? Нажмите ещё раз");
            return;
        }
        Settings.resetStats();
        resetAsked = false;
        resetButton.setText("Сбросить статистику");
        chart.repaint();
    }

    // Большая кнопка той же ширины, что и в меню.
    private MenuStyle.Button bigButton(String text, Runnable action) {
        MenuStyle.Button b = new MenuStyle.Button(text);
        b.setPreferredSize(new Dimension(380, 46));
        b.setMaximumSize(new Dimension(380, 46));
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.addActionListener(e -> action.run());
        return b;
    }

    // Привязывает клавишу к действию на кнопке.
    private void bindKey(JComponent where, int key, Runnable action) {
        String name = "statsKey" + key;
        where.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(key, 0), name);
        where.getActionMap().put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }

    // ---------- Рисование ----------

    // Область с числами и полосками. Рисует то, что выбрано на вкладке.
    private class Chart extends JComponent {
        Chart() {
            setPreferredSize(new Dimension(520, 290));
            setMaximumSize(new Dimension(520, 290));
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            // Светлая карточка под числами.
            g.setColor(new Color(255, 255, 255, 150));
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
            if (showHuman) {
                paintHuman(g);
            } else {
                paintBot(g);
            }
            g.dispose();
        }

        // Вкладка «Человек — Бот»: три строки (по сложностям).
        private void paintBot(Graphics2D g) {
            int left = 22;
            int width = getWidth() - 2 * left;
            drawLegend(g, left, 20, new String[]{"Победы", "Ничьи", "Поражения"},
                    new Color[]{COLOR_WIN, COLOR_DRAW, COLOR_LOSS});
            for (int level = 0; level < Settings.LEVELS; level++) {
                int wins = Settings.getCount(level, Settings.WIN);
                int draws = Settings.getCount(level, Settings.DRAW);
                int losses = Settings.getCount(level, Settings.LOSS);
                int total = wins + draws + losses;
                int top = 52 + level * 78;

                g.setColor(MenuStyle.TEXT);
                g.setFont(MenuStyle.font(Font.BOLD, 17));
                g.drawString(LEVEL_NAMES[level], left, top + 14);
                String info = total == 0 ? "партий ещё нет"
                        : wins + " / " + draws + " / " + losses + "   (всего " + total + ", побед "
                        + (100 * wins / total) + "%)";
                g.setFont(MenuStyle.font(Font.PLAIN, 13));
                g.setColor(new Color(0x6B6256));
                FontMetrics fm = g.getFontMetrics();
                g.drawString(info, left + width - fm.stringWidth(info), top + 14);

                drawBar(g, left, top + 24, width, 20,
                        new int[]{wins, draws, losses}, new Color[]{COLOR_WIN, COLOR_DRAW, COLOR_LOSS});
            }
        }

        // Вкладка «Человек — Человек»: гуси против лисы.
        private void paintHuman(Graphics2D g) {
            int left = 22;
            int width = getWidth() - 2 * left;
            int geese = Settings.getHumanCount(Settings.HH_GEESE);
            int fox = Settings.getHumanCount(Settings.HH_FOX);
            int draws = Settings.getHumanCount(Settings.HH_DRAW);
            int total = geese + fox + draws;

            // Слева гуси, справа лиса: картинка, число побед и подпись.
            BufferedImage gooseIcon = images.getGoose(72);
            BufferedImage foxIcon = images.getFox(72);
            int iconTop = 22;
            if (gooseIcon != null) {
                g.drawImage(gooseIcon, left + 10, iconTop, null);
            }
            if (foxIcon != null) {
                g.drawImage(foxIcon, left + width - 10 - 72, iconTop, null);
            }
            g.setFont(new Font(Font.SERIF, Font.BOLD, 44));
            FontMetrics big = g.getFontMetrics();
            g.setColor(MenuStyle.DARK);
            g.drawString(String.valueOf(geese), left + 10 + 72 + 16, iconTop + 52);
            g.setColor(MenuStyle.ORANGE);
            String foxText = String.valueOf(fox);
            g.drawString(foxText, left + width - 10 - 72 - 16 - big.stringWidth(foxText), iconTop + 52);

            g.setFont(MenuStyle.font(Font.PLAIN, 14));
            g.setColor(new Color(0x6B6256));
            FontMetrics fm = g.getFontMetrics();
            String leftText = "побед гусей";
            String rightText = "побед лисы";
            g.drawString(leftText, left + 10 + 72 + 16, iconTop + 72);
            g.drawString(rightText, left + width - 10 - 72 - 16 - fm.stringWidth(rightText), iconTop + 72);

            // Общая полоска: гуси | ничьи | лиса.
            drawBar(g, left, 138, width, 24,
                    new int[]{geese, draws, fox}, new Color[]{MenuStyle.DARK, COLOR_DRAW, MenuStyle.ORANGE});
            drawLegend(g, left, 182, new String[]{"Гуси", "Ничьи", "Лиса"},
                    new Color[]{MenuStyle.DARK, COLOR_DRAW, MenuStyle.ORANGE});

            // Итог.
            g.setColor(MenuStyle.TEXT);
            g.setFont(MenuStyle.font(Font.BOLD, 17));
            String summary = total == 0 ? "Партий двух игроков ещё нет"
                    : "Сыграно партий: " + total + "   |   ничьих: " + draws;
            FontMetrics sm = g.getFontMetrics();
            g.drawString(summary, (getWidth() - sm.stringWidth(summary)) / 2, 244);
        }

        // Подписи цветов в одну строку (цветной квадратик и текст).
        private void drawLegend(Graphics2D g, int x, int y, String[] names, Color[] colors) {
            g.setFont(MenuStyle.font(Font.PLAIN, 13));
            FontMetrics fm = g.getFontMetrics();
            for (int i = 0; i < names.length; i++) {
                g.setColor(colors[i]);
                g.fillRoundRect(x, y - 10, 12, 12, 4, 4);
                g.setColor(MenuStyle.TEXT);
                g.drawString(names[i], x + 18, y);
                x += 18 + fm.stringWidth(names[i]) + 22;
            }
        }

        // Полоска из цветных частей. Размер части зависит от числа. Если всё нули, рисуем пустую полоску.
        private void drawBar(Graphics2D g, int x, int y, int w, int h, int[] values, Color[] colors) {
            int sum = 0;
            for (int v : values) {
                sum += v;
            }
            RoundRectangle2D shape = new RoundRectangle2D.Float(x, y, w, h, h, h);
            g.setColor(COLOR_TRACK);
            g.fill(shape);
            if (sum > 0) {
                java.awt.Shape oldClip = g.getClip();
                g.clip(shape);                       // части не выходят за скруглённые края
                int pos = x;
                for (int i = 0; i < values.length; i++) {
                    int part = i == values.length - 1 ? x + w - pos : Math.round((float) w * values[i] / sum);
                    g.setColor(colors[i]);
                    g.fillRect(pos, y, part, h);
                    pos += part;
                }
                g.setClip(oldClip);
            }
            g.setColor(new Color(0xB9AE9C));
            g.setStroke(new BasicStroke(1.2f));
            g.draw(shape);
        }
    }
}
