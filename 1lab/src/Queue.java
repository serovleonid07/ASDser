// Собственная очередь на односвязном списке.
// FIFO: первым извлекается элемент, который был добавлен раньше.
public class Queue {
    private Node front; // начало очереди — отсюда удаляем
    private Node rear;  // конец очереди — сюда добавляем

    // Создаёт пустую очередь. Вызывается при new Queue().
    public Queue() {
        front = null;
        rear = null;
    }

    // Возвращает true, если в очереди нет элементов.
    // Вызывается из enqueue, dequeue и Main.
    public boolean isEmpty() {
        return front == null;
    }

    // Добавляет значение value в конец очереди.
    // Аргумент value: целое число, которое нужно поставить в очередь.
    public void enqueue(int value) {
        Node node = new Node(value);

        if (isEmpty()) {
            // Первый узел одновременно и начало, и конец очереди.
            front = node;
            rear = node;
        } else {
            rear.next = node;
            rear = node;
        }
    }

    // Удаляет первый элемент и возвращает его.
    // Аргументов нет. На пустой очереди выбрасывает IllegalStateException.
    public int dequeue() {
        if (isEmpty()) {
            throw new IllegalStateException("Нельзя извлечь элемент из пустой очереди");
        }

        int value = front.data;
        front = front.next;

        // Если удалили последний элемент, конец тоже должен стать null.
        if (front == null) {
            rear = null;
        }
        return value;
    }

    // Возвращает первый элемент, не удаляя его.
    // Аргументов нет. На пустой очереди выбрасывает IllegalStateException.
    public int peek() {
        if (isEmpty()) {
            throw new IllegalStateException("Пустая очередь не имеет первого элемента");
        }
        return front.data;
    }

    // Превращает очередь в строку вида [1, 2, 3] для печати.
    // Вызывается автоматически при System.out.println(очередь).
    @Override
    public String toString() {
        StringBuilder text = new StringBuilder("[");
        Node current = front;

        while (current != null) {
            text.append(current.data);
            if (current.next != null) {
                text.append(", ");
            }
            current = current.next;
        }

        text.append("]");
        return text.toString();
    }
}