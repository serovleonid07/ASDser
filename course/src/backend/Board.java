package backend;

// Игровое поле: двумерный массив 7x7 в форме креста.
// В каждой клетке лежит число: OUTSIDE, EMPTY, FOX или GOOSE (см. GameRules).
public class Board {
    private final int[][] cells = new int[GameRules.BOARD_SIZE][GameRules.BOARD_SIZE];

    // Создаёт пустое поле в форме креста. Вызывается при new Board().
    public Board() {
        for (int row = 0; row < GameRules.BOARD_SIZE; row++) {
            for (int col = 0; col < GameRules.BOARD_SIZE; col++) {
                if (isCorner(row, col)) {
                    cells[row][col] = GameRules.OUTSIDE;
                } else {
                    cells[row][col] = GameRules.EMPTY;
                }
            }
        }
    }

    // Возвращает true, если клетка (row, col) находится в одном из четырёх углов, которые вырезаны.
    // Угол — это когда и строка, и столбец лежат в крайних двух рядах (0-1 или 5-6).
    private boolean isCorner(int row, int col) {
        boolean rowIsEdge = row < 2 || row > 4;
        boolean colIsEdge = col < 2 || col > 4;
        return rowIsEdge && colIsEdge;
    }

    // Возвращает true, если клетка есть на поле (не вышла за границы и не угол).
    // row, col — номера строки и столбца, нумерация с 0.
    public boolean isInside(int row, int col) {
        if (row < 0 || row >= GameRules.BOARD_SIZE || col < 0 || col >= GameRules.BOARD_SIZE) {
            return false;
        }
        return cells[row][col] != GameRules.OUTSIDE;
    }

    // Возвращает содержимое клетки (EMPTY, FOX, GOOSE или OUTSIDE).
    public int get(int row, int col) {
        return cells[row][col];
    }

    // Записывает value в клетку. Аргумент value: EMPTY, FOX или GOOSE.
    public void set(int row, int col, int value) {
        cells[row][col] = value;
    }

    // Делает независимую копию поля. Нужна, чтобы компьютер мог «играть вперёд» на копии.
    public Board copy() {
        Board result = new Board();
        for (int row = 0; row < GameRules.BOARD_SIZE; row++) {
            for (int col = 0; col < GameRules.BOARD_SIZE; col++) {
                result.cells[row][col] = cells[row][col];
            }
        }
        return result;
    }
}
