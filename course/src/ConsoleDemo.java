// Проверка без графики: компьютер играет сам с собой, затем сравниваем скорость поиска.
// Запуск: java -cp out ConsoleDemo
public class ConsoleDemo {
    // Рисует поле текстом: F — лиса, G — гуси, . — пусто, пробел — вне поля.
    static void printBoard(GameState state) {
        Board board = state.getBoard();
        for (int row = 0; row < GameRules.BOARD_SIZE; row++) {
            StringBuilder line = new StringBuilder();
            line.append(GameRules.BOARD_SIZE - row).append(' ');
            for (int col = 0; col < GameRules.BOARD_SIZE; col++) {
                int cell = board.get(row, col);
                if (cell == GameRules.OUTSIDE) line.append("  ");
                else if (cell == GameRules.FOX) line.append("F ");
                else if (cell == GameRules.GOOSE) line.append("G ");
                else line.append(". ");
            }
            System.out.println(line);
        }
        System.out.println("  a b c d e f g");
    }

    public static void main(String[] args) {
        int depth = args.length > 0 ? Integer.parseInt(args[0]) : 6;
        AiPlayer ai = new AiPlayer();
        GameState state = new GameState();

        // Лису ставит компьютер.
        int[] start = ai.chooseFoxStart(state, depth);
        state.placeFox(start[0], start[1]);
        System.out.println("Fox starts at row " + start[0] + ", col " + start[1]);
        printBoard(state);

        MoveList moves = new MoveList();
        long startTime = System.currentTimeMillis();
        while (true) {
            MoveGenerator.getMoves(state, moves);
            int result = state.getResult(moves);
            if (result != GameRules.RESULT_NONE) {
                System.out.println("Result code: " + result + " (1 fox, 2 geese, 3 draw)");
                break;
            }
            Move move = ai.chooseMove(state, depth);
            state.makeMove(move);
            ai.remember(state);
            System.out.println("Move " + state.getMoveCount() + ": " + move
                    + "  geese left: " + state.getGeeseCount());
        }
        printBoard(state);
        System.out.println("Game time, ms: " + (System.currentTimeMillis() - startTime));

        // Сравнение скорости поиска на начальной позиции (ходят гуси).
        System.out.println("--- Search comparison (start position, depth 5) ---");
        GameState fresh = new GameState();
        fresh.placeFox(2, 3);
        compare(fresh, 5, false, false, "MiniMax only");
        compare(fresh, 5, true, false, "MiniMax + alpha-beta");
        compare(fresh, 5, true, true, "MiniMax + alpha-beta + table");
    }

    // Запускает поиск с выбранными настройками и печатает время и число просмотренных позиций.
    static void compare(GameState state, int depth, boolean alphaBeta, boolean table, String name) {
        AiPlayer ai = new AiPlayer();
        ai.setUseAlphaBeta(alphaBeta);
        ai.setUseTable(table);
        long t = System.currentTimeMillis();
        Move move = ai.chooseMove(state, depth);
        System.out.println(name + ": move " + move + ", nodes " + ai.getNodeCount()
                + ", time " + (System.currentTimeMillis() - t) + " ms");
    }
}
