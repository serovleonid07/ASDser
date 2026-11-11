Лиса и гуси (курсовая АСД, вариант 17), Java 21, Swing.
Сборка:   mkdir out && javac -encoding UTF-8 -d out src/*.java
Игра:     java -cp out Main        (запускать из папки course: картинки лежат в src/images)
Проверка без окна (компьютер против компьютера + сравнение MiniMax и альфа-бета): java -cp out ConsoleDemo 6
Картинки: src/images (fox.png, goose.png - фишки; background.png - фон). Если файлов нет, рисуются кружки на простом фоне.
В IntelliJ IDEA картинки копируются к классам автоматически.
