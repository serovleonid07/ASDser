Лиса и гуси (курсовая АСД, вариант 17). Java 17+, Swing.

КАК ЗАПУСТИТЬ
1. Двойной щелчок по Play.bat (нужна установленная Java 17 или новее).
   Можно и двойным щелчком по FoxAndGeese.jar.
2. Настоящая программа .exe с иконкой лисы (Java внутри, на другом компьютере Java не нужна):
   двойной щелчок по build_exe.bat (нужен JDK 17+). Через минуту появится папка exe\FoxAndGeese,
   в ней FoxAndGeese.exe. Из неё можно сделать ярлык на рабочий стол.
3. Из исходников: java -cp out Main (после сборки: mkdir out && javac -encoding UTF-8 -d out src/*.java,
   запускать из папки course: картинки лежат в src/images).

ЕСЛИ МЕНЯЛИ КОД: build.bat (Windows) или ./build.sh (Linux, macOS) заново собирают FoxAndGeese.jar.

ФАЙЛЫ
src/                исходный код (src/images: картинки, src/images/cursors: курсоры)
icon.ico            иконка для .exe
FoxAndGeese.jar     готовая игра
Play.bat            запуск двойным щелчком
build.bat, build.sh сборка jar
build_exe.bat       сборка .exe

Проверка без окна (компьютер против компьютера + сравнение MiniMax и альфа-бета):
java -cp out ConsoleDemo 6
