public class NumbrixSolver {
    private static Board board;
    private static int total;
    private static int[] givenRow, givenCol, nextGiven;
    private static int solutionCount, stepCount;
    private static int[][] firstSolution;

    public static void solve(Board newBoard) {
        board = newBoard;
        total = board.rows * board.cols;
        givenRow = new int[total + 1];
        givenCol = new int[total + 1];
        nextGiven = new int[total + 1];
        for (int i = 0; i <= total; i++) {
            givenRow[i] = -1;
            givenCol[i] = -1;
        }
        solutionCount = 0;
        stepCount = 0;
        firstSolution = null;
        prepareGivenNumbers();

        if (givenRow[1] != -1) {
            step(1, givenRow[1], givenCol[1]);
        } else {
            for (int r = 0; r < board.rows; r++)
                for (int c = 0; c < board.cols; c++)
                    if (board.isEmpty(r, c) && canReachNextGiven(1, r, c)) {
                        board.cells[r][c] = 1;
                        step(1, r, c);
                        board.cells[r][c] = 0;
                    }
        }
    }

    private static void prepareGivenNumbers() {
        for (int r = 0; r < board.rows; r++)
            for (int c = 0; c < board.cols; c++)
                if (board.fixed[r][c]) {
                    int n = board.cells[r][c];
                    givenRow[n] = r;
                    givenCol[n] = c;
                }

        int next = 0;
        for (int n = total; n >= 1; n--) {
            nextGiven[n] = next;
            if (givenRow[n] != -1) next = n;
        }
    }

    private static void step(int number, int r, int c) {
        stepCount++;
        if (number == total) {
            solutionCount++;
            if (firstSolution == null) firstSolution = board.copyCells();
            return;
        }

        int next = number + 1;
        if (givenRow[next] != -1) {
            if (distance(r, c, givenRow[next], givenCol[next]) == 1)
                step(next, givenRow[next], givenCol[next]);
            return;
        }

        for (int d = 0; d < 4; d++) {
            int nr = r + Board.ROW_STEP[d];
            int nc = c + Board.COL_STEP[d];
            if (board.isInside(nr, nc) && board.isEmpty(nr, nc)
                    && canReachNextGiven(next, nr, nc)) {
                board.cells[nr][nc] = next;
                step(next, nr, nc);
                board.cells[nr][nc] = 0;
            }
        }
    }

    private static boolean canReachNextGiven(int number, int r, int c) {
        int target = nextGiven[number];
        if (target == 0) return true;
        int gap = target - number;
        int dist = distance(r, c, givenRow[target], givenCol[target]);
        return dist <= gap && (gap - dist) % 2 == 0;
    }

    private static int distance(int r1, int c1, int r2, int c2) {
        return Math.abs(r1 - r2) + Math.abs(c1 - c2);
    }

    public static int getSolutionCount() { return solutionCount; }
    public static int getStepCount() { return stepCount; }
    public static int[][] getFirstSolution() { return firstSolution; }
}
