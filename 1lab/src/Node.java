// Один узел односвязного списка.
// data — значение, next — ссылка на следующий узел.
public class Node {
    int data;   // значение, которое хранит узел
    Node next;  // ссылка на следующий узел или null

    // Создаёт узел со значением data. Аргумент data: число для хранения.
    // Поле next сразу равно null, узел пока ни с чем не связан.
    public Node(int data) {
        this.data = data;
        this.next = null;
    }
}