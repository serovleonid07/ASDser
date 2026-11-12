// Решение головоломки «Фонари» рекурсивным перебором с возвратом (backtracking).
// Идея: идём по клеткам слева направо и сверху вниз (клетки нумеруем 0, 1, 2, ...).
// Для каждой белой клетки пробуем два варианта: поставить лампу или не ставить.
// Если по пути стало ясно, что решения не получится, возвращаемся на шаг назад.
// Правила, которые должны выполняться в конце:
// 1) все белые клетки освещены;
// 2) лампы не светят друг на друга;
// 3) около чёрной клетки с числом стоит ровно столько ламп, сколько в ней написано.
public class LightUpSolver {
    private static Board board;                // поле, которое решаем
    private static int total;                  // сколько всего клеток в поле
    private static int solutionCount;          // сколько решений нашли
    private static boolean[][] firstSolution;  // расстановка ламп в первом найденном решении
    private static int stepCount;              // сколько раз вызвали search (для статистики)

    // Решает головоломку и возвращает, сколько у неё решений.
    // Первое решение можно получить через getFirstSolution().
    public static int solve(Board puzzle) {
        board = puzzle;
        total = puzzle.rows * puzzle.cols;
        solutionCount = 0;
        firstSolution = null;
        stepCount = 0;

        search(0);
        return solutionCount;
    }

    // Возвращает первое найденное решение (массив: true — в клетке лампа) или null, если решений нет.
    public static boolean[][] getFirstSolution() {
        return firstSolution;
    }

    // Возвращает, сколько раз была вызвана рекурсивная функция search.
    public static int getStepCount() {
        return stepCount;
    }

    // Главная рекурсивная функция. Решает вопрос для клетки с номером index.
    // Все клетки с номерами меньше index уже решены (лампа есть или нет).
    private static void search(int index) {
        stepCount++;

        // Отсечения: если правила уже нарушены, дальше идти нет смысла.
        if (!numbersAreOk(index)) {
            return;
        }
        if (!everyCellCanBeLit(index)) {
            return;
        }

        // Все клетки решены, а проверки выше прошли: значит, это решение.
        if (index == total) {
            solutionCount++;
            if (firstSolution == null) {
                firstSolution = board.copyLamps();
            }
            return;
        }

        int r = index / board.cols;  // строка клетки
        int c = index % board.cols;  // столбец клетки

        // Чёрную клетку пропускаем: лампа в ней стоять не может.
        if (!board.isWhite(r, c)) {
            search(index + 1);
            return;
        }

        // Вариант 1: ставим лампу. Можно, только если клетка ещё не освещена,
        // иначе лампа окажется в луче другой лампы.
        if (board.lightCount[r][c] == 0) {
            board.putLamp(r, c);
            search(index + 1);
            board.removeLamp(r, c);  // возврат назад: убираем лампу
        }

        // Вариант 2: лампу не ставим.
        search(index + 1);
    }

    // Проверяет числа в чёрных клетках. index — сколько клеток уже решено.
    // Для каждой чёрной клетки с числом считаем:
    //   lamps — сколько ламп уже стоит рядом;
    //   undecided — сколько соседних белых клеток ещё не решено (там лампа ещё может появиться).
    // Если ламп уже больше числа, или даже со всеми будущими лампами не набрать число — это тупик.
    private static boolean numbersAreOk(int index) {
        for (int r = 0; r < board.rows; r++) {
            for (int c = 0; c < board.cols; c++) {
                if (!board.hasNumber(r, c)) {
                    continue;
                }

                int lamps = board.lampsAround(r, c);
                int undecided = 0;
                for (int d = 0; d < 4; d++) {
                    int nr = r + Board.ROW_STEP[d];
                    int nc = c + Board.COL_STEP[d];
                    if (board.isInside(nr, nc) && board.isWhite(nr, nc)
                            && nr * board.cols + nc >= index) {
                        undecided++;
                    }
                }

                int number = board.cells[r][c];
                if (lamps > number || lamps + undecided < number) {
                    return false;
                }
            }
        }
        return true;
    }

    // Проверяет, что каждую белую клетку ещё можно осветить. index — сколько клеток уже решено.
    // Если клетка пока темна, ей нужна лампа: либо в ней самой, либо в клетке на её луче.
    // Лампу можно ещё поставить только в нерешённую клетку (номер не меньше index),
    // которая сейчас не освещена. Если такой клетки нет, осветить уже не получится — тупик.
    private static boolean everyCellCanBeLit(int index) {
        for (int r = 0; r < board.rows; r++) {
            for (int c = 0; c < board.cols; c++) {
                if (board.isWhite(r, c) && board.lightCount[r][c] == 0
                        && !canGetLampNear(r, c, index)) {
                    return false;
                }
            }
        }
        return true;
    }

    // Ищет клетку, где ещё можно поставить лампу, чтобы осветить клетку (r, c).
    // Смотрим саму клетку и четыре луча от неё до чёрной клетки или края поля.
    private static boolean canGetLampNear(int r, int c, int index) {
        if (lampCanStillBePlaced(r, c, index)) {
            return true;
        }
        for (int d = 0; d < 4; d++) {
            int nr = r + Board.ROW_STEP[d];
            int nc = c + Board.COL_STEP[d];
            while (board.isInside(nr, nc) && board.isWhite(nr, nc)) {
                if (lampCanStillBePlaced(nr, nc, index)) {
                    return true;
                }
                nr += Board.ROW_STEP[d];
                nc += Board.COL_STEP[d];
            }
        }
        return false;
    }

    // Проверяет, можно ли ещё поставить лампу в клетку (r, c):
    // клетка не решена (номер не меньше index) и пока не освещена.
    private static boolean lampCanStillBePlaced(int r, int c, int index) {
        return r * board.cols + c >= index && board.lightCount[r][c] == 0;
    }
}
