import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Cursor;
import java.awt.Image;
import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JToggleButton;

// Общий стиль меню: цвета, шрифты и две кнопки (обычная и переключатель), которые рисуем сами.
public class MenuStyle {
    public static final Color ORANGE = new Color(0xE0701B);        // цвет лисы
    public static final Color ORANGE_HOVER = new Color(0xF08A3A);  // кнопка под мышью
    public static final Color ORANGE_DOWN = new Color(0xB85A12);   // нажатая кнопка
    public static final Color DARK = new Color(0x4A4A4A);          // цвет гуся
    public static final Color TEXT = new Color(0x3B342B);          // цвет обычного текста
    public static final Color CARD = new Color(251, 246, 234, 226);  // полупрозрачная карточка меню
    public static final Color FOCUS = new Color(0x1E5FD0);         // рамка выбранной кнопки (клавиатура)

    // Шрифт кнопок и обычного текста.
    public static Font font(int style, int size) {
        return new Font(Font.SANS_SERIF, style, size);
    }

    // Обычная кнопка меню (оранжевая).
    public static class Button extends JButton {
        public Button(String text) {
            super(text);
            setup(this);
        }

        @Override
        protected void paintComponent(Graphics g) {
            paintButton(g, this, false);
        }
    }

    // Переключатель (например, «Гуси» / «Лиса»). Выбранный закрашен тёмным.
    public static class Toggle extends JToggleButton {
        public Toggle(String text, Icon icon) {
            super(text, icon);
            setup(this);
        }

        @Override
        protected void paintComponent(Graphics g) {
            paintButton(g, this, true);
        }
    }

    // Общие настройки обеих кнопок: убираем стандартное оформление, рисуем всё сами.
    private static void setup(AbstractButton b) {
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setRolloverEnabled(true);
        b.setFont(font(Font.BOLD, 18));
        b.setForeground(Color.WHITE);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    // Рисует кнопку: скруглённый прямоугольник, значок и текст по центру.
    private static void paintButton(Graphics g0, AbstractButton b, boolean toggle) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        ButtonModel model = b.getModel();
        int w = b.getWidth();
        int h = b.getHeight();

        Color fill;
        Color textColor;
        Color border = null;
        if (toggle) {
            if (b.isSelected()) {
                fill = DARK;
                textColor = Color.WHITE;
            } else {
                fill = model.isRollover() ? Color.WHITE : new Color(255, 255, 255, 190);
                textColor = TEXT;
                border = new Color(0xB9AE9C);
            }
        } else {
            fill = model.isPressed() ? ORANGE_DOWN : (model.isRollover() ? ORANGE_HOVER : ORANGE);
            textColor = Color.WHITE;
        }

        g.setColor(new Color(0, 0, 0, 40));                    // лёгкая тень
        g.fillRoundRect(2, 3, w - 4, h - 4, 18, 18);
        g.setColor(fill);
        g.fillRoundRect(0, 0, w - 4, h - 4, 18, 18);
        if (border != null) {
            g.setColor(border);
            g.setStroke(new BasicStroke(1.5f));
            g.drawRoundRect(0, 0, w - 5, h - 5, 18, 18);
        }
        if (b.hasFocus()) {                                    // рамка при выборе с клавиатуры
            g.setColor(FOCUS);
            g.setStroke(new BasicStroke(3f));
            g.drawRoundRect(2, 2, w - 9, h - 9, 14, 14);
        }

        // Значок (если есть) и текст, вместе по центру.
        g.setFont(b.getFont());
        FontMetrics fm = g.getFontMetrics();
        Icon icon = b.getIcon();
        int iconWidth = icon == null ? 0 : icon.getIconWidth() + 8;
        int textWidth = fm.stringWidth(b.getText());
        int x = (w - 4 - iconWidth - textWidth) / 2;
        if (icon != null) {
            icon.paintIcon(b, g, x, (h - 4 - icon.getIconHeight()) / 2);
        }
        g.setColor(textColor);
        g.drawString(b.getText(), x + iconWidth, (h - 4 - fm.getHeight()) / 2 + fm.getAscent());
        g.dispose();
    }

    // Уменьшает картинку до нужной ширины, сохраняя пропорции. Уменьшаем по шагам, так край гладкий.
    public static Image scaleToWidth(java.awt.image.BufferedImage source, int width) {
        int height = Math.max(1, source.getHeight() * width / source.getWidth());
        java.awt.image.BufferedImage current = source;
        while (current.getWidth() / 2 >= width) {
            current = resize(current, current.getWidth() / 2, current.getHeight() / 2);
        }
        return resize(current, width, height);
    }

    // Один шаг изменения размера.
    private static java.awt.image.BufferedImage resize(java.awt.image.BufferedImage src, int w, int h) {
        java.awt.image.BufferedImage result = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(src, 0, 0, w, h, null);
        g.dispose();
        return result;
    }
}
