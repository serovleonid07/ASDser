// Рекурсивное рисование ёлки.
// Ёлка состоит из ярусов (треугольников), поставленных друг на друга.
// Верхний ярус самый маленький, каждый следующий вниз на один ряд выше и шире.
// Глубина рекурсии (depth) — сколько ярусов у ёлки.
// Рекурсия: «ёлка из n ярусов» = «верхняя ёлка из n - 1 ярусов» + «один большой ярус внизу».
public class FirTree {

    // Рисует ёлку из depth ярусов на холсте и возвращает номер строки,
    // на которой закончился самый нижний ярус (чтобы потом дорисовать ствол).
    //   depth — сколько ярусов нарисовать;
    //   row — строка холста, с которой начинается верхушка ёлки;
    //   center — столбец, по которому ёлка стоит по центру.
    public static int drawTree(Canvas canvas, int depth, int row, int center) {
        // База рекурсии: ярусов нет, рисовать нечего. Рисование закончилось на строке row.
        if (depth == 0) {
            return row;
        }

        // Шаг 1. Сначала рисуем верхнюю часть: ёлку из depth - 1 ярусов.
        // Она вернёт строку, с которой можно начинать нижний ярус.
        int nextRow = drawTree(canvas, depth - 1, row, center);

        // Шаг 2. Рисуем нижний ярус номер depth.
        return drawTier(canvas, depth, nextRow, center);
    }

    // Рисует один ярус номер k (1, 2, 3, ...) и возвращает строку под ним.
    // Ярус — это треугольник из k + 1 рядов. Верхний ряд имеет ширину 2 * k - 1,
    // каждый следующий ряд шире на 2 символа.
    // Края ряда рисуем как '/' и '\', а внутри — звёздочки '*'.
    private static int drawTier(Canvas canvas, int k, int row, int center) {
        for (int j = 0; j <= k; j++) {
            int width = 2 * k - 1 + 2 * j;   // ширина ряда
            int left = center - width / 2;   // столбец левого края

            for (int i = 0; i < width; i++) {
                char symbol = '*';
                if (width == 1) {
                    symbol = '*';          // ряд из одного символа — просто звёздочка
                } else if (i == 0) {
                    symbol = '/';
                } else if (i == width - 1) {
                    symbol = '\\';
                }
                canvas.put(row + j, left + i, symbol);
            }
        }
        return row + k + 1;  // следующая свободная строка под ярусом
    }

    // Рисует ёлку целиком (верхушка, ярусы, ствол) и печатает её на экран.
    // depth — глубина рекурсии: количество ярусов, его задаёт пользователь.
    public static void draw(int depth) {
        // Высота ярусов: 2 + 3 + ... + (depth + 1), плюс звезда, плюс ствол.
        int tiersHeight = 0;
        for (int k = 1; k <= depth; k++) {
            tiersHeight += k + 1;
        }
        int trunkHeight = 2;
        int width = 2 * depth - 1 + 2 * depth + 4;  // ширина самого нижнего ряда и запас
        int height = tiersHeight + trunkHeight + 1;

        Canvas canvas = new Canvas(height, width);
        int center = width / 2;

        canvas.put(0, center, '*');                    // звезда на верхушке
        int bottom = drawTree(canvas, depth, 1, center);

        // Ствол: две строки по три символа под ярусами.
        for (int i = 0; i < trunkHeight; i++) {
            canvas.put(bottom + i, center - 1, '|');
            canvas.put(bottom + i, center, '|');
            canvas.put(bottom + i, center + 1, '|');
        }

        canvas.print();
    }
}
