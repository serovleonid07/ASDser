package backend;

// Таблица уже виденных позиций (быстрый поиск ситуаций, которые уже были рассмотрены).
// Это своя хэш-таблица на массивах. Ключ — хэш позиции (число long из GameState).
// Если позиция уже оценена на достаточную глубину, берём готовый ответ и не считаем заново.
public class TranspositionTable {
    // Тип сохранённой оценки (нужен для альфа-бета отсечения).
    public static final int EXACT = 0;        // оценка точная
    public static final int LOWER_BOUND = 1;  // истинная оценка не меньше сохранённой
    public static final int UPPER_BOUND = 2;  // истинная оценка не больше сохранённой

    private final int size;          // сколько ячеек в таблице (степень двойки)
    private final long[] keys;       // хэши позиций
    private final int[] depths;      // на какую глубину считали
    private final int[] values;      // оценка
    private final int[] flags;       // тип оценки (EXACT, LOWER_BOUND, UPPER_BOUND)
    private final boolean[] used;    // занята ли ячейка

    // Создаёт таблицу на size ячеек. size должно быть степенью двойки (например, 1 << 18).
    public TranspositionTable(int size) {
        this.size = size;
        keys = new long[size];
        depths = new int[size];
        values = new int[size];
        flags = new int[size];
        used = new boolean[size];
    }

    // Находит номер ячейки по хэшу: берём остаток от деления на size (через маску битов).
    private int indexOf(long hash) {
        return (int) (hash & (size - 1));
    }

    // Очищает таблицу (например, перед новой партией).
    public void clear() {
        for (int i = 0; i < size; i++) {
            used[i] = false;
        }
    }

    // Записывает результат. Старая запись в этой ячейке просто заменяется.
    public void store(long hash, int depth, int value, int flag) {
        int index = indexOf(hash);
        keys[index] = hash;
        depths[index] = depth;
        values[index] = value;
        flags[index] = flag;
        used[index] = true;
    }

    // Возвращает номер ячейки, если позиция есть в таблице и считалась не мельче depth. Иначе -1.
    public int find(long hash, int depth) {
        int index = indexOf(hash);
        if (used[index] && keys[index] == hash && depths[index] >= depth) {
            return index;
        }
        return -1;
    }

    // Возвращает сохранённую оценку из ячейки index.
    public int getValue(int index) {
        return values[index];
    }

    // Возвращает тип сохранённой оценки из ячейки index.
    public int getFlag(int index) {
        return flags[index];
    }
}
