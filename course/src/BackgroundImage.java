import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

// Фоновая картинка (луг, забор, деревья). Квадратная, растягивается на всё окно.
// Есть три темы: день, вечер, зима (у каждой свой файл). Выбранная тема хранится в Settings.
// Если файла нет, метод get возвращает null, и поле рисуется на обычном однотонном фоне.
public class BackgroundImage {
    private static final String[] FILES = {"background.png", "background_evening.png", "background_winter.png"};

    private BufferedImage original = null;   // исходная картинка выбранной темы
    private int loadedTheme = -1;            // для какой темы она загружена
    private BufferedImage scaled = null;     // картинка нужного размера (кэш)
    private int scaledSize = -1;             // для какого размера сделан кэш

    // Следит, чтобы загруженная картинка соответствовала выбранной теме.
    private void checkTheme() {
        int theme = Settings.getTheme();
        if (theme != loadedTheme) {
            loadedTheme = theme;
            original = PieceImages.load(FILES[theme]);
            if (original == null) {
                original = PieceImages.load(FILES[0]);   // нет файла темы: берём дневной
            }
            scaled = null;
            scaledSize = -1;
        }
    }

    // Есть ли картинка (удалось ли её загрузить).
    public boolean isLoaded() {
        checkTheme();
        return original != null;
    }

    // Возвращает фон размером size x size пикселей (или null, если картинки нет).
    // size берём как большую сторону окна, чтобы фон закрыл его целиком.
    public BufferedImage get(int size) {
        checkTheme();
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
