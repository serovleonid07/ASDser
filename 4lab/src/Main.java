/*
 * Задача 17. Дано N-дерево. Найти самый длинный в дереве путь,
 * проходящий только по вершинам с нечётными номерами.
 */
public class Main {
    // Точка входа программы. Аргумент args не используется.
    // Строит два дерева, печатает их и ищет в каждом самый длинный нечётный путь.
    public static void main(String[] args) {
        System.out.println("Лабораторная № 4. Рекурсия. N-деревья.");

        // ---------- Первое дерево ----------
        // Корень 1 и три ребёнка: 3, 4, 5.
        TreeNode root = new TreeNode(1);
        TreeNode node3 = new TreeNode(3);
        TreeNode node4 = new TreeNode(4);
        TreeNode node5 = new TreeNode(5);
        root.addChild(node3);
        root.addChild(node4);
        root.addChild(node5);

        // Дети вершины 3: 7, 9, 2.
        TreeNode node7 = new TreeNode(7);
        TreeNode node9 = new TreeNode(9);
        node3.addChild(node7);
        node3.addChild(node9);
        node3.addChild(new TreeNode(2));

        // Дети вершины 7: 5 и 8.
        TreeNode node5low = new TreeNode(5);
        node7.addChild(node5low);
        node7.addChild(new TreeNode(8));

        // Вершина 5 (под семёркой) ведёт дальше к 11, а у 11 дети 17 и 6.
        TreeNode node11 = new TreeNode(11);
        node5low.addChild(node11);
        node11.addChild(new TreeNode(17));
        node11.addChild(new TreeNode(6));

        // Остальные ветки.
        node9.addChild(new TreeNode(13));
        node4.addChild(new TreeNode(11));  // под чётной 4: путь начнётся только с 11
        node5.addChild(new TreeNode(13));
        node5.addChild(new TreeNode(6));

        // Сначала печатаем дерево целиком, потом ищем путь.
        System.out.println("\nДерево 1 (полностью):");
        PathFinder.printTree(root);
        System.out.println();
        PathFinder.findLongestOddPath(root);

        // ---------- Второе дерево: все вершины чётные ----------
        TreeNode evenRoot = new TreeNode(2);
        evenRoot.addChild(new TreeNode(4));
        evenRoot.addChild(new TreeNode(6));

        System.out.println("\nДерево 2 (полностью):");
        PathFinder.printTree(evenRoot);
        System.out.println();
        PathFinder.findLongestOddPath(evenRoot);
    }
}
