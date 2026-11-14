// Холст для рисования символами. Это таблица символов: строка и столбец задают клетку.
// Рисуем в таблицу, а потом печатаем её на экран.
public class Canvas {
    int rows;          // высота холста
    int cols;          // ширина холста
    char[][] pixels;   // символы холста

    // Создаёт пустой холст (везде пробелы).
    public Canvas(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        pixels = new char[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                pixels[r][c] = ' ';
            }
        }
    }

    // Ставит символ в клетку (r, c). Если клетка за краем холста, ничего не делает.
    public void put(int r, int c, char symbol) {
        if (r >= 0 && r < rows && c >= 0 && c < cols) {
            pixels[r][c] = symbol;
        }
    }

    // Печатает холст. Пустые строки сверху и пробелы справа не печатаем.
    public void print() {
        int firstRow = 0;
        while (firstRow < rows && isRowEmpty(firstRow)) {
            firstRow++;
        }
        for (int r = firstRow; r < rows; r++) {
            int last = cols - 1;
            while (last >= 0 && pixels[r][last] == ' ') {
                last--;
            }
            String line = "";
            for (int c = 0; c <= last; c++) {
                line = line + pixels[r][c];
            }
            System.out.println(line);
        }
    }

    // Проверяет, что в строке r нет ни одного символа, кроме пробелов.
    private boolean isRowEmpty(int r) {
        for (int c = 0; c < cols; c++) {
            if (pixels[r][c] != ' ') {
                return false;
            }
        }
        return true;
    }
}
