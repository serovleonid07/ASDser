/*
 * Вариант 17: ключ — СНИЛС, хеш-функция — метод умножения,
 * разрешение коллизий — квадратичное опробование.
 * Нужно: поиск, вставка, удаление, печать таблицы и рехеширование при росте данных.
 */
public class Main {
    // Точка входа программы. Аргумент args не используется.
    // Добавляет записи, ищет, удаляет и печатает таблицу.
    public static void main(String[] args) {
        System.out.println("Лабораторная № 5. Хеширование.");

        // Данные для таблицы: СНИЛС (выдуманные) и ФИО.
        String[] snils = {
            "123-456-789 01", "234-567-890 12", "345-678-901 23", "456-789-012 34",
            "567-890-123 45", "678-901-234 56", "789-012-345 67", "890-123-456 78",
            "901-234-567 89", "012-345-678 90"
        };
        String[] names = {
            "Иванов Иван Иванович", "Петров Пётр Петрович", "Сидорова Анна Сергеевна",
            "Козлов Дмитрий Андреевич", "Морозова Елена Викторовна", "Волков Алексей Николаевич",
            "Соколова Ольга Павловна", "Лебедев Игорь Олегович", "Новикова Мария Александровна",
            "Фёдоров Сергей Михайлович"
        };

        // Начальный размер маленький (7), чтобы было видно рехеширование.
        HashTable table = new HashTable(7);

        System.out.println("\nДобавляем записи:");
        for (int i = 0; i < snils.length; i++) {
            table.insert(snils[i], names[i]);
            System.out.println("Добавлено: " + snils[i] + " " + names[i]);
        }

        System.out.println("\nТаблица после добавления:");
        table.printTable();

        // Поиск: один СНИЛС есть в таблице, другого нет.
        System.out.println("\nПоиск:");
        printSearch(table, "456-789-012 34");
        printSearch(table, "000-000-000 00");

        // Повторная вставка того же СНИЛС должна быть отклонена.
        System.out.println("\nПовторное добавление СНИЛС 123-456-789 01:");
        if (table.insert("123-456-789 01", "Другой Человек Иванович")) {
            System.out.println("Добавлено");
        } else {
            System.out.println("Не добавлено: такой СНИЛС уже есть");
        }

        // Удаление.
        System.out.println("\nУдаляем СНИЛС 345-678-901 23:");
        if (table.remove("345-678-901 23")) {
            System.out.println("Удалено");
        } else {
            System.out.println("Такого СНИЛС нет");
        }
        System.out.println("Удаляем СНИЛС 000-000-000 00:");
        if (table.remove("000-000-000 00")) {
            System.out.println("Удалено");
        } else {
            System.out.println("Такого СНИЛС нет");
        }

        System.out.println("\nПоиск после удаления:");
        printSearch(table, "345-678-901 23");

        System.out.println("\nТаблица после удаления:");
        table.printTable();

        // Проверка неверного СНИЛС: программа должна сообщить об ошибке, а не упасть.
        System.out.println("\nДобавляем неверный СНИЛС \"123\":");
        try {
            table.insert("123", "Неверный Ключ");
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    // Ищет СНИЛС в таблице и печатает результат.
    // table — таблица; snils — СНИЛС, который ищем.
    private static void printSearch(HashTable table, String snils) {
        String name = table.search(snils);
        if (name == null) {
            System.out.println(snils + " — не найден");
        } else {
            System.out.println(snils + " — " + name);
        }
    }
}
