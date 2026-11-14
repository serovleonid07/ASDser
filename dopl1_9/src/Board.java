// Поле головоломки «Нумератор» (Numbrix).
// Поле — прямоугольная сетка. В некоторых клетках уже стоят числа.
// Нужно заполнить пустые клетки так, чтобы числа от 1 до rows * cols
// шли подряд: каждое следующее число стоит в соседней клетке
// (сверху, снизу, слева или справа; по диагонали ходить нельзя).
public class Board {
    // Четыре направления: вверх, вниз, влево, вправо.
    // Для направления d сдвиг по строке равен ROW_STEP[d], по столбцу COL_STEP[d].
    public static final int[] ROW_STEP = {-1, 1, 0, 0};
    public static final int[] COL_STEP = {0, 0, -1, 1};

    int rows;           // сколько строк в поле
    int cols;           // сколько столбцов в поле
    int[][] cells;      // число в клетке; 0 значит, что клетка пустая
    boolean[][] fixed;  // true, если число в клетке дано в условии (его менять нельзя)

    // Строит поле из текста. Каждая строка массива lines — одна строка поля.
    // Клетки в строке разделены пробелами: '.' — пустая клетка, число — готовое число.
    public Board(String[] lines) {
        rows = lines.length;
        String[] firstLine = lines[0].trim().split("\\s+");
        cols = firstLine.length;
        cells = new int[rows][cols];
        fixed = new boolean[rows][cols];

        int total = rows * cols;
        boolean[] used = new boolean[total + 1];  // какие числа уже встретились

        for (int r = 0; r < rows; r++) {
            String[] parts = lines[r].trim().split("\\s+");
            if (parts.length != cols) {
                throw new IllegalArgumentException("Все строки поля должны быть одной длины");
            }
            for (int c = 0; c < cols; c++) {
                if (parts[c].equals(".")) {
                    continue;  // пустая клетка, в cells уже 0
                }
                int number = Integer.parseInt(parts[c]);
                if (number < 1 || number > total) {
                    throw new IllegalArgumentException("Число вне диапазона 1.." + total + ": " + number);
                }
                if (used[number]) {
                    throw new IllegalArgumentException("Число встречается дважды: " + number);
                }
                used[number] = true;
                cells[r][c] = number;
                fixed[r][c] = true;
            }
        }
    }

    // Проверяет, что клетка (r, c) лежит внутри поля.
    public boolean isInside(int r, int c) {
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }

    // Проверяет, что клетка (r, c) пустая.
    public boolean isEmpty(int r, int c) {
        return cells[r][c] == 0;
    }

    // Делает копию текущего заполнения поля (чтобы запомнить найденное решение).
    public int[][] copyCells() {
        int[][] copy = new int[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                copy[r][c] = cells[r][c];
            }
        }
        return copy;
    }

    // Печатает поле. values — числа, которые надо показать (например, найденное решение),
    // или null, если нужно показать текущее поле (условие).
    // Пустая клетка печатается как '.'.
    public void print(int[][] values) {
        int[][] shown = values;
        if (shown == null) {
            shown = cells;
        }
        int width = String.valueOf(rows * cols).length();  // ширина самого большого числа

        for (int r = 0; r < rows; r++) {
            String line = " ";
            for (int c = 0; c < cols; c++) {
                String text = ".";
                if (shown[r][c] != 0) {
                    text = String.valueOf(shown[r][c]);
                }
                // Дополняем пробелами слева, чтобы столбцы стояли ровно.
                while (text.length() < width) {
                    text = " " + text;
                }
                line = line + " " + text;
            }
            System.out.println(line);
        }
    }
}
