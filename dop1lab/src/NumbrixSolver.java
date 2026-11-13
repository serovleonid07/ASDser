// Решение головоломки «Нумератор» рекурсивным перебором с возвратом (backtracking).
// Идея: ставим числа по порядку 1, 2, 3, ... Каждое следующее число должно стоять
// в клетке, соседней с клеткой предыдущего числа.
//   - Если следующее число дано в условии, проверяем, что оно рядом.
//   - Иначе пробуем по очереди все пустые соседние клетки.
// Если путь зашёл в тупик, убираем число и возвращаемся на шаг назад.
public class NumbrixSolver {
    private static Board board;                // поле, которое решаем
    private static int total;                  // сколько всего клеток (и чисел) в поле
    private static int[] givenRow;             // givenRow[k] — строка заданного числа k (или -1, если его нет)
    private static int[] givenCol;             // givenCol[k] — столбец заданного числа k
    private static int[] nextGiven;            // nextGiven[k] — ближайшее заданное число, большее k (0, если такого нет)
    private static int solutionCount;          // сколько решений нашли
    private static int[][] firstSolution;      // заполнение поля в первом найденном решении
    private static int stepCount;              // сколько раз вызвали step (для статистики)

    // Решает головоломку и возвращает, сколько у неё решений.
    // Первое решение можно получить через getFirstSolution().
    public static int solve(Board puzzle) {
        board = puzzle;
        total = puzzle.rows * puzzle.cols;
        solutionCount = 0;
        firstSolution = null;
        stepCount = 0;
        prepareGivenNumbers();

        if (givenRow[1] != -1) {
            // Число 1 дано: начинаем с его клетки.
            step(1, givenRow[1], givenCol[1]);
        } else {
            // Числа 1 нет в условии: пробуем поставить 1 в каждую пустую клетку.
            for (int r = 0; r < board.rows; r++) {
                for (int c = 0; c < board.cols; c++) {
                    if (board.isEmpty(r, c) && canReachNextGiven(1, r, c)) {
                        board.cells[r][c] = 1;
                        step(1, r, c);
                        board.cells[r][c] = 0;  // возврат назад
                    }
                }
            }
        }
        return solutionCount;
    }

    // Возвращает первое найденное решение (таблицу чисел) или null, если решений нет.
    public static int[][] getFirstSolution() {
        return firstSolution;
    }

    // Возвращает, сколько раз была вызвана рекурсивная функция step.
    public static int getStepCount() {
        return stepCount;
    }

    // Запоминает, где стоят заданные числа, и для каждого числа k
    // находит ближайшее заданное число, которое больше k.
    private static void prepareGivenNumbers() {
        givenRow = new int[total + 2];
        givenCol = new int[total + 2];
        nextGiven = new int[total + 2];
        for (int k = 0; k <= total + 1; k++) {
            givenRow[k] = -1;
            givenCol[k] = -1;
        }
        for (int r = 0; r < board.rows; r++) {
            for (int c = 0; c < board.cols; c++) {
                if (board.fixed[r][c]) {
                    int number = board.cells[r][c];
                    givenRow[number] = r;
                    givenCol[number] = c;
                }
            }
        }
        // Идём с конца: nextGiven[k] — это k + 1, если оно дано, иначе то же, что у k + 1.
        nextGiven[total] = 0;
        for (int k = total - 1; k >= 0; k--) {
            if (givenRow[k + 1] != -1) {
                nextGiven[k] = k + 1;
            } else {
                nextGiven[k] = nextGiven[k + 1];
            }
        }
    }

    // Главная рекурсивная функция. Число number уже стоит в клетке (r, c).
    // Нужно поставить число number + 1 в соседнюю клетку.
    private static void step(int number, int r, int c) {
        stepCount++;

        // Дошли до последнего числа: все клетки заполнены, это решение.
        if (number == total) {
            solutionCount++;
            if (firstSolution == null) {
                firstSolution = board.copyCells();
            }
            return;
        }

        int next = number + 1;  // число, которое сейчас ставим

        // Случай 1: число next дано в условии. Оно должно стоять рядом с текущей клеткой.
        if (givenRow[next] != -1) {
            if (distance(r, c, givenRow[next], givenCol[next]) == 1) {
                step(next, givenRow[next], givenCol[next]);
            }
            return;
        }

        // Случай 2: числа next в условии нет. Пробуем все четыре соседние клетки.
        for (int d = 0; d < 4; d++) {
            int nr = r + Board.ROW_STEP[d];
            int nc = c + Board.COL_STEP[d];
            if (board.isInside(nr, nc) && board.isEmpty(nr, nc) && canReachNextGiven(next, nr, nc)) {
                board.cells[nr][nc] = next;   // ставим число
                step(next, nr, nc);
                board.cells[nr][nc] = 0;      // возврат назад: убираем число
            }
        }
    }

    // Отсечение: проверяет, что от клетки (r, c), где стоит число number,
    // ещё можно дойти до ближайшего заданного числа.
    // Пусть до него gap шагов (разница чисел), а по клеткам расстояние dist.
    //   - Если dist > gap, не успеем дойти.
    //   - Если gap - dist нечётно, не получится: каждый шаг меняет цвет клетки
    //     на шахматной доске, поэтому лишние шаги бывают только парами.
    private static boolean canReachNextGiven(int number, int r, int c) {
        int target = nextGiven[number];
        if (target == 0) {
            return true;  // заданных чисел впереди нет, проверять нечего
        }
        int gap = target - number;
        int dist = distance(r, c, givenRow[target], givenCol[target]);
        return dist <= gap && (gap - dist) % 2 == 0;
    }

    // Расстояние между клетками по ходам вверх/вниз/влево/вправо (манхэттенское).
    private static int distance(int r1, int c1, int r2, int c2) {
        return Math.abs(r1 - r2) + Math.abs(c1 - c2);
    }
}
