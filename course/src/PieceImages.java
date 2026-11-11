import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import javax.imageio.ImageIO;

// Картинки фишек (лиса и гусь). Загружает файлы и умеет менять их размер под размер клетки.
// Если картинки не найдены, методы возвращают null, и поле рисует обычные кружки.
public class PieceImages {
    private final BufferedImage foxOriginal = load("fox.png");     // исходная картинка лисы
    private final BufferedImage gooseOriginal = load("goose.png"); // исходная картинка гуся

    private BufferedImage foxScaled = null;     // лиса уже нужного размера (кэш)
    private BufferedImage gooseScaled = null;   // гусь уже нужного размера (кэш)
    private int scaledSize = -1;                // для какого размера сделан кэш

    // Загружает картинку из папки images. Сначала ищет в classpath (рядом с классами),
    // потом в папке src/images (если запускаем из папки проекта).
    static BufferedImage load(String name) {
        try {
            InputStream stream = PieceImages.class.getResourceAsStream("/images/" + name);
            if (stream != null) {
                return ImageIO.read(stream);
            }
            String[] folders = {"src/images/", "course/src/images/"};
            for (String folder : folders) {
                File file = new File(folder + name);
                if (file.exists()) {
                    return ImageIO.read(file);
                }
            }
        } catch (Exception e) {
            // Не смогли прочитать файл: вернём null, будут кружки.
        }
        return null;
    }

    // Возвращает картинку лисы размером size x size пикселей (или null).
    public BufferedImage getFox(int size) {
        prepare(size);
        return foxScaled;
    }

    // Возвращает картинку гуся размером size x size пикселей (или null).
    public BufferedImage getGoose(int size) {
        prepare(size);
        return gooseScaled;
    }

    // Если размер клетки изменился, заново готовим уменьшенные картинки.
    private void prepare(int size) {
        if (size == scaledSize) {
            return;
        }
        foxScaled = foxOriginal == null ? null : scale(foxOriginal, size);
        gooseScaled = gooseOriginal == null ? null : scale(gooseOriginal, size);
        scaledSize = size;
    }

    // Уменьшает картинку до size x size. Уменьшаем по шагам вдвое, так край получается гладким.
    private static BufferedImage scale(BufferedImage source, int size) {
        BufferedImage current = source;
        while (current.getWidth() / 2 >= size) {
            current = resize(current, current.getWidth() / 2);
        }
        return resize(current, size);
    }

    // Один шаг изменения размера (квадратная картинка).
    private static BufferedImage resize(BufferedImage source, int size) {
        BufferedImage result = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(source, 0, 0, size, size, null);
        g.dispose();
        return result;
    }
}
