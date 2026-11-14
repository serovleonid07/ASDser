// Хеш-таблица: ключ — СНИЛС, значение — ФИО владельца.
// Хеш-функция: метод умножения.
// Коллизии (две записи хотят одну ячейку): квадратичное опробование.
// Размер таблицы всегда простое число, а заполнена она не больше чем наполовину —
// тогда квадратичное опробование гарантированно найдёт свободную ячейку.
// Когда таблица заполняется, она увеличивается вдвое (рехеширование).
public class HashTable {
    private static final int EMPTY = 0;    // ячейка ещё ни разу не использовалась
    private static final int FILLED = 1;   // в ячейке лежит запись
    private static final int DELETED = 2;  // запись удалили (ячейка-«могила», см. remove)

    // Число A для метода умножения: (корень из 5 минус 1) делить на 2, золотое сечение.
    private static final double GOLDEN = 0.6180339887498949;

    private String[] keys;    // СНИЛС: ровно 11 цифр, без дефисов и пробелов
    private String[] names;   // ФИО владельца
    private int[] states;     // состояние каждой ячейки: EMPTY, FILLED или DELETED
    private int size;         // размер таблицы (простое число)
    private int filledCount;  // сколько ячеек в состоянии FILLED
    private int usedCount;    // сколько ячеек в состоянии FILLED или DELETED

    // Создаёт пустую таблицу.
    // startSize — желаемый начальный размер; берём ближайшее простое число не меньше него.
    public HashTable(int startSize) {
        if (startSize < 2) {
            throw new IllegalArgumentException("Размер таблицы должен быть не меньше 2");
        }
        this.size = nextPrime(startSize);
        this.keys = new String[size];
        this.names = new String[size];
        this.states = new int[size];   // сразу заполнен нулями, то есть EMPTY
        this.filledCount = 0;
        this.usedCount = 0;
    }

    // ---------- Работа с ключом (СНИЛС) ----------

    // Превращает текст СНИЛС в строку из 11 цифр.
    // Дефисы и пробелы выбрасываем: "123-456-789 01" становится "12345678901".
    // Любой другой символ или неверное число цифр — ошибка.
    public static String cleanKey(String text) {
        String digits = "";
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= '0' && c <= '9') {
                digits = digits + c;
            } else if (c != '-' && c != ' ') {
                throw new IllegalArgumentException("В СНИЛС могут быть только цифры, дефисы и пробелы: " + text);
            }
        }
        if (digits.length() != 11) {
            throw new IllegalArgumentException("В СНИЛС должно быть 11 цифр: " + text);
        }
        return digits;
    }

    // Делает из 11 цифр красивую запись вида 123-456-789 01 (для печати).
    public static String formatKey(String digits) {
        return digits.substring(0, 3) + "-" + digits.substring(3, 6) + "-"
                + digits.substring(6, 9) + " " + digits.substring(9, 11);
    }

    // ---------- Хеш-функция и опробование ----------

    // Метод умножения. Шаги:
    // 1) превращаем ключ в число k;
    // 2) считаем k * A и берём дробную часть (всё, что после запятой);
    // 3) умножаем дробную часть на размер таблицы и отбрасываем дробь — это номер ячейки.
    private int hash(String key) {
        long number = Long.parseLong(key);
        double product = number * GOLDEN;
        double fraction = product - Math.floor(product);  // дробная часть, число от 0 до 1
        int index = (int) (size * fraction);
        if (index >= size) {
            index = size - 1;  // защита от ошибок округления
        }
        return index;
    }

    // Квадратичное опробование: номер ячейки на попытке step.
    // start — ячейка из хеш-функции. Попытка 0: start, попытка 1: start + 1,
    // попытка 2: start + 4, попытка 3: start + 9 и так далее (всегда по модулю размера).
    private int probe(int start, int step) {
        return (int) ((start + (long) step * step) % size);
    }

    // ---------- Поиск, вставка, удаление ----------

    // Ищет ячейку с ключом key. Возвращает её номер или -1, если ключа нет.
    // Идём по цепочке опробования. Пустая ячейка значит, что дальше ключа точно нет.
    // «Могилу» (DELETED) пропускаем: за ней запись ещё может лежать.
    private int findIndex(String key) {
        int start = hash(key);
        for (int step = 0; step < size; step++) {
            int index = probe(start, step);
            if (states[index] == EMPTY) {
                return -1;
            }
            if (states[index] == FILLED && keys[index].equals(key)) {
                return index;
            }
        }
        return -1;
    }

    // Ищет владельца по СНИЛС. Возвращает ФИО или null, если такого СНИЛС нет.
    public String search(String snilsText) {
        int index = findIndex(cleanKey(snilsText));
        if (index == -1) {
            return null;
        }
        return names[index];
    }

    // Добавляет запись. Возвращает true, если добавили, и false, если такой СНИЛС уже есть.
    // snilsText — СНИЛС в любом виде (с дефисами или без); name — ФИО.
    public boolean insert(String snilsText, String name) {
        String key = cleanKey(snilsText);
        if (findIndex(key) != -1) {
            return false;
        }

        // Если после вставки таблица будет заполнена больше чем наполовину — увеличиваем её.
        if ((usedCount + 1) * 2 > size) {
            rehash();
        }

        put(key, name);
        return true;
    }

    // Кладёт запись в первую подходящую ячейку цепочки опробования.
    // Подходит пустая ячейка или «могила». Проверку «ключ уже есть» делает insert.
    private void put(String key, String name) {
        int start = hash(key);
        for (int step = 0; step < size; step++) {
            int index = probe(start, step);
            if (states[index] != FILLED) {
                if (states[index] == EMPTY) {
                    usedCount++;  // «могила» уже была посчитана в usedCount раньше
                }
                keys[index] = key;
                names[index] = name;
                states[index] = FILLED;
                filledCount++;
                return;
            }
        }
        throw new IllegalStateException("Не нашли свободную ячейку");
    }

    // Удаляет запись по СНИЛС. Возвращает true, если удалили, и false, если такого СНИЛС нет.
    // Ячейку нельзя просто сделать пустой: это оборвёт цепочку опробования,
    // и другие записи, лежащие дальше по цепочке, перестанут находиться.
    // Поэтому ставим метку «удалено» (DELETED).
    public boolean remove(String snilsText) {
        String key = cleanKey(snilsText);
        int index = findIndex(key);
        if (index == -1) {
            return false;
        }
        states[index] = DELETED;
        keys[index] = null;
        names[index] = null;
        filledCount--;
        return true;
    }

    // ---------- Рехеширование ----------

    // Делает таблицу больше (примерно вдвое) и заново кладёт в неё все записи.
    // Заново, потому что номер ячейки зависит от размера таблицы.
    // «Могилы» при этом не переносятся, то есть пропадают.
    private void rehash() {
        String[] oldKeys = keys;
        String[] oldNames = names;
        int[] oldStates = states;
        int oldSize = size;

        size = nextPrime(oldSize * 2 + 1);
        keys = new String[size];
        names = new String[size];
        states = new int[size];
        filledCount = 0;
        usedCount = 0;

        for (int i = 0; i < oldSize; i++) {
            if (oldStates[i] == FILLED) {
                put(oldKeys[i], oldNames[i]);
            }
        }
        System.out.println("  Рехеширование: размер " + oldSize + " -> " + size);
    }

    // ---------- Простые числа ----------

    // Возвращает ближайшее простое число, которое не меньше number.
    private static int nextPrime(int number) {
        int candidate = number;
        while (!isPrime(candidate)) {
            candidate++;
        }
        return candidate;
    }

    // Проверяет, простое ли число number: ищем делитель от 2 до корня из числа.
    private static boolean isPrime(int number) {
        if (number < 2) {
            return false;
        }
        for (int divisor = 2; divisor * divisor <= number; divisor++) {
            if (number % divisor == 0) {
                return false;
            }
        }
        return true;
    }

    // ---------- Печать ----------

    // Печатает всю таблицу: номер ячейки и что в ней лежит.
    public void printTable() {
        System.out.println("Размер таблицы: " + size + ", записей: " + filledCount);
        for (int i = 0; i < size; i++) {
            if (states[i] == FILLED) {
                System.out.printf("%3d | %s | %s%n", i, formatKey(keys[i]), names[i]);
            } else if (states[i] == DELETED) {
                System.out.printf("%3d | (удалено)%n", i);
            } else {
                System.out.printf("%3d | -%n", i);
            }
        }
    }
}
