// Узел бинарного дерева: значение и ссылки на двух возможных детей.
public class TreeNode {
    int value;      // значение (номер) вершины
    TreeNode left;  // левый потомок или null
    TreeNode right; // правый потомок или null

    // Создаёт вершину без потомков. Аргумент value: значение вершины.
    public TreeNode(int value) {
        this.value = value;
        this.left = null;
        this.right = null;
    }
}