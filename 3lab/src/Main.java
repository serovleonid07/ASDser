// Точка входа: дерево задано в коде, поиск и вывод выполняются для него.
public class Main {
    // Строит дерево, печатает его в прямом обходе и результат поиска.
    // args не используется.
    public static void main(String[] args) {
        TreeNode root = new TreeNode(8);
        root.left = new TreeNode(3);
        root.right = new TreeNode(9);
        root.left.left = new TreeNode(2);
        root.left.right = new TreeNode(6);
        root.right.right = new TreeNode(14);
        root.left.right.left = new TreeNode(5);
        root.left.right.right = new TreeNode(7);
        root.right.right.left = new TreeNode(11);
        root.right.right.right = new TreeNode(13);

        System.out.print("Дерево (прямой обход): ");
        BinaryTree.printPreorder(root);
        System.out.println();

        System.out.println("Поддеревья, в которых каждая вершина имеет номер нужной чётности:");
        BinaryTree.findMatchingSubtrees(root);
    }
}