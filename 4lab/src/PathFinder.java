// Поиск самого длинного пути по вершинам с нечётными номерами в N-дереве.
// Путь идёт сверху вниз: от вершины к её ребёнку, потом к ребёнку ребёнка и так далее.
// Путь может начинаться в любой вершине, не обязательно в корне.
// Длина пути считается в вершинах.
public class PathFinder {
    private static TreeNode bestStart;  // вершина, с которой начинается лучший найденный путь
    private static int bestLength;      // длина лучшего найденного пути

    // Проверяет, что число number нечётное.
    // Для отрицательных чисел тоже работает: -3 % 2 даёт -1, а это не ноль.
    private static boolean isOdd(int number) {
        return number % 2 != 0;
    }

    // Печатает дерево: каждый следующий уровень сдвинут вправо на 4 пробела.
    // node — текущая вершина; depth — её глубина (у корня 0).
    public static void printTree(TreeNode node, int depth) {
        if (node == null) {
            return;
        }
        for (int i = 0; i < depth; i++) {
            System.out.print("    ");
        }
        System.out.println(node.value);

        for (int i = 0; i < node.childCount; i++) {
            printTree(node.children[i], depth + 1);
        }
    }

    // Считает длину самого длинного нечётного пути, который НАЧИНАЕТСЯ в вершине node.
    // Если сама вершина чётная, пути нет, и возвращаем 0.
    // Иначе: 1 (сама вершина) + самый длинный путь среди её детей.
    private static int chainLength(TreeNode node) {
        if (node == null || !isOdd(node.value)) {
            return 0;
        }

        int longestBelow = 0;  // самый длинный путь вниз среди детей
        for (int i = 0; i < node.childCount; i++) {
            int length = chainLength(node.children[i]);
            if (length > longestBelow) {
                longestBelow = length;
            }
        }
        return 1 + longestBelow;
    }

    // Запускает поиск и печатает результат.
    // root — корень дерева.
    public static void findLongestOddPath(TreeNode root) {
        bestStart = null;
        bestLength = 0;
        inspect(root);

        if (bestStart == null) {
            System.out.println("В дереве нет вершин с нечётными номерами, пути нет.");
            return;
        }

        System.out.println("Длина самого длинного пути: " + bestLength + " (вершин)");
        System.out.print("Путь: ");
        printPath(bestStart);
        System.out.println();
    }

    // Обходит все вершины дерева. Для каждой считает, какой самый длинный путь
    // начинается в ней, и запоминает лучший результат.
    // node — текущая вершина.
    private static void inspect(TreeNode node) {
        if (node == null) {
            return;
        }

        int length = chainLength(node);
        if (length > bestLength) {
            bestLength = length;
            bestStart = node;
        }

        for (int i = 0; i < node.childCount; i++) {
            inspect(node.children[i]);
        }
    }

    // Печатает самый длинный путь, который начинается в вершине node.
    // На каждом шаге выбираем ребёнка, у которого путь вниз самый длинный.
    private static void printPath(TreeNode node) {
        System.out.print(node.value);

        TreeNode next = null;     // ребёнок, в которого идём дальше
        int nextLength = 0;       // длина пути, который начинается в этом ребёнке
        for (int i = 0; i < node.childCount; i++) {
            int length = chainLength(node.children[i]);
            if (length > nextLength) {
                nextLength = length;
                next = node.children[i];
            }
        }

        // next == null значит, что дальше идти некуда: путь закончился.
        if (next != null) {
            System.out.print(" -> ");
            printPath(next);
        }
    }
}
