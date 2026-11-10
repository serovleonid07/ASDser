/*
 * Задача 17. Дана разреженная матрица (CS). Осуществить циклический сдвиг в матрице каждого столбца на n разрядов.
 */
public class Main {
    // Точка входа программы. Аргумент args не используется.
    // Задаёт матрицу 4x4, сдвигает её на n позиций и печатает до и после.
    public static void main(String[] args) {
        // Матрица 4x4, заданная ненулевыми элементами: строка, столбец, значение (нумерация с 1)
        int rows = 4;
        int cols = 4;
        int[][] nonZero = {
            {1, 1, 5},
            {2, 1, 7},
            {3, 2, 9},
            {1, 3, 4},
            {4, 4, 2}
        };
        // На сколько позиций сдвигать столбцы вправо (отрицательное значение - влево)
        int n = 1;

        // Создаём матрицу и добавляем в неё каждый ненулевой элемент из массива.
        SparseMatrixCS matrix = new SparseMatrixCS(rows, cols, nonZero.length);
        for (int[] e : nonZero) {
            matrix.add(e[0], e[1], e[2]);
        }

        System.out.println("Лабораторная № 2. Разреженные матрицы");
        System.out.println("\nИсходная матрица:");
        matrix.printDense();

        matrix.shiftColumnsRight(n);

        System.out.println("\nМатрица после циклического сдвига:");
        matrix.printDense();

        System.out.println("\nНенулевые элементы в координатном формате CS:");
        matrix.printCS();
    }
}