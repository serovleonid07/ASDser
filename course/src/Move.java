// Один ход игрока: откуда, куда и (если был прыжок) какого гуся съели.
// Объект не меняется после создания (все поля final).
public class Move {
    public final int fromRow;      // строка, откуда идёт фишка
    public final int fromCol;      // столбец, откуда идёт фишка
    public final int toRow;        // строка, куда приходит фишка
    public final int toCol;        // столбец, куда приходит фишка
    public final int capturedRow;  // строка съеденного гуся, или -1 если это обычный шаг
    public final int capturedCol;  // столбец съеденного гуся, или -1 если это обычный шаг

    // Создаёт ход. Для обычного шага capturedRow и capturedCol равны -1.
    public Move(int fromRow, int fromCol, int toRow, int toCol, int capturedRow, int capturedCol) {
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
        this.capturedRow = capturedRow;
        this.capturedCol = capturedCol;
    }

    // Возвращает true, если в этом ходе лиса съела гуся (прыжок).
    public boolean isCapture() {
        return capturedRow >= 0;
    }

    // Возвращает строку вида «d4-d5» (шаг) или «d4xd6» (прыжок с поеданием).
    // Столбцы названы буквами a-g, строки числами 1-7 снизу вверх, как на шахматной доске.
    @Override
    public String toString() {
        String sign = isCapture() ? "x" : "-";
        return cellName(fromRow, fromCol) + sign + cellName(toRow, toCol);
    }

    // Превращает номер строки и столбца в имя клетки, например (3, 3) -> «d4».
    private static String cellName(int row, int col) {
        char letter = (char) ('a' + col);
        int number = GameRules.BOARD_SIZE - row;
        return "" + letter + number;
    }
}
