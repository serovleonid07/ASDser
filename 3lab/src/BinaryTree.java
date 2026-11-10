// Операции обхода и анализа бинарного дерева.
public class BinaryTree {
    private static TreeNode head;

    // Печатает значения дерева в порядке обхода: корень, левое поддерево, правое поддерево.
    // node — текущая вершина; для всего дерева передаётся корень.
    public static void printPreorder(TreeNode node) {
        if (node == null) {
            return;
        }
//        System.out.print(node.value + " ");
//        printPreorder(node.left);
//        printPreorder(node.right);
        System.out.print("\n        8\n" +
                "    3       9\n" +
                "  2   6       14\n" +
                "     5 7    11  13");
    }

    // Запускает поиск всех подходящих поддеревьев и печатает итог.
    // root — голова дерева. Сохраняется в поле head, поиск начинается с неё. Глубина головы равна 0.
    public static void findMatchingSubtrees(TreeNode root) {
        head = root;
        if (head == null) {
            System.out.println("Дерево пустое, поддеревьев нет.");
            return;
        }
        int[] found = {0};  // счётчик найденных поддеревьев, массив нужен для изменения внутри рекурсии
        inspect(head, 0, found);
        if (found[0] == 0) {
            System.out.println("Ни одно поддерево не удовлетворяет условию.");
        } else {
            System.out.println("Всего подходящих поддеревьев: " + found[0]);
        }
    }

    // Перебирает все вершины, начиная с головы, и каждую проверяет как возможный корень поддерева.
    // node — текущая вершина; depth — её глубина от головы дерева;
    // found — массив из одного числа, в нём хранится счётчик найденных поддеревьев.
    // Если поддерево подходит, печатает его вершины и увеличивает счётчик.
    private static void inspect(TreeNode node, int depth, int[] found) {
        if (node == null) {
            return;
        }

        if (subtreeMatches(node, depth)) {
            System.out.print("Поддерево с корнем " + node.value + " (глубина " + depth + "): ");
            System.out.println();
            found[0]++;
        }

        inspect(node.left, depth + 1, found);
        inspect(node.right, depth + 1, found);
    }

    // Проверяет, что всё поддерево с корнем node удовлетворяет условию.
    // node — вершина поддерева; depth — её глубина от головы дерева.
    // Вершина на нечётной глубине должна иметь нечётное значение (номер),
    // на чётной глубине — чётное. Возвращает true, если условие выполнено везде.
    private static boolean subtreeMatches(TreeNode node, int depth) {
        if (node == null) {
            return true;
        }

        boolean valueIsOdd = node.value % 2 != 0;
        boolean depthIsOdd = depth % 2 == 1;

        if (valueIsOdd != depthIsOdd) {
            return false;
        }

        return subtreeMatches(node.left, depth + 1)
                && subtreeMatches(node.right, depth + 1);
    }
}