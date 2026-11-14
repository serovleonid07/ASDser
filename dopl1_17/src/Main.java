public class Main {
    public static void solvePuzzle(String title, String[] lines) {
        System.out.println("\\n======================================");
        System.out.println(title);
        System.out.println("======================================");
        try {
            Board board = new Board(lines);
            System.out.println("Исходное поле:");
            board.print(null);
            NumbrixSolver.solve(board);
            System.out.println("Количество решений: " + NumbrixSolver.getSolutionCount());
            System.out.println("Количество вызовов рекурсии: " + NumbrixSolver.getStepCount());
            if (NumbrixSolver.getFirstSolution() != null) {
                System.out.println("Первое найденное решение:");
                board.print(NumbrixSolver.getFirstSolution());
            } else System.out.println("Решение не найдено.");
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка в условии: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        String[] puzzle1 = {
            "19 . . . 25",
            "18 . 16 . .",
            ". . . . .",
            ". 1 . . 12",
            "5 . . . ."
        };
        String[] puzzle2 = {
            "1 . .",
            ". . .",
            ". . 9"
        };
        String[] puzzle3 = {
            "1 9 .",
            ". . .",
            ". . ."
        };

        solvePuzzle("Головоломка 1 — поле 5 x 5", puzzle1);
        solvePuzzle("Головоломка 2 — поле 3 x 3", puzzle2);
        solvePuzzle("Головоломка 3 — решения нет", puzzle3);
    }
}
