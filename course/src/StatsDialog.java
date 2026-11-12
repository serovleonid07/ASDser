import java.awt.Component;
import javax.swing.JOptionPane;

// Окно со статистикой партий против компьютера (победы, поражения и ничьи по сложностям).
public class StatsDialog {
    private static final String[] LEVEL_NAMES = {"Лёгкая", "Средняя", "Сложная"};

    // Показывает окно. Кнопка «Сбросить» обнуляет статистику (после подтверждения).
    public static void show(Component parent) {
        String[] options = {"Закрыть", "Сбросить"};
        int choice = JOptionPane.showOptionDialog(parent, buildText(), "Статистика",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        if (choice == 1) {
            int sure = JOptionPane.showConfirmDialog(parent, "Обнулить всю статистику?", "Статистика",
                    JOptionPane.YES_NO_OPTION);
            if (sure == JOptionPane.YES_OPTION) {
                Settings.resetStats();
            }
        }
    }

    // Собирает таблицу в виде HTML.
    private static String buildText() {
        StringBuilder text = new StringBuilder("<html><body style='font-family:SansSerif'>");
        text.append("<b>Партии против компьютера</b> (вы выиграли / проиграли / ничья)<br><br>");
        text.append("<table cellpadding='4'>");
        text.append("<tr><th align='left'>Сложность</th><th>Победы</th><th>Поражения</th><th>Ничьи</th><th>Всего</th></tr>");
        int[] total = new int[3];
        for (int level = 0; level < Settings.LEVELS; level++) {
            int wins = Settings.getCount(level, Settings.WIN);
            int losses = Settings.getCount(level, Settings.LOSS);
            int draws = Settings.getCount(level, Settings.DRAW);
            total[0] += wins;
            total[1] += losses;
            total[2] += draws;
            text.append(row(LEVEL_NAMES[level], wins, losses, draws));
        }
        text.append(row("<b>Итого</b>", total[0], total[1], total[2]));
        text.append("</table></body></html>");
        return text.toString();
    }

    // Одна строка таблицы.
    private static String row(String name, int wins, int losses, int draws) {
        return "<tr><td>" + name + "</td><td align='center'>" + wins + "</td><td align='center'>" + losses
                + "</td><td align='center'>" + draws + "</td><td align='center'>" + (wins + losses + draws) + "</td></tr>";
    }
}
