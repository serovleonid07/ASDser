package backend;

// Генератор ходов: по позиции строит список всех допустимых ходов игрока, чей сейчас ход.
// Правила ходов:
//   гусь — на одну свободную клетку вверх, вниз, влево или вправо;
//   лиса — так же, плюс по диагонали; кроме того, лиса может перепрыгнуть через соседнего гуся
//          на свободную клетку сразу за ним (по прямой или по диагонали) и съесть его.
public class MoveGenerator {
    // Четыре прямых направления (вверх, вниз, влево, вправо). Для гусей.
    public static final int[] STRAIGHT_ROW = {-1, 1, 0, 0};
    public static final int[] STRAIGHT_COL = {0, 0, -1, 1};

    // Все восемь направлений (первые четыре — прямые, остальные — диагонали). Для лисы.
    public static final int[] ALL_ROW = {-1, 1, 0, 0, -1, -1, 1, 1};
    public static final int[] ALL_COL = {0, 0, -1, 1, -1, 1, -1, 1};

    // Записывает в список list все допустимые ходы для игрока, чей сейчас ход.
    // Старое содержимое списка стирается.
    public static void getMoves(GameState state, MoveList list) {
        list.clear();
        if (state.getCurrentPlayer() == GameRules.PLAYER_FOX) {
            getFoxMoves(state, list);
        } else {
            getGeeseMoves(state, list);
        }
    }

    // Ходы лисы: шаги на свободные клетки и прыжки через гусей.
    private static void getFoxMoves(GameState state, MoveList list) {
        if (!state.isFoxPlaced()) {
            return;  // лису ещё не поставили, ходов нет
        }
        Board board = state.getBoard();
        int row = state.getFoxRow();
        int col = state.getFoxCol();

        for (int d = 0; d < 8; d++) {
            int nextRow = row + ALL_ROW[d];
            int nextCol = col + ALL_COL[d];
            if (!board.isInside(nextRow, nextCol)) {
                continue;
            }
            int cell = board.get(nextRow, nextCol);
            if (cell == GameRules.EMPTY) {
                // Обычный шаг.
                list.add(new Move(row, col, nextRow, nextCol, -1, -1));
            } else if (cell == GameRules.GOOSE) {
                // Рядом гусь: пробуем перепрыгнуть на клетку за ним.
                int jumpRow = nextRow + ALL_ROW[d];
                int jumpCol = nextCol + ALL_COL[d];
                if (board.isInside(jumpRow, jumpCol) && board.get(jumpRow, jumpCol) == GameRules.EMPTY) {
                    list.add(new Move(row, col, jumpRow, jumpCol, nextRow, nextCol));
                }
            }
        }
    }

    // Ходы гусей: каждый гусь может шагнуть на свободную соседнюю клетку по прямой.
    private static void getGeeseMoves(GameState state, MoveList list) {
        Board board = state.getBoard();
        for (int row = 0; row < GameRules.BOARD_SIZE; row++) {
            for (int col = 0; col < GameRules.BOARD_SIZE; col++) {
                if (board.isInside(row, col) && board.get(row, col) == GameRules.GOOSE) {
                    for (int d = 0; d < 4; d++) {
                        int nextRow = row + STRAIGHT_ROW[d];
                        int nextCol = col + STRAIGHT_COL[d];
                        if (board.isInside(nextRow, nextCol)
                                && board.get(nextRow, nextCol) == GameRules.EMPTY) {
                            list.add(new Move(row, col, nextRow, nextCol, -1, -1));
                        }
                    }
                }
            }
        }
    }
}
