package backend;

// Собственный список ходов на обычном массиве фиксированного размера.
// (Готовые коллекции вроде ArrayList по заданию использовать нельзя.)
public class MoveList {
    // Максимум ходов в одной позиции: у гусей не больше 17 * 4 = 68, у лисы не больше 16.
    // Берём с запасом.
    private static final int CAPACITY = 128;

    private final Move[] items = new Move[CAPACITY];
    private int count = 0;  // сколько ходов лежит в списке сейчас

    // Очищает список. Сами объекты не стираем, просто начинаем писать с начала.
    public void clear() {
        count = 0;
    }

    // Добавляет ход в конец списка. Если место кончилось, выбрасывает ошибку.
    public void add(Move move) {
        if (count >= CAPACITY) {
            throw new IllegalStateException("Список ходов переполнен");
        }
        items[count] = move;
        count++;
    }

    // Возвращает ход с номером index (нумерация с 0).
    public Move get(int index) {
        return items[index];
    }

    // Возвращает, сколько ходов в списке.
    public int size() {
        return count;
    }

    // Меняет местами два хода с номерами i и j. Нужно для сортировки ходов.
    public void swap(int i, int j) {
        Move temp = items[i];
        items[i] = items[j];
        items[j] = temp;
    }
}
