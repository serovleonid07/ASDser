import java.util.Scanner;

/*
 * Доп. лабораторная работа № 2. Рекурсия и фракталы.
 * Задача 2. Рекурсивное рисование ёлки.
 * Пользователь сам задаёт глубину рекурсии.
 */
public class Main {
    // Точка входа программы. Спрашивает глубину и рисует ёлку.
    // Если в args передать число, оно используется как глубина (без вопроса).
    public static void main(String[] args) {
        System.out.println("Доп. лабораторная № 2. Рекурсия и фракталы. Ёлка.");

        int depth;
        if (args.length > 0) {
            depth = Integer.parseInt(args[0]);
        } else {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Введите глубину рекурсии (от 1 до 6): ");
            depth = scanner.nextInt();
        }

        if (depth < 1 || depth > 6) {
            System.out.println("Глубина должна быть от 1 до 6.");
            return;
        }

        System.out.println("Глубина " + depth + ":\n");
        FirTree.draw(depth);
    }
}
