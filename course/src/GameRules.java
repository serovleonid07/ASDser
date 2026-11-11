// Все постоянные значения игры «Лиса и гуси» в одном месте.
// Если нужно изменить правило (например, число гусей), меняем только здесь.
public class GameRules {
    // Размер поля 7x7. Из него вырезаем четыре угла, получается форма креста (33 клетки).
    public static final int BOARD_SIZE = 7;

    // Что может лежать в клетке поля.
    public static final int OUTSIDE = -1;  // клетки за пределами креста (углы)
    public static final int EMPTY = 0;     // пустая клетка
    public static final int FOX = 1;       // в клетке лиса
    public static final int GOOSE = 2;     // в клетке гусь

    // Номера игроков. Гуси ходят первыми (по правилам).
    public static final int PLAYER_GEESE = 0;
    public static final int PLAYER_FOX = 1;

    // Сколько гусей в начале игры.
    public static final int GEESE_AT_START = 17;

    // Лиса выигрывает, если на поле осталось 6 гусей или меньше.
    public static final int GEESE_LEFT_FOX_WINS = 6;

    // Защита от бесконечной игры: после стольких ходов (считаем оба игрока) будет ничья.
    // Этого правила нет в задании, добавлено, чтобы партия компьютер против компьютера не шла вечно.
    public static final int MAX_MOVES = 400;

    // Результат партии.
    public static final int RESULT_NONE = 0;       // игра продолжается
    public static final int RESULT_FOX_WINS = 1;   // победила лиса
    public static final int RESULT_GEESE_WIN = 2;  // победили гуси (лиса заперта)
    public static final int RESULT_DRAW = 3;       // ничья
}
