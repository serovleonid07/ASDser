// Задача 17. Даны две непустые очереди. Элементы каждой из очередей упорядочены по возрастанию.
// Объединить очереди в одну с сохранением упорядоченности элементов.
public class Main {
    // Точка входа программы. Аргумент args не используется.
    // Создаёт две очереди, сливает их в result и печатает результат.
    public static void main(String[] args) {
        // Две очереди, заполненные уже отсортированными числами.
        Queue first = new Queue();
        for (int value : new int[]{-2, 1, 3, 4, 5, 7, 9, 12}) {
            first.enqueue(value);
        }

        Queue second = new Queue();
        for (int value : new int[]{-5, 0, 2, 6, 8, 10, 11}) {
            second.enqueue(value);
        }

        Queue result = new Queue();

        System.out.println("Лабораторная № 1. Списки. Стеки. Очереди.");
        System.out.println("Первая очередь: " + first);
        System.out.println("Вторая очередь: " + second);

        // Пока обе очереди не пусты, переносим меньший из первых элементов.
        while (!first.isEmpty() && !second.isEmpty()) {
            if (first.peek() <= second.peek()) {
                result.enqueue(first.dequeue());
            } else {
                result.enqueue(second.dequeue());
            }
        }

        // В одной из очередей могли остаться элементы.
        while (!first.isEmpty()) {
            result.enqueue(first.dequeue());
        }
        while (!second.isEmpty()) {
            result.enqueue(second.dequeue());
        }

        System.out.println("Результат слияния: " + result);
    }
}