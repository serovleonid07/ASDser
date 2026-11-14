public class Board {
    public static final int[] ROW_STEP = {-1, 1, 0, 0};
    public static final int[] COL_STEP = {0, 0, -1, 1};
    int[][] cells;
    boolean[][] fixed;
    int rows, cols;

    public Board(String[] lines) {
        if (lines == null || lines.length == 0) throw new IllegalArgumentException("Поле пустое.");
        rows = lines.length;
        String[] first = lines[0].trim().split("\\s+");
        cols = first.length;
        cells = new int[rows][cols];
        fixed = new boolean[rows][cols];
        boolean[] used = new boolean[rows * cols + 1];

        for (int r = 0; r < rows; r++) {
            String[] parts = lines[r].trim().split("\\s+");
            if (parts.length != cols) throw new IllegalArgumentException("Строки должны иметь одинаковую длину.");
            for (int c = 0; c < cols; c++) {
                if (parts[c].equals(".")) continue;
                int value;
                try {
                    value = Integer.parseInt(parts[c]);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Допустимы только точки и целые числа.");
                }
                if (value < 1 || value > rows * cols)
                    throw new IllegalArgumentException("Число должно быть от 1 до " + (rows * cols) + ".");
                if (used[value]) throw new IllegalArgumentException("Число " + value + " повторяется.");
                used[value] = true;
                cells[r][c] = value;
                fixed[r][c] = true;
            }
        }
    }

    public boolean isInside(int r, int c) {
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }

    public boolean isEmpty(int r, int c) {
        return cells[r][c] == 0;
    }

    public int[][] copyCells() {
        int[][] copy = new int[rows][cols];
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++) copy[r][c] = cells[r][c];
        return copy;
    }

    public void print(int[][] values) {
        int[][] data = values == null ? cells : values;
        int width = String.valueOf(rows * cols).length();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                String s = data[r][c] == 0 ? "." : String.valueOf(data[r][c]);
                while (s.length() < width) s = " " + s;
                System.out.print(s + (c == cols - 1 ? "" : " "));
            }
            System.out.println();
        }
    }
}
