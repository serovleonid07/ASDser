// Пирамидальная сортировка (Heap Sort) по возрастанию.
// Пирамида (куча) — это массив, который мы считаем двоичным деревом:
// у элемента с номером i левый ребёнок имеет номер 2*i + 1, правый — 2*i + 2.
// В нашей пирамиде каждый родитель не меньше своих детей, значит наверху (номер 0) максимум.
// Алгоритм:
// 1) строим пирамиду из всего массива;
// 2) меняем максимум (номер 0) с последним элементом пирамиды, пирамиду сокращаем на 1
//    и чиним её; повторяем, пока пирамида не закончится.
public class HeapSort {

    // Меняет местами элементы массива data с номерами i и j.
    private static void swap(int[] data, int i, int j) {
        int temp = data[i];
        data[i] = data[j];
        data[j] = temp;
    }

    // Просеивание вниз: чинит пирамиду, если родитель с номером parent может быть меньше детей.
    // Берём самого большого из родителя и двух детей. Если это не родитель — меняем их местами
    // и повторяем уже для того места, куда опустился родитель.
    // heapSize — сколько первых элементов массива сейчас входит в пирамиду.
    private static void siftDown(int[] data, int parent, int heapSize) {
        while (true) {
            int left = 2 * parent + 1;
            int right = 2 * parent + 2;
            int biggest = parent;  // номер самого большого из трёх элементов

            if (left < heapSize && data[left] > data[biggest]) {
                biggest = left;
            }
            if (right < heapSize && data[right] > data[biggest]) {
                biggest = right;
            }

            if (biggest == parent) {
                return;  // родитель уже не меньше детей, чинить нечего
            }

            swap(data, parent, biggest);
            parent = biggest;  // идём вниз, туда, куда опустили родителя
        }
    }

    // Сортирует массив data по возрастанию.
    // showSteps — если true, после построения пирамиды и после каждого шага печатает
    // массив строкой и пирамиду схемой.
    public static void sort(int[] data, boolean showSteps) {
        int n = data.length;

        // Шаг 1. Строим пирамиду. Элементы с номера n/2 и дальше — листья, у них нет детей,
        // поэтому начинаем с последнего родителя (n/2 - 1) и идём к корню.
        for (int i = n / 2 - 1; i >= 0; i--) {
            siftDown(data, i, n);
        }
        if (showSteps) {
            printStep("Пирамида построена", data, n);
        }

        // Шаг 2. Максимум стоит в начале. Ставим его в конец, а пирамиду сокращаем на 1.
        // Новый элемент в начале может нарушать порядок, поэтому просеиваем его вниз.
        for (int end = n - 1; end > 0; end--) {
            swap(data, 0, end);
            siftDown(data, 0, end);
            if (showSteps) {
                printStep("Поставили " + data[end] + " на место " + end, data, end);
            }
        }
    }

    // Проверяет, что массив отсортирован по возрастанию.
    public static boolean isSorted(int[] data) {
        for (int i = 1; i < data.length; i++) {
            if (data[i - 1] > data[i]) {
                return false;
            }
        }
        return true;
    }

    // Печатает массив одной строкой. title — подпись.
    public static void printArray(String title, int[] data) {
        printLine(title, data, data.length);
    }

    // Печатает весь массив в виде пирамиды (дерева по уровням). title — подпись.
    // Нужна, чтобы показать, как выглядит дерево, даже если массив ещё не стал пирамидой.
    public static void printAsPyramid(String title, int[] data) {
        System.out.println(title + ":");
        printPyramid(data, data.length);
    }

    // Печатает один шаг сортировки: строку с массивом и под ней пирамиду.
    // heapSize — сколько первых элементов входит в пирамиду.
    private static void printStep(String title, int[] data, int heapSize) {
        printLine(title, data, heapSize);
        printPyramid(data, heapSize);
        System.out.println();
    }

    // Печатает массив строкой и вертикальной чертой отделяет пирамиду от отсортированного хвоста.
    // Всё, что справа от черты, уже стоит на своём окончательном месте.
    private static void printLine(String title, int[] data, int heapSize) {
        System.out.print(title + ": ");
        for (int i = 0; i < data.length; i++) {
            if (i == heapSize) {
                System.out.print("| ");
            }
            System.out.print(data[i] + " ");
        }
        System.out.println();
    }

    // Рисует первые heapSize элементов массива в виде пирамиды.
    // Уровень 0 — один элемент (вершина), уровень 1 — два, уровень 2 — четыре и так далее:
    // на каждом уровне элементов в два раза больше. Каждый уровень печатается отдельной строкой.
    // Чтобы получилась пирамида, каждый элемент стоит по центру своего «участка» строки:
    // нижний уровень делится на много узких участков, верхние на меньшее число широких.
    private static void printPyramid(int[] data, int heapSize) {
        if (heapSize == 0) {
            return;
        }

        // Ширина самого длинного числа (с минусом), чтобы числа не слипались.
        int maxLength = 1;
        for (int i = 0; i < heapSize; i++) {
            int length = String.valueOf(data[i]).length();
            if (length > maxLength) {
                maxLength = length;
            }
        }
        int cellWidth = maxLength + 2;  // одно число вместе с отступами по бокам

        // Считаем, сколько уровней нужно: на уровне k помещается 2^k элементов.
        int levels = 0;
        int placed = 0;     // сколько элементов уже поместили в уровни
        int levelSize = 1;  // сколько элементов на текущем уровне
        while (placed < heapSize) {
            placed += levelSize;
            levelSize *= 2;
            levels++;
        }

        // Ширина строки равна ширине нижнего уровня, если бы он был заполнен полностью.
        int totalWidth = cellWidth * (1 << (levels - 1));

        int index = 0;  // номер элемента массива, который печатаем следующим
        levelSize = 1;
        for (int level = 0; level < levels; level++) {
            int slotWidth = totalWidth / levelSize;  // ширина участка одного элемента на этом уровне
            String line = "";
            for (int i = 0; i < levelSize && index < heapSize; i++) {
                String text = String.valueOf(data[index]);
                int leftSpaces = (slotWidth - text.length()) / 2;
                int rightSpaces = slotWidth - text.length() - leftSpaces;
                line = line + spaces(leftSpaces) + text + spaces(rightSpaces);
                index++;
            }
            System.out.println("  " + line);
            levelSize *= 2;
        }
    }

    // Возвращает строку из count пробелов.
    private static String spaces(int count) {
        String result = "";
        for (int i = 0; i < count; i++) {
            result = result + " ";
        }
        return result;
    }
}
