import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

// Настройки и статистика, которые помнятся между запусками игры.
// Хранятся в обычном текстовом файле в домашней папке пользователя:
//   строка 1: номер темы (0 день, 1 вечер, 2 зима);
//   строка 2: девять чисел: для каждой сложности (лёгкая, средняя, сложная) побед, поражений, ничьих.
// Все поля и методы статические, поэтому класс можно вызывать из любого места программы.
public class Settings {
    public static final int THEME_DAY = 0;
    public static final int THEME_EVENING = 1;
    public static final int THEME_WINTER = 2;
    public static final String[] THEME_NAMES = {"День", "Вечер", "Зима"};

    public static final int LEVELS = 3;     // число уровней сложности
    public static final int WIN = 0;        // номера в массиве статистики
    public static final int LOSS = 1;
    public static final int DRAW = 2;

    // Файл с настройками.
    private static final File FILE = new File(System.getProperty("user.home"), ".fox_and_geese.txt");

    private static int theme = THEME_DAY;
    private static final int[][] stats = new int[LEVELS][3];   // stats[сложность][победа/поражение/ничья]
    private static boolean loaded = false;

    // ---------- Тема ----------

    public static int getTheme() {
        load();
        return theme;
    }

    public static void setTheme(int value) {
        load();
        theme = value;
        save();
    }

    // ---------- Статистика ----------

    // Записывает итог партии против компьютера. level: 0, 1 или 2; kind: WIN, LOSS или DRAW.
    public static void addResult(int level, int kind) {
        load();
        stats[level][kind]++;
        save();
    }

    public static int getCount(int level, int kind) {
        load();
        return stats[level][kind];
    }

    // Обнуляет статистику.
    public static void resetStats() {
        load();
        for (int level = 0; level < LEVELS; level++) {
            for (int kind = 0; kind < 3; kind++) {
                stats[level][kind] = 0;
            }
        }
        save();
    }

    // ---------- Файл ----------

    // Читает файл один раз. Если файла нет или он испорчен, остаются значения по умолчанию.
    private static void load() {
        if (loaded) {
            return;
        }
        loaded = true;
        if (!FILE.exists()) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE))) {
            String line1 = reader.readLine();
            String line2 = reader.readLine();
            int newTheme = Integer.parseInt(line1.trim());
            String[] parts = line2.trim().split(" ");
            int[][] newStats = new int[LEVELS][3];
            for (int i = 0; i < LEVELS * 3; i++) {
                newStats[i / 3][i % 3] = Integer.parseInt(parts[i]);
            }
            // Всё прочиталось: применяем.
            if (newTheme >= 0 && newTheme < THEME_NAMES.length) {
                theme = newTheme;
            }
            for (int level = 0; level < LEVELS; level++) {
                stats[level] = newStats[level];
            }
        } catch (IOException | RuntimeException e) {
            // Файл испорчен: просто игнорируем его.
        }
    }

    // Записывает файл. Если не получилось (нет прав), молча пропускаем: игре это не мешает.
    private static void save() {
        try (FileWriter writer = new FileWriter(FILE)) {
            StringBuilder text = new StringBuilder();
            text.append(theme).append("\n");
            for (int level = 0; level < LEVELS; level++) {
                for (int kind = 0; kind < 3; kind++) {
                    text.append(stats[level][kind]).append(" ");
                }
            }
            text.append("\n");
            writer.write(text.toString());
        } catch (IOException e) {
            // не страшно
        }
    }
}
