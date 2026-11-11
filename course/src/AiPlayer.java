// Компьютерный игрок. Выбирает ход алгоритмом MiniMax с альфа-бета отсечением
// и таблицей уже виденных позиций.
// Идея MiniMax: лиса хочет, чтобы оценка была как можно БОЛЬШЕ (максимизирует),
// гуси хотят, чтобы оценка была как можно МЕНЬШЕ (минимизируют).
// Альфа-бета: если ветка точно хуже уже найденной, дальше её не считаем.
public class AiPlayer {
    private static final int INFINITY = 1000000;  // больше любой оценки

    private final TranspositionTable table = new TranspositionTable(1 << 18);
    private MoveList[] lists;        // отдельный список ходов для каждой глубины поиска
    private long nodeCount = 0;      // сколько позиций просмотрено (для статистики)
    private boolean useAlphaBeta = true;     // можно выключить, чтобы сравнить с чистым MiniMax
    private boolean useTable = true;         // можно выключить таблицу позиций

    // История позиций партии (хэши). Нужна, чтобы компьютер не повторял одни и те же позиции бесконечно.
    private static final int HISTORY_SIZE = 2048;
    private static final int REPEAT_PENALTY = 60;   // штраф за ход, ведущий в уже виденную позицию
    private final long[] history = new long[HISTORY_SIZE];
    private int historyCount = 0;

    // Включает или выключает альфа-бета отсечение (нужно для сравнения скорости в отчёте).
    public void setUseAlphaBeta(boolean value) {
        useAlphaBeta = value;
    }

    // Включает или выключает таблицу позиций.
    public void setUseTable(boolean value) {
        useTable = value;
    }

    // Запоминает текущую позицию партии. Вызывать после каждого сделанного хода.
    public void remember(GameState state) {
        if (historyCount < HISTORY_SIZE) {
            history[historyCount] = state.getHash();
            historyCount++;
        }
    }

    // Забывает всю историю (при начале новой партии).
    public void forgetHistory() {
        historyCount = 0;
    }

    // Считает, сколько раз позиция с хэшем hash уже встречалась в партии.
    private int timesSeen(long hash) {
        int times = 0;
        for (int i = 0; i < historyCount; i++) {
            if (history[i] == hash) {
                times++;
            }
        }
        return times;
    }

    // Сколько позиций просмотрел последний поиск.
    public long getNodeCount() {
        return nodeCount;
    }

    // Выбирает лучший ход для игрока, чей сейчас ход. depth — на сколько ходов вперёд смотрим.
    // Возвращает null, если ходов нет.
    public Move chooseMove(GameState state, int depth) {
        nodeCount = 0;
        table.clear();
        prepareLists(depth);

        MoveList moves = lists[depth];
        MoveGenerator.getMoves(state, moves);
        if (moves.size() == 0) {
            return null;
        }
        orderMoves(moves);

        boolean foxTurn = state.getCurrentPlayer() == GameRules.PLAYER_FOX;
        Move bestMove = moves.get(0);
        int bestValue = foxTurn ? -INFINITY : INFINITY;
        int alpha = -INFINITY;
        int beta = INFINITY;

        for (int i = 0; i < moves.size(); i++) {
            Move move = moves.get(i);
            state.makeMove(move);
            int value = search(state, depth - 1, alpha, beta);
            // Штрафуем ход в уже виденную позицию: лиса получает меньше, гуси — больше.
            int repeats = timesSeen(state.getHash());
            if (foxTurn) {
                value -= repeats * REPEAT_PENALTY;
            } else {
                value += repeats * REPEAT_PENALTY;
            }
            state.undoMove(move);

            if (foxTurn && value > bestValue) {
                bestValue = value;
                bestMove = move;
                if (value > alpha) alpha = value;
            } else if (!foxTurn && value < bestValue) {
                bestValue = value;
                bestMove = move;
                if (value < beta) beta = value;
            }
        }
        return bestMove;
    }

    // Выбирает клетку для старта лисы (лису ставят до начала партии).
    // Пробуем каждую свободную клетку и выбираем ту, где оценка для лисы лучше всего.
    // Возвращает массив из двух чисел: {строка, столбец}.
    public int[] chooseFoxStart(GameState state, int depth) {
        nodeCount = 0;
        prepareLists(depth);
        Board board = state.getBoard();
        int bestRow = -1;
        int bestCol = -1;
        int bestValue = -INFINITY;

        for (int row = 0; row < GameRules.BOARD_SIZE; row++) {
            for (int col = 0; col < GameRules.BOARD_SIZE; col++) {
                if (board.isInside(row, col) && board.get(row, col) == GameRules.EMPTY) {
                    GameState copy = state.copy();
                    copy.placeFox(row, col);
                    table.clear();
                    // После постановки лисы ходят гуси, поэтому ищем минимум для них.
                    int value = search(copy, depth, -INFINITY, INFINITY);
                    if (value > bestValue) {
                        bestValue = value;
                        bestRow = row;
                        bestCol = col;
                    }
                }
            }
        }
        return new int[]{bestRow, bestCol};
    }

    // Создаёт списки ходов для всех глубин (один раз, чтобы не создавать их при каждом вызове).
    private void prepareLists(int depth) {
        if (lists == null || lists.length < depth + 1) {
            lists = new MoveList[depth + 1];
            for (int i = 0; i < lists.length; i++) {
                lists[i] = new MoveList();
            }
        }
    }

    // Главная рекурсивная функция поиска.
    // depth — сколько ходов ещё можно посмотреть вперёд, alpha и beta — границы отсечения.
    // Возвращает оценку позиции (для лисы: больше — лучше).
    private int search(GameState state, int depth, int alpha, int beta) {
        nodeCount++;

        // Считаем ходы текущего игрока (нужны и для проверки конца игры, и для перебора).
        MoveList moves = lists[depth];
        MoveGenerator.getMoves(state, moves);

        // Проверка конца игры. Чем раньше победа, тем она лучше: добавляем depth.
        int result = state.getResult(moves);
        if (result == GameRules.RESULT_FOX_WINS) {
            return Evaluator.WIN_SCORE + depth;
        }
        if (result == GameRules.RESULT_GEESE_WIN) {
            return -Evaluator.WIN_SCORE - depth;
        }
        if (result == GameRules.RESULT_DRAW) {
            return 0;
        }
        if (depth == 0) {
            return Evaluator.evaluate(state);
        }

        // Смотрим в таблицу: вдруг эту позицию уже считали.
        int originalAlpha = alpha;
        int originalBeta = beta;
        if (useTable) {
            int index = table.find(state.getHash(), depth);
            if (index >= 0) {
                int value = table.getValue(index);
                int flag = table.getFlag(index);
                if (flag == TranspositionTable.EXACT) {
                    return value;
                }
                if (flag == TranspositionTable.LOWER_BOUND && value > alpha) {
                    alpha = value;
                }
                if (flag == TranspositionTable.UPPER_BOUND && value < beta) {
                    beta = value;
                }
                if (useAlphaBeta && alpha >= beta) {
                    return value;
                }
            }
        }

        orderMoves(moves);
        boolean foxTurn = state.getCurrentPlayer() == GameRules.PLAYER_FOX;
        int best = foxTurn ? -INFINITY : INFINITY;

        for (int i = 0; i < moves.size(); i++) {
            Move move = moves.get(i);
            state.makeMove(move);
            int value = search(state, depth - 1, alpha, beta);
            state.undoMove(move);

            if (foxTurn) {
                if (value > best) best = value;
                if (value > alpha) alpha = value;
            } else {
                if (value < best) best = value;
                if (value < beta) beta = value;
            }
            // Отсечение: эта ветка уже не повлияет на результат.
            if (useAlphaBeta && alpha >= beta) {
                break;
            }
        }

        // Запоминаем результат в таблице.
        if (useTable) {
            int flag = TranspositionTable.EXACT;
            if (best <= originalAlpha) {
                flag = TranspositionTable.UPPER_BOUND;
            } else if (best >= originalBeta) {
                flag = TranspositionTable.LOWER_BOUND;
            }
            table.store(state.getHash(), depth, best, flag);
        }
        return best;
    }

    // Ставит ходы с поеданием в начало списка. Так отсечений бывает больше, поиск быстрее.
    private void orderMoves(MoveList moves) {
        int front = 0;  // сюда ставим следующий ход с поеданием
        for (int i = 0; i < moves.size(); i++) {
            if (moves.get(i).isCapture()) {
                moves.swap(i, front);
                front++;
            }
        }
    }
}
