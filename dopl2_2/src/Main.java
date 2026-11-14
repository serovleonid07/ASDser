import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/*
 * Доп. лабораторная работа № 2. Рекурсия и фракталы.
 * Задача 2. Рекурсивное рисование ёлки.
 * Ёлка рисуется в окне. Управление:
 *   колесо мыши — приближение и отдаление;
 *   стрелки     — сдвиг картинки;
 *   + и -  (или PageUp и PageDown) — глубина рекурсии;
 *   R           — вернуть всё как было.
 */
public class Main {
    // Точка входа программы: создаёт окно.
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Лаб. 2. Фракталы. Ёлка");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new TreePanel());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    // Панель, на которой рисуется ёлка. Она же слушает клавиши и колесо мыши.
    static class TreePanel extends JPanel {
        private static final int START_DEPTH = 6;   // начальная глубина рекурсии
        private static final int MAX_DEPTH = 9;     // больше рисовать слишком долго
        private static final int MOVE_STEP = 30;    // на сколько пикселей двигают стрелки

        private int depth = START_DEPTH;  // глубина рекурсии
        private double zoom = 1.0;        // масштаб (1 — обычный)
        private double offsetX = 0;       // сдвиг картинки по горизонтали
        private double offsetY = 0;       // сдвиг картинки по вертикали

        // Настраивает панель: размер, цвет фона, реакцию на клавиши и колесо.
        TreePanel() {
            setPreferredSize(new Dimension(900, 750));
            setBackground(new Color(160, 208, 150));
            setFocusable(true);  // без этого панель не получит нажатия клавиш

            addKeyListener(new KeyAdapter() {
                // Вызывается при каждом нажатии клавиши.
                @Override
                public void keyPressed(KeyEvent event) {
                    handleKey(event.getKeyCode());
                }
            });

            addMouseWheelListener(new MouseWheelListener() {
                // Вызывается при вращении колеса. Вперёд — приближаем, назад — отдаляем.
                @Override
                public void mouseWheelMoved(MouseWheelEvent event) {
                    double factor = Math.pow(1.1, -event.getPreciseWheelRotation());
                    zoom = zoom * factor;
                    repaint();
                }
            });
        }

        // Меняет состояние (сдвиг, глубина) по коду нажатой клавиши и перерисовывает окно.
        private void handleKey(int key) {
            if (key == KeyEvent.VK_LEFT) {
                offsetX = offsetX + MOVE_STEP;   // картинка едет влево... вправо на экране
            } else if (key == KeyEvent.VK_RIGHT) {
                offsetX = offsetX - MOVE_STEP;
            } else if (key == KeyEvent.VK_UP) {
                offsetY = offsetY + MOVE_STEP;
            } else if (key == KeyEvent.VK_DOWN) {
                offsetY = offsetY - MOVE_STEP;
            } else if (key == KeyEvent.VK_PLUS || key == KeyEvent.VK_EQUALS
                    || key == KeyEvent.VK_ADD || key == KeyEvent.VK_PAGE_UP) {
                if (depth < MAX_DEPTH) {
                    depth++;
                }
            } else if (key == KeyEvent.VK_MINUS || key == KeyEvent.VK_SUBTRACT
                    || key == KeyEvent.VK_PAGE_DOWN) {
                if (depth > 1) {
                    depth--;
                }
            } else if (key == KeyEvent.VK_R) {
                depth = START_DEPTH;
                zoom = 1.0;
                offsetX = 0;
                offsetY = 0;
            }
            repaint();
        }

        // Рисует всё содержимое окна. Вызывается автоматически при repaint().
        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Подсказка в углу (рисуется без масштаба).
            g.setColor(new Color(20, 60, 20));
            g.setFont(new Font("SansSerif", Font.PLAIN, 14));
            g.drawString("Глубина: " + depth + "   Масштаб: " + Math.round(zoom * 100) + "%", 12, 22);
            g.drawString("Колесо мыши — масштаб, стрелки — сдвиг, +/- — глубина, R — сброс", 12, 42);

            // Основание ствола ставим внизу по центру, затем применяем сдвиг и масштаб.
            g.translate(getWidth() / 2.0 + offsetX, getHeight() * 0.95 + offsetY);
            g.scale(zoom, zoom);

            g.setColor(new Color(10, 100, 20));
            // Рисуем ствол: он растёт вверх (угол 90 градусов) из точки (0, 0).
            FirTree.drawBranch(g, 0, 0, 90, 520, depth, true);
        }
    }
}
