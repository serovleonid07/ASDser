// Контроллер игры: связывает правила (GameState), компьютерного игрока (AiPlayer)
// и интерфейс. Здесь нет графики, поэтому его можно проверять без окна.
// Интерфейс только вызывает методы этого класса и рисует то, что он вернул.
public class GameController {
    private GameState state;                       // текущее состояние партии
    private final AiPlayer ai = new AiPlayer();    // компьютерный игрок
    private final boolean[] isHuman = new boolean[2];  // isHuman[игрок]: true, если играет человек
    private int aiDepth = 5;                       // глубина поиска компьютера

    // Стек сыгранных ходов (для отмены хода). Размер с запасом.
    private final Move[] moveStack = new Move[GameRules.MAX_MOVES + 10];
    private int stackSize = 0;

    private int result = GameRules.RESULT_NONE;    // результат партии
    private Move lastMove = null;                  // последний сделанный ход (для подсветки)
    private final MoveList tempMoves = new MoveList();  // рабочий список ходов

    public GameController() {
        newGame(true, false, 5);
    }

    // Начинает новую партию.
    // geeseHuman: true, если за гусей играет человек. foxHuman: true, если за лису играет человек.
    // depth: глубина поиска компьютера (чем больше, тем он сильнее, но медленнее).
    public void newGame(boolean geeseHuman, boolean foxHuman, int depth) {
        state = new GameState();
        isHuman[GameRules.PLAYER_GEESE] = geeseHuman;
        isHuman[GameRules.PLAYER_FOX] = foxHuman;
        aiDepth = depth;
        stackSize = 0;
        result = GameRules.RESULT_NONE;
        lastMove = null;
        ai.forgetHistory();
    }

    // Меняет глубину поиска во время игры.
    public void setDepth(int depth) {
        aiDepth = depth;
    }

    public int getDepth() {
        return aiDepth;
    }

    // ---------- Чей сейчас ход ----------

    // Возвращает, кто сейчас действует: пока лису не поставили — игрок-лиса, потом по очереди.
    public int getActivePlayer() {
        if (!state.isFoxPlaced()) {
            return GameRules.PLAYER_FOX;
        }
        return state.getCurrentPlayer();
    }

    public boolean isGameOver() {
        return result != GameRules.RESULT_NONE;
    }

    // Сейчас ход человека?
    public boolean isHumanTurn() {
        return !isGameOver() && isHuman[getActivePlayer()];
    }

    // Сейчас ход компьютера?
    public boolean isComputerTurn() {
        return !isGameOver() && !isHuman[getActivePlayer()];
    }

    // Играет ли за этого игрока (PLAYER_FOX или PLAYER_GEESE) человек?
    public boolean isHuman(int player) {
        return isHuman[player];
    }

    // Чей курсор показывать. Если человек один, то курсор его персонажа.
    // Если играют двое, то курсор того, чей сейчас ход.
    public int getCursorPlayer() {
        boolean geeseHuman = isHuman[GameRules.PLAYER_GEESE];
        boolean foxHuman = isHuman[GameRules.PLAYER_FOX];
        if (geeseHuman && !foxHuman) {
            return GameRules.PLAYER_GEESE;
        }
        if (foxHuman && !geeseHuman) {
            return GameRules.PLAYER_FOX;
        }
        return getActivePlayer();
    }

    // Нужно ли сейчас выбрать стартовую клетку лисы?
    public boolean needFoxPlacement() {
        return !state.isFoxPlaced();
    }

    // ---------- Действия человека ----------

    // Ставит лису в клетку (row, col). Возвращает true, если получилось.
    public boolean tryPlaceFox(int row, int col) {
        if (!isHumanTurn() || state.isFoxPlaced()) {
            return false;
        }
        Board board = state.getBoard();
        if (!board.isInside(row, col) || board.get(row, col) != GameRules.EMPTY) {
            return false;
        }
        state.placeFox(row, col);
        updateResult();
        return true;
    }

    // Пытается сделать ход из (fromRow, fromCol) в (toRow, toCol).
    // Ход выполняется, только если он есть в списке допустимых. Возвращает true, если ход сделан.
    public boolean tryMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (!isHumanTurn() || !state.isFoxPlaced()) {
            return false;
        }
        MoveGenerator.getMoves(state, tempMoves);
        for (int i = 0; i < tempMoves.size(); i++) {
            Move move = tempMoves.get(i);
            if (move.fromRow == fromRow && move.fromCol == fromCol
                    && move.toRow == toRow && move.toCol == toCol) {
                applyMove(move);
                return true;
            }
        }
        return false;
    }

    // Записывает в out все допустимые ходы фишки из клетки (row, col). Нужно для подсветки.
    public void getMovesFrom(int row, int col, MoveList out) {
        out.clear();
        if (isGameOver() || !state.isFoxPlaced()) {
            return;
        }
        MoveList all = new MoveList();
        MoveGenerator.getMoves(state, all);
        for (int i = 0; i < all.size(); i++) {
            Move move = all.get(i);
            if (move.fromRow == row && move.fromCol == col) {
                out.add(move);
            }
        }
    }

    // ---------- Действия компьютера ----------

    // Компьютер делает одно действие: ставит лису или делает ход.
    public void doComputerStep() {
        if (!isComputerTurn()) {
            return;
        }
        if (!state.isFoxPlaced()) {
            int depth = Math.min(aiDepth, 5);
            int[] cell = ai.chooseFoxStart(state, depth);
            state.placeFox(cell[0], cell[1]);
            updateResult();
            return;
        }
        Move move = ai.chooseMove(state, aiDepth);
        if (move != null) {
            applyMove(move);
        }
    }

    // ---------- Подсказка ----------

    // Глубина поиска для подсказки: небольшая, чтобы подсказка появлялась сразу.
    private static final int HINT_DEPTH = 4;

    // Возвращает лучший ход для игрока, чей сейчас ход (или null, если подсказывать нечего).
    // Для постановки лисы лучше использовать getFoxStartHint.
    public Move getHintMove() {
        if (isGameOver() || !state.isFoxPlaced()) {
            return null;
        }
        return ai.chooseMove(state, HINT_DEPTH);
    }

    // Возвращает лучшую стартовую клетку лисы {строка, столбец}.
    public int[] getFoxStartHint() {
        if (isGameOver() || state.isFoxPlaced()) {
            return null;
        }
        return ai.chooseFoxStart(state, 3);
    }

    // ---------- Отмена хода ----------

    // Можно ли отменить ход? Против компьютера отменяются сразу два хода (свой и компьютера).
    public boolean canUndo() {
        boolean twoHumans = isHuman[0] && isHuman[1];
        if (twoHumans) {
            return stackSize >= 1;
        }
        return stackSize >= 2;
    }

    // Отменяет ход (или два хода против компьютера), пока снова не наступит ход человека.
    public void undo() {
        if (!canUndo()) {
            return;
        }
        do {
            stackSize--;
            state.undoMove(moveStack[stackSize]);
            updateResult();
        } while (stackSize > 0 && !isHumanTurn());
        lastMove = stackSize > 0 ? moveStack[stackSize - 1] : null;
    }

    // ---------- Служебные методы ----------

    // Выполняет ход: меняет состояние, кладёт ход в стек, запоминает позицию для компьютера.
    private void applyMove(Move move) {
        state.makeMove(move);
        moveStack[stackSize] = move;
        stackSize++;
        ai.remember(state);
        lastMove = move;
        updateResult();
    }

    // Пересчитывает результат партии.
    private void updateResult() {
        if (!state.isFoxPlaced()) {
            result = GameRules.RESULT_NONE;
            return;
        }
        MoveGenerator.getMoves(state, tempMoves);
        result = state.getResult(tempMoves);
    }

    // ---------- Данные для интерфейса ----------
    public GameState getState() { return state; }
    public int getResult() { return result; }
    public Move getLastMove() { return lastMove; }
    public int getMoveCount() { return stackSize; }
}
