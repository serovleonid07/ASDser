// Поле головоломки «Фонари» (Light Up).
// Клетка белая или чёрная. В чёрной клетке может стоять число от 0 до 4:
// столько ламп должно быть в соседних с ней клетках (сверху, снизу, слева и справа).
// Лампа освещает свою клетку и светит лучами вверх, вниз, влево и вправо
// до ближайшей чёрной клетки или до края поля.
public class Board {
    public static final int WHITE = -1;  // белая клетка
    public static final int BLACK = -2;  // чёрная клетка без числа
    // Числа 0, 1, 2, 3, 4 в массиве cells значат чёрную клетку с этим числом.

    // Четыре направления: вверх, вниз, влево, вправо.
    // Для направления d сдвиг по строке равен ROW_STEP[d], по столбцу COL_STEP[d].
    public static final int[] ROW_STEP = {-1, 1, 0, 0};
    public static final int[] COL_STEP = {0, 0, -1, 1};

    int rows;               // сколько строк в поле
    int cols;               // сколько столбцов в поле
    int[][] cells;          // что лежит в клетке: WHITE, BLACK или число от 0 до 4
    boolean[][] hasLamp;    // true, если в клетке стоит лампа
    int[][] lightCount;     // сколько ламп освещают клетку (лампа освещает и свою клетку)

    // Строит поле из текста. Каждая строка массива lines — одна строка поля:
    // '.' — белая клетка, '#' — чёрная без числа, '0'..'4' — чёрная с этим числом.
    public Board(String[] lines) {
        rows = lines.length;
        cols = lines[0].length();
        cells = new int[rows][cols];
        hasLamp = new boolean[rows][cols];
        lightCount = new int[rows][cols];

        for (int r = 0; r < rows; r++) {
            if (lines[r].length() != cols) {
                throw new IllegalArgumentException("Все строки поля должны быть одной длины");
            }
            for (int c = 0; c < cols; c++) {
                char symbol = lines[r].charAt(c);
                if (symbol == '.') {
                    cells[r][c] = WHITE;
                } else if (symbol == '#') {
                    cells[r][c] = BLACK;
                } else if (symbol >= '0' && symbol <= '4') {
                    cells[r][c] = symbol - '0';  // символ '3' превращаем в число 3
                } else {
                    throw new IllegalArgumentException("Неизвестный символ в поле: " + symbol);
                }
            }
        }
    }

    // Проверяет, что клетка (r, c) лежит внутри поля.
    public boolean isInside(int r, int c) {
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }

    // Проверяет, что клетка (r, c) белая.
    public boolean isWhite(int r, int c) {
        return cells[r][c] == WHITE;
    }

    // Проверяет, что в клетке (r, c) чёрная клетка с числом.
    public boolean hasNumber(int r, int c) {
        return cells[r][c] >= 0;
    }

    // Ставит лампу в белую клетку (r, c) и обновляет освещённость клеток.
    public void putLamp(int r, int c) {
        hasLamp[r][c] = true;
        changeLight(r, c, 1);
    }

    // Убирает лампу из клетки (r, c) и обновляет освещённость клеток.
    // Нужна для возврата назад в рекурсии.
    public void removeLamp(int r, int c) {
        hasLamp[r][c] = false;
        changeLight(r, c, -1);
    }

    // Меняет освещённость всех клеток, которые видит лампа из клетки (r, c).
    // delta = 1 — лампу поставили, delta = -1 — лампу убрали.
    private void changeLight(int r, int c, int delta) {
        lightCount[r][c] += delta;  // сама клетка с лампой

        for (int d = 0; d < 4; d++) {
            int nr = r + ROW_STEP[d];
            int nc = c + COL_STEP[d];
            // Идём по лучу, пока не вышли за поле и не упёрлись в чёрную клетку.
            while (isInside(nr, nc) && isWhite(nr, nc)) {
                lightCount[nr][nc] += delta;
                nr += ROW_STEP[d];
                nc += COL_STEP[d];
            }
        }
    }

    // Считает, сколько ламп стоит в четырёх соседних клетках около клетки (r, c).
    public int lampsAround(int r, int c) {
        int count = 0;
        for (int d = 0; d < 4; d++) {
            int nr = r + ROW_STEP[d];
            int nc = c + COL_STEP[d];
            if (isInside(nr, nc) && hasLamp[nr][nc]) {
                count++;
            }
        }
        return count;
    }

    // Делает копию текущей расстановки ламп (чтобы запомнить найденное решение).
    public boolean[][] copyLamps() {
        boolean[][] copy = new boolean[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                copy[r][c] = hasLamp[r][c];
            }
        }
        return copy;
    }

    // Печатает поле. lamps — расстановка ламп, которую надо показать (буква L),
    // или null, если нужно напечатать только условие без ламп.
    // Обозначения: '.' белая клетка, '#' чёрная без числа, цифра — чёрная с числом.
    public void print(boolean[][] lamps) {
        for (int r = 0; r < rows; r++) {
            String line = "  ";
            for (int c = 0; c < cols; c++) {
                char symbol;
                if (lamps != null && lamps[r][c]) {
                    symbol = 'L';
                } else if (cells[r][c] == WHITE) {
                    symbol = '.';
                } else if (cells[r][c] == BLACK) {
                    symbol = '#';
                } else {
                    symbol = (char) ('0' + cells[r][c]);  // число 3 превращаем в символ '3'
                }
                line = line + symbol + " ";
            }
            System.out.println(line);
        }
    }
}
