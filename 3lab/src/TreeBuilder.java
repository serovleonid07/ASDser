// Строит бинарное дерево из лексем прямого обхода, # — пустой узел.
public class TreeBuilder {
    private final String[] tokens;
    private int position = 0;

    // tokens — лексемы входной строки.
    public TreeBuilder(String[] tokens) {
        this.tokens = tokens;
    }

    // Строит дерево и возвращает его корень.
    public TreeNode build() {
        return buildRecursive();
    }

    // Берёт следующую лексему: # даёт пустой узел, число создаёт вершину и строит её поддеревья.
    private TreeNode buildRecursive() {
        if (position >= tokens.length) {
            throw new IllegalArgumentException("Недостаточно данных для дерева");
        }
        String token = tokens[position++];
        if (token.equals("#")) {
            return null;
        }
        TreeNode node = new TreeNode(Integer.parseInt(token));
        node.left = buildRecursive();
        node.right = buildRecursive();
        return node;
    }

    // Возвращает true, если после построения остались непрочитанные лексемы.
    public boolean hasUnusedTokens() {
        return position < tokens.length;
    }
}