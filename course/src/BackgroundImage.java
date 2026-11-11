import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

// Фоновая картинка (луг, забор, деревья). Квадратная, растягивается на всё окно.
// Если файла нет, метод get возвращает null, и поле рисуется на обычном однотонном фоне.
public class BackgroundImage {
    private final BufferedImage original = PieceImages.load("background.png");  // исходная картинка
    private BufferedImage scaled = null;   // картинка нужного размера (кэш)
    private int scaledSize = -1;           // для какого размера сделан кэш

    // Есть ли картинка (удалось ли её загрузить).
    public boolean isLoaded() {
        return original != null;
    }

    // Возвращает фон размером size x size пикселей (или null, если картинки нет).
    // size берём как большую сторону окна, чтобы фон закрыл его целиком.
    public BufferedImage get(int size) {
        if (original == null || size <= 0) {
            return null;
        }
        if (size != scaledSize) {
            scaled = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = scaled.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(original, 0, 0, size, size, null);
            g.dispose();
            scaledSize = size;
        }
        return scaled;
    }
}
