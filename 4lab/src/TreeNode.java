// Узел N-дерева: значение и массив детей (детей может быть сколько угодно).
public class TreeNode {
    int value;            // значение (номер) вершины
    TreeNode[] children;  // массив детей; занята только его первая часть
    int childCount;       // сколько детей уже добавлено

    // Создаёт вершину без детей. Аргумент value: значение вершины.
    public TreeNode(int value) {
        this.value = value;
        this.children = new TreeNode[2];  // начальный размер, дальше массив растёт сам
        this.childCount = 0;
    }

    // Добавляет ребёнка child в конец списка детей.
    // Если массив уже заполнен, делаем новый массив в два раза больше
    // и копируем в него всех старых детей.
    public void addChild(TreeNode child) {
        if (childCount == children.length) {
            TreeNode[] bigger = new TreeNode[children.length * 2];
            for (int i = 0; i < childCount; i++) {
                bigger[i] = children[i];
            }
            children = bigger;
        }
        children[childCount] = child;
        childCount++;
    }
}
