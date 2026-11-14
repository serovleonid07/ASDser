import java.awt.BasicStroke;
import java.awt.Graphics2D;

// Рекурсивное рисование ёлки-фрактала.
// Ёлка — это ствол, от которого в стороны отходят ветки. Каждая ветка устроена так же,
// как вся ёлка: у неё есть свои веточки, а у тех — свои. Это и есть рекурсия.
// Глубина (depth) показывает, сколько раз ветки делятся на более мелкие.
public class FirTree {
    // Сколько веток отходит от ствола и сколько — от каждой ветки.
    private static final int TRUNK_BRANCHES = 6;
    private static final int SMALL_BRANCHES = 4;
    // Угол (в градусах) между стволом и главной веткой, которая от него отходит.
    private static final double MAIN_SPREAD = 72;
    // Угол между веткой и её веточкой (для всех более мелких уровней).
    private static final double SMALL_SPREAD = 52;

    // Рисует ветку (или ствол) вместе со всеми её веточками.
    //   g — чем рисуем;
    //   x, y — начало ветки (в «мировых» координатах, y растёт вниз);
    //   angle — направление ветки в градусах: 90 — вверх, 0 — вправо, 180 — влево;
    //   length — длина ветки;
    //   depth — сколько уровней веточек ещё надо нарисовать;
    //   isTrunk — true только для самого ствола.
    public static void drawBranch(Graphics2D g, double x, double y, double angle,
                                  double length, int depth, boolean isTrunk) {
        // База рекурсии: глубина закончилась или ветка слишком короткая.
        if (depth <= 0 || length < 0.5) {
            return;
        }

        // Считаем конец ветки. Угол переводим в радианы.
        double radians = Math.toRadians(angle);
        double endX = x + length * Math.cos(radians);
        double endY = y - length * Math.sin(radians);  // минус: на экране y растёт вниз

        // Рисуем саму ветку. Чем глубже уровень, тем линия тоньше.
        float width = (float) (0.5 + depth * 0.45);
        if (isTrunk) {
            width = 2f;  // ствол рисуем тонкой двойной линией
        }
        g.setStroke(new BasicStroke(width));
        g.drawLine(round(x), round(y), round(endX), round(endY));

        // Веточки отходят от ветки по очереди: то в одну сторону, то в другую.
        double spread = SMALL_SPREAD;
        int count = SMALL_BRANCHES;
        double firstT = 0.2;  // первая веточка стоит на 20% длины ветки
        if (isTrunk) {
            spread = MAIN_SPREAD;
            count = TRUNK_BRANCHES;
            firstT = 0.3;     // у ствола низ остаётся голым
        }
        for (int i = 0; i < count; i++) {
            // t — на какой доле длины стоит веточка: 0 — начало ветки, 1 — конец.
            double t = firstT + (0.97 - firstT) * i / (count - 1);

            // Сторона: чётные веточки идут вправо (-1), нечётные влево (+1) от направления ветки.
            int side = -1;
            if (i % 2 == 1) {
                side = 1;
            }

            double startX = x + (endX - x) * t;
            double startY = y + (endY - y) * t;

            // Нижние веточки длиннее, верхние короче, поэтому получается силуэт ёлки.
            double childLength = length * 0.5 * (1 - t) + length * 0.1;
            if (isTrunk) {
                childLength = length * 0.6 * (1 - t) + length * 0.12;
            }

            // Угол веточки. Если она смотрит вниз, разворачиваем на другую сторону,
            // чтобы мелкие веточки росли вверх, как на настоящей ёлке.
            double childAngle = angle + side * spread;
            if (!isTrunk && Math.sin(Math.toRadians(childAngle)) < 0.15) {
                childAngle = angle - side * spread;
            }

            // Веточка — это такая же ветка, только короче. Вызываем ту же функцию (рекурсия).
            drawBranch(g, startX, startY, childAngle, childLength, depth - 1, false);
        }

        // Кончик ветки продолжает её вверх маленькой веточкой: так ветка не обрывается.
        drawBranch(g, endX, endY, angle, length * 0.2, depth - 1, false);
    }

    // Превращает дробное число в целое (координаты точек должны быть целыми).
    private static int round(double value) {
        return (int) Math.round(value);
    }
}
