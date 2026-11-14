/*
 * Задача 17. Реализовать пирамидальную сортировку.
 */
public class Main {
    // Точка входа программы. Аргумент args не используется.
    // Сортирует массив пирамидальной сортировкой и показывает каждый шаг.
    public static void main(String[] args) {
        System.out.println("Лабораторная № 6. Методы сортировки.");

        // Массив с повторами и отрицательными числами.
        int[] data = {5, -3, 12, 8, 0, 7, 12, -9, 4, 1, 45, 12, -99};

        System.out.println();
        HeapSort.printArray("Исходный массив", data);

        // Тот же массив, нарисованный как дерево. Пока это ещё не пирамида:
        // у родителей бывают дети больше них.
        System.out.println();
        HeapSort.printAsPyramid("Исходный массив в виде дерева", data);

        System.out.println("\nХод сортировки (слева от | пирамида, справа готовая часть):\n");
        HeapSort.sort(data, true);

        System.out.println();
        HeapSort.printArray("Результат", data);
        if (HeapSort.isSorted(data)) {
            System.out.println("Проверка: массив отсортирован");
        } else {
            System.out.println("Проверка: ОШИБКА, массив не отсортирован");
        }

        // Крайние случаи: пустой массив и массив из одного элемента.
        int[] empty = {};
        int[] single = {42};
        HeapSort.sort(empty, false);
        HeapSort.sort(single, false);
        System.out.println();
        HeapSort.printArray("Пустой массив", empty);
        HeapSort.printArray("Один элемент", single);
    }
}
