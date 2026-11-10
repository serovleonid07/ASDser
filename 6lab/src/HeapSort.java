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
    // showSteps — если true, печатает массив после построения пирамиды и после каждого шага.
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
        printStep(title, data, data.length);
    }

    // Печатает массив и вертикальной чертой отделяет пирамиду от уже отсортированного хвоста.
    // heapSize — сколько первых элементов входит в пирамиду.
    // Всё, что справа от черты, уже стоит на своём окончательном месте.
    private static void printStep(String title, int[] data, int heapSize) {
        System.out.print(title + ": ");
        for (int i = 0; i < data.length; i++) {
            if (i == heapSize) {
                System.out.print("| ");
            }
            System.out.print(data[i] + " ");
        }
        System.out.println();
    }
}
