// Состояние игры: поле, где лиса, сколько гусей, чей ход, сколько ходов сыграно.
// Ход можно сделать (makeMove) и отменить (undoMove). Это нужно компьютеру,
// чтобы быстро «пробовать» ходы без копирования всего поля.
// Также хранит хэш позиции (число, по которому быстро узнаём уже виденные позиции).
public class GameState {
    // ---------- Случайные числа для хэша позиции (метод Зобриста) ----------
    // Для каждой пары (клетка, вид фишки) заранее выбрано своё случайное число.
    // Хэш позиции = XOR чисел всех фишек на поле. Ход меняет хэш двумя-тремя XOR.
    private static long seed = 88172645463325252L;
    private static final long[][][] KEYS =
            new long[GameRules.BOARD_SIZE][GameRules.BOARD_SIZE][2];  // [строка][столбец][0 лиса, 1 гусь]
    private static final long TURN_KEY;  // число, которое «включено» в хэш, когда ходит лиса

    static {
        for (int row = 0; row < GameRules.BOARD_SIZE; row++) {
            for (int col = 0; col < GameRules.BOARD_SIZE; col++) {
                for (int piece = 0; piece < 2; piece++) {
                    KEYS[row][col][piece] = nextRandom();
                }
            }
        }
        TURN_KEY = nextRandom();
    }

    // Простой генератор случайных чисел (xorshift). Свой, чтобы результат был одинаковым при каждом запуске.
    private static long nextRandom() {
        seed ^= seed << 13;
        seed ^= seed >>> 7;
        seed ^= seed << 17;
        return seed;
    }

    // Номер вида фишки для массива KEYS: лиса -> 0, гусь -> 1.
    private static int pieceIndex(int piece) {
        return piece == GameRules.FOX ? 0 : 1;
    }

    // ---------- Поля состояния ----------
    private final Board board;     // игровое поле
    private int foxRow;            // строка лисы, или -1 если лису ещё не поставили
    private int foxCol;            // столбец лисы, или -1 если лису ещё не поставили
    private int geeseCount;        // сколько гусей сейчас на поле
    private int currentPlayer;     // чей ход: PLAYER_GEESE или PLAYER_FOX
    private int moveCount;         // сколько ходов уже сделано
    private long hash;             // хэш текущей позиции

    // Создаёт начальную позицию: 17 гусей стоят в рядах 3, 4 и 5, лисы на поле пока нет.
    // Лису ставит игрок сам методом placeFox (по правилам она может встать в любую свободную клетку).
    public GameState() {
        board = new Board();
        foxRow = -1;
        foxCol = -1;
        geeseCount = 0;
        for (int row = 3; row <= 5; row++) {
            for (int col = 0; col < GameRules.BOARD_SIZE; col++) {
                if (board.isInside(row, col)) {
                    board.set(row, col, GameRules.GOOSE);
                    geeseCount++;
                    hash ^= KEYS[row][col][1];
                }
            }
        }
        currentPlayer = GameRules.PLAYER_GEESE;  // гуси ходят первыми
        moveCount = 0;
    }

    // Приватный конструктор для copy(): просто переписывает все поля.
    private GameState(Board board, int foxRow, int foxCol, int geeseCount,
                      int currentPlayer, int moveCount, long hash) {
        this.board = board;
        this.foxRow = foxRow;
        this.foxCol = foxCol;
        this.geeseCount = geeseCount;
        this.currentPlayer = currentPlayer;
        this.moveCount = moveCount;
        this.hash = hash;
    }

    // Делает полную независимую копию состояния.
    public GameState copy() {
        return new GameState(board.copy(), foxRow, foxCol, geeseCount, currentPlayer, moveCount, hash);
    }

    // Ставит лису в свободную клетку (row, col). Можно вызвать только один раз, до начала партии.
    public void placeFox(int row, int col) {
        if (foxRow >= 0) {
            throw new IllegalStateException("Лиса уже стоит на поле");
        }
        if (!board.isInside(row, col) || board.get(row, col) != GameRules.EMPTY) {
            throw new IllegalArgumentException("Лису можно поставить только в свободную клетку");
        }
        board.set(row, col, GameRules.FOX);
        foxRow = row;
        foxCol = col;
        hash ^= KEYS[row][col][0];
    }

    // Делает ход move. Предполагается, что ход корректный (его создал MoveGenerator).
    // Шаги: переносим фишку, убираем съеденного гуся (если был прыжок), передаём ход другому игроку.
    public void makeMove(Move move) {
        int piece = board.get(move.fromRow, move.fromCol);

        board.set(move.fromRow, move.fromCol, GameRules.EMPTY);
        board.set(move.toRow, move.toCol, piece);
        hash ^= KEYS[move.fromRow][move.fromCol][pieceIndex(piece)];
        hash ^= KEYS[move.toRow][move.toCol][pieceIndex(piece)];

        if (move.isCapture()) {
            board.set(move.capturedRow, move.capturedCol, GameRules.EMPTY);
            hash ^= KEYS[move.capturedRow][move.capturedCol][1];
            geeseCount--;
        }

        if (piece == GameRules.FOX) {
            foxRow = move.toRow;
            foxCol = move.toCol;
        }

        switchPlayer();
        moveCount++;
    }

    // Отменяет ход move (должен быть последним сделанным). Делает всё в обратном порядке.
    public void undoMove(Move move) {
        int piece = board.get(move.toRow, move.toCol);

        board.set(move.toRow, move.toCol, GameRules.EMPTY);
        board.set(move.fromRow, move.fromCol, piece);
        hash ^= KEYS[move.toRow][move.toCol][pieceIndex(piece)];
        hash ^= KEYS[move.fromRow][move.fromCol][pieceIndex(piece)];

        if (move.isCapture()) {
            board.set(move.capturedRow, move.capturedCol, GameRules.GOOSE);
            hash ^= KEYS[move.capturedRow][move.capturedCol][1];
            geeseCount++;
        }

        if (piece == GameRules.FOX) {
            foxRow = move.fromRow;
            foxCol = move.fromCol;
        }

        switchPlayer();
        moveCount--;
    }

    // Передаёт ход другому игроку и обновляет хэш (число TURN_KEY включено, когда ходит лиса).
    private void switchPlayer() {
        if (currentPlayer == GameRules.PLAYER_GEESE) {
            currentPlayer = GameRules.PLAYER_FOX;
        } else {
            currentPlayer = GameRules.PLAYER_GEESE;
        }
        hash ^= TURN_KEY;
    }

    // Определяет результат партии.
    // legalMoves — список допустимых ходов игрока, который сейчас ходит (его делает MoveGenerator).
    // Порядок проверок: лиса съела достаточно -> у текущего игрока нет ходов -> достигнут предел ходов.
    public int getResult(MoveList legalMoves) {
        if (geeseCount <= GameRules.GEESE_LEFT_FOX_WINS) {
            return GameRules.RESULT_FOX_WINS;
        }
        if (legalMoves.size() == 0) {
            // Тот, кто не может ходить, проигрывает: заперта лиса -> выиграли гуси.
            if (currentPlayer == GameRules.PLAYER_FOX) {
                return GameRules.RESULT_GEESE_WIN;
            }
            return GameRules.RESULT_FOX_WINS;
        }
        if (moveCount >= GameRules.MAX_MOVES) {
            return GameRules.RESULT_DRAW;
        }
        return GameRules.RESULT_NONE;
    }

    // ---------- Простые «геттеры» ----------
    public Board getBoard() { return board; }
    public boolean isFoxPlaced() { return foxRow >= 0; }
    public int getFoxRow() { return foxRow; }
    public int getFoxCol() { return foxCol; }
    public int getGeeseCount() { return geeseCount; }
    public int getCurrentPlayer() { return currentPlayer; }
    public int getMoveCount() { return moveCount; }
    public long getHash() { return hash; }
}
