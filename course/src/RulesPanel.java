import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

// Подробные правила игры: текст и схемы. Один и тот же текст показываем в меню и во время игры.
public class RulesPanel {
    private static final int TEXT_WIDTH = 490;   // ширина текста в пикселях

    // Собирает всю страницу правил (заголовки, текст и схемы) в одну панель.
    public static JComponent createContent(PieceImages images) {
        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setOpaque(false);
        page.setBorder(new EmptyBorder(6, 14, 10, 14));

        add(page, title("Правила игры «Лиса и гуси»"));
        add(page, text("Играют двое: один управляет лисой, другой — 17 гусями. "
                + "У каждой стороны своя цель, и победить можно только одним из способов, описанных ниже."));

        // ----- Поле и фигуры -----
        add(page, heading("Поле и фигуры"));
        add(page, text("Поле похоже на крест и состоит из 33 клеток: по углам клетки «вырезаны». "
                + "Клетки называются как на шахматной доске: буквы <b>a–g</b> слева направо и цифры <b>1–7</b> снизу вверх. "
                + "На поле одна лиса и 17 гусей."));
        add(page, text("В начале гуси стоят в нижней части креста: ряды <b>3</b> и <b>4</b> заполнены целиком (по 7 гусей) "
                + "и ещё три гуся стоят в ряду <b>2</b> (клетки c2, d2, e2). Самый нижний ряд пуст, а верхняя часть креста свободна для лисы."));

        // ----- Начало партии -----
        add(page, heading("Начало партии"));
        add(page, text("Сначала игрок-лиса выбирает, где начнёт: лису можно поставить на <b>любую свободную клетку</b>. "
                + "Если вы играете за гусей, лису ставит компьютер. После этого первыми ходят <b>гуси</b>, "
                + "дальше стороны ходят по очереди. Пропускать ход нельзя."));

        // ----- Как ходят -----
        add(page, heading("Как ходят фигуры"));
        add(page, bullets(
                "<b>Гусь</b> ходит на одну соседнюю свободную клетку: вверх, вниз, влево или вправо. "
                        + "Назад ходить тоже можно. По диагонали гуси не ходят.",
                "<b>Лиса</b> ходит на одну соседнюю свободную клетку в любую из 8 сторон, "
                        + "включая диагонали.",
                "Гуси лису не берут: они могут только окружить её."));

        // ----- Взятие -----
        add(page, heading("Как лиса берёт гусей"));
        add(page, text("Если рядом с лисой стоит гусь, а клетка <b>сразу за ним на той же линии</b> свободна, "
                + "лиса может перепрыгнуть через гуся: она встаёт на эту свободную клетку, а гусь снимается с поля. "
                + "Прыгать можно по прямой и по диагонали. Брать гуся не обязательно. "
                + "За один ход лиса делает один прыжок."));
        add(page, pair(images, new String[]{"FGx"}, new int[]{0, 0, 0, 2}, new String[]{"..F"}));
        add(page, caption("Прыжок по прямой: лиса перепрыгивает гуся и встаёт на зелёную клетку."));
        add(page, pair(images, new String[]{"F..", ".G.", "..x"}, new int[]{0, 0, 2, 2},
                new String[]{"...", "...", "..F"}));
        add(page, caption("Прыжок по диагонали работает так же."));
        add(page, text("Если за гусем стоит другая фишка или край поля, прыгнуть нельзя. "
                + "Поэтому главное для гусей: <b>за каждым гусем рядом с лисой должна быть опора</b>."));
        add(page, centered(new MiniBoard(images, new String[]{"FG."}, null),
                new MiniBoard(images, new String[]{"FGG"}, null)));
        add(page, caption("Слева гусь в опасности: за ним пусто. Справа он защищён: за ним стоит другой гусь."));

        // ----- Победа гусей -----
        add(page, heading("Как выиграть за гусей"));
        add(page, text("Нужно <b>запереть лису</b>: окружить её так, чтобы она не могла ни сделать шаг, ни прыгнуть. "
                + "Как только у лисы нет ни одного хода, гуси побеждают."));
        add(page, centered(new MiniBoard(images, new String[]{"#GFG#", "#GGG#", "GGGGG"}, null)));
        add(page, caption("Лиса в верхней клетке заперта: все соседние клетки заняты, а за гусями тоже стоят гуси."));
        add(page, text("Советы за гусей:"));
        add(page, bullets(
                "Продвигайтесь вперёд <b>плотной линией</b> и не оставляйте «дыр» за гусями.",
                "Сначала двигайте гусей с краёв и закрывайте дыры сзади, а не выдвигайте одного гуся далеко вперёд.",
                "Постепенно оттесняйте лису к верхней части креста: там ей тесно.",
                "Одним гусем можно пожертвовать, если это загонит лису в ловушку. "
                        + "Запас большой: проиграете только когда потеряете 11 гусей."));

        // ----- Победа лисы -----
        add(page, heading("Как выиграть за лису"));
        add(page, text("Нужно <b>съесть 11 гусей</b>, чтобы на поле осталось 6 или меньше. "
                + "Тогда гуси уже не смогут запереть лису, и она побеждает. "
                + "Лиса также побеждает, если у гусей не осталось ни одного хода."));
        add(page, text("Советы за лису:"));
        add(page, bullets(
                "Держитесь рядом с гусями и ищите того, за кем осталась свободная клетка.",
                "Угрожайте сразу двум гусям: защитить гуси смогут только одного.",
                "Не забирайтесь в узкие места и в углы креста: там легко оказаться запертой.",
                "Если гуси держат плотный строй, не торопитесь. Им придётся идти вперёд, и строй рано или поздно разойдётся."));

        // ----- Ничья -----
        add(page, heading("Ничья"));
        add(page, text("Если за " + GameRules.MAX_MOVES + " ходов (считаются ходы обеих сторон) никто не победил, "
                + "объявляется ничья."));

        // ----- Управление -----
        add(page, heading("Управление"));
        add(page, bullets(
                "<b>Мышь:</b> щёлкните по своей фишке, затем по зелёной точке (обычный ход) "
                        + "или по красному кольцу (прыжок со взятием). Чтобы передумать, щёлкните по другой своей фишке.",
                "<b>Клавиатура:</b> стрелки двигают синий курсор по полю, Enter или пробел выбирают фишку и делают ход, "
                        + "Esc снимает выбор.",
                "<b>Ctrl+Z</b> отменяет ход (против компьютера сразу два: ваш и его).",
                "<b>Сложность</b> компьютера: лёгкая просчитывает 3 хода вперёд, средняя — 5, сложная — 7."));
        return page;
    }

    // Показывает правила в отдельном окне (из игры). parent нужен, чтобы окно появилось по центру.
    public static void showDialog(Component parent) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, "Правила игры", java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        PieceImages images = new PieceImages();

        JPanel back = new JPanel(new BorderLayout());
        back.setBackground(new Color(0xFBF6EA));
        JScrollPane scroll = new JScrollPane(createContent(images));
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        back.add(scroll, BorderLayout.CENTER);

        MenuStyle.Button close = new MenuStyle.Button("Закрыть");
        close.setPreferredSize(new Dimension(180, 46));
        close.addActionListener(e -> dialog.dispose());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        bottom.setOpaque(false);
        bottom.add(close);
        back.add(bottom, BorderLayout.SOUTH);

        // Esc закрывает окно.
        back.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
        back.getActionMap().put("close", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });

        dialog.setContentPane(back);
        dialog.setSize(560, 640);
        dialog.setLocationRelativeTo(parent);
        dialog.getRootPane().setDefaultButton(close);
        dialog.setVisible(true);
    }

    // ---------- Маленькие помощники для сборки страницы ----------

    // Добавляет компонент на страницу выровненным по левому краю.
    private static void add(JPanel page, JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        page.add(c);
    }

    // Надпись с HTML-текстом точной ширины. Высоту считаем в тот момент, когда окно просит размер
    // (а не при создании): только тогда известны настоящие шрифты и масштаб экрана.
    static class Block extends JLabel {
        private final int blockWidth;    // ширина текста в пикселях
        private final int top;           // отступ сверху
        private final int bottom;        // отступ снизу
        private final int minHeight;     // минимальная высота (чтобы блок не «прыгал» при смене текста)

        Block(String innerHtml, int width, int top, int bottom, int minHeight) {
            super("<html>" + innerHtml + "</html>");
            this.blockWidth = width;
            this.top = top;
            this.bottom = bottom;
            this.minHeight = minHeight;
            setBorder(new EmptyBorder(top, 0, bottom, 0));
            setVerticalAlignment(javax.swing.SwingConstants.TOP);
        }

        @Override
        public Dimension getPreferredSize() {
            javax.swing.text.View view = (javax.swing.text.View) getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey);
            int height = 0;
            if (view != null) {
                view.setSize(blockWidth, 0);   // говорим тексту, какой он ширины, и спрашиваем, какой он высоты
                height = (int) Math.ceil(view.getPreferredSpan(javax.swing.text.View.Y_AXIS));
            }
            return new Dimension(blockWidth, Math.max(minHeight, height + top + bottom));
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }
    }

    // Короткая запись: надпись без минимальной высоты.
    static JLabel htmlLabel(String innerHtml, int width, int top, int bottom) {
        return new Block(innerHtml, width, top, bottom, 0);
    }

    // Надпись с минимальной высотой.
    static JLabel htmlLabel(String innerHtml, int width, int top, int bottom, int minHeight) {
        return new Block(innerHtml, width, top, bottom, minHeight);
    }

    // Крупный заголовок всей страницы.
    private static JComponent title(String s) {
        return htmlLabel("<div style='font-family:SansSerif;font-size:17pt;font-weight:bold;color:#B85A12'>" + s + "</div>",
                TEXT_WIDTH, 0, 8);
    }

    // Заголовок раздела.
    private static JComponent heading(String s) {
        return htmlLabel("<div style='font-family:SansSerif;font-size:14pt;font-weight:bold;color:#3B342B'>" + s + "</div>",
                TEXT_WIDTH, 14, 4);
    }

    // Обычный абзац (можно использовать теги b и i).
    private static JComponent text(String html) {
        return htmlLabel("<div style='font-family:SansSerif;font-size:12pt;color:#3B342B'>" + html + "</div>",
                TEXT_WIDTH, 2, 4);
    }

    // Подпись под схемой (мелкий курсив по центру).
    private static JComponent caption(String html) {
        return htmlLabel("<div style='font-family:SansSerif;font-size:11pt;color:#6B6256;text-align:center'><i>" + html + "</i></div>",
                TEXT_WIDTH, 0, 6);
    }

    // Список с точками.
    private static JComponent bullets(String... items) {
        StringBuilder sb = new StringBuilder("<ul style='margin-top:0;margin-bottom:0'>");
        for (String item : items) {
            sb.append("<li style='margin-bottom:3px'>").append(item).append("</li>");
        }
        sb.append("</ul>");
        return text(sb.toString());
    }

    // Схема «до» -> «после» в одной строке.
    private static JComponent pair(PieceImages images, String[] before, int[] arrow, String[] after) {
        JLabel next = new JLabel("→");
        next.setFont(MenuStyle.font(java.awt.Font.BOLD, 30));
        next.setForeground(MenuStyle.ORANGE_DOWN);
        return centered(new MiniBoard(images, before, arrow), next, new MiniBoard(images, after, null));
    }

    // Выстраивает компоненты в ряд по центру.
    private static JComponent centered(JComponent... parts) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 2));
        row.setOpaque(false);
        for (JComponent part : parts) {
            row.add(part);
        }
        row.setMaximumSize(new Dimension(TEXT_WIDTH + 20, row.getPreferredSize().height + 4));
        return row;
    }
}
