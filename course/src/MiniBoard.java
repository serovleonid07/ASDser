import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.awt.Rectangle;
import javax.swing.JComponent;

// Маленькая схема кусочка поля для страницы правил.
// Схема задаётся строками, по одному символу на клетку:
//   F - лиса, G - гусь, . - пустая клетка, x - клетка, куда прыгнет лиса, # - клетки нет.
// Можно добавить стрелку (откуда, куда), чтобы показать ход.
public class MiniBoard extends JComponent {
    private static final int CELL = 54;     // размер клетки на схеме
    private static final int PAD = 10;      // отступ вокруг схемы

    private final PieceImages images;
    private final String[] rows;
    private final int[] arrow;              // {строка откуда, столбец откуда, строка куда, столбец куда} или null

    public MiniBoard(PieceImages images, String[] rows, int[] arrow) {
        this.images = images;
        this.rows = rows;
        this.arrow = arrow;
        int cols = 0;
        for (String row : rows) {
            cols = Math.max(cols, row.length());
        }
        setPreferredSize(new Dimension(cols * CELL + 2 * PAD, rows.length * CELL + 2 * PAD));
        setOpaque(false);
    }

    // Есть ли клетка в этом месте схемы.
    private boolean hasCell(int row, int col) {
        return row >= 0 && row < rows.length && col >= 0 && col < rows[row].length() && rows[row].charAt(col) != '#';
    }

    private int centerX(int col) {
        return PAD + col * CELL + CELL / 2;
    }

    private int centerY(int row) {
        return PAD + row * CELL + CELL / 2;
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Светлая подложка под всеми клетками.
        Area plate = new Area();
        for (int r = 0; r < rows.length; r++) {
            for (int c = 0; c < rows[r].length(); c++) {
                if (hasCell(r, c)) {
                    plate.add(new Area(new Rectangle(PAD + c * CELL, PAD + r * CELL, CELL, CELL)));
                }
            }
        }
        g.setColor(new Color(255, 255, 255, 170));
        g.fill(plate);

        // Линии поля (прямые и диагонали) между соседними клетками.
        g.setColor(new Color(0x8A7F70));
        g.setStroke(new BasicStroke(2f));
        int[] dRow = {0, 1, 1, 1};
        int[] dCol = {1, 0, 1, -1};
        for (int r = 0; r < rows.length; r++) {
            for (int c = 0; c < rows[r].length(); c++) {
                if (!hasCell(r, c)) {
                    continue;
                }
                for (int d = 0; d < 4; d++) {
                    if (hasCell(r + dRow[d], c + dCol[d])) {
                        g.drawLine(centerX(c), centerY(r), centerX(c + dCol[d]), centerY(r + dRow[d]));
                    }
                }
                g.fillOval(centerX(c) - 3, centerY(r) - 3, 6, 6);
            }
        }

        // Зелёная точка там, куда прыгнет лиса.
        for (int r = 0; r < rows.length; r++) {
            for (int c = 0; c < rows[r].length(); c++) {
                if (hasCell(r, c) && rows[r].charAt(c) == 'x') {
                    g.setColor(new Color(0x2E9E4F));
                    g.fillOval(centerX(c) - 9, centerY(r) - 9, 18, 18);
                }
            }
        }

        // Фигуры.
        int size = CELL - 6;
        BufferedImage fox = images.getFox(size);
        BufferedImage goose = images.getGoose(size);
        for (int r = 0; r < rows.length; r++) {
            for (int c = 0; c < rows[r].length(); c++) {
                if (!hasCell(r, c)) {
                    continue;
                }
                char ch = rows[r].charAt(c);
                BufferedImage icon = ch == 'F' ? fox : (ch == 'G' ? goose : null);
                if (icon != null) {
                    g.drawImage(icon, centerX(c) - size / 2, centerY(r) - size / 2, null);
                } else if (ch == 'F' || ch == 'G') {
                    g.setColor(ch == 'F' ? new Color(0xE0701B) : Color.WHITE);   // запасной вариант без картинок
                    g.fillOval(centerX(c) - size / 3, centerY(r) - size / 3, 2 * size / 3, 2 * size / 3);
                }
            }
        }

        // Стрелка хода.
        if (arrow != null) {
            drawArrow(g, centerX(arrow[1]), centerY(arrow[0]), centerX(arrow[3]), centerY(arrow[2]));
        }
        g.dispose();
    }

    // Рисует красную пунктирную стрелку от (x1, y1) к (x2, y2).
    private void drawArrow(Graphics2D g, int x1, int y1, int x2, int y2) {
        double angle = Math.atan2(y2 - y1, x2 - x1);
        // Начало и конец сдвигаем, чтобы стрелка не лежала на самих фигурах.
        int sx = (int) (x1 + Math.cos(angle) * 22);
        int sy = (int) (y1 + Math.sin(angle) * 22);
        int ex = (int) (x2 - Math.cos(angle) * 14);
        int ey = (int) (y2 - Math.sin(angle) * 14);
        g.setColor(new Color(0xC62828));
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[]{7f, 5f}, 0f));
        g.drawLine(sx, sy, ex, ey);
        g.setStroke(new BasicStroke(3f));
        int head = 11;
        g.drawLine(ex, ey, (int) (ex - head * Math.cos(angle - 0.5)), (int) (ey - head * Math.sin(angle - 0.5)));
        g.drawLine(ex, ey, (int) (ex - head * Math.cos(angle + 0.5)), (int) (ey - head * Math.sin(angle + 0.5)));
    }
}
