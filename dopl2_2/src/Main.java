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
 * Ёлка рисуется в окне. Глубина рекурсии меняется так:
 *   стрелки вверх и вправо, колесо мыши вперёд  — глубина больше;
 *   стрелки вниз и влево,   колесо мыши назад   — глубина меньше;
 *   R — вернуть начальную глубину.
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

        private int depth = START_DEPTH;  // глубина рекурсии

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
                // Вызывается при вращении колеса. Вперёд (от себя) — глубина больше, назад — меньше.
                @Override
                public void mouseWheelMoved(MouseWheelEvent event) {
                    if (event.getWheelRotation() < 0) {
                        changeDepth(1);
                    } else if (event.getWheelRotation() > 0) {
                        changeDepth(-1);
                    }
                    repaint();
                }
            });
        }

        // Меняет глубину на delta (+1 или -1), но не выходит за границы от 1 до MAX_DEPTH.
        private void changeDepth(int delta) {
            int newDepth = depth + delta;
            if (newDepth >= 1 && newDepth <= MAX_DEPTH) {
                depth = newDepth;
            }
        }

        // Меняет глубину по коду нажатой клавиши и перерисовывает окно.
        private void handleKey(int key) {
            if (key == KeyEvent.VK_UP || key == KeyEvent.VK_RIGHT) {
                changeDepth(1);
            } else if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_LEFT) {
                changeDepth(-1);
            } else if (key == KeyEvent.VK_R) {
                depth = START_DEPTH;
            }
            repaint();
        }

        // Рисует всё содержимое окна. Вызывается автоматически при repaint().
        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Подсказка в углу .
            g.setColor(new Color(20, 60, 20));
            g.setFont(new Font("SansSerif", Font.PLAIN, 14));
            g.drawString("Глубина рекурсии: " + depth + " из " + MAX_DEPTH, 12, 22);
            g.drawString("Стрелки и колесо мыши — глубина (вверх/вправо — больше, вниз/влево — меньше), R — сброс", 12, 42);

            // Основание ствола ставим внизу по центру окна.
            g.translate(getWidth() / 2.0, getHeight() * 0.95);

            g.setColor(new Color(10, 100, 20));
            // Рисуем ствол: он растёт вверх (угол 90 градусов) из точки (0, 0).
            FirTree.drawBranch(g, 0, 0, 90, 520, depth, true);
        }
    }
}
