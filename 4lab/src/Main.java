/*
 * Задача 17. Дано N-дерево. Найти самый длинный в дереве путь,
 * проходящий только по вершинам с нечётными номерами.
 */
public class Main {
    // Точка входа программы. Аргумент args не используется.
    // Строит два дерева, печатает их целиком и ищет в каждом самый длинный нечётный путь.
    public static void main(String[] args) {
        System.out.println("Лабораторная № 4. Рекурсия. N-деревья.");

        // ---------- Первое дерево ----------
        // Имя переменной показывает путь до вершины: v3_7_5 — это вершина 5,
        // которая лежит под 7, а та под 3, а 3 под корнем.
        TreeNode root = new TreeNode(1);

        // Четыре ребёнка корня: 3, 4, 5, 8.
        TreeNode v3 = add(root, 3);
        TreeNode v4 = add(root, 4);
        TreeNode v5 = add(root, 5);
        TreeNode v8 = add(root, 8);

        // Ветка 3: здесь самый длинный нечётный путь.
        TreeNode v3_7 = add(v3, 7);
        TreeNode v3_7_5 = add(v3_7, 5);
        TreeNode v3_7_5_11 = add(v3_7_5, 11);
        TreeNode v3_7_5_11_17 = add(v3_7_5_11, 17);
        add(v3_7_5_11_17, 19);
        add(v3_7_5_11_17, 20);
        add(v3_7_5_11, 6);
        TreeNode v3_7_5_9 = add(v3_7_5, 9);
        add(v3_7_5_9, 4);
        TreeNode v3_7_8 = add(v3_7, 8);
        TreeNode v3_7_8_13 = add(v3_7_8, 13);
        add(v3_7_8_13, 15);

        TreeNode v3_9 = add(v3, 9);
        TreeNode v3_9_13 = add(v3_9, 13);
        add(v3_9_13, 21);
        add(v3_9_13, 2);
        TreeNode v3_9_10 = add(v3_9, 10);
        add(v3_9_10, 25);

        TreeNode v3_2 = add(v3, 2);
        TreeNode v3_2_7 = add(v3_2, 7);
        add(v3_2_7, 9);

        // Ветка 4: сама вершина 4 чётная, поэтому путь тут может начаться только с 11.
        TreeNode v4_11 = add(v4, 11);
        TreeNode v4_11_15 = add(v4_11, 15);
        TreeNode v4_11_15_17 = add(v4_11_15, 17);
        TreeNode v4_11_15_17_23 = add(v4_11_15_17, 23);
        add(v4_11_15_17_23, 27);
        add(v4, 6);

        // Ветка 5.
        TreeNode v5_13 = add(v5, 13);
        add(v5_13, 7);
        TreeNode v5_12 = add(v5, 12);
        add(v5_12, 3);
        add(v5, 9);

        // Ветка 8: начинается с чётной вершины.
        TreeNode v8_21 = add(v8, 21);
        TreeNode v8_21_33 = add(v8_21, 33);
        add(v8_21_33, 35);

        // Сначала печатаем дерево целиком, потом ищем путь.
        System.out.println("\nДерево 1 (полностью):");
        PathFinder.printTree(root);
        System.out.println();
        PathFinder.findLongestOddPath(root);

        // ---------- Второе дерево: все вершины чётные ----------
        TreeNode evenRoot = new TreeNode(2);
        TreeNode even4 = add(evenRoot, 4);
        add(evenRoot, 6);
        add(even4, 8);
        add(even4, 10);

        System.out.println("\nДерево 2 (полностью):");
        PathFinder.printTree(evenRoot);
        System.out.println();
        PathFinder.findLongestOddPath(evenRoot);
    }

    // Создаёт вершину со значением value, делает её ребёнком вершины parent
    // и возвращает созданную вершину, чтобы к ней можно было добавлять своих детей.
    private static TreeNode add(TreeNode parent, int value) {
        TreeNode child = new TreeNode(value);
        parent.addChild(child);
        return child;
    }
}
