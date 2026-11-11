// Оценочная функция: превращает позицию в одно число.
// Чем число БОЛЬШЕ, тем лучше позиция для ЛИСЫ. Чем МЕНЬШЕ (отрицательнее), тем лучше для ГУСЕЙ.
public class Evaluator {
    // Очень большое число: оценка победы. Больше любой обычной оценки.
    public static final int WIN_SCORE = 100000;

    // Веса частей оценки. Их можно менять, чтобы компьютер играл по-другому.
    public static int WEIGHT_EATEN_GOOSE = 300;  // за каждого съеденного гуся
    public static int WEIGHT_FOX_STEP = 12;      // за каждый свободный ход лисы
    public static int WEIGHT_FOX_CAPTURE = 100;  // за каждую возможность съесть гуся прямо сейчас
    public static int WEIGHT_GOOSE_ROW = 20;     // за то, что гуси стоят далеко от верха (слабо продвинулись)
    public static int WEIGHT_VULNERABLE = 8;   // за каждого гуся, которого можно перепрыгнуть

    // Возвращает оценку позиции (для лисы: больше — лучше).
    public static int evaluate(GameState state) {
        Board board = state.getBoard();

        // 1) Сколько гусей уже съедено.
        int eaten = GameRules.GEESE_AT_START - state.getGeeseCount();
        int score = eaten * WEIGHT_EATEN_GOOSE;

        // 2) Подвижность лисы: считаем шаги и прыжки.
        if (state.isFoxPlaced()) {
            int steps = 0;
            int captures = 0;
            int row = state.getFoxRow();
            int col = state.getFoxCol();
            for (int d = 0; d < 8; d++) {
                int nextRow = row + MoveGenerator.ALL_ROW[d];
                int nextCol = col + MoveGenerator.ALL_COL[d];
                if (!board.isInside(nextRow, nextCol)) {
                    continue;
                }
                int cell = board.get(nextRow, nextCol);
                if (cell == GameRules.EMPTY) {
                    steps++;
                } else if (cell == GameRules.GOOSE) {
                    int jumpRow = nextRow + MoveGenerator.ALL_ROW[d];
                    int jumpCol = nextCol + MoveGenerator.ALL_COL[d];
                    if (board.isInside(jumpRow, jumpCol) && board.get(jumpRow, jumpCol) == GameRules.EMPTY) {
                        captures++;
                    }
                }
            }
            score += steps * WEIGHT_FOX_STEP;
            score += captures * WEIGHT_FOX_CAPTURE;
        }

        // 3) Продвижение гусей. Чем выше (меньше номер строки) стоят гуси, тем лучше для гусей.
        // Складываем номера строк всех гусей: большая сумма = гуси стоят низко = лучше для лисы.
        int rowSum = 0;
        for (int row = 0; row < GameRules.BOARD_SIZE; row++) {
            for (int col = 0; col < GameRules.BOARD_SIZE; col++) {
                if (board.isInside(row, col) && board.get(row, col) == GameRules.GOOSE) {
                    rowSum += row;
                }
            }
        }
        score += rowSum * WEIGHT_GOOSE_ROW;

        // 4) Уязвимые гуси: гусь, которого можно перепрыгнуть (по одной линии с одной стороны
        // свободно или лиса, а с другой стороны свободно). Чем их больше, тем лучше для лисы.
        score += countVulnerableGeese(board) * WEIGHT_VULNERABLE;

        return score;
    }

    // Считает гусей, которых лиса теоретически может съесть (если дойдёт до нужной клетки).
    private static int countVulnerableGeese(Board board) {
        int count = 0;
        // Пары противоположных направлений в массивах ALL_ROW / ALL_COL:
        // (вверх, вниз), (влево, вправо), (вверх-влево, вниз-вправо), (вверх-вправо, вниз-влево).
        int[] firstDir = {0, 2, 4, 5};
        int[] secondDir = {1, 3, 7, 6};

        for (int row = 0; row < GameRules.BOARD_SIZE; row++) {
            for (int col = 0; col < GameRules.BOARD_SIZE; col++) {
                if (!board.isInside(row, col) || board.get(row, col) != GameRules.GOOSE) {
                    continue;
                }
                for (int p = 0; p < 4; p++) {
                    int rowA = row + MoveGenerator.ALL_ROW[firstDir[p]];
                    int colA = col + MoveGenerator.ALL_COL[firstDir[p]];
                    int rowB = row + MoveGenerator.ALL_ROW[secondDir[p]];
                    int colB = col + MoveGenerator.ALL_COL[secondDir[p]];
                    if (!board.isInside(rowA, colA) || !board.isInside(rowB, colB)) {
                        continue;  // у края поля через гуся не перепрыгнуть
                    }
                    int a = board.get(rowA, colA);
                    int b = board.get(rowB, colB);
                    boolean fromAToB = (a == GameRules.EMPTY || a == GameRules.FOX) && b == GameRules.EMPTY;
                    boolean fromBToA = (b == GameRules.EMPTY || b == GameRules.FOX) && a == GameRules.EMPTY;
                    if (fromAToB || fromBToA) {
                        count++;
                        break;  // этот гусь уже уязвим, другие линии не считаем
                    }
                }
            }
        }
        return count;
    }
}
