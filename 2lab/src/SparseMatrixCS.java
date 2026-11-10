// Разреженная матрица в координатном формате CS (Coordinate Storage).
// Для каждого ненулевого значения храним отдельно строку, столбец и значение.
// Внутри класса не создаётся плотная матрица для выполнения сдвига.
public class SparseMatrixCS {
    private final int rowsCount;      // число строк матрицы
    private final int colsCount;      // число столбцов матрицы
    private final int[] rowIndex;     // строка каждого ненулевого элемента (нумерация с 0)
    private final int[] colIndex;     // столбец каждого ненулевого элемента (нумерация с 0)
    private final int[] values;       // значение каждого ненулевого элемента
    private int count;                // сколько элементов уже добавлено

    // Создаёт пустую матрицу.
    // rowsCount — число строк, colsCount — число столбцов,
    // capacity — максимум ненулевых элементов (размер массивов).
    public SparseMatrixCS(int rowsCount, int colsCount, int capacity) {
        if (rowsCount <= 0 || colsCount <= 0 || capacity < 0) {
            throw new IllegalArgumentException("Некорректный размер матрицы");
        }
        this.rowsCount = rowsCount;
        this.colsCount = colsCount;
        this.rowIndex = new int[capacity];
        this.colIndex = new int[capacity];
        this.values = new int[capacity];
        this.count = 0;
    }

    // Добавляет ненулевой элемент в матрицу.
    // row, col — координаты с нумерацией от 1; value — ненулевое значение.
    // Проверяет границы, ненулевость, место в массивах и отсутствие дубликата координаты.
    public void add(int row, int col, int value) {
        if (row < 1 || row > rowsCount || col < 1 || col > colsCount) {
            throw new IllegalArgumentException("Координата находится вне матрицы");
        }
        if (value == 0) {
            throw new IllegalArgumentException("В CS сохраняем только ненулевые значения");
        }
        if (count >= values.length) {
            throw new IllegalStateException("Введено больше элементов, чем было объявлено");
        }

        // Не разрешаем две записи для одной координаты.
        for (int i = 0; i < count; i++) {
            if (rowIndex[i] == row - 1 && colIndex[i] == col - 1) {
                throw new IllegalArgumentException("Координата уже была введена");
            }
        }

        rowIndex[count] = row - 1;
        colIndex[count] = col - 1;
        values[count] = value;
        count++;
    }

    // Циклический сдвиг столбцов вправо на n позиций: столбец c переходит в (c + n) mod число_столбцов.
    // Меняется только столбец элемента; строки остаются прежними.
    // Аргумент n: величина сдвига; отрицательное значение даёт сдвиг вверх.
    public void shiftColumnsRight(int n) {
        // приводим сдвиг к диапазону 0..colsCount-1
        int shift = ((n % colsCount) + colsCount) % colsCount;
        for (int i = 0; i < count; i++) {
            colIndex[i] = (colIndex[i] + shift) % colsCount;
        }
    }

    // Печатает ненулевые элементы в формате «строка, столбец, значение».
    // Аргументов нет. Используется для показа результата.
    public void printCS() {
        System.out.println("Строка  Столбец  Значение");
        for (int i = 0; i < count; i++) {
            System.out.printf("%6d %8d %9d%n",
                    rowIndex[i] + 1, colIndex[i] + 1, values[i]);
        }
        if (count == 0) {
            System.out.println("(ненулевых элементов нет)");
        }
    }

    // Печатает полную матрицу с нулями. Только для показа, к задаче сдвига не относится.
    // Аргументов нет. Для каждой ячейки вызывает valueAt.
    public void printDense() {
        for (int row = 0; row < rowsCount; row++) {
            for (int col = 0; col < colsCount; col++) {
                int value = valueAt(row, col);
                System.out.printf("%5d", value);
            }
            System.out.println();
        }
    }

    // Ищет значение в ячейке (row, col) с индексами от 0. Если элемента нет, возвращает 0.
    // Вызывается только из printDense.
    private int valueAt(int row, int col) {
        for (int i = 0; i < count; i++) {
            if (rowIndex[i] == row && colIndex[i] == col) {
                return values[i];
            }
        }
        return 0;
    }
}