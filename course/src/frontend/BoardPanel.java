package frontend;

import backend.*;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import java.awt.AlphaComposite;

// Панель, которая рисует игровое поле и принимает мышь и клавиатуру.
// Мышь: щелчок по своей фишке выбирает её, щелчок по зелёной точке делает ход.
// Клавиатура: стрелки двигают курсор, Enter или пробел = щелчок, Esc снимает выбор.
public class BoardPanel extends JPanel {
    private final GameController controller;  // логика игры
    private final Runnable onChange;          // что вызвать после действия человека (обновить статус, запустить компьютер)

    private int selectedRow = -1;   // выбранная фишка (-1 = ничего не выбрано)
    private int selectedCol = -1;
    private int cursorRow = 3;      // клетка, на которой стоит клавиатурный курсор
    private int cursorCol = 3;
    private final MoveList targets = new MoveList();  // ходы выбранной фишки (для подсветки)
    private final PieceImages images = new PieceImages();  // картинки лисы и гуся
    private final BackgroundImage background = new BackgroundImage();  // фон: луг, забор, деревья
    private final CursorSet cursors;   // курсоры лисы и гуся
    private int hoverRow = -1;         // клетка под мышью (-1 = мышь вне поля)
    private int hoverCol = -1;

    // Подсказка: ход, который советует компьютер (или стартовая клетка лисы).
    private Move hintMove = null;
    private int hintCellRow = -1;      // клетка для постановки лисы (-1 = нет такой подсказки)
    private int hintCellCol = -1;

    // Анимация последнего хода: фишка плавно переезжает из клетки в клетку.
    private static final int ANIM_MS = 240;       // сколько длится анимация
    private Move animMove = null;                 // какой ход сейчас анимируется (null = нет анимации)
    private long animStart = 0;                   // время начала анимации (в наносекундах)
    private final Timer animTimer = new Timer(15, e -> animationTick());

    // Цвета (светлая тема).
    private static final Color COLOR_BACKGROUND = new Color(0xF4EFE6);
    private static final Color COLOR_PLATE = new Color(251, 246, 234, 95);  // полупрозрачная подложка под полем
    private static final Color COLOR_LABEL_BACK = new Color(251, 246, 234, 215);  // подложка под подписями
    private static final Color COLOR_LINE = new Color(0x8A7F70);
    private static final Color COLOR_GOOSE = new Color(0xFFFFFF);
    private static final Color COLOR_GOOSE_BORDER = new Color(0x4A4A4A);
    private static final Color COLOR_FOX = new Color(0xE0701B);
    private static final Color COLOR_FOX_BORDER = new Color(0x8A3F05);
    private static final Color COLOR_SELECTED = new Color(0xF2C200);
    private static final Color COLOR_TARGET = new Color(0x2E9E4F);
    private static final Color COLOR_CAPTURE = new Color(0xC62828);
    private static final Color COLOR_CURSOR = new Color(0x1E5FD0);
    private static final Color COLOR_HINT = new Color(0x00A3A3);
    private static final Color COLOR_LAST_MOVE = new Color(242, 194, 0, 100);  // подсветка последнего хода

    // Короткий вариант конструктора (для проверок): курсоры создаются внутри.
    public BoardPanel(GameController controller, Runnable onChange) {
        this(controller, new CursorSet(), onChange);
    }

    public BoardPanel(GameController controller, CursorSet cursors, Runnable onChange) {
        this.controller = controller;
        this.cursors = cursors;
        this.onChange = onChange;
        setBackground(COLOR_BACKGROUND);
        setPreferredSize(new Dimension(640, 640));
        setFocusable(true);
        setupMouse();
        setupKeys();
    }

    // ---------- Геометрия ----------

    // Размер одной клетки в пикселях (поле вписывается в панель с небольшим отступом).
    // Отступ по краям панели, в нём рисуются подписи a-g и 1-7.
    private static final int MARGIN = 28;

    private int cellSize() {
        int side = Math.min(getWidth(), getHeight()) - 2 * MARGIN;
        return Math.max(20, side / GameRules.BOARD_SIZE);
    }

    // Левый отступ, чтобы поле стояло по центру.
    private int offsetX() {
        return (getWidth() - cellSize() * GameRules.BOARD_SIZE) / 2;
    }

    // Верхний отступ.
    private int offsetY() {
        return (getHeight() - cellSize() * GameRules.BOARD_SIZE) / 2;
    }

    // Координата x центра клетки в столбце col.
    private int centerX(int col) {
        return offsetX() + col * cellSize() + cellSize() / 2;
    }

    // Координата y центра клетки в строке row.
    private int centerY(int row) {
        return offsetY() + row * cellSize() + cellSize() / 2;
    }

    // ---------- Мышь ----------
    private void setupMouse() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                int col = (e.getX() - offsetX()) / cellSize();
                int row = (e.getY() - offsetY()) / cellSize();
                if (e.getX() < offsetX() || e.getY() < offsetY()) {
                    return;  // щелчок вне поля
                }
                cursorRow = Math.max(0, Math.min(GameRules.BOARD_SIZE - 1, row));
                cursorCol = Math.max(0, Math.min(GameRules.BOARD_SIZE - 1, col));
                activateCell(row, col);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hoverRow = -1;
                hoverCol = -1;
                updateCursor();
            }
        });
        // Следим за положением мыши, чтобы менять курсор над фишками и клетками хода.
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (e.getX() < offsetX() || e.getY() < offsetY()) {
                    hoverRow = -1;
                    hoverCol = -1;
                } else {
                    hoverCol = (e.getX() - offsetX()) / cellSize();
                    hoverRow = (e.getY() - offsetY()) / cellSize();
                }
                updateCursor();
            }
        });
    }

    // ---------- Курсор мыши ----------

    // Выбирает курсор для текущей ситуации и ставит его на панель.
    // Если думает компьютер, то курсор «занят». Если мышь над тем, на что можно нажать, то «рука».
    // Иначе обычный указатель. Персонаж (лиса или гусь) зависит от того, за кого играет человек.
    // Вызывается при движении мыши и после каждого изменения игры.
    public void updateCursor() {
        int player = controller.getCursorPlayer();
        int kind = CursorSet.NORMAL;
        if (controller.isComputerTurn()) {
            kind = CursorSet.BUSY;
        } else if (isActionable(hoverRow, hoverCol)) {
            kind = CursorSet.LINK;
        }
        setCursor(cursors.get(player, kind));
    }

    // Можно ли сейчас нажать на клетку (row, col): это своя фишка, клетка для хода или место для лисы.
    private boolean isActionable(int row, int col) {
        if (!controller.isHumanTurn()) {
            return false;
        }
        Board board = controller.getState().getBoard();
        if (!board.isInside(row, col)) {
            return false;
        }
        if (controller.needFoxPlacement()) {
            return board.get(row, col) == GameRules.EMPTY;
        }
        // Клетка, в которую выбранная фишка может пойти.
        for (int i = 0; i < targets.size(); i++) {
            if (targets.get(i).toRow == row && targets.get(i).toCol == col) {
                return true;
            }
        }
        // Своя фишка.
        boolean foxTurn = controller.getActivePlayer() == GameRules.PLAYER_FOX;
        int piece = board.get(row, col);
        return foxTurn ? piece == GameRules.FOX : piece == GameRules.GOOSE;
    }

    // ---------- Клавиатура ----------
    private void setupKeys() {
        bindKey(KeyEvent.VK_UP, () -> moveCursor(-1, 0));
        bindKey(KeyEvent.VK_DOWN, () -> moveCursor(1, 0));
        bindKey(KeyEvent.VK_LEFT, () -> moveCursor(0, -1));
        bindKey(KeyEvent.VK_RIGHT, () -> moveCursor(0, 1));
        bindKey(KeyEvent.VK_ENTER, () -> activateCell(cursorRow, cursorCol));
        bindKey(KeyEvent.VK_SPACE, () -> activateCell(cursorRow, cursorCol));
        bindKey(KeyEvent.VK_H, this::showHint);
        bindKey(KeyEvent.VK_ESCAPE, () -> {
            clearSelection();
            repaint();
            updateCursor();
        });
    }

    // Привязывает клавишу к действию.
    private void bindKey(int keyCode, Runnable action) {
        String name = "key" + keyCode;
        getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(keyCode, 0), name);
        getActionMap().put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }

    // Двигает курсор на клетку в направлении (dRow, dCol), перешагивая вырезанные углы.
    private void moveCursor(int dRow, int dCol) {
        Board board = controller.getState().getBoard();
        int row = cursorRow + dRow;
        int col = cursorCol + dCol;
        while (row >= 0 && row < GameRules.BOARD_SIZE && col >= 0 && col < GameRules.BOARD_SIZE) {
            if (board.isInside(row, col)) {
                cursorRow = row;
                cursorCol = col;
                break;
            }
            row += dRow;
            col += dCol;
        }
        repaint();
    }

    // ---------- Действие над клеткой (общее для мыши и клавиатуры) ----------

    // Что делает игрок, когда «нажал» на клетку (row, col). После действия обновляем курсор.
    private void activateCell(int row, int col) {
        doActivateCell(row, col);
        updateCursor();
    }

    // Само действие над клеткой (выбор фишки, ход или постановка лисы).
    private void doActivateCell(int row, int col) {
        if (!controller.isHumanTurn()) {
            return;
        }
        Board board = controller.getState().getBoard();
        if (!board.isInside(row, col)) {
            return;
        }

        // Этап расстановки: игрок-лиса выбирает стартовую клетку.
        if (controller.needFoxPlacement()) {
            if (controller.tryPlaceFox(row, col)) {
                clearSelection();
                repaint();
                onChange.run();
            }
            return;
        }

        // Если уже выбрана фишка и нажали на клетку-цель, пробуем сделать ход.
        if (selectedRow >= 0 && controller.tryMove(selectedRow, selectedCol, row, col)) {
            clearSelection();
            startAnimation(controller.getLastMove());
            repaint();
            onChange.run();
            return;
        }

        // Иначе пробуем выбрать свою фишку в этой клетке.
        int piece = board.get(row, col);
        boolean foxTurn = controller.getActivePlayer() == GameRules.PLAYER_FOX;
        boolean ownPiece = foxTurn ? piece == GameRules.FOX : piece == GameRules.GOOSE;
        if (ownPiece) {
            selectedRow = row;
            selectedCol = col;
            controller.getMovesFrom(row, col, targets);
        } else {
            clearSelection();
        }
        repaint();
    }

    // Снимает выбор фишки.
    public void clearSelection() {
        selectedRow = -1;
        selectedCol = -1;
        targets.clear();
        hintMove = null;       // подсказка тоже пропадает
        hintCellRow = -1;
        hintCellCol = -1;
    }

    // ---------- Подсказка ----------

    // Показывает подсказку: считает лучший ход (или клетку для лисы) и рисует его на поле.
    public void showHint() {
        if (!controller.isHumanTurn()) {
            return;
        }
        clearSelection();
        if (controller.needFoxPlacement()) {
            int[] cell = controller.getFoxStartHint();
            if (cell != null) {
                hintCellRow = cell[0];
                hintCellCol = cell[1];
            }
        } else {
            hintMove = controller.getHintMove();
        }
        repaint();
    }

    // Рисует подсказку: кольцо на фишке и стрелка к клетке, куда стоит пойти.
    private void drawHint(Graphics2D g, int cell) {
        g.setColor(COLOR_HINT);
        g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int r = cell * 9 / 20;
        if (hintCellRow >= 0) {                    // подсказка для постановки лисы
            g.drawOval(centerX(hintCellCol) - r, centerY(hintCellRow) - r, 2 * r, 2 * r);
            return;
        }
        if (hintMove == null) {
            return;
        }
        int x1 = centerX(hintMove.fromCol);
        int y1 = centerY(hintMove.fromRow);
        int x2 = centerX(hintMove.toCol);
        int y2 = centerY(hintMove.toRow);
        g.drawOval(x1 - r, y1 - r, 2 * r, 2 * r);          // кольцо вокруг фишки, которой ходить
        g.drawOval(x2 - r / 2, y2 - r / 2, r, r);           // маленькое кольцо там, куда идти
        // Стрелка между кольцами.
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int sx = (int) (x1 + Math.cos(angle) * r);
        int sy = (int) (y1 + Math.sin(angle) * r);
        int ex = (int) (x2 - Math.cos(angle) * r / 2);
        int ey = (int) (y2 - Math.sin(angle) * r / 2);
        g.drawLine(sx, sy, ex, ey);
        int head = cell / 5;
        g.drawLine(ex, ey, (int) (ex - head * Math.cos(angle - 0.45)), (int) (ey - head * Math.sin(angle - 0.45)));
        g.drawLine(ex, ey, (int) (ex - head * Math.cos(angle + 0.45)), (int) (ey - head * Math.sin(angle + 0.45)));
    }

    // ---------- Анимация хода ----------

    // Запускает анимацию хода: фишка плавно едет из старой клетки в новую.
    public void startAnimation(Move move) {
        if (move == null) {
            return;
        }
        animMove = move;
        animStart = System.nanoTime();
        animTimer.start();
    }

    // Запускает анимацию последнего хода (его сделал компьютер).
    public void animateLastMove() {
        startAnimation(controller.getLastMove());
    }

    // Останавливает анимацию (при новой игре или отмене хода).
    public void stopAnimation() {
        animMove = null;
        animTimer.stop();
    }

    // Сколько анимации уже прошло: число от 0 до 1 (1 = анимация закончилась).
    private double animProgress() {
        double t = (System.nanoTime() - animStart) / 1_000_000.0 / ANIM_MS;
        return Math.max(0, Math.min(1, t));
    }

    // Вызывается таймером: перерисовываем, а когда время вышло, выключаем анимацию.
    private void animationTick() {
        if (animMove == null || animProgress() >= 1) {
            stopAnimation();
        }
        repaint();
    }

    // ---------- Рисование ----------
    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Board board = controller.getState().getBoard();
        int cell = cellSize();

        drawBackground(g);
        drawPlate(g, board, cell);
        drawLastMove(g, cell);
        drawGridLines(g, board);
        drawPoints(g, board, cell);
        drawTargets(g, cell);
        drawPieces(g, board, cell);
        drawSelection(g, cell);   // кольцо выбора рисуем поверх фишки
        drawHint(g, cell);
        drawCursor(g, cell);
        drawLabels(g, cell);
    }

    // Рисует фоновую картинку на всю панель (по центру, с обрезкой лишнего по краям).
    private void drawBackground(Graphics2D g) {
        BufferedImage image = background.get(Math.max(getWidth(), getHeight()));
        if (image != null) {
            g.drawImage(image, (getWidth() - image.getWidth()) / 2, (getHeight() - image.getHeight()) / 2, null);
        }
    }

    // Рисует светлую полупрозрачную подложку под клетками поля, чтобы линии были хорошо видны на фоне.
    private void drawPlate(Graphics2D g, Board board, int cell) {
        if (!background.isLoaded()) {
            return;  // без фона подложка не нужна
        }
        // Собираем все клетки в одну фигуру и заливаем один раз: так нет швов и двойной прозрачности.
        Area plate = new Area();
        for (int row = 0; row < GameRules.BOARD_SIZE; row++) {
            for (int col = 0; col < GameRules.BOARD_SIZE; col++) {
                if (board.isInside(row, col)) {
                    plate.add(new Area(new Rectangle(centerX(col) - cell / 2, centerY(row) - cell / 2, cell, cell)));
                }
            }
        }
        g.setColor(COLOR_PLATE);
        g.fill(plate);
    }

    // Подсвечивает клетки последнего хода.
    private void drawLastMove(Graphics2D g, int cell) {
        Move last = controller.getLastMove();
        if (last == null) {
            return;
        }
        g.setColor(COLOR_LAST_MOVE);
        int r = cell / 2;
        g.fillOval(centerX(last.fromCol) - r, centerY(last.fromRow) - r, 2 * r, 2 * r);
        g.fillOval(centerX(last.toCol) - r, centerY(last.toRow) - r, 2 * r, 2 * r);
    }

    // Рисует линии поля: горизонтали, вертикали и диагонали между соседними клетками.
    private void drawGridLines(Graphics2D g, Board board) {
        g.setColor(COLOR_LINE);
        g.setStroke(new BasicStroke(2f));
        for (int row = 0; row < GameRules.BOARD_SIZE; row++) {
            for (int col = 0; col < GameRules.BOARD_SIZE; col++) {
                if (!board.isInside(row, col)) {
                    continue;
                }
                // Четыре направления вперёд (вправо, вниз, вниз-вправо, вниз-влево), чтобы не рисовать линию дважды.
                int[] dRow = {0, 1, 1, 1};
                int[] dCol = {1, 0, 1, -1};
                for (int d = 0; d < 4; d++) {
                    int nextRow = row + dRow[d];
                    int nextCol = col + dCol[d];
                    if (board.isInside(nextRow, nextCol)) {
                        g.drawLine(centerX(col), centerY(row), centerX(nextCol), centerY(nextRow));
                    }
                }
            }
        }
    }

    // Рисует маленькие точки-узлы в пустых клетках.
    private void drawPoints(Graphics2D g, Board board, int cell) {
        g.setColor(COLOR_LINE);
        int r = Math.max(3, cell / 14);
        for (int row = 0; row < GameRules.BOARD_SIZE; row++) {
            for (int col = 0; col < GameRules.BOARD_SIZE; col++) {
                if (board.isInside(row, col)) {
                    g.fillOval(centerX(col) - r, centerY(row) - r, 2 * r, 2 * r);
                }
            }
        }
    }

    // Рисует возможные ходы выбранной фишки: зелёная точка (шаг) или красное кольцо (прыжок).
    private void drawTargets(Graphics2D g, int cell) {
        for (int i = 0; i < targets.size(); i++) {
            Move move = targets.get(i);
            int cx = centerX(move.toCol);
            int cy = centerY(move.toRow);
            int r = cell / 4;
            if (move.isCapture()) {
                g.setColor(COLOR_CAPTURE);
                g.setStroke(new BasicStroke(3f));
                g.drawOval(cx - r, cy - r, 2 * r, 2 * r);
                g.fillOval(cx - r / 3, cy - r / 3, 2 * r / 3, 2 * r / 3);
            } else {
                g.setColor(COLOR_TARGET);
                g.fillOval(cx - r / 2, cy - r / 2, r, r);
            }
        }
    }

    // Обводит выбранную фишку жёлтым кольцом.
    private void drawSelection(Graphics2D g, int cell) {
        if (selectedRow < 0) {
            return;
        }
        int r = cell * 9 / 20 + 2;
        g.setColor(COLOR_SELECTED);
        g.setStroke(new BasicStroke(4f));
        g.drawOval(centerX(selectedCol) - r, centerY(selectedRow) - r, 2 * r, 2 * r);
    }

    // Рисует гусей и лису. Если идёт анимация, едущая фишка рисуется отдельно, между клетками.
    private void drawPieces(Graphics2D g, Board board, int cell) {
        for (int row = 0; row < GameRules.BOARD_SIZE; row++) {
            for (int col = 0; col < GameRules.BOARD_SIZE; col++) {
                if (!board.isInside(row, col)) {
                    continue;
                }
                // Клетку, куда сейчас едет фишка, пока не рисуем: фишка нарисуется на пути.
                if (animMove != null && row == animMove.toRow && col == animMove.toCol) {
                    continue;
                }
                drawPiece(g, board.get(row, col), centerX(col), centerY(row), cell);
            }
        }
        if (animMove == null) {
            return;
        }
        double t = animProgress();
        double eased = t * t * (3 - 2 * t);    // плавный старт и плавная остановка
        // Съеденный гусь медленно тает на своём месте.
        if (animMove.isCapture()) {
            java.awt.Composite old = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) (1 - eased)));
            drawPiece(g, GameRules.GOOSE, centerX(animMove.capturedCol), centerY(animMove.capturedRow), cell);
            g.setComposite(old);
        }
        // Едущая фишка: между старой и новой клеткой.
        double x = centerX(animMove.fromCol) + (centerX(animMove.toCol) - centerX(animMove.fromCol)) * eased;
        double y = centerY(animMove.fromRow) + (centerY(animMove.toRow) - centerY(animMove.fromRow)) * eased;
        drawPiece(g, board.get(animMove.toRow, animMove.toCol), (int) x, (int) y, cell);
    }

    // Рисует одну фишку (piece: GOOSE, FOX или пусто) с центром в (cx, cy).
    private void drawPiece(Graphics2D g, int piece, int cx, int cy, int cell) {
        if (piece != GameRules.GOOSE && piece != GameRules.FOX) {
            return;
        }
        // Если есть картинки фишек, рисуем их.
        int iconSize = cell * 86 / 100;
        BufferedImage icon = piece == GameRules.FOX ? images.getFox(iconSize) : images.getGoose(iconSize);
        if (icon != null) {
            g.drawImage(icon, cx - iconSize / 2, cy - iconSize / 2, null);
            return;
        }

        // Запасной вариант без картинок: обычные кружки.
        int r = cell * 2 / 5 - 2;
        if (piece == GameRules.GOOSE) {
            g.setColor(COLOR_GOOSE);
            g.fillOval(cx - r, cy - r, 2 * r, 2 * r);
            g.setColor(COLOR_GOOSE_BORDER);
            g.setStroke(new BasicStroke(2f));
            g.drawOval(cx - r, cy - r, 2 * r, 2 * r);
        } else {
            g.setColor(COLOR_FOX);
            g.fillOval(cx - r, cy - r, 2 * r, 2 * r);
            g.setColor(COLOR_FOX_BORDER);
            g.setStroke(new BasicStroke(3f));
            g.drawOval(cx - r, cy - r, 2 * r, 2 * r);
            // Буква «Л» (лиса) в центре фишки.
            g.setColor(Color.WHITE);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.max(12, r)));
            FontMetrics fm = g.getFontMetrics();
            String text = "Л";
            g.drawString(text, cx - fm.stringWidth(text) / 2, cy + (fm.getAscent() - fm.getDescent()) / 2);
        }
    }

    // Рисует синий квадрат клавиатурного курсора (только когда сейчас ход человека).
    private void drawCursor(Graphics2D g, int cell) {
        if (!controller.isHumanTurn() || !isFocusOwner()) {
            return;
        }
        int half = cell / 2 - 2;
        g.setColor(COLOR_CURSOR);
        g.setStroke(new BasicStroke(3f));
        g.drawRoundRect(centerX(cursorCol) - half, centerY(cursorRow) - half, 2 * half, 2 * half, 10, 10);
    }

    // Подписи клеток по краям: буквы a-g снизу и числа 1-7 слева (как в записи ходов).
    private void drawLabels(Graphics2D g, int cell) {
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.max(10, cell / 6)));
        FontMetrics fm = g.getFontMetrics();
        for (int i = 0; i < GameRules.BOARD_SIZE; i++) {
            String letter = String.valueOf((char) ('a' + i));
            drawLabel(g, fm, letter, centerX(i) - fm.stringWidth(letter) / 2,
                    offsetY() + cell * GameRules.BOARD_SIZE + fm.getAscent());
            String number = String.valueOf(GameRules.BOARD_SIZE - i);
            drawLabel(g, fm, number, offsetX() - fm.stringWidth(number) - 6,
                    centerY(i) + fm.getAscent() / 2);
        }
    }

    // Рисует одну подпись на светлой плашке, чтобы её было видно на любом фоне.
    private void drawLabel(Graphics2D g, FontMetrics fm, String text, int x, int y) {
        int width = fm.stringWidth(text);
        g.setColor(COLOR_LABEL_BACK);
        g.fillRoundRect(x - 5, y - fm.getAscent() - 1, width + 10, fm.getHeight() + 2, 10, 10);
        g.setColor(new Color(0x5B5246));
        g.drawString(text, x, y);
    }
}
