import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

// Набор курсоров мыши для двух персонажей: лисы и гуся.
// У каждого персонажа три курсора:
//   NORMAL - обычный указатель,
//   LINK   - «рука»: курсор над тем, на что можно нажать (своя фишка, клетка для хода),
//   BUSY   - «занят»: компьютер думает.
// Картинки лежат в src/images/cursors. Если картинок нет или система не умеет
// свои курсоры, возвращаются обычные курсоры Java.
public class CursorSet {
    public static final int NORMAL = 0;
    public static final int LINK = 1;
    public static final int BUSY = 2;

    // Имена файлов: сначала лиса (игрок PLAYER_FOX = 1), потом гусь (игрок PLAYER_GEESE = 0).
    private static final String[] KIND_NAMES = {"normal", "link", "busy"};

    // «Острие» курсора (точка, которая попадает по клетке) для каждого файла: {x, y}.
    // Порядок: [normal, link, busy]. Значения взяты из самих файлов курсоров.
    private static final int[][] FOX_HOTSPOT = {{0, 0}, {8, 1}, {12, 11}};
    private static final int[][] GOOSE_HOTSPOT = {{1, 1}, {3, 2}, {3, 0}};

    // cursors[игрок][вид]: готовые курсоры. Может быть null, если не получилось создать.
    private final Cursor[][] cursors = new Cursor[2][3];

    public CursorSet() {
        for (int kind = 0; kind < 3; kind++) {
            cursors[GameRules.PLAYER_FOX][kind] = create("fox_" + KIND_NAMES[kind], FOX_HOTSPOT[kind]);
            cursors[GameRules.PLAYER_GEESE][kind] = create("goose_" + KIND_NAMES[kind], GOOSE_HOTSPOT[kind]);
        }
    }

    // Возвращает курсор для игрока (PLAYER_FOX или PLAYER_GEESE) и вида (NORMAL, LINK или BUSY).
    public Cursor get(int player, int kind) {
        Cursor cursor = cursors[player][kind];
        if (cursor != null) {
            return cursor;
        }
        // Запасной вариант: обычные курсоры Java.
        if (kind == LINK) {
            return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
        }
        if (kind == BUSY) {
            return Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
        }
        return Cursor.getDefaultCursor();
    }

    // Создаёт один курсор из файла. Возвращает null, если не получилось.
    private static Cursor create(String fileName, int[] hotspot) {
        try {
            if (GraphicsEnvironment.isHeadless()) {
                return null;  // без экрана курсоры не нужны
            }
            BufferedImage image = PieceImages.load("cursors/" + fileName + ".png");
            if (image == null) {
                return null;
            }
            // Система сама решает, какого размера бывают курсоры (чаще всего 32x32).
            // Рисуем нашу картинку в левом верхнем углу холста нужного размера.
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Dimension best = toolkit.getBestCursorSize(image.getWidth(), image.getHeight());
            if (best.width <= 0 || best.height <= 0) {
                return null;
            }
            BufferedImage canvas = new BufferedImage(best.width, best.height, BufferedImage.TYPE_INT_ARGB);
            canvas.getGraphics().drawImage(image, 0, 0, null);
            // Острие не должно выходить за холст.
            int x = Math.min(hotspot[0], best.width - 1);
            int y = Math.min(hotspot[1], best.height - 1);
            return toolkit.createCustomCursor(canvas, new Point(x, y), fileName);
        } catch (Exception e) {
            return null;
        }
    }
}
